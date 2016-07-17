package com.isatimur.blog.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.isatimur.blog.domain.Tag;
import com.isatimur.blog.repository.TagRepository;
import com.isatimur.blog.repository.search.TagSearchRepository;
import com.isatimur.blog.web.rest.util.HeaderUtil;
import com.isatimur.blog.web.rest.util.PaginationUtil;
import com.isatimur.blog.web.rest.dto.TagDTO;
import com.isatimur.blog.web.rest.mapper.TagMapper;
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
 * REST controller for managing Tag.
 */
@RestController
@RequestMapping("/api")
public class TagResource {

    private final Logger log = LoggerFactory.getLogger(TagResource.class);
        
    @Inject
    private TagRepository tagRepository;
    
    @Inject
    private TagMapper tagMapper;
    
    @Inject
    private TagSearchRepository tagSearchRepository;
    
    /**
     * POST  /tags : Create a new tag.
     *
     * @param tagDTO the tagDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new tagDTO, or with status 400 (Bad Request) if the tag has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/tags",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<TagDTO> createTag(@Valid @RequestBody TagDTO tagDTO) throws URISyntaxException {
        log.debug("REST request to save Tag : {}", tagDTO);
        if (tagDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("tag", "idexists", "A new tag cannot already have an ID")).body(null);
        }
        Tag tag = tagMapper.tagDTOToTag(tagDTO);
        tag = tagRepository.save(tag);
        TagDTO result = tagMapper.tagToTagDTO(tag);
        tagSearchRepository.save(tag);
        return ResponseEntity.created(new URI("/api/tags/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("tag", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /tags : Updates an existing tag.
     *
     * @param tagDTO the tagDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated tagDTO,
     * or with status 400 (Bad Request) if the tagDTO is not valid,
     * or with status 500 (Internal Server Error) if the tagDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/tags",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<TagDTO> updateTag(@Valid @RequestBody TagDTO tagDTO) throws URISyntaxException {
        log.debug("REST request to update Tag : {}", tagDTO);
        if (tagDTO.getId() == null) {
            return createTag(tagDTO);
        }
        Tag tag = tagMapper.tagDTOToTag(tagDTO);
        tag = tagRepository.save(tag);
        TagDTO result = tagMapper.tagToTagDTO(tag);
        tagSearchRepository.save(tag);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("tag", tagDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /tags : get all the tags.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of tags in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/tags",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<TagDTO>> getAllTags(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Tags");
        Page<Tag> page = tagRepository.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/tags");
        return new ResponseEntity<>(tagMapper.tagsToTagDTOs(page.getContent()), headers, HttpStatus.OK);
    }

    /**
     * GET  /tags/:id : get the "id" tag.
     *
     * @param id the id of the tagDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the tagDTO, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/tags/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<TagDTO> getTag(@PathVariable Long id) {
        log.debug("REST request to get Tag : {}", id);
        Tag tag = tagRepository.findOne(id);
        TagDTO tagDTO = tagMapper.tagToTagDTO(tag);
        return Optional.ofNullable(tagDTO)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /tags/:id : delete the "id" tag.
     *
     * @param id the id of the tagDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/tags/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        log.debug("REST request to delete Tag : {}", id);
        tagRepository.delete(id);
        tagSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("tag", id.toString())).build();
    }

    /**
     * SEARCH  /_search/tags?query=:query : search for the tag corresponding
     * to the query.
     *
     * @param query the query of the tag search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/tags",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<TagDTO>> searchTags(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Tags for query {}", query);
        Page<Tag> page = tagSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/tags");
        return new ResponseEntity<>(tagMapper.tagsToTagDTOs(page.getContent()), headers, HttpStatus.OK);
    }


}
