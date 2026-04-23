package me.mdtalim.botguard.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import me.mdtalim.botguard.exception.BotCapExceededException;
import me.mdtalim.botguard.exception.CooldownActiveException;
import me.mdtalim.botguard.redis.RedisKeys;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GuardrailService {

    private static final long BOT_REPLY_CAP = 100L;
    private static final long COOLDOWN_SECONDS = 600L; // 10 minutes

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
            throw new IllegalStateException("Redis increment returned null for key: " + postId);
        }

        if (newCount > BOT_REPLY_CAP) {
            redisTemplate.opsForValue().decrement(key);
            throw new BotCapExceededException(
                "Post %d has reached the maximum of %d bot replies.".formatted(postId, BOT_REPLY_CAP)
            );
        }
    }

    /**
     * Cooldown: a bot cannot interact with the same human's post
     * more than once per 10 minutes.
     *
     * Strategy: SETNX with expiration. If key exists, cooldown is active.
     */
    public void checkCooldown(Long botId, Long humanAuthorId) {
        String key = RedisKeys.cooldown(botId, humanAuthorId);

        Boolean wasSet = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(COOLDOWN_SECONDS));

        if (Boolean.FALSE.equals(wasSet)) {
            throw new CooldownActiveException("Bot " + botId + " must wait before interacting with this user again.");
        }
    }
}
