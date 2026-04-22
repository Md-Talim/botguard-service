package me.mdtalim.botguard.redis;

public final class RedisKeys {

    private RedisKeys() {}

    public static String viralityScore(Long postId) {
        return "post:" + postId + ":virality_score";
    }

    public static String botCount(Long postId) {
        return "post:" + postId + ":bot_count";
    }
}
