package com.isatimur.blog.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.isatimur.blog.domain.Blog;
import com.isatimur.blog.repository.BlogRepository;
import com.isatimur.blog.repository.search.BlogSearchRepository;
import com.isatimur.blog.web.rest.util.HeaderUtil;
import com.isatimur.blog.web.rest.dto.BlogDTO;
import com.isatimur.blog.web.rest.mapper.BlogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * REST controller for managing Blog.
 */
@RestController
@RequestMapping("/api")
public class BlogResource {

    private final Logger log = LoggerFactory.getLogger(BlogResource.class);
        
    @Inject
    private BlogRepository blogRepository;
    
    @Inject
    private BlogMapper blogMapper;
    
    @Inject
    private BlogSearchRepository blogSearchRepository;
    
    /**
     * POST  /blogs : Create a new blog.
     *
     * @param blogDTO the blogDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new blogDTO, or with status 400 (Bad Request) if the blog has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/blogs",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<BlogDTO> createBlog(@Valid @RequestBody BlogDTO blogDTO) throws URISyntaxException {
        log.debug("REST request to save Blog : {}", blogDTO);
        if (blogDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("blog", "idexists", "A new blog cannot already have an ID")).body(null);
        }
        Blog blog = blogMapper.blogDTOToBlog(blogDTO);
        blog = blogRepository.save(blog);
        BlogDTO result = blogMapper.blogToBlogDTO(blog);
        blogSearchRepository.save(blog);
        return ResponseEntity.created(new URI("/api/blogs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("blog", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /blogs : Updates an existing blog.
     *
     * @param blogDTO the blogDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated blogDTO,
     * or with status 400 (Bad Request) if the blogDTO is not valid,
     * or with status 500 (Internal Server Error) if the blogDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/blogs",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<BlogDTO> updateBlog(@Valid @RequestBody BlogDTO blogDTO) throws URISyntaxException {
        log.debug("REST request to update Blog : {}", blogDTO);
        if (blogDTO.getId() == null) {
            return createBlog(blogDTO);
        }
        Blog blog = blogMapper.blogDTOToBlog(blogDTO);
        blog = blogRepository.save(blog);
        BlogDTO result = blogMapper.blogToBlogDTO(blog);
        blogSearchRepository.save(blog);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("blog", blogDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /blogs : get all the blogs.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of blogs in body
     */
    @RequestMapping(value = "/blogs",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<BlogDTO> getAllBlogs() {
        log.debug("REST request to get all Blogs");
        List<Blog> blogs = blogRepository.findByUserIsCurrentUser();
        return blogMapper.blogsToBlogDTOs(blogs);
    }

    /**
     * GET  /blogs/:id : get the "id" blog.
     *
     * @param id the id of the blogDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the blogDTO, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/blogs/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<BlogDTO> getBlog(@PathVariable Long id) {
        log.debug("REST request to get Blog : {}", id);
        Blog blog = blogRepository.findOne(id);
        BlogDTO blogDTO = blogMapper.blogToBlogDTO(blog);
        return Optional.ofNullable(blogDTO)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /blogs/:id : delete the "id" blog.
     *
     * @param id the id of the blogDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/blogs/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        log.debug("REST request to delete Blog : {}", id);
        blogRepository.delete(id);
        blogSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("blog", id.toString())).build();
    }

    /**
     * SEARCH  /_search/blogs?query=:query : search for the blog corresponding
     * to the query.
     *
     * @param query the query of the blog search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/blogs",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<BlogDTO> searchBlogs(@RequestParam String query) {
        log.debug("REST request to search Blogs for query {}", query);
        return StreamSupport
            .stream(blogSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .map(blogMapper::blogToBlogDTO)
            .collect(Collectors.toList());
    }


}
