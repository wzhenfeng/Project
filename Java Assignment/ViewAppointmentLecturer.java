import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ViewAppointmentLecturer extends JFrame {
    private JTable appointmentsTable;
    private JTable pastAppointmentsTable;

    public ViewAppointmentLecturer() {
        setTitle("View Appointment");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);

        // Set default font
        Font timesNewRomanBold = new Font("Times New Roman", Font.BOLD, 12);

        // Current Appointments Section
        JLabel titleLabel = new JLabel("My Appointments");
        titleLabel.setFont(timesNewRomanBold);
        titleLabel.setBounds(20, 20, 150, 20);
        add(titleLabel);

        String[] columnNames = {"Student", "Date", "Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        appointmentsTable = new JTable(model);
        appointmentsTable.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        appointmentsTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 12));
        JScrollPane scrollPane = new JScrollPane(appointmentsTable);
        scrollPane.setBounds(20, 50, 740, 180);
        add(scrollPane);

        // Past Appointments Section
        JLabel pastLabel = new JLabel("Past Appointments");
        pastLabel.setFont(timesNewRomanBold);
        pastLabel.setBounds(20, 240, 150, 20);
        add(pastLabel);

        DefaultTableModel pastModel = new DefaultTableModel(columnNames, 0);
        pastAppointmentsTable = new JTable(pastModel);
        pastAppointmentsTable.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        pastAppointmentsTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 12));
        JScrollPane pastScrollPane = new JScrollPane(pastAppointmentsTable);
        pastScrollPane.setBounds(20, 270, 740, 180);
        add(pastScrollPane);

        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Times New Roman", Font.BOLD, 12));
        backButton.setBounds(20, 460, 100, 25);
        add(backButton);

        backButton.addActionListener(e -> {
            new LecturerDashboard(null).setVisible(true);
            dispose();
        });

        // Load data
        loadAppointmentsFromFile();
        loadPastAppointmentsFromFile();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadAppointmentsFromFile() {
        File file = new File("Appointments.txt");
        if (!file.exists()) return;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                String[] appointmentDetails = line.split(",");
                if (appointmentDetails.length >= 5) {
                    String studentId = appointmentDetails[4].trim();
                    String studentName = getStudentName(studentId);
                    String date = appointmentDetails[2].trim();
                    String time = convertTo12HourFormat(appointmentDetails[3].trim());
                    
                    // Check if appointment is in the past
                    if (isAppointmentPast(date, time)) {
                        // Move to past appointments file
                        moveToPastAppointments(line);
                    } else {
                        DefaultTableModel model = (DefaultTableModel) appointmentsTable.getModel();
                        model.addRow(new Object[]{studentName, date, time});
                    }
                }
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error reading appointments file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPastAppointmentsFromFile() {
        File file = new File("pastAppointments.txt");
        if (!file.exists()) return;

        DefaultTableModel pastModel = (DefaultTableModel) pastAppointmentsTable.getModel();
        pastModel.setRowCount(0); // Clear existing rows

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] appointmentDetails = line.split(",");
                if (appointmentDetails.length >= 6) {
                    String studentId = appointmentDetails[4].trim();
                    String studentName = getStudentName(studentId);
                    String date = appointmentDetails[2].trim();
                    String time = convertTo12HourFormat(appointmentDetails[3].trim());
                    String status = appointmentDetails[5].trim();

                    // Add to past appointments table
                    pastModel.addRow(new Object[]{studentName, date, time, status});
                }
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error loading past appointments: " + e.getMessage());
        }
    }

    private String getStudentName(String studentId) {
        try (Scanner scanner = new Scanner(new File("students.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[0].equals(studentId)) {
                    return parts[1];
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
            if (parts.length != 2) return time24;

            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            String period = hour >= 12 ? "PM" : "AM";
            if (hour > 12) hour -= 12;
            else if (hour == 0) hour = 12;

            return String.format("%d:%02d %s", hour, minute, period);
        } catch (NumberFormatException e) {
            return time24;
        }
    }

    private void moveToPastAppointments(String appointmentLine) {
        try (FileWriter fw = new FileWriter("pastAppointments.txt", true);
             PrintWriter pw = new PrintWriter(fw)) {
            
            // Split the raw appointment line
            String[] appointmentDetails = appointmentLine.split(",");
            if (appointmentDetails.length >= 5) {
                // Generate the next P ID
                String pastAppointmentId = getNextPastAppointmentId();
                String lecturerId = appointmentDetails[1].trim();
                String date = appointmentDetails[2].trim();
                String time = appointmentDetails[3].trim();
                String studentId = appointmentDetails[4].trim();
                String status = "Complete"; // Default status for past appointments
                
                // Write formatted line to pastAppointments.txt
                pw.printf("%s,%s,%s,%s,%s,%s%n", 
                          pastAppointmentId, 
                          lecturerId, 
                          date, 
                          time, 
                          studentId, 
                          status);
            } else {
                System.err.println("Malformed appointment line: " + appointmentLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getNextPastAppointmentId() {
        File file = new File("pastAppointments.txt");
        if (!file.exists()) {
            // If the file doesn't exist, start with P001
            return "P001";
        }
    
        int maxId = 0; // Track the highest numerical ID
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
    
                String[] details = line.split(",");
                if (details.length > 0 && details[0].startsWith("P")) {
                    // Extract the numerical part of the ID
                    String numericPart = details[0].substring(1);
                    try {
                        int id = Integer.parseInt(numericPart);
                        maxId = Math.max(maxId, id); // Update the highest ID found
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid P ID format: " + details[0]);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    
        // Generate the next ID
        return String.format("P%03d", maxId + 1);
    }
    
    
    

    private boolean isAppointmentPast(String date, String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            Date appointmentDate = sdf.parse(date + " " + time);
            Date currentDate = new Date(); // Use the system's current time
    
            // Debugging: Log the comparison
            System.out.println("Comparing appointment date: " + appointmentDate + " with current date: " + currentDate);
    
            return appointmentDate.before(currentDate); // True if the appointment is in the past
        } catch (ParseException e) {
            e.printStackTrace();
            return false; // Treat as not in the past if there's a parsing error
        }
    }
    
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ViewAppointmentLecturer app = new ViewAppointmentLecturer();
            app.setVisible(true);
        });
    }
}
