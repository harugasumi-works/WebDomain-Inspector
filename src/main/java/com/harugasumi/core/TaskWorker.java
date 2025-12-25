package com.harugasumi.core;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.harugasumi.model.LogLevel;

public class TaskWorker {

	Map<LogLevel, ArrayList<String>> data;
	private TaskEngine workload;
	private final Queue<String> logBuffer = new ConcurrentLinkedQueue<>();
	ObjectMapper mapper = new ObjectMapper();

	List<String> rawLines = new ArrayList<>();
	List<LinkEntries> newEntries = new ArrayList<>();

	public TaskWorker(TaskEngine engine) {
		this.workload = engine;
	}
	
	public void setRawLines(List<String> lines) {
	    this.rawLines = lines;
	}

	public void registerData() {
	    // 1. Initialize empty list for the new JSON structure
	    List<LinkEntries> newEntries = new ArrayList<>();
	    
	    // 2. Process all lines in memory first
	    for (String line : rawLines) {
	        if (line.startsWith("#")) continue;

	        int idx = line.indexOf('=');
	        if (idx < 0) continue;

	        String keyStr = line.substring(0, idx).trim();
	        String value = line.substring(idx + 1).trim();

	        // Add to temporary list
	        newEntries.add(new LinkEntries(keyStr, value));
	    }

	    // 3. WRITE TO DISK ONCE (The "Save" button behavior)
	    try {
	        mapper.enable(SerializationFeature.INDENT_OUTPUT);
	        mapper.writeValue(new File("session.json"), newEntries);
	        System.out.println("Migration Complete! Saved " + newEntries.size() + " entries to session.json");
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

	public void loadData() {
		try {
			// 1. Read JSON file -> Convert to List of ScanTarget objects
			List<LinkEntries> targets = mapper.readValue(new File("session.json"),
					new TypeReference<List<LinkEntries>>() {
					});

			// 2. Convert POJOs -> Your HashMap (for the engine)
			try {
			data = new EnumMap<>(LogLevel.class);
			for (LinkEntries item : targets) {
				LogLevel level = LogLevel.valueOf(item.getLevel().toUpperCase());
				String url = item.getURL();

				// Add to map
				try {
					data.computeIfAbsent(level, _ -> new ArrayList<>()).add(url);
				} catch (IllegalArgumentException ex) {
					System.err.println("Warning: Invalid config level " + level);
				}
			
				
			}
			} catch (NullPointerException e) {
				System.err.print("Data is null");
			}
			
			System.out.println("Successfully loaded " + targets.size() + " targets from JSON.");

		} catch (IOException e) {
			System.err.println("Could not read session.json. Is the file missing?");
		}
	}


	public void generateTasks() {
		try {
		data.forEach((level, urlList) -> {
			for (String url : urlList) {
				this.workload.addTask(new HttpCheckTask(level, url));
			}
		});
		} catch (NullPointerException e) {
			System.err.print("Data is null");
		}
		}

	public void runTasks(java.util.function.Consumer<String> uiCallback) {
		logBuffer.clear();
		this.workload.executeAll(msg -> {
			this.logBuffer.add(msg); // Save to memory
			if (uiCallback != null) {
				uiCallback.accept(msg); // Send to UI
			}
		});
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
	public String showReport() {
		return this.workload.showReport();
	}

	public void stop() {
		this.workload.stop();
	}

}
