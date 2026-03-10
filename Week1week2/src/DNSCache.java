import java.util.*;
import java.util.concurrent.*;

class DNSEntry {
    String domain;
    String ipAddress;
    long expiryTime; // in milliseconds

    public DNSEntry(String domain, String ipAddress, long ttlSeconds) {
        this.domain = domain;
        this.ipAddress = ipAddress;
        this.expiryTime = System.currentTimeMillis() + ttlSeconds * 1000;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}

public class DNSCache {

    private final int MAX_CACHE_SIZE;
    private LinkedHashMap<String, DNSEntry> cache;
    private ScheduledExecutorService cleaner;

    // Stats
    private int hits = 0;
    private int misses = 0;
    private long totalLookupTime = 0; // in nanoseconds

    public DNSCache(int maxCacheSize) {
        MAX_CACHE_SIZE = maxCacheSize;

        cache = new LinkedHashMap<String, DNSEntry>(16, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        };

        // Background thread to clean expired entries every second
        cleaner = Executors.newScheduledThreadPool(1);
        cleaner.scheduleAtFixedRate(() -> cleanExpiredEntries(), 1, 1, TimeUnit.SECONDS);
    }

    // Resolve a domain
    public String resolve(String domain) {
        long start = System.nanoTime();
        DNSEntry entry;

        synchronized (cache) {
            entry = cache.get(domain);
            if (entry != null && !entry.isExpired()) {
                hits++;
                long end = System.nanoTime();
                totalLookupTime += (end - start);
                System.out.println(domain + " → Cache HIT → " + entry.ipAddress);
                return entry.ipAddress;
            } else {
                if (entry != null) cache.remove(domain); // remove expired
                misses++;
            }
        }

        // Simulate upstream DNS query
        String ipAddress = queryUpstreamDNS(domain);
        addEntry(domain, ipAddress, 5); // TTL: 5 sec for demo
        long end = System.nanoTime();
        totalLookupTime += (end - start);
        System.out.println(domain + " → Cache MISS → Query upstream → " + ipAddress);
        return ipAddress;
    }

    // Add entry to cache
    private void addEntry(String domain, String ipAddress, long ttlSeconds) {
        synchronized (cache) {
            cache.put(domain, new DNSEntry(domain, ipAddress, ttlSeconds));
        }
    }

    // Simulate upstream DNS query
    private String queryUpstreamDNS(String domain) {
        // In real-world: Use InetAddress.getByName(domain)
        return "172.217.14." + new Random().nextInt(255);
    }

    // Remove expired entries
    private void cleanExpiredEntries() {
        synchronized (cache) {
            Iterator<Map.Entry<String, DNSEntry>> it = cache.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, DNSEntry> e = it.next();
                if (e.getValue().isExpired()) it.remove();
            }
        }
    }

    // Cache stats
    public void getCacheStats() {
        int total = hits + misses;
        double hitRate = total == 0 ? 0 : ((double) hits / total) * 100;
        double avgLookupMs = total == 0 ? 0 : totalLookupTime / 1_000_000.0 / total;
        System.out.printf("Hit Rate: %.2f%%, Avg Lookup Time: %.2fms%n", hitRate, avgLookupMs);
    }

    public static void main(String[] args) throws InterruptedException {
        DNSCache dnsCache = new DNSCache(3); // max 3 entries for demo

        dnsCache.resolve("google.com");
        dnsCache.resolve("example.com");
        dnsCache.resolve("google.com"); // cache hit

        Thread.sleep(6000); // wait for TTL to expire

        dnsCache.resolve("google.com"); // cache expired, query upstream again
        dnsCache.getCacheStats();
    }
}
