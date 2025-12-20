import java.io.File;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== DIAGNOSTIC START ===");

        // CHECKPOINT 1: Verify File Existence
        File f = new File("links.txt");
        System.out.println("Looking for file at: " + f.getAbsolutePath());
        if (!f.exists()) {
            System.err.println("FATAL ERROR: links.txt NOT FOUND at this path!");
            System.err.println("Make sure the file is in the PROJECT ROOT, not inside src.");
            return; // Stop here
        } else {
            System.out.println("SUCCESS: File found. Size: " + f.length() + " bytes.");
        }

        // Setup
        TaskEngine engine = new TaskEngine();
        TaskWorker worker = new TaskWorker(engine);

        // CHECKPOINT 2: Register Data
        System.out.println("...Reading file...");
        worker.registerData();
        
        // We need to inspect the 'data' map inside worker. 
        // Since 'data' is package-private in your code, we rely on console output.
        // DID YOU SEE ANY PRINT STATEMENTS FROM registerData? 
        // If not, the parser skipped every line.

        // CHECKPOINT 3: Generate Tasks
        System.out.println("...Generating Tasks...");
        worker.generateTasks();
        
        // CHECKPOINT 4: Execution
        System.out.println("...Executing Engine...");
        worker.runTasks();
        
        System.out.println("=== WAITING FOR RESULTS ===");
        worker.showReport();
        
        worker.stop();
        System.out.println("=== DIAGNOSTIC END ===");
    }
}