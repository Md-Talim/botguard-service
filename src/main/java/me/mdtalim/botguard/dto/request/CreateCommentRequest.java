package me.mdtalim.botguard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import me.mdtalim.botguard.common.AuthorType;

@Data
public class CreateCommentRequest {

    @NotNull
    private AuthorType authorType;

    @NotNull
    private Long authorId;

    @NotBlank
    private String content;

    private Long parentCommentId;
}
