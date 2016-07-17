package com.isatimur.blog.repository.search;

import com.isatimur.blog.domain.Tag;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Tag entity.
 */
public interface TagSearchRepository extends ElasticsearchRepository<Tag, Long> {
}
