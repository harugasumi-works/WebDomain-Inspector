package com.harugasumi.core;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.harugasumi.model.LogLevel;

import java.util.List;
 
public class TaskWorker {
 
    Map<LogLevel, ArrayList<String>> data;  
    private TaskEngine workload ;
    private final Queue<String> logBuffer = new ConcurrentLinkedQueue<>();
 
    public TaskWorker (TaskEngine engine) {
        this.workload = engine;
    }
 
    public void registerData() {
        data = new EnumMap<>(LogLevel.class);
    
    // 2. Delegate file reading to the expert class
    List<String> rawLines = new LinkLoader().loadLinks(); // Load from resources

    // 3. Process the logic
    for (String line : rawLines) {
        // Logic is now clean and focused
        if (line.startsWith("#")) continue;

        int idx = line.indexOf('=');
        if (idx < 0) continue;

        String keyStr = line.substring(0, idx).trim();
        String value  = line.substring(idx + 1).trim();

        try {
            LogLevel level = LogLevel.valueOf(keyStr.toUpperCase());
            data.computeIfAbsent(level, k -> new ArrayList<>()).add(value);
        } catch (IllegalArgumentException ex) {
            System.err.println("Warning: Invalid config level " + keyStr);
        }
    }
    }
 
    public void generateTasks() {
    data.forEach((level, urlList) -> {
        for (String url : urlList) {
            this.workload.addTask(new HttpCheckTask(level, url));
        }
    });
    }
 
 
    public void runTasks() {
        logBuffer.clear();
        this.workload.executeAll(logBuffer::add);
    }
 
    /** 
     * @return {@code List<String>}
     */
    public List<String> getLogs() {
        return new ArrayList<>(logBuffer);
    }
 
    /** 
     * @return String
     */
    public String showReport(){
        return this.workload.showReport();
    }
 
    public void stop() {
        this.workload.stop();
    }

}
