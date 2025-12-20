import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TaskWorker {

    Map<LogLevel, ArrayList<String>> data;  
    String sourcefile = "links.txt";
    private TaskEngine workload ;

    public TaskWorker (TaskEngine engine) {
        this.workload = engine;
    }

    public void registerData() {
        data = new HashMap<>(); // Map<LogLevel, List<String>>
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

    public void generateTasksWithCallback(LogCallback logger) {
    data.forEach((level, urlList) -> {
        for (String url : urlList) {
            // Pass the 'logger' (the UI window) into the task
            this.workload.addTask(new HttpCheckTask(level, url, logger));
        }
    });
}


    public void runTasks() {
        this.workload.executeAll();
    }

    public void showReport(){
        this.workload.showReport();
    }

    public void stop() {
        this.workload.stop();
    }
}
