package com.isatimur.blog.repository;

import com.isatimur.blog.domain.Posts;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Posts entity.
 */
@SuppressWarnings("unused")
public interface PostsRepository extends JpaRepository<Posts,Long> {

    @Query("select distinct posts from Posts posts left join fetch posts.tags")
    List<Posts> findAllWithEagerRelationships();

    @Query("select posts from Posts posts left join fetch posts.tags where posts.id =:id")
    Posts findOneWithEagerRelationships(@Param("id") Long id);

}
