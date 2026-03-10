import java.util.*;

// Video Data
class VideoData {
    String videoId;
    String content;

    public VideoData(String videoId, String content) {
        this.videoId = videoId;
        this.content = content;
    }
}

public class MultiLevelCache {

    // L1 Cache (Memory) - 10,000 videos
    private final int L1_CAPACITY = 10000;

    // L2 Cache (SSD) - 100,000 videos
    private final int L2_CAPACITY = 100000;

    // L1 Cache using LinkedHashMap for LRU
    private LinkedHashMap<String, VideoData> L1;

    // L2 Cache
    private LinkedHashMap<String, VideoData> L2;

    // L3 Database (simulated)
    private HashMap<String, VideoData> database;

    // Access counts
    private HashMap<String, Integer> accessCount;

    // Statistics
    private int L1Hits = 0;
    private int L2Hits = 0;
    private int L3Hits = 0;

    public MultiLevelCache() {

        L1 = new LinkedHashMap<>(L1_CAPACITY, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, VideoData> eldest) {
                return size() > L1_CAPACITY;
            }
        };

        L2 = new LinkedHashMap<>(L2_CAPACITY, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, VideoData> eldest) {
                return size() > L2_CAPACITY;
            }
        };

        database = new HashMap<>();
        accessCount = new HashMap<>();

        // Populate database with sample videos
        for (int i = 1; i <= 200000; i++) {
            database.put("video_" + i,
                    new VideoData("video_" + i, "Video Content " + i));
        }
    }

    // Get video
    public VideoData getVideo(String videoId) {

        long start = System.currentTimeMillis();

        // L1 check
        if (L1.containsKey(videoId)) {
            L1Hits++;
            System.out.println("L1 Cache HIT (0.5ms)");
            return L1.get(videoId);
        }

        System.out.println("L1 Cache MISS");

        // L2 check
        if (L2.containsKey(videoId)) {
            L2Hits++;
            System.out.println("L2 Cache HIT (5ms)");

            VideoData data = L2.get(videoId);

            promoteToL1(videoId, data);

            return data;
        }

        System.out.println("L2 Cache MISS");

        // L3 database
        if (database.containsKey(videoId)) {
            L3Hits++;
            System.out.println("L3 Database HIT (150ms)");

            VideoData data = database.get(videoId);

            L2.put(videoId, data);

            accessCount.put(videoId,
                    accessCount.getOrDefault(videoId, 0) + 1);

            return data;
        }

        System.out.println("Video not found");
        return null;
    }

    // Promote video to L1
    private void promoteToL1(String videoId, VideoData data) {

        int count = accessCount.getOrDefault(videoId, 0) + 1;

        accessCount.put(videoId, count);

        if (count > 2) {
            L1.put(videoId, data);
            System.out.println("Promoted to L1");
        }
    }

    // Invalidate cache when video updates
    public void invalidate(String videoId) {

        L1.remove(videoId);
        L2.remove(videoId);

        System.out.println("Cache invalidated for " + videoId);
    }

    // Cache statistics
    public void getStatistics() {

        int total = L1Hits + L2Hits + L3Hits;

        double L1Rate = total == 0 ? 0 : (L1Hits * 100.0) / total;
        double L2Rate = total == 0 ? 0 : (L2Hits * 100.0) / total;
        double L3Rate = total == 0 ? 0 : (L3Hits * 100.0) / total;

        System.out.println("\nCache Statistics:");
        System.out.println("L1 Hit Rate: " + String.format("%.2f", L1Rate) + "%");
        System.out.println("L2 Hit Rate: " + String.format("%.2f", L2Rate) + "%");
        System.out.println("L3 Hit Rate: " + String.format("%.2f", L3Rate) + "%");

        double overall = ((L1Hits + L2Hits) * 100.0) / total;

        System.out.println("Overall Cache Hit Rate: "
                + String.format("%.2f", overall) + "%");
    }

    // Demo
    public static void main(String[] args) {

        MultiLevelCache cache = new MultiLevelCache();

        cache.getVideo("video_123");
        cache.getVideo("video_123");
        cache.getVideo("video_123");

        cache.getVideo("video_999");

        cache.invalidate("video_123");

        cache.getStatistics();
    }
}
