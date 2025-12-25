package com.harugasumi.core;

import com.harugasumi.model.LogLevel;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SentinelUI extends JFrame {

    private static final long serialVersionUID = 1L;

    // Backend Components
    private TaskEngine engine;
    private TaskWorker worker;
    private List<String> stagedLines; // We store the list here instead of a text area

    // UI Components
    private JComboBox<LogLevel> levelSelector;
    private JTextField urlInput;
    private DefaultListModel<String> listModel; // The visible list on screen
    private JTextArea logArea;
    private JTabbedPane tabbedPane;
    private JButton startButton;
    private JProgressBar progressBar;

    public SentinelUI() {
        // 1. Setup Main Window (SINGLE WINDOW, NO MORE POP-UPS)
        setTitle("Sentinel Web Monitor");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Initialize Data
        this.stagedLines = new ArrayList<>();
        this.engine = new TaskEngine();
        this.worker = new TaskWorker(engine);

        // 2. Create the Tabs
        tabbedPane = new JTabbedPane();

        // --- TAB 1: THE BUILDER (Your Vision) ---
        JPanel builderPanel = createBuilderPanel();
        tabbedPane.addTab("1. Build Task List", builderPanel);

        // --- TAB 2: THE MONITOR (The Terminal) ---
        JPanel monitorPanel = createMonitorPanel();
        tabbedPane.addTab("2. Live Terminal", monitorPanel);

        add(tabbedPane);
    }

    // --- VISUAL SETUP METHODS ---

    private JPanel createBuilderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top: The Entry Form
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Dropdown for Enum (Critical, Info, etc.)
        levelSelector = new JComboBox<>(LogLevel.values());
        
        // URL Input (Single line, but we add it to a list)
        JLabel prefixLabel = new JLabel("https://");
        urlInput = new JTextField(25);
        urlInput.setToolTipText("Enter URL (e.g. google.com)");
        
        // "Add" Button
        JButton addButton = new JButton("Add Entry");
        addButton.addActionListener(_ -> addEntry());

        formPanel.add(new JLabel("Priority:"));
        formPanel.add(levelSelector);
        formPanel.add(prefixLabel); 
        formPanel.add(urlInput);
        formPanel.add(addButton);

        // Center: The Visual List
        listModel = new DefaultListModel<>();
        JList<String> displayList = new JList<>(listModel);
        JScrollPane listScroll = new JScrollPane(displayList);
        listScroll.setBorder(BorderFactory.createTitledBorder("Queue"));

        // Bottom: The Big Red Button
        startButton = new JButton("LOAD & SCAN");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.addActionListener(_ -> runScanSequence()); // Trigger your logic

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(listScroll, BorderLayout.CENTER);
        panel.add(startButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMonitorPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.PINK); // Kept your PINK color
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.SOUTH);
        return panel;
    }

    // --- LOGIC METHODS ---

    // 1. Handle the "Add Entry" Logic
    private void addEntry() {
        String rawUrl = urlInput.getText().trim();
        if (rawUrl.isEmpty()) return;

        // Auto-fix URL
        String fullUrl = rawUrl.startsWith("http") ? rawUrl : "https://" + rawUrl;
        
        LogLevel selectedLevel = (LogLevel) levelSelector.getSelectedItem();

        // Save to backend list (LEVEL=URL)
        stagedLines.add(selectedLevel + "=" + fullUrl);

        // Show on screen
        listModel.addElement("[" + selectedLevel + "] " + fullUrl);

        // Reset Box
        urlInput.setText("");
        urlInput.requestFocus();
    }

    // 2. YOUR LOGIC (The SwingWorker)
    private void runScanSequence() {
        if (stagedLines.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Queue is empty!");
            return;
        }

        // UI Prep
        startButton.setEnabled(false);
        tabbedPane.setSelectedIndex(1); // Auto-switch to terminal
        
        // Inject the list we built
        worker.setRawLines(stagedLines);

        // *** YOUR SWINGWORKER LOGIC IS HERE ***
        SwingWorker<String, String> swingWorker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                publish("--- Starting Sentinel ---\n");

                // 1. Data Cycle
                worker.registerData();
                worker.loadData();
                worker.generateTasks();

                // 2. Execution (Using your lambda logic)
                worker.runTasks(msg -> publish(msg));

                // 3. Wait
                String result = engine.waitForCompletion(10, java.util.concurrent.TimeUnit.SECONDS);
                
                // 4. Report
                publish(worker.showReport());

                return result;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String msg : chunks) {
                    logArea.append(msg + "\n");
                }
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }

            @Override
            protected void done() {
                try {
                    logArea.append("\n=== SCAN FINISHED ===\n");
                    logArea.append(get() + "\n");
                } catch (Exception ex) {
                    logArea.append("Error: " + ex.getMessage());
                } finally {
                    startButton.setEnabled(true);
                    progressBar.setIndeterminate(false);
                }
            }
        };

        progressBar.setIndeterminate(true);
        swingWorker.execute();
    }
}