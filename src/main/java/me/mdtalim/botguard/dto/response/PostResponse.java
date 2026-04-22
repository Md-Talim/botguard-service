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
        PostResponse r = new PostResponse();
        r.id = post.getId();
        r.authorType = post.getAuthorType();
        r.authorId = post.getAuthorId();
        r.content = post.getContent();
        r.createdAt = post.getCreatedAt();
        return r;
    }
}
