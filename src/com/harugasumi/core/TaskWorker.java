package com.harugasumi.core;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.harugasumi.model.LogLevel;

import java.util.List;
 
public class TaskWorker {
 
    Map<LogLevel, ArrayList<String>> data;  
    String sourcefile = "links.txt";
    private TaskEngine workload ;
    private final List<String> logBuffer = new ArrayList<>();
 
    public TaskWorker (TaskEngine engine) {
        this.workload = engine;
    }
 
    public void registerData() {
        data = new HashMap<>(); 
        try (BufferedReader br = new BufferedReader(new FileReader(sourcefile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int idx = line.indexOf('=');
                if (idx < 0) continue;
                String key = line.substring(0, idx).trim();
                String val = line.substring(idx + 1).trim();
                try {
                    LogLevel lvl = LogLevel.valueOf(key.toUpperCase());
                    data.computeIfAbsent(lvl, k -> new ArrayList<>()).add(val);
                } catch (IllegalArgumentException ex) {
                    System.err.println("Failed to parse level: '" + key + "'");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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
     * @return List<String>
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
