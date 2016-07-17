package com.isatimur.blog.web.rest;

import com.isatimur.blog.BlogisterApp;
import com.isatimur.blog.domain.Blog;
import com.isatimur.blog.repository.BlogRepository;
import com.isatimur.blog.repository.search.BlogSearchRepository;

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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the BlogResource REST controller.
 *
 * @see BlogResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BlogisterApp.class)
@WebAppConfiguration
@IntegrationTest
public class BlogResourceIntTest {

    private static final String DEFAULT_NAME = "AAA";
    private static final String UPDATED_NAME = "BBB";
    private static final String DEFAULT_HANDLE = "AAAAA";
    private static final String UPDATED_HANDLE = "BBBBB";

    @Inject
    private BlogRepository blogRepository;

    @Inject
    private BlogSearchRepository blogSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restBlogMockMvc;

    private Blog blog;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        BlogResource blogResource = new BlogResource();
        ReflectionTestUtils.setField(blogResource, "blogSearchRepository", blogSearchRepository);
        ReflectionTestUtils.setField(blogResource, "blogRepository", blogRepository);
        this.restBlogMockMvc = MockMvcBuilders.standaloneSetup(blogResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        blogSearchRepository.deleteAll();
        blog = new Blog();
        blog.setName(DEFAULT_NAME);
        blog.setHandle(DEFAULT_HANDLE);
    }

    @Test
    @Transactional
    public void createBlog() throws Exception {
        int databaseSizeBeforeCreate = blogRepository.findAll().size();

        // Create the Blog

        restBlogMockMvc.perform(post("/api/blogs")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(blog)))
                .andExpect(status().isCreated());

        // Validate the Blog in the database
        List<Blog> blogs = blogRepository.findAll();
        assertThat(blogs).hasSize(databaseSizeBeforeCreate + 1);
        Blog testBlog = blogs.get(blogs.size() - 1);
        assertThat(testBlog.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testBlog.getHandle()).isEqualTo(DEFAULT_HANDLE);

        // Validate the Blog in ElasticSearch
        Blog blogEs = blogSearchRepository.findOne(testBlog.getId());
        assertThat(blogEs).isEqualToComparingFieldByField(testBlog);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = blogRepository.findAll().size();
        // set the field null
        blog.setName(null);

        // Create the Blog, which fails.

        restBlogMockMvc.perform(post("/api/blogs")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(blog)))
                .andExpect(status().isBadRequest());

        List<Blog> blogs = blogRepository.findAll();
        assertThat(blogs).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkHandleIsRequired() throws Exception {
        int databaseSizeBeforeTest = blogRepository.findAll().size();
        // set the field null
        blog.setHandle(null);

        // Create the Blog, which fails.

        restBlogMockMvc.perform(post("/api/blogs")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(blog)))
                .andExpect(status().isBadRequest());

        List<Blog> blogs = blogRepository.findAll();
        assertThat(blogs).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllBlogs() throws Exception {
        // Initialize the database
        blogRepository.saveAndFlush(blog);

        // Get all the blogs
        restBlogMockMvc.perform(get("/api/blogs?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(blog.getId().intValue())))
                .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
                .andExpect(jsonPath("$.[*].handle").value(hasItem(DEFAULT_HANDLE.toString())));
    }

    @Test
    @Transactional
    public void getBlog() throws Exception {
        // Initialize the database
        blogRepository.saveAndFlush(blog);

        // Get the blog
        restBlogMockMvc.perform(get("/api/blogs/{id}", blog.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(blog.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.handle").value(DEFAULT_HANDLE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingBlog() throws Exception {
        // Get the blog
        restBlogMockMvc.perform(get("/api/blogs/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateBlog() throws Exception {
        // Initialize the database
        blogRepository.saveAndFlush(blog);
        blogSearchRepository.save(blog);
        int databaseSizeBeforeUpdate = blogRepository.findAll().size();

        // Update the blog
        Blog updatedBlog = new Blog();
        updatedBlog.setId(blog.getId());
        updatedBlog.setName(UPDATED_NAME);
        updatedBlog.setHandle(UPDATED_HANDLE);

        restBlogMockMvc.perform(put("/api/blogs")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedBlog)))
                .andExpect(status().isOk());

        // Validate the Blog in the database
        List<Blog> blogs = blogRepository.findAll();
        assertThat(blogs).hasSize(databaseSizeBeforeUpdate);
        Blog testBlog = blogs.get(blogs.size() - 1);
        assertThat(testBlog.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testBlog.getHandle()).isEqualTo(UPDATED_HANDLE);

        // Validate the Blog in ElasticSearch
        Blog blogEs = blogSearchRepository.findOne(testBlog.getId());
        assertThat(blogEs).isEqualToComparingFieldByField(testBlog);
    }

    @Test
    @Transactional
    public void deleteBlog() throws Exception {
        // Initialize the database
        blogRepository.saveAndFlush(blog);
        blogSearchRepository.save(blog);
        int databaseSizeBeforeDelete = blogRepository.findAll().size();

        // Get the blog
        restBlogMockMvc.perform(delete("/api/blogs/{id}", blog.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean blogExistsInEs = blogSearchRepository.exists(blog.getId());
        assertThat(blogExistsInEs).isFalse();

        // Validate the database is empty
        List<Blog> blogs = blogRepository.findAll();
        assertThat(blogs).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchBlog() throws Exception {
        // Initialize the database
        blogRepository.saveAndFlush(blog);
        blogSearchRepository.save(blog);

        // Search the blog
        restBlogMockMvc.perform(get("/api/_search/blogs?query=id:" + blog.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(blog.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].handle").value(hasItem(DEFAULT_HANDLE.toString())));
    }
}
