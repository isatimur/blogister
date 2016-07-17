package com.isatimur.blog.web.rest;

import com.isatimur.blog.BlogisterApp;
import com.isatimur.blog.domain.Posts;
import com.isatimur.blog.repository.PostsRepository;
import com.isatimur.blog.repository.search.PostsSearchRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the PostsResource REST controller.
 *
 * @see PostsResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BlogisterApp.class)
@WebAppConfiguration
@IntegrationTest
public class PostsResourceIntTest {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("Z"));

    private static final String DEFAULT_TITLE = "AAAAA";
    private static final String UPDATED_TITLE = "BBBBB";
    private static final String DEFAULT_CONTENT = "AAAAA";
    private static final String UPDATED_CONTENT = "BBBBB";

    private static final ZonedDateTime DEFAULT_CREATTION_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault());
    private static final ZonedDateTime UPDATED_CREATTION_DATE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final String DEFAULT_CREATTION_DATE_STR = dateTimeFormatter.format(DEFAULT_CREATTION_DATE);

    @Inject
    private PostsRepository postsRepository;

    @Inject
    private PostsSearchRepository postsSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restPostsMockMvc;

    private Posts posts;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        PostsResource postsResource = new PostsResource();
        ReflectionTestUtils.setField(postsResource, "postsSearchRepository", postsSearchRepository);
        ReflectionTestUtils.setField(postsResource, "postsRepository", postsRepository);
        this.restPostsMockMvc = MockMvcBuilders.standaloneSetup(postsResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        postsSearchRepository.deleteAll();
        posts = new Posts();
        posts.setTitle(DEFAULT_TITLE);
        posts.setContent(DEFAULT_CONTENT);
        posts.setCreattionDate(DEFAULT_CREATTION_DATE);
    }

    @Test
    @Transactional
    public void createPosts() throws Exception {
        int databaseSizeBeforeCreate = postsRepository.findAll().size();

        // Create the Posts

        restPostsMockMvc.perform(post("/api/posts")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(posts)))
                .andExpect(status().isCreated());

        // Validate the Posts in the database
        List<Posts> posts = postsRepository.findAll();
        assertThat(posts).hasSize(databaseSizeBeforeCreate + 1);
        Posts testPosts = posts.get(posts.size() - 1);
        assertThat(testPosts.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testPosts.getContent()).isEqualTo(DEFAULT_CONTENT);
        assertThat(testPosts.getCreattionDate()).isEqualTo(DEFAULT_CREATTION_DATE);

        // Validate the Posts in ElasticSearch
        Posts postsEs = postsSearchRepository.findOne(testPosts.getId());
        assertThat(postsEs).isEqualToComparingFieldByField(testPosts);
    }

    @Test
    @Transactional
    public void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = postsRepository.findAll().size();
        // set the field null
        posts.setTitle(null);

        // Create the Posts, which fails.

        restPostsMockMvc.perform(post("/api/posts")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(posts)))
                .andExpect(status().isBadRequest());

        List<Posts> posts = postsRepository.findAll();
        assertThat(posts).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkContentIsRequired() throws Exception {
        int databaseSizeBeforeTest = postsRepository.findAll().size();
        // set the field null
        posts.setContent(null);

        // Create the Posts, which fails.

        restPostsMockMvc.perform(post("/api/posts")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(posts)))
                .andExpect(status().isBadRequest());

        List<Posts> posts = postsRepository.findAll();
        assertThat(posts).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCreattionDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = postsRepository.findAll().size();
        // set the field null
        posts.setCreattionDate(null);

        // Create the Posts, which fails.

        restPostsMockMvc.perform(post("/api/posts")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(posts)))
                .andExpect(status().isBadRequest());

        List<Posts> posts = postsRepository.findAll();
        assertThat(posts).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllPosts() throws Exception {
        // Initialize the database
        postsRepository.saveAndFlush(posts);

        // Get all the posts
        restPostsMockMvc.perform(get("/api/posts?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(posts.getId().intValue())))
                .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
                .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT.toString())))
                .andExpect(jsonPath("$.[*].creattionDate").value(hasItem(DEFAULT_CREATTION_DATE_STR)));
    }

    @Test
    @Transactional
    public void getPosts() throws Exception {
        // Initialize the database
        postsRepository.saveAndFlush(posts);

        // Get the posts
        restPostsMockMvc.perform(get("/api/posts/{id}", posts.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(posts.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE.toString()))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT.toString()))
            .andExpect(jsonPath("$.creattionDate").value(DEFAULT_CREATTION_DATE_STR));
    }

    @Test
    @Transactional
    public void getNonExistingPosts() throws Exception {
        // Get the posts
        restPostsMockMvc.perform(get("/api/posts/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePosts() throws Exception {
        // Initialize the database
        postsRepository.saveAndFlush(posts);
        postsSearchRepository.save(posts);
        int databaseSizeBeforeUpdate = postsRepository.findAll().size();

        // Update the posts
        Posts updatedPosts = new Posts();
        updatedPosts.setId(posts.getId());
        updatedPosts.setTitle(UPDATED_TITLE);
        updatedPosts.setContent(UPDATED_CONTENT);
        updatedPosts.setCreattionDate(UPDATED_CREATTION_DATE);

        restPostsMockMvc.perform(put("/api/posts")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedPosts)))
                .andExpect(status().isOk());

        // Validate the Posts in the database
        List<Posts> posts = postsRepository.findAll();
        assertThat(posts).hasSize(databaseSizeBeforeUpdate);
        Posts testPosts = posts.get(posts.size() - 1);
        assertThat(testPosts.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testPosts.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(testPosts.getCreattionDate()).isEqualTo(UPDATED_CREATTION_DATE);

        // Validate the Posts in ElasticSearch
        Posts postsEs = postsSearchRepository.findOne(testPosts.getId());
        assertThat(postsEs).isEqualToComparingFieldByField(testPosts);
    }

    @Test
    @Transactional
    public void deletePosts() throws Exception {
        // Initialize the database
        postsRepository.saveAndFlush(posts);
        postsSearchRepository.save(posts);
        int databaseSizeBeforeDelete = postsRepository.findAll().size();

        // Get the posts
        restPostsMockMvc.perform(delete("/api/posts/{id}", posts.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean postsExistsInEs = postsSearchRepository.exists(posts.getId());
        assertThat(postsExistsInEs).isFalse();

        // Validate the database is empty
        List<Posts> posts = postsRepository.findAll();
        assertThat(posts).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchPosts() throws Exception {
        // Initialize the database
        postsRepository.saveAndFlush(posts);
        postsSearchRepository.save(posts);

        // Search the posts
        restPostsMockMvc.perform(get("/api/_search/posts?query=id:" + posts.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(posts.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT.toString())))
            .andExpect(jsonPath("$.[*].creattionDate").value(hasItem(DEFAULT_CREATTION_DATE_STR)));
    }
}
