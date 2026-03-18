import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;

class TokenBucket {
    private final int maxTokens;
    private final int refillRate; // tokens per hour
    private AtomicInteger tokens;
    private long lastRefillTime;

    public TokenBucket(int maxTokens, int refillRate) {
        this.maxTokens = maxTokens;
        this.refillRate = refillRate;
        this.tokens = new AtomicInteger(maxTokens);
        this.lastRefillTime = System.currentTimeMillis();
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRefillTime;
        if (elapsed >= 3600_000) { // 1 hour
            tokens.set(maxTokens);
            lastRefillTime = now;
        }
    }

    public synchronized boolean allowRequest() {
        refill();
        if (tokens.get() > 0) {
            tokens.decrementAndGet();
            return true;
        }
        return false;
    }

    public int getRemainingTokens() {
        refill();
        return tokens.get();
    }

    public long getResetTime() {
        return lastRefillTime + 3600_000;
    }
}

public class RateLimiter {
    private final Map<String, TokenBucket> clientBuckets = new ConcurrentHashMap<>();
    private final int maxTokens = 1000;
    private final int refillRate = 1000;

    public String checkRateLimit(String clientId) {
        TokenBucket bucket = clientBuckets.computeIfAbsent(clientId,
                k -> new TokenBucket(maxTokens, refillRate));

        if (bucket.allowRequest()) {
            return "Allowed (" + bucket.getRemainingTokens() + " requests remaining)";
        } else {
            long retryAfter = (bucket.getResetTime() - System.currentTimeMillis()) / 1000;
            return "Denied (0 requests remaining, retry after " + retryAfter + "s)";
        }
    }

    public String getRateLimitStatus(String clientId) {
        TokenBucket bucket = clientBuckets.get(clientId);
        if (bucket == null) return "Client not found";
        int used = maxTokens - bucket.getRemainingTokens();
        return "{used: " + used + ", limit: " + maxTokens + ", reset: " + bucket.getResetTime() + "}";
    }

    public static void main(String[] args) {
        RateLimiter limiter = new RateLimiter();

        for (int i = 0; i < 1005; i++) {
            System.out.println("Request " + (i+1) + ": " + limiter.checkRateLimit("abc123"));
        }

        System.out.println(limiter.getRateLimitStatus("abc123"));
    }
}