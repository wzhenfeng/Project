import java.awt.Font;
import java.awt.Window;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MakeConsultation extends JFrame {
    private JTable availableSlotsTable;
    private JTable myAppointmentsTable;
    private DefaultTableModel availableSlotsModel;
    private DefaultTableModel myAppointmentsModel;
    private static final String SLOTS_FILE = "slots.txt";
    private static final String APPOINTMENTS_FILE = "Appointments.txt";
    private String studentId; // Current student's ID

    public MakeConsultation(String studentId) {
        this.studentId = studentId;

        setTitle("Appointment Management");
        setSize(800, 650); // Increased overall window height
        setLayout(null);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Available Slots section
        JLabel availableSlotsLabel = new JLabel("Available Slots");
        availableSlotsLabel.setBounds(20, 20, 200, 25);
        availableSlotsLabel.setFont(new Font("Times New Roman", Font.BOLD, 15));
        add(availableSlotsLabel);

        availableSlotsModel = new DefaultTableModel(new Object[]{"Lecturer", "Date", "Time", "LecturerID"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        availableSlotsTable = new JTable(availableSlotsModel);
        availableSlotsTable.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        availableSlotsTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 12));
        availableSlotsTable.getColumnModel().getColumn(3).setMaxWidth(0);
        availableSlotsTable.getColumnModel().getColumn(3).setMinWidth(0);
        availableSlotsTable.getColumnModel().getColumn(3).setPreferredWidth(0);
        JScrollPane availableSlotsScrollPane = new JScrollPane(availableSlotsTable);
        availableSlotsScrollPane.setBounds(20, 50, 740, 200); // Increased table height
        add(availableSlotsScrollPane);

        JButton bookButton = new JButton("Book Selected");
        bookButton.setBounds(20, 260, 150, 35);
        bookButton.setFont(new Font("Times New Roman", Font.BOLD, 15)); 
        add(bookButton);

        // My Appointments section
        JLabel myAppointmentsLabel = new JLabel("My Appointments");
        myAppointmentsLabel.setBounds(20, 300, 200, 25);
        myAppointmentsLabel.setFont(new Font("Times New Roman", Font.BOLD, 15)); 
        add(myAppointmentsLabel);

        myAppointmentsModel = new DefaultTableModel(new Object[]{"Lecturer", "Date", "Time", "LecturerID"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        myAppointmentsTable = new JTable(myAppointmentsModel);
        myAppointmentsTable.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        myAppointmentsTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 12));
        myAppointmentsTable.getColumnModel().getColumn(3).setMaxWidth(0);
        myAppointmentsTable.getColumnModel().getColumn(3).setMinWidth(0);
        myAppointmentsTable.getColumnModel().getColumn(3).setPreferredWidth(0);
        JScrollPane myAppointmentsScrollPane = new JScrollPane(myAppointmentsTable);
        myAppointmentsScrollPane.setBounds(20, 330, 740, 200);
        add(myAppointmentsScrollPane);

        // Load data
        loadSlotsFromFile();
        loadAppointmentsFromFile();

        // Add action listener for book button
        bookButton.addActionListener(e -> bookSelectedSlot());

        // Add back button
        JButton backButton = new JButton("Back");
        backButton.setBounds(20, 550, 100, 35);
        backButton.setFont(new Font("Times New Roman", Font.BOLD, 15));
        add(backButton);

        backButton.addActionListener(e -> {
            new StudentDashboard(getStudentName(studentId)).setVisible(true);
            dispose();
        });
    }

    private String getStudentName(String studentId) {
        try (Scanner scanner = new Scanner(new File("students.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 4 && parts[0].equals(studentId)) {
                    return parts[3]; // Return student's name
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void loadSlotsFromFile() {
        // Clean up duplicate slots first
        cleanupDuplicateSlots();
        
        // Clear existing slots
        availableSlotsModel.setRowCount(0);
        
        // First, get all booked slots
        Set<String> bookedSlots = new HashSet<>();
        try (Scanner scanner = new Scanner(new File(APPOINTMENTS_FILE))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    // Create a unique key for each booked slot (lecturerId + date + time)
                    String key = parts[1] + "," + parts[2] + "," + parts[3];
                    bookedSlots.add(key);
                }
            }
        } catch (FileNotFoundException e) {
            // Appointments file might not exist yet
        }
        
        // Track unique slots to prevent duplicates
        Set<String> uniqueSlots = new HashSet<>();
        
        // Now load available slots, excluding the booked ones and duplicates
        File file = new File(SLOTS_FILE);
        if (!file.exists()) return;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] slotDetails = line.split(",", 4); // SlotID,LecturerID,Date,Time
                if (slotDetails.length == 4) {
                    // Convert date format if needed
                    String date = slotDetails[2];
                    if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        String[] dateParts = date.split("-");
                        date = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];
                    }
                    
                    // Create unique key for slot (lecturerId + date + time)
                    String slotKey = slotDetails[1] + "," + date + "," + slotDetails[3];
                    
                    // Only add if not booked and not a duplicate
                    if (!bookedSlots.contains(slotKey) && uniqueSlots.add(slotKey)) {
                        String lecturerName = getLecturerName(slotDetails[1]);
                        String time12Hour = convertTo12HourFormat(slotDetails[3]);
                        availableSlotsModel.addRow(new Object[]{lecturerName, date, time12Hour, slotDetails[1]});
                    }
                }
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error reading slots file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cleanupDuplicateSlots() {
        Set<String> uniqueSlots = new HashSet<>();
        List<String> cleanedSlots = new ArrayList<>();
        
        try (Scanner scanner = new Scanner(new File(SLOTS_FILE))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                
                String[] slotDetails = line.split(",", 4);
                if (slotDetails.length == 4) {
                    // Standardize date format to dd-MM-yyyy
                    String date = slotDetails[2];
                    if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        String[] dateParts = date.split("-");
                        date = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];
                    }
                    
                    // Create unique key (lecturerId + date + time)
                    String slotKey = slotDetails[1] + "," + date + "," + slotDetails[3];
                    
                    // Only add if it's not a duplicate
                    if (uniqueSlots.add(slotKey)) {
                        // Create new slot with standardized date format
                        String newSlot = String.format("%s,%s,%s,%s", 
                            slotDetails[0], slotDetails[1], date, slotDetails[3]);
                        cleanedSlots.add(newSlot);
                    }
                }
            }
            
            // Write back cleaned slots
            try (PrintWriter writer = new PrintWriter(new FileWriter(SLOTS_FILE))) {
                for (String slot : cleanedSlots) {
                    writer.println(slot);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error cleaning up slots file: " + e.getMessage(),
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

    private void loadAppointmentsFromFile() {
        // Clear existing appointments
        
        File file = new File(APPOINTMENTS_FILE);
        if (!file.exists()) return;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                // Skip lines that start with R (reschedule requests)
                if (line.startsWith("R")) continue;
                
                String[] appointmentDetails = line.split(","); // AppointmentID,LecturerID,Date,Time,StudentID
                if (appointmentDetails.length == 5 && appointmentDetails[4].equals(studentId)) {
                    String lecturerName = getLecturerName(appointmentDetails[1]);
                    String time12Hour = convertTo12HourFormat(appointmentDetails[3]);
                    String date = appointmentDetails[2];
                    myAppointmentsModel.addRow(new Object[]{lecturerName, date, time12Hour, appointmentDetails[1]});
                }
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error reading appointments file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String convertTo12HourFormat(String time24) {
        try {
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

    private void bookSelectedSlot() {
        int selectedRow = availableSlotsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a slot to book.");
            return;
        }

        String date = (String) availableSlotsModel.getValueAt(selectedRow, 1);
        String time = (String) availableSlotsModel.getValueAt(selectedRow, 2);
        String lecturerId = (String) availableSlotsModel.getValueAt(selectedRow, 3);

        // Generate new appointment ID
        String appointmentId = generateNewAppointmentId();

        try {
            // Write to Appointments.txt
            try (FileWriter writer = new FileWriter(APPOINTMENTS_FILE, true)) {
                writer.write(String.format("%s,%s,%s,%s,%s%n", 
                    appointmentId, lecturerId, date, convertTo24HourFormat(time), studentId));
            }

            // Remove the booked slot from slots.txt
            removeBookedSlot(lecturerId, date, convertTo24HourFormat(time));

            // Show success message
            JOptionPane.showMessageDialog(this, "Appointment booked successfully!");

            // Refresh the slots table
            loadSlotsFromFile();
            
            // Refresh my appointments table
            loadAppointmentsFromFile();

            // Refresh any open ViewAppointmentStudent windows
            for (Window window : Window.getWindows()) {
                if (window instanceof ViewAppointmentStudent && window.isVisible()) {
                    ((ViewAppointmentStudent) window).refreshTables();
                }
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error booking appointment: " + e.getMessage());
        }
    }

    private String generateNewAppointmentId() {
        int maxId = 0;
        try (Scanner scanner = new Scanner(new File(APPOINTMENTS_FILE))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split(",", 2);
                    if (parts.length > 0 && parts[0].startsWith("A")) {
                        try {
                            int id = Integer.parseInt(parts[0].substring(1));
                            maxId = Math.max(maxId, id);
                        } catch (NumberFormatException e) {
                            // Skip invalid ID format
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error generating appointment ID: " + e.getMessage());
        }
        return String.format("A%03d", maxId + 1);
    }

    private void removeBookedSlot(String lecturerId, String date, String time) throws IOException {
        File inputFile = new File(SLOTS_FILE);
        File tempFile = new File("slots_temp.txt");
        
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 4 || 
                    !parts[1].trim().equals(lecturerId) || 
                    !parts[2].trim().equals(date) || 
                    !parts[3].trim().equals(time)) {
                    writer.write(line + System.lineSeparator());
                }
            }
        }
        
        // Delete the original file
        if (!inputFile.delete()) {
            throw new IOException("Could not delete original slots file");
        }
        
        // Rename the temp file to the original file name
        if (!tempFile.renameTo(inputFile)) {
            throw new IOException("Could not rename temp file");
        }
    }

    private String convertTo24HourFormat(String time12Hour) {
        try {
            String[] parts = time12Hour.split(" ");
            String[] timeParts = parts[0].split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minutes = Integer.parseInt(timeParts[1]);
            
            if (parts[1].equals("PM") && hour < 12) {
                hour += 12;
            } else if (parts[1].equals("AM") && hour == 12) {
                hour = 0;
            }
            
            return String.format("%02d:%02d", hour, minutes);
        } catch (Exception e) {
            return time12Hour;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // For testing purposes only - in real usage, the actual student ID should be passed
            MakeConsultation app = new MakeConsultation("ST001");
            app.setVisible(true);
        });
    }
}
