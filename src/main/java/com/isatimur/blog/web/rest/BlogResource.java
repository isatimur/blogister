package com.isatimur.blog.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.isatimur.blog.domain.Blog;
import com.isatimur.blog.repository.BlogRepository;
import com.isatimur.blog.repository.search.BlogSearchRepository;
import com.isatimur.blog.web.rest.util.HeaderUtil;
import com.isatimur.blog.web.rest.util.PaginationUtil;
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
    private BlogSearchRepository blogSearchRepository;
    
    /**
     * POST  /blogs : Create a new blog.
     *
     * @param blog the blog to create
     * @return the ResponseEntity with status 201 (Created) and with body the new blog, or with status 400 (Bad Request) if the blog has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/blogs",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Blog> createBlog(@Valid @RequestBody Blog blog) throws URISyntaxException {
        log.debug("REST request to save Blog : {}", blog);
        if (blog.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("blog", "idexists", "A new blog cannot already have an ID")).body(null);
        }
        Blog result = blogRepository.save(blog);
        blogSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/blogs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("blog", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /blogs : Updates an existing blog.
     *
     * @param blog the blog to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated blog,
     * or with status 400 (Bad Request) if the blog is not valid,
     * or with status 500 (Internal Server Error) if the blog couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/blogs",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Blog> updateBlog(@Valid @RequestBody Blog blog) throws URISyntaxException {
        log.debug("REST request to update Blog : {}", blog);
        if (blog.getId() == null) {
            return createBlog(blog);
        }
        Blog result = blogRepository.save(blog);
        blogSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("blog", blog.getId().toString()))
            .body(result);
    }

    /**
     * GET  /blogs : get all the blogs.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of blogs in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/blogs",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Blog>> getAllBlogs(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Blogs");
        Page<Blog> page = blogRepository.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/blogs");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /blogs/:id : get the "id" blog.
     *
     * @param id the id of the blog to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the blog, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/blogs/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Blog> getBlog(@PathVariable Long id) {
        log.debug("REST request to get Blog : {}", id);
        Blog blog = blogRepository.findOne(id);
        return Optional.ofNullable(blog)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /blogs/:id : delete the "id" blog.
     *
     * @param id the id of the blog to delete
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
    public ResponseEntity<List<Blog>> searchBlogs(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Blogs for query {}", query);
        Page<Blog> page = blogSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/blogs");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
