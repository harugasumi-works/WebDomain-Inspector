package com.harugasumi.core;

import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

/**
 * Made by @author harugasumi-works
 * このクラスは、優先度付きタスクキューを管理し、タスクの実行と結果の集計を行います。
 * タスクは優先度に基づいて実行され、ログ出力のためのロガーを設定できます。
 * タスクの実行はスレッドプールを使用して並行して行われます。
 * タスクの実行結果は集計され、最終的なレポートが生成されます。
 * タスクの追加、実行、レポート生成、停止、および完了待機の各機能を提供します。
 */
public class TaskEngine {

    private static final int QUEUE_DEFAULT_CAPACITY = 11;
    private static final Comparator<Task> HIGHEST_PRIORITY_FIRST = Comparator.comparingInt(Task::getPriority)
            .reversed();

    /**
     * 優先順位に基づいてタスクを処理するブロッキングキュー。
     * <p>
     * {@link #HIGHEST_PRIORITY_FIRST} により、優先度が高い（数値が大きい）タスクが
     * 常にキューの先頭に来るように順序付けられます。
     * </p>
     * <p>
     * 初期容量は {@value #DEFAULT_CAPACITY} ですが、要素数に応じて自動的に拡張されます。
     * スレッドセーフであるため、複数の生産者・消費者スレッドから同時にアクセス可能です。
     * </p>
     */
    private PriorityBlockingQueue<Task> queue = new PriorityBlockingQueue<>(QUEUE_DEFAULT_CAPACITY,
            HIGHEST_PRIORITY_FIRST);
    private final ExecutorService pool = Executors.newFixedThreadPool(10);
    private final List<CompletableFuture<Boolean>> futures = Collections.synchronizedList(new ArrayList<>());

    /**
     * タスクをキューに追加します。
     * 
     * @param task
     */
    public void addTask(Task task) {
        queue.add(task);
    }

/**
 * 現在キューに滞留しているすべてのタスクを取り出し、スレッドプールへ送信して実行します。
 * <p>
 * このメソッドはノンブロッキングです。実行前に、完了済みのタスク（Future）は
 * 内部リストから削除され、メモリリークを防止します。
 * </p>
 * * @param logBridge タスク実行時のログ出力を処理するための {@link Consumer}
 */
public synchronized void executeAll(Consumer<String> logBridge) {
    this.futures.removeIf(Future::isDone);

    Task currentTask;
    while ((currentTask = queue.poll()) != null) {
        Task finalCurrentTask = currentTask;
        finalCurrentTask.setLogger(logBridge);

        CompletableFuture<Boolean> cf = CompletableFuture.supplyAsync(() -> {
            try {
                return finalCurrentTask.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, pool);
        this.futures.add(cf);
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
 public String waitForCompletion(long timeout, TimeUnit unit) {
        List<CompletableFuture<Boolean>> snap;
        
        // 1. Snapshot (The Copy)
        synchronized (futures) {
            snap = new ArrayList<>(futures);
        }

        if (snap.isEmpty()) {
            return "No tasks to wait for."; //$NON-NLS-1$
        }

        // 2. The Blocking Wait
        CompletableFuture<Void> allOf = CompletableFuture.allOf(
            snap.toArray(new CompletableFuture[0])
        );

        boolean timedOut = false;

        try {
            // Wait for your chosen 5 seconds
            allOf.get(timeout, unit);
        } catch (TimeoutException e) {
            timedOut = true;
            System.err.println("Timeout hit! cancelling " + snap.size() + " tasks to free resources.");
        } catch (Exception e) {
            // Handle InterruptedException or ExecutionException
            System.err.println("Wait error: " + e.getMessage());
        }

        // 3. Count & Cleanup (The Expert Touch)
        long success = 0;
        long fail = 0;

        for (CompletableFuture<Boolean> cf : snap) {
            if (timedOut) {
                // FORCE CANCEL the task if it's still running.
                // This ensures your lightweight app doesn't accumulate garbage.
                cf.cancel(true); 
                fail++;
            } else {
                // Normal counting logic
                if (cf.isDone() && !cf.isCompletedExceptionally() && !cf.isCancelled()) {
                    if (cf.getNow(false)) {
                        success++;
                    } else {
                        fail++;
                    }
                } else {
                    fail++;
                }
            }
        }

        return String.format("Result: %d Success, %d Failed (Timeout was: %s)", success, fail, timedOut);
    }
}

