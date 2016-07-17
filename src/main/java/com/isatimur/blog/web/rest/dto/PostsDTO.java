package com.isatimur.blog.web.rest.dto;

import java.time.ZonedDateTime;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;


/**
 * A DTO for the Posts entity.
 */
public class PostsDTO implements Serializable {

    private Long id;

    @NotNull
    private String title;

    @NotNull
    private String content;

    @NotNull
    private ZonedDateTime creattionDate;


    private Set<TagDTO> tags = new HashSet<>();

    private Long blogId;
    

    private String blogName;

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

    public Set<TagDTO> getTags() {
        return tags;
    }

    public void setTags(Set<TagDTO> tags) {
        this.tags = tags;
    }

    public Long getBlogId() {
        return blogId;
    }

    public void setBlogId(Long blogId) {
        this.blogId = blogId;
    }


    public String getBlogName() {
        return blogName;
    }

    public void setBlogName(String blogName) {
        this.blogName = blogName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PostsDTO postsDTO = (PostsDTO) o;

        if ( ! Objects.equals(id, postsDTO.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "PostsDTO{" +
            "id=" + id +
            ", title='" + title + "'" +
            ", content='" + content + "'" +
            ", creattionDate='" + creattionDate + "'" +
            '}';
    }
}
