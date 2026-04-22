package me.mdtalim.botguard.dto.response;

import java.time.OffsetDateTime;
import lombok.Data;
import me.mdtalim.botguard.common.AuthorType;
import me.mdtalim.botguard.entity.Comment;

@Data
public class CommentResponse {

    private Long id;
    private Long postId;
    private Long parentCommentId;
    private AuthorType authorType;
    private Long authorId;
    private String content;
    private int depthLevel;
    private OffsetDateTime createdAt;

    public static CommentResponse from(Comment c) {
        CommentResponse r = new CommentResponse();
        r.id = c.getId();
        r.postId = c.getPostId();
        r.parentCommentId = c.getParentCommentId();
        r.authorType = c.getAuthorType();
        r.authorId = c.getAuthorId();
        r.content = c.getContent();
        r.depthLevel = c.getDepthLevel();
        r.createdAt = c.getCreatedAt();
        return r;
    }
}
