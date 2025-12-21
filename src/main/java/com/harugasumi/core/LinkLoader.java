package com.harugasumi.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LinkLoader {

    public List<String> loadLinks() {
        List<String> links = new ArrayList<>();
        
        // 1. Get the file from the "resources" folder
        // The "/" at the start means "root of the classpath" (src/main/resources)
        String fileName = "/links.txt"; 
        
        try (InputStream inputStream = getClass().getResourceAsStream(fileName)) {
            
            // Expert Check: Always handle the case where the file is missing
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found! Check if 'links.txt' is in src/main/resources");
            }

            // 2. Read the stream safely
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    // Optional: Trim whitespace and ignore empty lines
                    if (!line.trim().isEmpty()) {
                        links.add(line.trim());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Or use a Logger
        }
        
        return links;
    }
}