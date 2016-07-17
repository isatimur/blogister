package com.isatimur.blog.web.rest.mapper;

import com.isatimur.blog.domain.*;
import com.isatimur.blog.web.rest.dto.PostsDTO;

import org.mapstruct.*;
import java.util.List;

/**
 * Mapper for the entity Posts and its DTO PostsDTO.
 */
@Mapper(componentModel = "spring", uses = {TagMapper.class, })
public interface PostsMapper {

    @Mapping(source = "blog.id", target = "blogId")
    @Mapping(source = "blog.name", target = "blogName")
    PostsDTO postsToPostsDTO(Posts posts);

    List<PostsDTO> postsToPostsDTOs(List<Posts> posts);

    @Mapping(source = "blogId", target = "blog")
    Posts postsDTOToPosts(PostsDTO postsDTO);

    List<Posts> postsDTOsToPosts(List<PostsDTO> postsDTOs);

    default Tag tagFromId(Long id) {
        if (id == null) {
            return null;
        }
        Tag tag = new Tag();
        tag.setId(id);
        return tag;
    }

    default Blog blogFromId(Long id) {
        if (id == null) {
            return null;
        }
        Blog blog = new Blog();
        blog.setId(id);
        return blog;
    }
}
