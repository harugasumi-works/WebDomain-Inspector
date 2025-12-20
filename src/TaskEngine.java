import java.util.concurrent.PriorityBlockingQueue;
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

    public void executeAll(){
        while (!queue.isEmpty()) {
        Task currentTask = queue.poll();
            if (currentTask != null) {
                futures.add(pool.submit(currentTask));
            }
        }
    }

    public void showReport() {
    int success = 0;
    int fail = 0;
    
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
    
    System.out.println("\n=== FINAL SCOREBOARD ===");
    System.out.println("Total Targets: " + (success + fail));
    System.out.println("✅ Online: " + success);
    System.out.println("❌ Offline/Blocked: " + fail);
    System.out.println("========================");
    
    futures.clear();
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
}
