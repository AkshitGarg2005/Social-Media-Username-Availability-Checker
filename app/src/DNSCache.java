import java.util.*;
import java.util.concurrent.*;

class DNSEntry {
    String domain;
    String ipAddress;
    long expiryTime;

    DNSEntry(String domain, String ipAddress, int ttlSeconds) {
        this.domain = domain;
        this.ipAddress = ipAddress;
        this.expiryTime = System.currentTimeMillis() + ttlSeconds * 1000L;
    }

    boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}

public class DNSCache {
    private final int capacity;
    private final Map<String, DNSEntry> cache;
    private int hits = 0;
    private int misses = 0;

    public DNSCache(int capacity) {
        this.capacity = capacity;
        this.cache = new LinkedHashMap<>(capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > DNSCache.this.capacity;
            }
        };

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::cleanExpiredEntries, 5, 5, TimeUnit.SECONDS);
    }

    public synchronized String resolve(String domain) {
        DNSEntry entry = cache.get(domain);

        if (entry != null && !entry.isExpired()) {
            hits++;
            return "Cache HIT → " + entry.ipAddress;
        } else {
            misses++;
            String ip = queryUpstreamDNS(domain);
            cache.put(domain, new DNSEntry(domain, ip, 300));
            return (entry == null ? "Cache MISS → " : "Cache EXPIRED → ") + ip;
        }
    }

    private String queryUpstreamDNS(String domain) {
        // Simulated upstream DNS query (random IP for demo)
        return "172.217." + new Random().nextInt(255) + "." + new Random().nextInt(255);
    }

    private synchronized void cleanExpiredEntries() {
        Iterator<Map.Entry<String, DNSEntry>> iterator = cache.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, DNSEntry> entry = iterator.next();
            if (entry.getValue().isExpired()) {
                iterator.remove();
            }
        }
    }

    public synchronized String getCacheStats() {
        int total = hits + misses;
        double hitRate = total == 0 ? 0 : (hits * 100.0 / total);
        return "Hit Rate: " + hitRate + "%, Hits: " + hits + ", Misses: " + misses;
    }

    public static void main(String[] args) throws InterruptedException {
        DNSCache dnsCache = new DNSCache(5);

        System.out.println(dnsCache.resolve("google.com"));
        System.out.println(dnsCache.resolve("google.com"));
        Thread.sleep(310 * 1000); // wait for TTL expiry
        System.out.println(dnsCache.resolve("google.com"));

        System.out.println(dnsCache.resolve("yahoo.com"));
        System.out.println(dnsCache.resolve("bing.com"));
        System.out.println(dnsCache.resolve("duckduckgo.com"));
        System.out.println(dnsCache.resolve("example.com"));
        System.out.println(dnsCache.resolve("github.com")); // triggers LRU eviction

        System.out.println(dnsCache.getCacheStats());
    }
}