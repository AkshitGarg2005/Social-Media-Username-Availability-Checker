import java.util.*;
import java.util.concurrent.*;

class PageViewEvent {
    String url;
    String userId;
    String source;

    PageViewEvent(String url, String userId, String source) {
        this.url = url;
        this.userId = userId;
        this.source = source;
    }
}

public class RealTimeAnalytics {
    private final Map<String, Integer> pageViews = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();
    private final Map<String, Integer> trafficSources = new ConcurrentHashMap<>();

    public void processEvent(PageViewEvent event) {
        pageViews.merge(event.url, 1, Integer::sum);
        uniqueVisitors.computeIfAbsent(event.url, k -> ConcurrentHashMap.newKeySet()).add(event.userId);
        trafficSources.merge(event.source.toLowerCase(), 1, Integer::sum);
    }

    public void getDashboard() {
        System.out.println("Top Pages:");
        pageViews.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(10)
                .forEach(entry -> {
                    String url = entry.getKey();
                    int views = entry.getValue();
                    int uniques = uniqueVisitors.getOrDefault(url, Collections.emptySet()).size();
                    System.out.println(url + " - " + views + " views (" + uniques + " unique)");
                });

        int totalSources = trafficSources.values().stream().mapToInt(Integer::intValue).sum();
        System.out.println("\nTraffic Sources:");
        trafficSources.forEach((source, count) -> {
            double percentage = (count * 100.0) / totalSources;
            System.out.println(source + ": " + String.format("%.1f", percentage) + "%");
        });
    }

    public static void main(String[] args) throws InterruptedException {
        RealTimeAnalytics analytics = new RealTimeAnalytics();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(analytics::getDashboard, 5, 5, TimeUnit.SECONDS);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        Random random = new Random();
        String[] urls = {"/article/breaking-news", "/sports/championship", "/tech/ai-trends"};
        String[] sources = {"google", "facebook", "direct", "other"};

        for (int i = 0; i < 1000; i++) {
            final int userId = i;
            executor.submit(() -> {
                String url = urls[random.nextInt(urls.length)];
                String source = sources[random.nextInt(sources.length)];
                analytics.processEvent(new PageViewEvent(url, "user_" + userId, source));
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }
}