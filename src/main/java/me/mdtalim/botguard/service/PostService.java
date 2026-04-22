package me.mdtalim.botguard.service;

import lombok.RequiredArgsConstructor;
import me.mdtalim.botguard.common.AuthorType;
import me.mdtalim.botguard.dto.request.CreateCommentRequest;
import me.mdtalim.botguard.dto.request.CreatePostRequest;
import me.mdtalim.botguard.dto.request.LikePostRequest;
import me.mdtalim.botguard.dto.response.CommentResponse;
import me.mdtalim.botguard.dto.response.PostResponse;
import me.mdtalim.botguard.entity.Comment;
import me.mdtalim.botguard.entity.Like;
import me.mdtalim.botguard.entity.Post;
import me.mdtalim.botguard.exception.DepthLimitExceededException;
import me.mdtalim.botguard.exception.DuplicateLikeException;
import me.mdtalim.botguard.exception.ResourceNotFoundException;
import me.mdtalim.botguard.repository.BotRepository;
import me.mdtalim.botguard.repository.CommentRepository;
import me.mdtalim.botguard.repository.LikeRepository;
import me.mdtalim.botguard.repository.PostRepository;
import me.mdtalim.botguard.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final UserRepository userRepository;
    private final BotRepository botRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    private final ViralityService viralityService;

    @Transactional
    public PostResponse createPost(CreatePostRequest req) {
        validateAuthorExists(req.getAuthorType(), req.getAuthorId());

        Post post = Post.builder()
            .authorType(req.getAuthorType())
            .authorId(req.getAuthorId())
            .content(req.getContent())
            .build();

        return PostResponse.from(postRepository.save(post));
    }

    @Transactional
    public CommentResponse createComment(
        Long postId,
        CreateCommentRequest req
    ) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found: " + postId);
        }

        validateAuthorExists(req.getAuthorType(), req.getAuthorId());

        int depthLevel = 0;
        Long parentCommentId = req.getParentCommentId();

        if (parentCommentId != null) {
            Comment parent = commentRepository
                .findByIdAndPostId(parentCommentId, postId)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Parent comment not found: " + parentCommentId
                    )
                );

            depthLevel = parent.getDepthLevel() + 1;
        }

        if (depthLevel > 20) {
            throw new DepthLimitExceededException(
                "Comment thread cannot exceed 20 levels deep."
            );
        }

        Comment comment = Comment.builder()
            .postId(postId)
            .parentCommentId(parentCommentId)
            .authorType(req.getAuthorType())
            .authorId(req.getAuthorId())
            .content(req.getContent())
            .depthLevel(depthLevel)
            .build();

        comment = commentRepository.save(comment);
        switch (req.getAuthorType()) {
            case BOT -> viralityService.onBotReply(postId);
            case USER -> viralityService.onHumanComment(postId);
        }

        return CommentResponse.from(comment);
    }

    @Transactional
    public void likePost(Long postId, LikePostRequest req) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found: " + postId);
        }
        if (!userRepository.existsById(req.getUserId())) {
            throw new ResourceNotFoundException(
                "User not found: " + req.getUserId()
            );
        }

        if (
            likeRepository
                .findByPostIdAndUserId(postId, req.getUserId())
                .isPresent()
        ) {
            throw new DuplicateLikeException("User already liked this post");
        }

        Like like = Like.builder()
            .postId(postId)
            .userId(req.getUserId())
            .build();

        likeRepository.save(like);
        viralityService.onHumanLike(postId);
    }

    private void validateAuthorExists(AuthorType type, Long authorId) {
        boolean exists = switch (type) {
            case USER -> userRepository.existsById(authorId);
            case BOT -> botRepository.existsById(authorId);
        };
        if (!exists) {
            throw new ResourceNotFoundException(
                type + " not found: " + authorId
            );
        }
    }
}
