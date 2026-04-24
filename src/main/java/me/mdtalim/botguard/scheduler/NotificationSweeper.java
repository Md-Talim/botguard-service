package me.mdtalim.botguard.scheduler;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSweeper {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Runs every 5 minutes.
     * Scans for all users with pending notifications,
     * pops and summarizes them, then clears the list.
     *
     * KEYS scan pattern: user:*:pending_notifs
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5 minutes in ms
    public void sweep() {
        log.info("[SWEEPER] Starting notification sweep...");

        Set<String> keys = redisTemplate.keys("user:*:pending_notifs");

        if (keys == null || keys.isEmpty()) {
            log.info("[SWEEPER] No pending notifications found.");
            return;
        }

        for (String key : keys) {
            processPendingNotifs(key);
        }

        log.info("[SWEEPER] Sweep complete. Processed {} users.", keys.size());
    }

    private void processPendingNotifs(String key) {
        List<String> messages = redisTemplate.opsForList().range(key, 0, -1);

        if (messages == null || messages.isEmpty()) return;

        redisTemplate.delete(key);

        String first = messages.get(0);
        int others = messages.size() - 1;

        String summary = first;
        if (others > 0) {
            String botName = first.split(" ")[1];
            summary = "Bot %s and %d others interacted with your posts.".formatted(botName, others);
        }

        String userId = key.split(":")[1];
        log.info("[SUMMARIZED PUSH] User:{} -> {}", userId, summary);
    }
}
