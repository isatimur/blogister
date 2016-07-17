package com.isatimur.blog.web.rest.mapper;

import com.isatimur.blog.domain.*;
import com.isatimur.blog.web.rest.dto.BlogDTO;

import org.mapstruct.*;
import java.util.List;

/**
 * Mapper for the entity Blog and its DTO BlogDTO.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class, })
public interface BlogMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.login", target = "userLogin")
    BlogDTO blogToBlogDTO(Blog blog);

    List<BlogDTO> blogsToBlogDTOs(List<Blog> blogs);

    @Mapping(source = "userId", target = "user")
    @Mapping(target = "posts", ignore = true)
    Blog blogDTOToBlog(BlogDTO blogDTO);

    List<Blog> blogDTOsToBlogs(List<BlogDTO> blogDTOs);
}
