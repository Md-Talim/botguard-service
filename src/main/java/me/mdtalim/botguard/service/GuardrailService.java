package me.mdtalim.botguard.service;

import lombok.RequiredArgsConstructor;
import me.mdtalim.botguard.exception.BotCapExceededException;
import me.mdtalim.botguard.redis.RedisKeys;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GuardrailService {

    private static final long BOT_REPLY_CAP = 100L;

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Horizontal cap: max 100 bot replies per post.
     *
     * Strategy: INCR first, reject + rollback if over limit.
     * INCR is atomic, concurrent threads always get unique return values.
     */
    public void checkAndIncrementBotCount(Long postId) {
        String key = RedisKeys.botCount(postId);
        Long newCount = redisTemplate.opsForValue().increment(key);

        if (newCount == null) {
            throw new IllegalStateException(
                "Redis increment returned null for key: " + postId
            );
        }

        if (newCount > BOT_REPLY_CAP) {
            redisTemplate.opsForValue().decrement(key);
            throw new BotCapExceededException(
                "Post %d has reached the maximum of %d bot replies.".formatted(
                    postId,
                    BOT_REPLY_CAP
                )
            );
        }
    }
}
