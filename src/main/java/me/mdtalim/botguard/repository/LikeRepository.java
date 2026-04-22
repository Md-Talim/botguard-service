package me.mdtalim.botguard.repository;

import java.util.Optional;
import me.mdtalim.botguard.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByPostIdAndUserId(Long postId, Long userId);
}
