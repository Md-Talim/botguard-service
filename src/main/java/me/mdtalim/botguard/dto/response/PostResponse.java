package me.mdtalim.botguard.dto.response;

import java.time.OffsetDateTime;
import lombok.Data;
import me.mdtalim.botguard.common.AuthorType;
import me.mdtalim.botguard.entity.Post;

@Data
public class PostResponse {

    private Long id;
    private AuthorType authorType;
    private Long authorId;
    private String content;
    private OffsetDateTime createdAt;

    public static PostResponse from(Post post) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setAuthorType(post.getAuthorType());
        response.setAuthorId(post.getAuthorId());
        response.setContent(post.getContent());
        response.setCreatedAt(
            post.getCreatedAt().atOffset(OffsetDateTime.now().getOffset())
        );
        return response;
    }
}
