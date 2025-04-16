import java.awt.*;
import java.awt.Window;
import java.io.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.*;

public class RescheduleStudent extends JFrame {
    private String appointmentId;
    private String lecturerId;
    private String lecturerName;
    private DefaultTableModel tableModel;
    private JTable rescheduleTable;
    private String studentId;
    private String studentName;

    public RescheduleStudent(String username) {
        this.studentName = username;
        try (Scanner scanner = new Scanner(new File("students.txt"))) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",");
                if (parts.length >= 4 && parts[3].equals(username)) {
                    this.studentId = parts[0];
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (this.studentId == null) {
            JOptionPane.showMessageDialog(this, "Error: Student ID not found for username: " + username);
            dispose();
            return;
        }

        setTitle("Reschedule Management");
        setLayout(null);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        createRescheduleRequestsView();
        setLocationRelativeTo(null);
    }

    public RescheduleStudent(String appointmentId, String lecturerId, String date, String time, String studentId, String studentName) {
        this.appointmentId = appointmentId;
        this.lecturerId = lecturerId;
        this.studentId = studentId;
        this.studentName = studentName;

        setTitle("Reschedule Management");
        setLayout(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        if (appointmentId == null) {
            // View Reschedule mode - show only the requests table
            setSize(800, 600);
            createRescheduleRequestsView();
        } else {
            // Request Reschedule mode - show original appointment and available slots
            setSize(500, 500);
            this.lecturerName = getLecturerName(lecturerId);
            createRescheduleRequestView(date, time);
        }

        setLocationRelativeTo(null);
    }

    private void createRescheduleRequestsView() {
        // Reschedule Requests Table
        JLabel requestsLabel = new JLabel("My Reschedule Requests");
        requestsLabel.setBounds(20, 20, 300, 25);
        requestsLabel.setFont(new Font("Times New Roman", Font.BOLD, 20));
        add(requestsLabel);

        tableModel = new DefaultTableModel(new Object[]{"Lecturer", "Date", "Time", "Status"}, 0);
        rescheduleTable = new JTable(tableModel);
        rescheduleTable.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        rescheduleTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 12));
        JScrollPane tableScrollPane = new JScrollPane(rescheduleTable);
        tableScrollPane.setBounds(20, 60, 740, 450);
        add(tableScrollPane);

        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Times New Roman", Font.BOLD, 12));
        backButton.setBounds(20, 520, 100, 25);
        add(backButton);

        backButton.addActionListener(e -> {
            // Check for existing StudentDashboard windows
            boolean foundDashboard = false;
            for (Window window : Window.getWindows()) {
                if (window instanceof StudentDashboard && window.isVisible()) {
                    window.toFront();
                    foundDashboard = true;
                    break;
                }
            }
            
            // Only create a new dashboard if none exists
            if (!foundDashboard) {
                new StudentDashboard(studentName).setVisible(true);
            }
            dispose();
        });

        loadRescheduleRequests();
    }

    private void createRescheduleRequestView(String date, String time) {
        // Original Appointment Details
        JLabel originalLabel = new JLabel("Original Appointment:");
        originalLabel.setBounds(20, 20, 200, 25);
        originalLabel.setFont(new Font("Times New Roman", Font.BOLD, 15));
        add(originalLabel);

        JLabel lecturerLabel = new JLabel("Lecturer: " + lecturerName);
        lecturerLabel.setBounds(40, 50, 300, 25);
        lecturerLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        add(lecturerLabel);

        JLabel dateLabel = new JLabel("Date: " + date);
        dateLabel.setBounds(40, 75, 300, 25);
        dateLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        add(dateLabel);

        JLabel timeLabel = new JLabel("Time: " + convertTo12HourFormat(time));
        timeLabel.setBounds(40, 100, 300, 25);
        timeLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        add(timeLabel);

        // Available Slots
        JLabel slotsLabel = new JLabel("Available Slots:");
        slotsLabel.setBounds(20, 140, 200, 25);
        slotsLabel.setFont(new Font("Times New Roman", Font.BOLD, 15));
        add(slotsLabel);

        // Create ComboBox for slots
        DefaultComboBoxModel<String> slotsModel = new DefaultComboBoxModel<>();
        loadAvailableSlots(slotsModel);
        JComboBox<String> slotsComboBox = new JComboBox<>(slotsModel);
        slotsComboBox.setBounds(40, 170, 420, 25);
        slotsComboBox.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        add(slotsComboBox);

        // Reason for Rescheduling
        JLabel reasonLabel = new JLabel("Reason for Rescheduling:");
        reasonLabel.setBounds(20, 210, 200, 25);
        reasonLabel.setFont(new Font("Times New Roman", Font.BOLD, 15));
        add(reasonLabel);

        JTextArea reasonTextArea = new JTextArea();
        reasonTextArea.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        reasonTextArea.setLineWrap(true);
        reasonTextArea.setWrapStyleWord(true);
        JScrollPane reasonScrollPane = new JScrollPane(reasonTextArea);
        reasonScrollPane.setBounds(40, 240, 420, 80);
        add(reasonScrollPane);

        // Buttons
        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Times New Roman", Font.BOLD, 12));
        backButton.setBounds(20, 460, 100, 25);
        add(backButton);

        backButton.addActionListener(e -> {
            // Check for existing StudentDashboard windows
            boolean foundDashboard = false;
            for (Window window : Window.getWindows()) {
                if (window instanceof StudentDashboard && window.isVisible()) {
                    window.toFront();
                    foundDashboard = true;
                    break;
                }
            }
            
            // Only create a new dashboard if none exists
            if (!foundDashboard) {
                new StudentDashboard(studentName).setVisible(true);
            }
            dispose();
        });

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(140, 460, 100, 25);
        submitButton.setFont(new Font("Times New Roman", Font.BOLD, 12));
        add(submitButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(260, 460, 100, 25);
        cancelButton.setFont(new Font("Times New Roman", Font.BOLD, 12));
        add(cancelButton);

        submitButton.addActionListener(e -> {
            String selectedSlot = (String) slotsComboBox.getSelectedItem();
            String reason = reasonTextArea.getText().trim();
            
            if (selectedSlot == null) {
                JOptionPane.showMessageDialog(this, "Please select a slot.");
                return;
            }
            
            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please provide a reason for rescheduling.");
                return;
            }

            submitRescheduleRequest(selectedSlot, reason);
        });

        cancelButton.addActionListener(e -> dispose());
    }

    private void loadRescheduleRequests() {
        if (studentId == null || studentId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Error: Student ID not found");
            dispose();
            return;
        }

        tableModel.setRowCount(0);
        File file = new File("rescheduleRequests.txt");
        if (!file.exists()) {
            return;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                
                String[] parts = line.split(",");
                // Format: requestId,lecturerId,newDate,newTime,studentId,status,reason
                if (parts.length >= 7 && parts[4].trim().equals(studentId.trim())) {
                    String lecturerName = getLecturerName(parts[1].trim());
                    String newDate = parts[2].trim(); // Date is already in dd-MM-yyyy format
                    String newTime = convertTo12HourFormat(parts[3].trim());
                    String status = parts[5].trim();
                    
                    tableModel.addRow(new Object[]{
                        lecturerName,
                        newDate,
                        newTime,
                        status
                    });
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading reschedule requests: " + e.getMessage());
        }
    }

    private void loadAvailableSlots(DefaultComboBoxModel<String> model) {
        try (Scanner scanner = new Scanner(new File("slots.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                String[] parts = line.split(",");
                if (parts.length >= 4 && parts[1].equals(lecturerId)) {
                    String date = parts[2];
                    String time = parts[3];
                    
                    // Only check for pending reschedule requests since booked slots are removed from slots.txt
                    if (!isSlotInRescheduleRequests(date, time)) {
                        model.addElement(date + " " + convertTo12HourFormat(time));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean isSlotInRescheduleRequests(String date, String time) {
        try (Scanner scanner = new Scanner(new File("rescheduleRequests.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String requestDate = parts[2];
                    String requestTime = parts[3];
                    if (date.equals(requestDate) && time.equals(requestTime) && 
                        "Pending".equalsIgnoreCase(parts[4])) {
                        return true;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // If file doesn't exist, treat as no conflicts
            return false;
        }
        return false;
    }

    private void submitRescheduleRequest(String selectedSlot, String reason) {
        if (selectedSlot == null || selectedSlot.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a valid slot.");
            return;
        }

        if (reason == null || reason.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide a reason for rescheduling.");
            return;
        }

        String[] parts = selectedSlot.split(" ", 2); // Split into date and time
        if (parts.length < 2) {
            JOptionPane.showMessageDialog(this, "Invalid slot format.");
            return;
        }

        String newDate = parts[0];
        String newTime = convertTo24HourFormat(parts[1]); // Convert to 24-hour format for storage

        try {
            // First read existing content
            StringBuilder content = new StringBuilder();
            try (Scanner scanner = new Scanner(new File("rescheduleRequests.txt"))) {
                while (scanner.hasNextLine()) {
                    content.append(scanner.nextLine()).append("\n");
                }
            } catch (FileNotFoundException e) {
                // File doesn't exist yet, that's okay
            }

            // Now write everything back plus the new request
            try (PrintWriter writer = new PrintWriter(new FileWriter("rescheduleRequests.txt"))) {
                if (content.length() > 0) {
                    writer.print(content);
                }
                // Format: requestId,lecturerId,newDate,newTime,status,reason
                writer.println(String.format("%s,%s,%s,%s,Pending,%s", 
                    appointmentId, lecturerId, newDate, newTime, reason.replace(",", ";")));
            }
            
            JOptionPane.showMessageDialog(this, "Reschedule request submitted successfully!");
            dispose();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error submitting reschedule request: " + e.getMessage());
        }
    }

    private String getLecturerName(String lecturerId) {
        try (Scanner scanner = new Scanner(new File("lecturers.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length == 4 && parts[0].equals(lecturerId)) {
                    return parts[3];
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return lecturerId;
    }

    private String convertTo12HourFormat(String time24) {
        try {
            // If already in 12-hour format, return as is
            if (time24.contains("AM") || time24.contains("PM")) {
                return time24;
            }

            String[] parts = time24.split(":");
            if (parts.length == 2) {
                int hour = Integer.parseInt(parts[0]);
                String minutes = parts[1];
                String period = "AM";
                
                if (hour >= 12) {
                    period = "PM";
                    if (hour > 12) {
                        hour -= 12;
                    }
                } else if (hour == 0) {
                    hour = 12;
                }
                
                return String.format("%d:%s %s", hour, minutes, period);
            }
        } catch (NumberFormatException e) {
            // If parsing fails, return the original time
        }
        return time24;
    }

    private String convertTo24HourFormat(String time12) {
        try {
            // If already in 24-hour format, return as is
            if (!time12.contains("AM") && !time12.contains("PM")) {
                return time12;
            }

            String[] timeParts = time12.split(" ")[0].split(":");
            String period = time12.split(" ")[1];
            
            int hour = Integer.parseInt(timeParts[0]);
            String minutes = timeParts[1];

            if (period.equals("PM") && hour != 12) {
                hour += 12;
            } else if (period.equals("AM") && hour == 12) {
                hour = 0;
            }

            return String.format("%02d:%s", hour, minutes);
        } catch (Exception e) {
            return time12;
        }
    }
}
