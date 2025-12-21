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

    /** 
     * @param task
     */
    public void addTask (Task task){
        queue.add(task);
    }

    /** 
     * @param logBridge
     */
    public void executeAll(Consumer<String> logBridge){
        Task currentTask;
        while ((currentTask = queue.poll()) != null) {
            currentTask.setLogger(logBridge);
            futures.add(pool.submit(currentTask));
        }
    }

    /** 
     * @return String
     */
    public String showReport() {
    int success = 0;
    int fail = 0;
    List<String> report = new ArrayList<>();
    for (Future<Boolean> result : futures) {
        try {
            if (result.get()) {
                success++;
            } else {
                fail++;
            }
        } catch (Exception e) {
            fail++;
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
    pool.shutdown(); 
    try {
        if (!pool.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
            pool.shutdownNow(); 
        }
    } catch (InterruptedException e) {
        pool.shutdownNow();
        Thread.currentThread().interrupt();
        }
    }

    /** 
     * @return String
     * @throws InterruptedException
     */
    public String waitForCompletion() throws InterruptedException {
        try {
            for (Future<Boolean> future : futures) {
                try {
                    future.get(); 
                } catch (java.util.concurrent.ExecutionException e) {
                }
            }
            return "All tasks done";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }
}
