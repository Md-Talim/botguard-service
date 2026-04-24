package me.mdtalim.botguard.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.mdtalim.botguard.redis.RedisKeys;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Duration NOTIF_COOLDOWN = Duration.ofMinutes(15);

    private final RedisTemplate<String, String> redisTemplate;

    public void handleBotInteraction(String botName, Long postOwnerId) {
        String cooldownKey = RedisKeys.notifCooldown(postOwnerId);
        String pendingKey = RedisKeys.pendingNotifs(postOwnerId);
        String message = "Bot " + botName + " replied to your post";

        Boolean isFirstNotif = redisTemplate.opsForValue().setIfAbsent(cooldownKey, "1", NOTIF_COOLDOWN);

        if (Boolean.TRUE.equals(isFirstNotif)) {
            // No cooldown, send notification immediately
            log.info("[PUSH SENT] User:{} -> {}", postOwnerId, message);
        } else {
            // Cooldown active, add to pending notifications for batch sweep
            redisTemplate.opsForList().rightPush(pendingKey, message);
            log.debug("[QUEUED] User:{} pending notification added", postOwnerId);
        }
    }
}
