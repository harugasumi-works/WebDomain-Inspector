import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TaskEngine {
    private PriorityBlockingQueue<Task> queue = new PriorityBlockingQueue<>(11, Comparator.comparingInt(Task::getPriority).reversed());
    private final ExecutorService pool = Executors.newFixedThreadPool(10);
    private List<Future<Boolean>> futures = new ArrayList<>();

    public void addTask (Task task){
        queue.add(task);
    }

    public void executeAll(Consumer<String> logBridge){
        Task currentTask;
        while ((currentTask = queue.poll()) != null) {
            currentTask.setLogger(logBridge);
            futures.add(pool.submit(currentTask));
        }
    }

    public String showReport() {
    int success = 0;
    int fail = 0;
    List<String> report = new ArrayList<>();
    // We wait for all tasks to finish here
    for (Future<Boolean> result : futures) {
        try {
            if (result.get()) {
                success++;
            } else {
                fail++;
            }
        } catch (Exception e) {
            fail++; // Count crashed tasks as failures
        }
    }
    
    report.add("\n=== FINAL SCOREBOARD ===");
    report.add("Total Targets: " + (success + fail));
    report.add("✅ Online: " + success);
    report.add("❌ Offline/Blocked: " + fail);
    report.add("========================");
    
    futures.clear();
    String result = report.stream()
      .map(String::valueOf)
      .collect(Collectors.joining("\n", "", ""));
    return result;

    }

    public void stop() {
    pool.shutdown(); // Stop accepting new tasks
    try {
        // Wait up to 60 seconds for existing tasks to finish
        if (!pool.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
            pool.shutdownNow(); // Force kill if they take too long
        }
    } catch (InterruptedException e) {
        pool.shutdownNow();
        Thread.currentThread().interrupt();
        }
    }

    public String waitForCompletion() throws InterruptedException {
        try {
            for (Future<Boolean> future : futures) {
                try {
                    future.get(); // blocks until this task completes
                } catch (java.util.concurrent.ExecutionException e) {
                    // task threw; treat as completed and continue
                }
            }
            return "All tasks done";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }
}
