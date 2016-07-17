package com.isatimur.blog.service.impl;

import com.isatimur.blog.service.BlogService;
import com.isatimur.blog.domain.Blog;
import com.isatimur.blog.repository.BlogRepository;
import com.isatimur.blog.repository.search.BlogSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing Blog.
 */
@Service
@Transactional
public class BlogServiceImpl implements BlogService{

    private final Logger log = LoggerFactory.getLogger(BlogServiceImpl.class);
    
    @Inject
    private BlogRepository blogRepository;
    
    @Inject
    private BlogSearchRepository blogSearchRepository;
    
    /**
     * Save a blog.
     * 
     * @param blog the entity to save
     * @return the persisted entity
     */
    public Blog save(Blog blog) {
        log.debug("Request to save Blog : {}", blog);
        Blog result = blogRepository.save(blog);
        blogSearchRepository.save(result);
        return result;
    }

    /**
     *  Get all the blogs.
     *  
     *  @return the list of entities
     */
    @Transactional(readOnly = true) 
    public List<Blog> findAll() {
        log.debug("Request to get all Blogs");
        List<Blog> result = blogRepository.findByUserIsCurrentUser();
        return result;
    }

    /**
     *  Get one blog by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true) 
    public Blog findOne(Long id) {
        log.debug("Request to get Blog : {}", id);
        Blog blog = blogRepository.findOne(id);
        return blog;
    }

    /**
     *  Delete the  blog by id.
     *  
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Blog : {}", id);
        blogRepository.delete(id);
        blogSearchRepository.delete(id);
    }

    /**
     * Search for the blog corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<Blog> search(String query) {
        log.debug("Request to search Blogs for query {}", query);
        return StreamSupport
            .stream(blogSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .collect(Collectors.toList());
    }
}
