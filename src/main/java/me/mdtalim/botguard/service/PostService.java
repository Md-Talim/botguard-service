package me.mdtalim.botguard.service;

import lombok.RequiredArgsConstructor;
import me.mdtalim.botguard.dto.request.CreatePostRequest;
import me.mdtalim.botguard.dto.response.PostResponse;
import me.mdtalim.botguard.entity.Post;
import me.mdtalim.botguard.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    @Transactional
    public PostResponse createPost(CreatePostRequest req) {
        Post post = Post.builder()
            .authorType(req.getAuthorType())
            .authorId(req.getAuthorId())
            .content(req.getContent())
            .build();

        return PostResponse.from(postRepository.save(post));
    }
}
