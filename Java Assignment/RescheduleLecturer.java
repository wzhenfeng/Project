import java.awt.Font;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class RescheduleLecturer extends JFrame {
    private JTable rescheduleTable;
    private DefaultTableModel tableModel;

    public RescheduleLecturer() {
        setTitle("View Reschedule Requests");
        setSize(800, 500);
        setLayout(null);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel rescheduleLabel = new JLabel("Reschedule Requests");
        rescheduleLabel.setBounds(20, 10, 200, 25);
        rescheduleLabel.setFont(new Font("Times New Roman", Font.BOLD, 15));
        add(rescheduleLabel);

        tableModel = new DefaultTableModel(new Object[]{"Lecturer", "Date", "Time", "Student", "Status"}, 0);
        rescheduleTable = new JTable(tableModel);
        rescheduleTable.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        rescheduleTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 12));
        JScrollPane tableScrollPane = new JScrollPane(rescheduleTable);
        tableScrollPane.setBounds(20, 40, 740, 300);
        add(tableScrollPane);

        JButton approveButton = new JButton("Approve");
        approveButton.setBounds(20, 350, 100, 30);
        approveButton.setFont(new Font("Times New Roman", Font.BOLD, 15));
        add(approveButton);

        JButton rejectButton = new JButton("Reject");
        rejectButton.setBounds(140, 350, 100, 30);
        rejectButton.setFont(new Font("Times New Roman", Font.BOLD, 15));
        add(rejectButton);

        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Times New Roman", Font.BOLD, 12));
        backButton.setBounds(20, 420, 100, 25);
        add(backButton);

        approveButton.addActionListener(e -> approveReschedule());
        rejectButton.addActionListener(e -> rejectReschedule());
        backButton.addActionListener(e -> {
            new LecturerDashboard("").setVisible(true);
            dispose();
        });

        loadRescheduleRequests();
    }

    private void loadRescheduleRequests() {
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
                if (parts.length >= 6) {
                    String lecturerId = parts[1];
                    String date = parts[2];
                    String time = parts[3];
                    String studentId = parts[4];
                    String status = parts[5];
                    
                    String lecturerName = getLecturerName(lecturerId);
                    String studentName = getStudentName(studentId);
                    time = convertTo12HourFormat(time);

                    // Show all requests, including approved and rejected ones
                    tableModel.addRow(new Object[]{lecturerName, date, time, studentName, status});
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading reschedule requests: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
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

    private String convertTo12HourFormat(String time24) {
        try {
            String[] parts = time24.split(":");
            int hour = Integer.parseInt(parts[0]);
            String minutes = parts[1];
            
            String period = (hour >= 12) ? "PM" : "AM";
            if (hour > 12) {
                hour -= 12;
            } else if (hour == 0) {
                hour = 12;
            }
            
            return String.format("%d:%s %s", hour, minutes, period);
        } catch (Exception e) {
            return time24; // Return original format if parsing fails
        }
    }

    private void approveReschedule() {
        int selectedRow = rescheduleTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a reschedule request to approve.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Get the reschedule request details
            String line = "";
            String studentId = "";
            String newDate = "";
            String newTime = "";
            String lecturerId = "";
            
            try (Scanner scanner = new Scanner(new File("rescheduleRequests.txt"))) {
                for (int i = 0; i <= selectedRow; i++) {
                    line = scanner.nextLine();
                }
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    lecturerId = parts[1];
                    newDate = parts[2];
                    newTime = parts[3];
                    studentId = parts[4];
                }
            }

            // Find and remove the student's current appointment
            List<String> remainingAppointments = new ArrayList<>();
            String currentAppointment = null;
            
            try (Scanner scanner = new Scanner(new File("Appointments.txt"))) {
                while (scanner.hasNextLine()) {
                    String appLine = scanner.nextLine();
                    String[] parts = appLine.split(",");
                    if (parts.length >= 5 && parts[4].equals(studentId)) {
                        currentAppointment = appLine;
                    } else {
                        remainingAppointments.add(appLine);
                    }
                }
            }

            // Add the old appointment slot back to available slots
            if (currentAppointment != null) {
                String[] parts = currentAppointment.split(",");
                String oldDate = parts[2];
                String oldTime = parts[3];
                
                try (PrintWriter writer = new PrintWriter(new FileWriter("slots.txt", true))) {
                    String slotId = String.format("S%03d", getNextSlotId());
                    writer.println(String.format("%s,%s,%s,%s", slotId, parts[1], oldDate, oldTime));
                }
            }

            // Write remaining appointments
            try (PrintWriter writer = new PrintWriter(new FileWriter("Appointments.txt"))) {
                for (String app : remainingAppointments) {
                    writer.println(app);
                }
            }

            // Create new appointment for the requested time
            try (PrintWriter writer = new PrintWriter(new FileWriter("Appointments.txt", true))) {
                String appointmentId = String.format("A%03d", getNextAppointmentId());
                writer.println(String.format("%s,%s,%s,%s,%s", 
                    appointmentId, lecturerId, newDate, newTime, studentId));
            }

            // Update the status to Approved in rescheduleRequests.txt
            updateRequestStatus(selectedRow, "Approved");
            
            // Refresh the table
            loadRescheduleRequests();
            
            JOptionPane.showMessageDialog(this, "Reschedule request approved successfully.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error processing reschedule: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getNextSlotId() {
        int maxId = 0;
        try (Scanner scanner = new Scanner(new File("slots.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length > 0 && parts[0].startsWith("S")) {
                        try {
                            int id = Integer.parseInt(parts[0].substring(1));
                            maxId = Math.max(maxId, id);
                        } catch (NumberFormatException e) {
                            // Skip invalid IDs
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return maxId + 1;
    }

    private int getNextAppointmentId() {
        int maxId = 0;
        try (Scanner scanner = new Scanner(new File("Appointments.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length > 0 && parts[0].startsWith("A")) {
                        try {
                            int id = Integer.parseInt(parts[0].substring(1));
                            maxId = Math.max(maxId, id);
                        } catch (NumberFormatException e) {
                            // Skip invalid IDs
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return maxId + 1;
    }

    private void rejectReschedule() {
        int selectedRow = rescheduleTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a reschedule request to reject.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Update the status to Rejected in rescheduleRequests.txt
            updateRequestStatus(selectedRow, "Rejected");
            
            // Refresh the table
            loadRescheduleRequests();
            
            JOptionPane.showMessageDialog(this, "Reschedule request rejected.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error processing rejection: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateRequestStatus(int rowIndex, String newStatus) throws IOException {
        File file = new File("rescheduleRequests.txt");
        File tempFile = new File("rescheduleRequests_temp.txt");
        
        try (Scanner scanner = new Scanner(file);
             PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
            
            int currentRow = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (currentRow == rowIndex && parts.length >= 7) {
                    // Preserve all parts except status
                    writer.println(String.format("%s,%s,%s,%s,%s,%s,%s",
                        parts[0], parts[1], parts[2], parts[3], parts[4], newStatus, parts[6]));
                } else {
                    writer.println(line);
                }
                currentRow++;
            }
        }

        // Replace the original file with the updated file
        if (!file.delete() || !tempFile.renameTo(file)) {
            throw new IOException("Could not update reschedule requests file");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RescheduleLecturer rescheduleApp = new RescheduleLecturer();
            rescheduleApp.setVisible(true);
        });
    }
}