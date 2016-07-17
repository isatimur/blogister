package com.isatimur.blog.service.impl;

import com.isatimur.blog.service.PostsService;
import com.isatimur.blog.domain.Posts;
import com.isatimur.blog.repository.PostsRepository;
import com.isatimur.blog.repository.search.PostsSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing Posts.
 */
@Service
@Transactional
public class PostsServiceImpl implements PostsService{

    private final Logger log = LoggerFactory.getLogger(PostsServiceImpl.class);
    
    @Inject
    private PostsRepository postsRepository;
    
    @Inject
    private PostsSearchRepository postsSearchRepository;
    
    /**
     * Save a posts.
     * 
     * @param posts the entity to save
     * @return the persisted entity
     */
    public Posts save(Posts posts) {
        log.debug("Request to save Posts : {}", posts);
        Posts result = postsRepository.save(posts);
        postsSearchRepository.save(result);
        return result;
    }

    /**
     *  Get all the posts.
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true) 
    public Page<Posts> findAll(Pageable pageable) {
        log.debug("Request to get all Posts");
        Page<Posts> result = postsRepository.findAllByBlogUserLogin(pageable);
        return result;
    }

    /**
     *  Get one posts by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true) 
    public Posts findOne(Long id) {
        log.debug("Request to get Posts : {}", id);
        Posts posts = postsRepository.findOneWithEagerRelationships(id);
        return posts;
    }

    /**
     *  Delete the  posts by id.
     *  
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Posts : {}", id);
        postsRepository.delete(id);
        postsSearchRepository.delete(id);
    }

    /**
     * Search for the posts corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Posts> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Posts for query {}", query);
        return postsSearchRepository.search(queryStringQuery(query), pageable);
    }
}
