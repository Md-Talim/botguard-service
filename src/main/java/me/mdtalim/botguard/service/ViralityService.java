package me.mdtalim.botguard.service;

import lombok.RequiredArgsConstructor;
import me.mdtalim.botguard.redis.RedisKeys;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ViralityService {

    private static final long POINTS_BOT_REPLY = 1L;
    private static final long POINTS_HUMAN_LIKE = 20L;
    private static final long POINTS_HUMAN_COMMENT = 50L;

    private final RedisTemplate<String, String> redisTemplate;

    public long onBotReply(Long postId) {
        return increment(postId, POINTS_BOT_REPLY);
    }

    public long onHumanLike(Long postId) {
        return increment(postId, POINTS_HUMAN_LIKE);
    }

    public long onHumanComment(Long postId) {
        return increment(postId, POINTS_HUMAN_COMMENT);
    }

    public long getScore(Long postId) {
        String val = redisTemplate.opsForValue().get(RedisKeys.viralityScore(postId));
        return val == null ? 0L : Long.parseLong(val);
    }

    private long increment(Long postId, long points) {
        String key = RedisKeys.viralityScore(postId);
        Long newScore = redisTemplate.opsForValue().increment(key, points);
        return newScore == null ? 0L : newScore;
    }
}
