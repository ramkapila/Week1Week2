import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;

class PageViewEvent {
    String url;
    String userId;
    String source;

    public PageViewEvent(String url, String userId, String source) {
        this.url = url;
        this.userId = userId;
        this.source = source;
    }
}

public class RealTimeAnalyticsDashboard {

    // Page URL -> total views
    private ConcurrentHashMap<String, AtomicInteger> pageViews;

    // Page URL -> unique visitors
    private ConcurrentHashMap<String, Set<String>> uniqueVisitors;

    // Traffic source -> count
    private ConcurrentHashMap<String, AtomicInteger> trafficSources;

    // For periodic dashboard updates
    private ScheduledExecutorService scheduler;

    public RealTimeAnalyticsDashboard() {
        pageViews = new ConcurrentHashMap<>();
        uniqueVisitors = new ConcurrentHashMap<>();
        trafficSources = new ConcurrentHashMap<>();

        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> printDashboard(), 5, 5, TimeUnit.SECONDS);
    }

    // Process an incoming page view event
    public void processEvent(PageViewEvent event) {
        // Update total page views
        pageViews.computeIfAbsent(event.url, k -> new AtomicInteger(0)).incrementAndGet();

        // Update unique visitors
        uniqueVisitors.computeIfAbsent(event.url, k -> ConcurrentHashMap.newKeySet()).add(event.userId);

        // Update traffic source
        trafficSources.computeIfAbsent(event.source, k -> new AtomicInteger(0)).incrementAndGet();
    }

    // Print top pages and traffic stats
    public void printDashboard() {
        System.out.println("\n=== Real-Time Dashboard ===");

        // Top 10 pages by total views
        List<String> topPages = pageViews.entrySet().stream()
                .sorted((a, b) -> b.getValue().get() - a.getValue().get())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        System.out.println("Top Pages:");
        int rank = 1;
        for (String url : topPages) {
            int views = pageViews.get(url).get();
            int unique = uniqueVisitors.get(url).size();
            System.out.printf("%d. %s - %d views (%d unique)\n", rank++, url, views, unique);
        }

        // Traffic sources
        System.out.println("\nTraffic Sources:");
        trafficSources.forEach((source, count) -> {
            System.out.printf("%s → %d visits\n", source, count.get());
        });
    }

    // Stop the scheduler when shutting down
    public void shutdown() {
        scheduler.shutdown();
    }

    // Demo
    public static void main(String[] args) throws InterruptedException {
        RealTimeAnalyticsDashboard dashboard = new RealTimeAnalyticsDashboard();

        // Simulate incoming page view events
        Random rand = new Random();
        String[] pages = {"/article/breaking-news", "/sports/championship", "/tech/new-gadget"};
        String[] sources = {"google", "facebook", "twitter", "direct"};

        for (int i = 0; i < 50; i++) {
            String url = pages[rand.nextInt(pages.length)];
            String userId = "user_" + rand.nextInt(20);
            String source = sources[rand.nextInt(sources.length)];

            dashboard.processEvent(new PageViewEvent(url, userId, source));
            Thread.sleep(100); // simulate event frequency
        }

        // Let the dashboard run for a few updates
        Thread.sleep(15000);
        dashboard.shutdown();
    }
}
