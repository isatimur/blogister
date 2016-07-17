package com.isatimur.blog.repository.search;

import com.isatimur.blog.domain.Posts;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Posts entity.
 */
public interface PostsSearchRepository extends ElasticsearchRepository<Posts, Long> {
}
