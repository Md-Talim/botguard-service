package me.mdtalim.botguard.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.mdtalim.botguard.dto.request.CreateCommentRequest;
import me.mdtalim.botguard.dto.request.CreatePostRequest;
import me.mdtalim.botguard.dto.request.LikePostRequest;
import me.mdtalim.botguard.dto.response.CommentResponse;
import me.mdtalim.botguard.dto.response.PostResponse;
import me.mdtalim.botguard.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(
        @Valid @RequestBody CreatePostRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            postService.createPost(req)
        );
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
        @PathVariable Long postId,
        @Valid @RequestBody CreateCommentRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            postService.createComment(postId, req)
        );
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> likePost(
        @PathVariable Long postId,
        @Valid @RequestBody LikePostRequest req
    ) {
        postService.likePost(postId, req);
        return ResponseEntity.ok().build();
    }
}
