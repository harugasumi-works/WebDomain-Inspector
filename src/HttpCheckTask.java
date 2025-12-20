import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;


public class HttpCheckTask implements Task{
    private final LogLevel level;
    private final String url;
    private final LogCallback logger;
    private static final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL) // Handle 301 Redirects automatically
            .build();

    public HttpCheckTask(LogLevel level, String url, LogCallback logger) {
        this.level = level;
        this.url = url;
        this.logger = logger;
    }

    @Override
    public Boolean call() throws Exception {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(this.url))
                    .timeout(Duration.ofSeconds(3)) // Spec: 3 Second Timeout
                    .header("User-Agent", "Mozilla/5.0 (Sentinel/1.0)")
                    .method("HEAD", HttpRequest.BodyPublishers.noBody()) // Spec: Lightweight Check
                    .build();

            // Synchronous send (blocking this worker thread only)
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            String msg = String.format("[%d] %s", response.statusCode(), url);
            logger.onLog(msg);
            return true;
           

        } catch (Exception e) {
           System.out.printf("[ERROR] %s - %s%n", url, e.getMessage());
           return false;
        }
    }


    public int getPriority() {
        return level.getValue();
    }
}
