package me.mdtalim.botguard.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import me.mdtalim.botguard.common.AuthorType;

@Data
public class CreatePostRequest {

    @NotNull
    private AuthorType authorType;

    @NotNull
    private Long authorId;

    @NotNull
    private String content;
}
