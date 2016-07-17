package com.isatimur.blog.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.isatimur.blog.domain.Posts;
import com.isatimur.blog.repository.PostsRepository;
import com.isatimur.blog.repository.search.PostsSearchRepository;
import com.isatimur.blog.web.rest.util.HeaderUtil;
import com.isatimur.blog.web.rest.util.PaginationUtil;
import com.isatimur.blog.web.rest.dto.PostsDTO;
import com.isatimur.blog.web.rest.mapper.PostsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Posts.
 */
@RestController
@RequestMapping("/api")
public class PostsResource {

    private final Logger log = LoggerFactory.getLogger(PostsResource.class);
        
    @Inject
    private PostsRepository postsRepository;
    
    @Inject
    private PostsMapper postsMapper;
    
    @Inject
    private PostsSearchRepository postsSearchRepository;
    
    /**
     * POST  /posts : Create a new posts.
     *
     * @param postsDTO the postsDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new postsDTO, or with status 400 (Bad Request) if the posts has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/posts",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<PostsDTO> createPosts(@Valid @RequestBody PostsDTO postsDTO) throws URISyntaxException {
        log.debug("REST request to save Posts : {}", postsDTO);
        if (postsDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("posts", "idexists", "A new posts cannot already have an ID")).body(null);
        }
        Posts posts = postsMapper.postsDTOToPosts(postsDTO);
        posts = postsRepository.save(posts);
        PostsDTO result = postsMapper.postsToPostsDTO(posts);
        postsSearchRepository.save(posts);
        return ResponseEntity.created(new URI("/api/posts/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("posts", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /posts : Updates an existing posts.
     *
     * @param postsDTO the postsDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated postsDTO,
     * or with status 400 (Bad Request) if the postsDTO is not valid,
     * or with status 500 (Internal Server Error) if the postsDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/posts",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<PostsDTO> updatePosts(@Valid @RequestBody PostsDTO postsDTO) throws URISyntaxException {
        log.debug("REST request to update Posts : {}", postsDTO);
        if (postsDTO.getId() == null) {
            return createPosts(postsDTO);
        }
        Posts posts = postsMapper.postsDTOToPosts(postsDTO);
        posts = postsRepository.save(posts);
        PostsDTO result = postsMapper.postsToPostsDTO(posts);
        postsSearchRepository.save(posts);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("posts", postsDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /posts : get all the posts.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of posts in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/posts",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<PostsDTO>> getAllPosts(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Posts");
        Page<Posts> page = postsRepository.findAllByBlogUserLogin(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/posts");
        return new ResponseEntity<>(postsMapper.postsToPostsDTOs(page.getContent()), headers, HttpStatus.OK);
    }

    /**
     * GET  /posts/:id : get the "id" posts.
     *
     * @param id the id of the postsDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the postsDTO, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/posts/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<PostsDTO> getPosts(@PathVariable Long id) {
        log.debug("REST request to get Posts : {}", id);
        Posts posts = postsRepository.findOneWithEagerRelationships(id);
        PostsDTO postsDTO = postsMapper.postsToPostsDTO(posts);
        return Optional.ofNullable(postsDTO)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /posts/:id : delete the "id" posts.
     *
     * @param id the id of the postsDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/posts/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deletePosts(@PathVariable Long id) {
        log.debug("REST request to delete Posts : {}", id);
        postsRepository.delete(id);
        postsSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("posts", id.toString())).build();
    }

    /**
     * SEARCH  /_search/posts?query=:query : search for the posts corresponding
     * to the query.
     *
     * @param query the query of the posts search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/posts",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<PostsDTO>> searchPosts(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Posts for query {}", query);
        Page<Posts> page = postsSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/posts");
        return new ResponseEntity<>(postsMapper.postsToPostsDTOs(page.getContent()), headers, HttpStatus.OK);
    }


}
