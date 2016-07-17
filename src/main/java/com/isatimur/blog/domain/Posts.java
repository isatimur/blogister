package com.isatimur.blog.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A Posts.
 */
@Entity
@Table(name = "posts")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "posts")
public class Posts implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull
    @Column(name = "content", nullable = false)
    private String content;

    @NotNull
    @Column(name = "creattion_date", nullable = false)
    private ZonedDateTime creattionDate;

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "posts_tag",
               joinColumns = @JoinColumn(name="posts_id", referencedColumnName="ID"),
               inverseJoinColumns = @JoinColumn(name="tags_id", referencedColumnName="ID"))
    private Set<Tag> tags = new HashSet<>();

    @ManyToOne
    private Blog blog;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ZonedDateTime getCreattionDate() {
        return creattionDate;
    }

    public void setCreattionDate(ZonedDateTime creattionDate) {
        this.creattionDate = creattionDate;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public Blog getBlog() {
        return blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Posts posts = (Posts) o;
        if(posts.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, posts.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Posts{" +
            "id=" + id +
            ", title='" + title + "'" +
            ", content='" + content + "'" +
            ", creattionDate='" + creattionDate + "'" +
            '}';
    }
}
