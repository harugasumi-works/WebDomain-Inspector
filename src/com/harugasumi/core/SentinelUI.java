package com.harugasumi.core;
import javax.swing.*;
import java.awt.*;
public class SentinelUI extends JFrame {
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
            SwingWorker<String, String> swingWorker = new SwingWorker<String, String>() {

                @Override
                protected String doInBackground() throws Exception {
                    logArea.setText(""); // Clear old logs
                    startButton.setEnabled(false); // Prevent double-clicking
                    logArea.append("--- Starting Sentinel ---\n");
                    progressBar.setIndeterminate(true); // Show "loading" animation
                    try {
                        // We need to modify TaskWorker to accept 'this' (the logger)
                        // For now, let's assume we fixed TaskWorker manually below
                        worker.registerData();

                        // IMPORTANT: We need to update TaskWorker to pass 'this'
                        // to the new HttpCheckTask(..., this)
                        worker.generateTasks();

                        worker.runTasks();
                        engine.waitForCompletion(); // Wait for all tasks to finish
                        
                        publish(worker.getLogs().toArray(new String[0]));
                        publish(worker.showReport());

                        // When done:
                        SwingUtilities.invokeLater(() -> {
                            logArea.append("--- Scan Complete ---\n");
                            startButton.setEnabled(true);
                            progressBar.setIndeterminate(false);
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // This return value gets sent to done()
                    return "SUCCESS \n";
                }

                // This replaces your onLog logic
                // It runs on the UI thread AUTOMATICALLY
                @Override
                protected void process(java.util.List<String> chunks) {
                    for (String message : chunks) {
                        logArea.append(message + "\n");
                    }
                    // Auto-scroll
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                }

                // This runs on the UI thread when finished
                @Override
                protected void done() {
                    try {
                        String result = get(); // Get the return value from doInBackground
                        logArea.append("\n----------------\n");
                        logArea.append(result + "\n");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };

            // 2. Run it
            swingWorker.execute();
        });
    }}
