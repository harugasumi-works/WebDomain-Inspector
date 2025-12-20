import javax.swing.*;
import java.awt.*;

public class SentinelUI extends JFrame implements LogCallback {
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
        startButton.addActionListener(e -> startScan());
        topPanel.add(startButton);
        add(topPanel, BorderLayout.NORTH);

        // 4. Progress Bar (Bottom)
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        add(progressBar, BorderLayout.SOUTH);

        // 5. Initialize Engine
        this.engine = new TaskEngine();
        this.worker = new TaskWorker(engine);
    }

    // This is the method triggered by the button
    private void startScan() {
        logArea.setText(""); // Clear old logs
        startButton.setEnabled(false); // Prevent double-clicking
        logArea.append("--- Starting Sentinel ---\n");
        progressBar.setIndeterminate(true); // Show "loading" animation

        // RUN IN BACKGROUND THREAD
        // (If we run this on the main thread, the UI will freeze!)
        new Thread(() -> {
            try {
                // We need to modify TaskWorker to accept 'this' (the logger)
                // For now, let's assume we fixed TaskWorker manually below
                worker.registerData();

                // IMPORTANT: We need to update TaskWorker to pass 'this'
                // to the new HttpCheckTask(..., this)
                worker.generateTasksWithCallback(this);

                worker.runTasks();

                // When done:
                SwingUtilities.invokeLater(() -> {
                    logArea.append("--- Scan Complete ---\n");
                    worker.showReport(); // This prints to console, we might need to redirect it too
                    startButton.setEnabled(true);
                    progressBar.setIndeterminate(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // The Callback Implementation
    @Override
    public void onLog(String message) {
        // Swing is not thread-safe! We must update the UI on the Event Thread.
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            // Auto-scroll to bottom
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

}
