package com.harugasumi.core;
import javax.swing.*;
import java.awt.*;
public class SentinelUI extends JFrame {
  
	private static final long serialVersionUID = 1L;
	private JTextArea logArea;
    private JButton startButton;
    private JProgressBar progressBar;
    private TaskEngine engine;
    private TaskWorker worker;

    public SentinelUI() {
        // 1. Setup the Window
        setTitle("Sentinel Web Monitor");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 2. The Log Area (Center)
        logArea = new JTextArea();
        logArea.setEditable(false); // Read-only
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.PINK);
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);

        // 3. The Control Panel (Top)
        JPanel topPanel = new JPanel();
        startButton = new JButton("Load & Scan");
        topPanel.add(startButton);
        add(topPanel, BorderLayout.NORTH);

        // 4. Progress Bar (Bottom)
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        add(progressBar, BorderLayout.SOUTH);

        // 5. Initialize Engine
        this.engine = new TaskEngine();
        this.worker = new TaskWorker(engine);

        // Setup button listener
          
        startButton.addActionListener(e -> {
        	startButton.setEnabled(false);
            
            SwingWorker<String, String> swingWorker = new SwingWorker<String, String>() {

                @Override
                protected String doInBackground() throws Exception {
                    // 1. Setup UI (Safe to do here because SwingWorker handles it)
                    publish("--- Starting Sentinel ---\n");
                    
                    // 2. Prepare Data
                    worker.registerData();
                    worker.generateTasks();

                    // 3. RUN THE TASKS (The Critical Change)
                    // We pass a lambda "msg -> publish(msg)" which sends logs to the UI live!
                    worker.runTasks(msg -> publish(msg));

                    // 4. Wait for them to finish (Blocking the worker, NOT the UI)
                    String waitResult = engine.waitForCompletion(5, java.util.concurrent.TimeUnit.SECONDS);
                    
                    // 5. Show Final Stats
                    publish(worker.showReport());
                    
                    return waitResult;
                }

                // This receives the "publish(msg)" data on the UI Thread
                @Override
                protected void process(java.util.List<String> chunks) {
                    for (String message : chunks) {
                        logArea.append(message + "\n");
                    }
                    // Auto-scroll to bottom
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                }

                // Runs when everything is 100% done
                @Override
                protected void done() {
                    try {
                        // Get the result from doInBackground
                        String result = get(); 
                        logArea.append("\n=== SCAN FINISHED ===\n");
                        logArea.append(result + "\n");
                    } catch (Exception ex) {
                        logArea.append("Error: " + ex.getMessage());
                    } finally {
                        // Re-enable the button
                        startButton.setEnabled(true);
                        progressBar.setIndeterminate(false);
                    }
                }
            };

            // Start the loading animation
            progressBar.setIndeterminate(true);
            // Fire the worker
            swingWorker.execute();
        });
    }
}
