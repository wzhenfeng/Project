import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Font;

public class FeedbackLecturer extends JFrame {
    private JTable studentFeedbackTable;
    private JTable lecturerFeedbackTable;
    private DefaultTableModel studentTableModel;
    private DefaultTableModel lecturerTableModel;
    private String lecturerId;

    public FeedbackLecturer(String lecturerId) {
        this.lecturerId = lecturerId;
        setTitle("Feedback");
        setSize(800, 600); 
        setLayout(null);
        setLocationRelativeTo(null);

        JLabel studentFeedbackLabel = new JLabel("Student Feedback");
        studentFeedbackLabel.setFont(new Font("Times New Roman", Font.BOLD, 15));
        studentFeedbackLabel.setBounds(20, 20, 200, 25);
        add(studentFeedbackLabel);

        studentTableModel = new DefaultTableModel(new Object[]{"Student", "Date", "Time", "Feedback"}, 0);
        studentFeedbackTable = new JTable(studentTableModel);
        studentFeedbackTable.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        JScrollPane studentTableScrollPane = new JScrollPane(studentFeedbackTable);
        studentTableScrollPane.setBounds(20, 50, 740, 200);
        add(studentTableScrollPane);

        JLabel lecturerFeedbackLabel = new JLabel("Lecturer Feedback");
        lecturerFeedbackLabel.setFont(new Font("Times New Roman", Font.BOLD, 15));
        lecturerFeedbackLabel.setBounds(20, 260, 200, 25);
        add(lecturerFeedbackLabel);

        lecturerTableModel = new DefaultTableModel(new Object[]{"Lecturer", "Date", "Time", "Feedback"}, 0);
        lecturerFeedbackTable = new JTable(lecturerTableModel);
        lecturerFeedbackTable.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        JScrollPane lecturerTableScrollPane = new JScrollPane(lecturerFeedbackTable);
        lecturerTableScrollPane.setBounds(20, 290, 740, 200);
        add(lecturerTableScrollPane);

        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Times New Roman", Font.BOLD, 12));
        backButton.setBounds(20, 500, 100, 25);
        add(backButton);

        JButton provideFeedbackButton = new JButton("Provide Feedback");
        provideFeedbackButton.setFont(new Font("Times New Roman", Font.BOLD, 12));
        provideFeedbackButton.setBounds(140, 500, 150, 25);
        add(provideFeedbackButton);

        backButton.addActionListener(e -> {
            new LecturerDashboard(getLecturerName(lecturerId)).setVisible(true);
            dispose();
        });

        provideFeedbackButton.addActionListener(e -> provideFeedback());

        studentFeedbackTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    provideFeedback();
                }
            }
        });

        lecturerFeedbackTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    provideFeedback();
                }
            }
        });

        loadFeedback(studentTableModel);
        loadFeedback(lecturerTableModel);
    }

    private void loadFeedback(DefaultTableModel model) {
        model.setRowCount(0);
        try (Scanner scanner = new Scanner(new File("feedback.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split(",", 6); // Split into max 6 parts to keep feedback intact
                    if (parts.length >= 6) {
                        String studentId = parts[1];
                        String lecId = parts[2];
                        String date = parts[3];
                        String time = convertTo12HourFormat(parts[4]);
                        String feedback = parts[5].trim();

                        if (lecId.equals(this.lecturerId)) {
                            String studentName = getStudentName(studentId);
                            // Student feedback table shows feedback from students
                            if (model == studentTableModel && feedback.startsWith("Student:")) {
                                String studentFeedback = feedback.substring(8).trim(); // Remove "Student:" and trim
                                model.addRow(new Object[]{studentName, date, time, studentFeedback});
                            }
                            // Lecturer feedback table shows feedback from lecturer
                            else if (model == lecturerTableModel && feedback.startsWith("Lecturer:")) {
                                String lecturerFeedback = feedback.substring(9).trim(); // Remove "Lecturer:" and trim
                                model.addRow(new Object[]{getLecturerName(lecId), date, time, lecturerFeedback});
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error reading feedback file: " + e.getMessage());
        }
    }

    private String convertTo12HourFormat(String time24) {
        try {
            String[] timeParts = time24.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            String minutes = timeParts[1];
            
            String period = "AM";
            if (hour >= 12) {
                period = "PM";
                if (hour > 12) {
                    hour -= 12;
                }
            }
            if (hour == 0) {
                hour = 12;
            }
            
            return String.format("%d:%s %s", hour, minutes, period);
        } catch (Exception e) {
            return time24; // Return original if parsing fails
        }
    }

    private String getStudentName(String studentId) {
        try (Scanner scanner = new Scanner(new File("students.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 4 && parts[0].equals(studentId)) {
                    return parts[3]; // Return student's full name
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error reading students file: " + e.getMessage());
        }
        return studentId; // Return ID if name not found
    }

    private String getLecturerName(String lecturerId) {
        try (Scanner scanner = new Scanner(new File("lecturers.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length == 4 && parts[0].equals(lecturerId)) {
                    return parts[3]; // Return lecturer's full name
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error reading lecturers file: " + e.getMessage());
        }
        return lecturerId; // Return ID if name not found
    }

    private void provideFeedback() {
        // Get selected row from either table
        int studentRow = studentFeedbackTable.getSelectedRow();
        int lecturerRow = lecturerFeedbackTable.getSelectedRow();
        
        String student = null;
        String date = null;
        String time = null;
        
        if (studentRow != -1) {
            student = (String) studentTableModel.getValueAt(studentRow, 0);
            date = (String) studentTableModel.getValueAt(studentRow, 1);
            time = (String) studentTableModel.getValueAt(studentRow, 2);
        } else if (lecturerRow != -1) {
            student = (String) lecturerTableModel.getValueAt(lecturerRow, 0);
            date = (String) lecturerTableModel.getValueAt(lecturerRow, 1);
            time = (String) lecturerTableModel.getValueAt(lecturerRow, 2);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a feedback entry to respond to.");
            return;
        }

        // Create feedback input dialog
        JTextArea feedbackArea = new JTextArea(5, 30);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(feedbackArea);

        Object[] message = {
            "Provide feedback for appointment with " + student + " on " + date + " at " + time + ":",
            scrollPane
        };

        int option = JOptionPane.showConfirmDialog(
            this,
            message,
            "Provide Feedback",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION && !feedbackArea.getText().trim().isEmpty()) {
            saveFeedback(student, date, time, feedbackArea.getText().trim());
        }
    }

    private void saveFeedback(String student, String date, String time, String feedback) {
        try {
            // Get the student ID for the selected student
            String studentId = getStudentIdByName(student);
            if (studentId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Error: Could not find student ID");
                return;
            }

            // Check if feedback already exists for this appointment
            if (hasExistingFeedback(studentId, date, time, "Lecturer:")) {
                JOptionPane.showMessageDialog(this, "You have already provided feedback for this appointment.");
                return;
            }

            // Create new feedback entry
            String feedbackId = String.format("F%03d", getNextFeedbackId());
            String formattedFeedback = String.format("%s,%s,%s,%s,%s,Lecturer:%s",
                feedbackId, studentId, lecturerId, date, time, feedback.trim());

            // Append to feedback.txt
            try (PrintWriter writer = new PrintWriter(new FileWriter("feedback.txt", true))) {
                writer.println(formattedFeedback);
                JOptionPane.showMessageDialog(this, "Feedback saved successfully!");
                
                // Refresh the tables
                loadFeedback(studentTableModel);
                loadFeedback(lecturerTableModel);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving feedback: " + e.getMessage());
        }
    }

    private boolean hasExistingFeedback(String studentId, String date, String time, String prefix) {
        try (Scanner scanner = new Scanner(new File("feedback.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split(",", 6);
                    if (parts.length >= 6 && 
                        parts[1].equals(studentId) && 
                        parts[2].equals(this.lecturerId) && 
                        parts[3].equals(date) && 
                        parts[4].equals(time) && 
                        parts[5].startsWith(prefix)) {
                        return true;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error reading feedback file: " + e.getMessage());
        }
        return false;
    }

    private String getStudentIdByName(String studentName) {
        try (Scanner scanner = new Scanner(new File("students.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 4 && parts[3].equals(studentName)) {
                    return parts[0];
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error reading students file: " + e.getMessage());
        }
        return "";
    }

    private int getNextFeedbackId() {
        int maxId = 0;
        try (Scanner scanner = new Scanner(new File("feedback.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty() && line.startsWith("F")) {
                    try {
                        int id = Integer.parseInt(line.substring(1, 4));
                        maxId = Math.max(maxId, id);
                    } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                        // Skip invalid format
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet, start with ID 1
            return 1;
        }
        return maxId + 1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FeedbackLecturer feedback = new FeedbackLecturer("L001");
            feedback.setVisible(true);
        });
    }
}
