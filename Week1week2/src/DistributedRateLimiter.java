import java.util.concurrent.ConcurrentHashMap;

public class DistributedRateLimiter {

    // Max requests allowed per hour
    private static final int MAX_TOKENS = 1000;

    // Refill rate: tokens per millisecond
    private static final double REFILL_RATE = (double) MAX_TOKENS / (60 * 60 * 1000);

    // Store client buckets
    private ConcurrentHashMap<String, TokenBucket> clientBuckets = new ConcurrentHashMap<>();

    // Token Bucket class
    static class TokenBucket {
        double tokens;
        long lastRefillTime;
        int maxTokens;
        double refillRate;

        public TokenBucket(int maxTokens, double refillRate) {
            this.tokens = maxTokens;
            this.maxTokens = maxTokens;
            this.refillRate = refillRate;
            this.lastRefillTime = System.currentTimeMillis();
        }

        // refill tokens based on elapsed time
        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;

            double tokensToAdd = elapsed * refillRate;
            tokens = Math.min(maxTokens, tokens + tokensToAdd);

            lastRefillTime = now;
        }

        // try to consume a token
        public synchronized boolean allowRequest() {
            refill();

            if (tokens >= 1) {
                tokens -= 1;
                return true;
            }

            return false;
        }

        public synchronized int getRemainingTokens() {
            refill();
            return (int) tokens;
        }

        public synchronized long getRetryAfterSeconds() {
            refill();
            if (tokens >= 1) return 0;

            double tokensNeeded = 1 - tokens;
            return (long) (tokensNeeded / refillRate / 1000);
        }
    }

    // Check rate limit
    public String checkRateLimit(String clientId) {

        TokenBucket bucket = clientBuckets.computeIfAbsent(
                clientId,
                id -> new TokenBucket(MAX_TOKENS, REFILL_RATE)
        );

        if (bucket.allowRequest()) {
            int remaining = bucket.getRemainingTokens();
            return "Allowed (" + remaining + " requests remaining)";
        } else {
            long retry = bucket.getRetryAfterSeconds();
            return "Denied (0 requests remaining, retry after " + retry + "s)";
        }
    }

    // Get client rate limit status
    public String getRateLimitStatus(String clientId) {

        TokenBucket bucket = clientBuckets.get(clientId);

        if (bucket == null) {
            return "{used:0, limit:" + MAX_TOKENS + ", reset:0}";
        }

        int remaining = bucket.getRemainingTokens();
        int used = MAX_TOKENS - remaining;

        long resetTime = System.currentTimeMillis() + (remaining / (long) REFILL_RATE);

        return "{used:" + used +
                ", limit:" + MAX_TOKENS +
                ", reset:" + resetTime / 1000 + "}";
    }

    // Demo
    public static void main(String[] args) {

        DistributedRateLimiter limiter = new DistributedRateLimiter();

        String clientId = "abc123";

        for (int i = 0; i < 5; i++) {
            System.out.println(limiter.checkRateLimit(clientId));
        }

        System.out.println(limiter.getRateLimitStatus(clientId));
    }
}