package com.isatimur.blog.service;

import com.isatimur.blog.domain.Posts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service Interface for managing Posts.
 */
public interface PostsService {

    /**
     * Save a posts.
     * 
     * @param posts the entity to save
     * @return the persisted entity
     */
    Posts save(Posts posts);

    /**
     *  Get all the posts.
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<Posts> findAll(Pageable pageable);

    /**
     *  Get the "id" posts.
     *  
     *  @param id the id of the entity
     *  @return the entity
     */
    Posts findOne(Long id);

    /**
     *  Delete the "id" posts.
     *  
     *  @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the posts corresponding to the query.
     * 
     *  @param query the query of the search
     *  @return the list of entities
     */
    Page<Posts> search(String query, Pageable pageable);
}
