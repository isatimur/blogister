package com.isatimur.blog.web.rest.mapper;

import com.isatimur.blog.domain.*;
import com.isatimur.blog.web.rest.dto.TagDTO;

import org.mapstruct.*;
import java.util.List;

/**
 * Mapper for the entity Tag and its DTO TagDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface TagMapper {

    TagDTO tagToTagDTO(Tag tag);

    List<TagDTO> tagsToTagDTOs(List<Tag> tags);

    @Mapping(target = "posts", ignore = true)
    Tag tagDTOToTag(TagDTO tagDTO);

    List<Tag> tagDTOsToTags(List<TagDTO> tagDTOs);
}
