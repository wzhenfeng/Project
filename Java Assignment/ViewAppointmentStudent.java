import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ViewAppointmentStudent extends JFrame {
    private static final String APPOINTMENTS_FILE = "Appointments.txt";
    private JTable appointmentsTable;
    private JTable pastAppointmentsTable;
    private JButton rescheduleButton;
    private JButton cancelButton;
    private JButton feedbackButton;
    private JButton backButton;
    private JButton viewRescheduleButton;
    private String username;

    public ViewAppointmentStudent(String username) {
        this.username = username;
        setTitle("View Appointment");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);

        JLabel titleLabel = new JLabel("My Appointments");
        titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 15));
        titleLabel.setBounds(20, 20, 150, 20);
        add(titleLabel);

        String[] columnNames = {"Lecturer", "Date", "Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        appointmentsTable = new JTable(model);
        appointmentsTable.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        appointmentsTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 12));
        JScrollPane scrollPane = new JScrollPane(appointmentsTable);
        scrollPane.setBounds(20, 50, 740, 180);
        add(scrollPane);

        rescheduleButton = new JButton("Request Reschedule");
        rescheduleButton.setFont(new Font("Times New Roman", Font.BOLD, 12));
        rescheduleButton.setBounds(20, 240, 150, 25);
        add(rescheduleButton);

        viewRescheduleButton = new JButton("View Reschedule Requests");
        viewRescheduleButton.setFont(new Font("Times New Roman", Font.BOLD, 12));
        viewRescheduleButton.setBounds(360, 240, 180, 25);

        cancelButton = new JButton("Cancel Appointment");
        cancelButton.setFont(new Font("Times New Roman", Font.BOLD, 12));
        cancelButton.setBounds(190, 240, 150, 25);
        add(cancelButton);

        JLabel pastLabel = new JLabel("Past Appointments");
        pastLabel.setFont(new Font("Times New Roman", Font.BOLD, 15));
        pastLabel.setBounds(20, 280, 150, 20);
        add(pastLabel);

        DefaultTableModel pastModel = new DefaultTableModel(columnNames, 0);
        pastAppointmentsTable = new JTable(pastModel);
        pastAppointmentsTable.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        pastAppointmentsTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 12));
        JScrollPane pastScrollPane = new JScrollPane(pastAppointmentsTable);
        pastScrollPane.setBounds(20, 310, 740, 180);
        add(pastScrollPane);

        feedbackButton = new JButton("Provide Feedback");
        feedbackButton.setFont(new Font("Times New Roman", Font.BOLD, 12));
        feedbackButton.setBounds(20, 500, 150, 25);
        add(feedbackButton);

        backButton = new JButton("Back");
        backButton.setFont(new Font("Times New Roman", Font.BOLD, 12));
        backButton.setBounds(20, 530, 100, 25);
        add(backButton);

        rescheduleButton.addActionListener(e -> {
            requestReschedule();
            saveAppointmentsToFile();
        });

        viewRescheduleButton.addActionListener(e -> {
            String studentId = getStudentId(username);
            if (!studentId.isEmpty()) {
                new RescheduleStudent(null, null, null, null, studentId, username).setVisible(true);
            }
        });

        cancelButton.addActionListener(e -> {
            cancelAppointment();
            saveAppointmentsToFile();
        });
        feedbackButton.addActionListener(e -> provideFeedback());

        backButton.addActionListener(e -> {
            refreshTables();
            new StudentDashboard(username).setVisible(true);
            dispose();
        });

        checkAndMovePastAppointments();

        loadAppointmentsFromFile();
        loadPastAppointmentsFromFile();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadAppointmentsFromFile() {
        DefaultTableModel model = (DefaultTableModel) appointmentsTable.getModel();
        model.setRowCount(0);
        String studentId = getStudentId(username);
        if (studentId.isEmpty()) return;

        try (Scanner scanner = new Scanner(new File(APPOINTMENTS_FILE))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("R")) continue;
                
                String[] appointmentDetails = line.split(",");
                if (appointmentDetails.length == 5 && appointmentDetails[4].equals(studentId)) {
                    String lecturerName = getLecturerName(appointmentDetails[1]);
                    String time12Hour = convertTo12HourFormat(appointmentDetails[3]);
                    String date = appointmentDetails[2];
                    model.addRow(new Object[]{lecturerName, date, time12Hour});
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        this.addComponentListener(new ComponentAdapter() {
    @Override
    public void componentShown(ComponentEvent e) {
        loadAppointmentsFromFile();
    }
});
    }

    private void saveAppointmentsToFile() {
        try {
            List<String> appointments = new ArrayList<>();
            try (Scanner scanner = new Scanner(new File(APPOINTMENTS_FILE))) {
                while (scanner.hasNextLine()) {
                    appointments.add(scanner.nextLine());
                }
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(APPOINTMENTS_FILE))) {
                for (String appointment : appointments) {
                    writer.println(appointment);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getStudentId(String username) {
        try (Scanner scanner = new Scanner(new File("students.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 4 && parts[3].equals(username)) {
                    return parts[0];
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void loadPastAppointmentsFromFile() {
        DefaultTableModel model = (DefaultTableModel) pastAppointmentsTable.getModel();
        model.setRowCount(0);

        String studentId = getStudentId(username);
        if (studentId.isEmpty()) {
            return;
        }

        try (Scanner scanner = new Scanner(new File("pastAppointments.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 6 && parts[4].equals(studentId)) {
                    String lecturerId = parts[1];
                    String date = parts[2];
                    String time = convertTo12HourFormat(parts[3].trim());
                    String status = parts[5];
                    model.addRow(new Object[]{getLecturerName(lecturerId), date, time, status});
                }
            }
        } catch (FileNotFoundException e) {
        }

        this.addComponentListener(new ComponentAdapter() {
    @Override
    public void componentShown(ComponentEvent e) {
        loadPastAppointmentsFromFile();
    }
});
    }

    private String getLecturerName(String lecturerId) {
        try (Scanner scanner = new Scanner(new File("lecturers.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                String[] parts = line.split(",");
                if (parts.length >= 4 && parts[0].equals(lecturerId)) {
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
            String[] parts = time12.split(" ");
            String time = parts[0];
            String period = parts[1];

            String[] timeParts = time.split(":");
            if (timeParts.length == 2) {
                int hour = Integer.parseInt(timeParts[0]);
                String minutes = timeParts[1];

                if (period.equals("PM") && hour != 12) {
                    hour += 12;
                } else if (period.equals("AM") && hour == 12) {
                    hour = 0;
                }

                return String.format("%02d:%s", hour, minutes);
            }
        } catch (NumberFormatException e) {
            // If parsing fails, return the original time
        }
        return time12;
    }

    private void requestReschedule() {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to reschedule.");
            return;
        }

        String studentId = getStudentId(username);
        if (studentId.isEmpty()) return;

        String lecturer = (String) appointmentsTable.getValueAt(selectedRow, 0);
        String date = (String) appointmentsTable.getValueAt(selectedRow, 1);
        String time = (String) appointmentsTable.getValueAt(selectedRow, 2);

        JDialog rescheduleDialog = new JDialog(this, "Reschedule Appointment", true);
        rescheduleDialog.setLayout(null);
        rescheduleDialog.setSize(400, 450);
        rescheduleDialog.setLocationRelativeTo(this);

        JLabel titleLabel = new JLabel("Original Appointment:");
        titleLabel.setBounds(20, 20, 200, 25);
        titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 14));
        rescheduleDialog.add(titleLabel);

        JLabel lecturerLabel = new JLabel("Lecturer: " + lecturer);
        lecturerLabel.setBounds(20, 50, 300, 25);
        lecturerLabel.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        rescheduleDialog.add(lecturerLabel);

        JLabel dateLabel = new JLabel("Date: " + date);
        dateLabel.setBounds(20, 75, 300, 25);
        dateLabel.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        rescheduleDialog.add(dateLabel);

        JLabel timeLabel = new JLabel("Time: " + time);
        timeLabel.setBounds(20, 100, 300, 25);
        timeLabel.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        rescheduleDialog.add(timeLabel);

        JLabel selectLabel = new JLabel("Select Slot:");
        selectLabel.setBounds(20, 140, 200, 25);
        selectLabel.setFont(new Font("Times New Roman", Font.BOLD, 14));
        rescheduleDialog.add(selectLabel);

        DefaultComboBoxModel<String> slotsModel = new DefaultComboBoxModel<>();
        JComboBox<String> slotsComboBox = new JComboBox<>(slotsModel);
        slotsComboBox.setBounds(20, 170, 340, 30);
        slotsComboBox.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        rescheduleDialog.add(slotsComboBox);

        String lecturerId = getLecturerId(lecturer);
        List<String> bookedSlots = new ArrayList<>();
        
        try (Scanner scanner = new Scanner(new File("Appointments.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String slotKey = String.format("%s,%s,%s", 
                        parts[1].trim(),
                        parts[2].trim(),
                        parts[3].trim());
                    bookedSlots.add(slotKey);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Then load available slots that aren't booked
        try (Scanner scanner = new Scanner(new File("slots.txt"))) {
        System.out.println("Reading available slots from slots.txt...");
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            String[] parts = line.split(",");
            if (parts.length < 4) {
                System.out.println("Invalid slot format: " + line);
                continue;
            }

            String slotId = parts[0].trim();
            String slotLecturerId = parts[1].trim();
            String slotDate = parts[2].trim();
            String slotTime = parts[3].trim();

            if (!slotLecturerId.equals(lecturerId)) {
                System.out.println("Slot does not match Lecturer ID, skipping: " + line);
                continue;
            }

            String slotKey = String.format("%s,%s,%s", slotLecturerId, slotDate, slotTime);
            if (bookedSlots.contains(slotKey)) {
                System.out.println("Slot is already booked, skipping: " + slotKey);
                continue;
            }

            String displayTime = convertTo12HourFormat(slotTime);
            System.out.println("Available Slot Added: " + slotDate + " " + displayTime);
            slotsModel.addElement(slotDate + " " + displayTime);
        }
    } catch (FileNotFoundException e) {
        System.out.println("slots.txt not found!");
    }








        JLabel reasonLabel = new JLabel("Reason for Rescheduling:");
        reasonLabel.setBounds(20, 220, 200, 25);
        reasonLabel.setFont(new Font("Times New Roman", Font.BOLD, 14));
        rescheduleDialog.add(reasonLabel);

        JTextArea reasonArea = new JTextArea();
        reasonArea.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        JScrollPane reasonScrollPane = new JScrollPane(reasonArea);
        reasonScrollPane.setBounds(20, 250, 340, 100);
        rescheduleDialog.add(reasonScrollPane);

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(100, 370, 100, 30);
        submitButton.setFont(new Font("Times New Roman", Font.BOLD, 12));
        rescheduleDialog.add(submitButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(220, 370, 100, 30);
        cancelButton.setFont(new Font("Times New Roman", Font.BOLD, 12));
        rescheduleDialog.add(cancelButton);

        // Get appointment ID and lecturer ID
        String lecturerIdFinal = "";
        try (Scanner scanner = new Scanner(new File(APPOINTMENTS_FILE))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String currentLecturerId = parts[1].trim();
                    String currentDate = parts[2].trim();
                    String currentTime = parts[3].trim();
                    String currentStudentId = parts[4].trim();
                    
                    if (currentStudentId.equals(studentId) && 
                        currentDate.equals(date) && 
                        currentTime.equals(convertTo24HourFormat(time))) {
                        lecturerIdFinal = currentLecturerId;
                        break;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        final String finalLecturerId = lecturerIdFinal;

        submitButton.addActionListener(e -> {
            String selectedSlot = (String) slotsComboBox.getSelectedItem();
            String reason = reasonArea.getText().trim();
            
            if (selectedSlot == null || selectedSlot.isEmpty()) {
                JOptionPane.showMessageDialog(rescheduleDialog, "Please select a slot.");
                return;
            }
            
            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(rescheduleDialog, "Please provide a reason for rescheduling.");
                return;
            }

            String[] slotParts = selectedSlot.split(" ");
            String newDate = slotParts[0];
            String newTime = convertTo24HourFormat(slotParts[1] + " " + slotParts[2]);

            if (isDuplicateAppointment(finalLecturerId, newDate, newTime, studentId)) {
                JOptionPane.showMessageDialog(rescheduleDialog, 
                    "This slot is already booked. Please select a different time slot.",
                    "Slot Unavailable",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter("rescheduleRequests.txt", true))) {
                String formattedRequest = String.format("R%03d,%s,%s,%s,%s,Pending,%s", 
                    getNextAppointmentId(), finalLecturerId, newDate, newTime, studentId, reason.replace(",", ";"));
                writer.println(formattedRequest);
                JOptionPane.showMessageDialog(rescheduleDialog, "Reschedule request submitted successfully!");
                rescheduleDialog.dispose();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(rescheduleDialog, "Error submitting reschedule request.");
                ex.printStackTrace();
            }
        });

        cancelButton.addActionListener(e -> rescheduleDialog.dispose());

        rescheduleDialog.setVisible(true);
    }

    private int getNextAppointmentId() {
        int maxId = 0;
        try (Scanner scanner = new Scanner(new File("rescheduleRequests.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length > 0 && parts[0].startsWith("R")) {
                        try {
                            int id = Integer.parseInt(parts[0].substring(1));
                            maxId = Math.max(maxId, id);
                        } catch (NumberFormatException e) {
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            return 1;
        }
        return maxId + 1;
    }

    private boolean isDuplicateAppointment(String lecturerId, String date, String time, String studentId) {
        try (Scanner scanner = new Scanner(new File(APPOINTMENTS_FILE))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    if (parts[1].equals(lecturerId) && 
                        parts[2].equals(date) && 
                        parts[3].equals(time)) {
                        return true;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try (Scanner scanner = new Scanner(new File("pastAppointments.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    if (parts[1].equals(lecturerId) && 
                        parts[2].equals(date) && 
                        parts[3].equals(time)) {
                        return true;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try (Scanner scanner = new Scanner(new File("rescheduleRequests.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    if (parts[1].equals(lecturerId) && 
                        parts[2].equals(date) && 
                        parts[3].equals(time) &&
                        parts[5].equals("Pending")) {
                        return true;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void cancelAppointment() {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to cancel.");
            return;
        }

        String lecturer = (String) appointmentsTable.getValueAt(selectedRow, 0);
        String date = (String) appointmentsTable.getValueAt(selectedRow, 1);
        String time = (String) appointmentsTable.getValueAt(selectedRow, 2);
        String lecturerId = getLecturerId(lecturer);
        String studentId = getStudentId(username);

        if (studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Error: Student ID not found");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to cancel this appointment?", 
            "Confirm Cancellation", 
            JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Remove from appointments.txt
        List<String> remainingAppointments = new ArrayList<>();
        boolean appointmentFound = false;
        try (Scanner scanner = new Scanner(new File(APPOINTMENTS_FILE))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length < 5 || 
                    !parts[1].equals(lecturerId) || 
                    !parts[2].equals(date) || 
                    !parts[3].equals(convertTo24HourFormat(time)) || 
                    !parts[4].equals(studentId)) {
                    remainingAppointments.add(line);
                } else {
                    appointmentFound = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error reading appointments file.");
            return;
        }

        if (!appointmentFound) {
            JOptionPane.showMessageDialog(this, "This appointment no longer exists.");
            loadAppointmentsFromFile();
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(APPOINTMENTS_FILE))) {
            for (String appointment : remainingAppointments) {
                writer.println(appointment);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating appointments file.");
            return;
        }

        // Add back to slots.txt
        try (PrintWriter writer = new PrintWriter(new FileWriter("slots.txt", true))) {
            String slotId = String.format("S%03d", getNextSlotId());
            String formattedDate = convertDateFormat(date);
            writer.println(String.format("%s,%s,%s,%s", 
                slotId, lecturerId, formattedDate, convertTo24HourFormat(time)));
            
            JOptionPane.showMessageDialog(this, "Appointment cancelled successfully!");
            
            loadAppointmentsFromFile();
            loadPastAppointmentsFromFile();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error restoring slot.");
            try (PrintWriter writer = new PrintWriter(new FileWriter(APPOINTMENTS_FILE, true))) {
                String appointmentId = String.format("A%03d", getNextAppointmentId());
                writer.println(String.format("%s,%s,%s,%s,%s", 
                    appointmentId, lecturerId, date, convertTo24HourFormat(time), studentId));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Critical error: Could not restore appointment.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
            loadAppointmentsFromFile();
        }
    }

    private int getNextSlotId() {
        int maxId = 0;
        try (Scanner scanner = new Scanner(new File("slots.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.startsWith("S")) {
                    try {
                        int id = Integer.parseInt(line.substring(1, 4));
                        maxId = Math.max(maxId, id);
                    } catch (NumberFormatException e) {
                        // Skip invalid format
                    }
                }
            }
        } catch (FileNotFoundException e) {
            return 1;
        }
        return maxId + 1;
    }

    private int getNextFeedbackId() {
        int maxId = 0;
        File feedbackFile = new File("feedback.txt");

        if (!feedbackFile.exists()) {
            return 1;
        }

        try (Scanner scanner = new Scanner(feedbackFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                String[] details = line.split(",", 2);
                if (details.length > 0 && details[0].startsWith("F")) {
                    try {
                        int currentId = Integer.parseInt(details[0].substring(1));
                        maxId = Math.max(maxId, currentId);
                    } catch (NumberFormatException e) {
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return maxId + 1;
    }
    

    private void provideFeedback() {
    int selectedRow = pastAppointmentsTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a past appointment to provide feedback.");
        return;
    }

    String studentId = getStudentId(username);
    if (studentId.isEmpty()) return;

    String lecturer = (String) pastAppointmentsTable.getValueAt(selectedRow, 0);
    String date = (String) pastAppointmentsTable.getValueAt(selectedRow, 1);
    String time = (String) pastAppointmentsTable.getValueAt(selectedRow, 2);

    JTextArea feedbackArea = new JTextArea(5, 30);
    feedbackArea.setLineWrap(true);
    feedbackArea.setWrapStyleWord(true);

    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.add(new JLabel("Enter your feedback:"), BorderLayout.NORTH);
    panel.add(new JScrollPane(feedbackArea), BorderLayout.CENTER);

    int result = JOptionPane.showConfirmDialog(this, panel,
            "Feedback for " + lecturer + " on " + date + " at " + time,
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (result == JOptionPane.OK_OPTION && !feedbackArea.getText().trim().isEmpty()) {
        try {
            int nextFeedbackId = getNextFeedbackId();
            String feedbackContent = "Student:" + feedbackArea.getText().trim(); // Add the "Student:" prefix
            String feedback = String.format("F%03d,%s,%s,%s,%s,%s",
                    nextFeedbackId, studentId, getLecturerId(lecturer), date, convertTo24HourFormat(time), feedbackContent);

            try (PrintWriter writer = new PrintWriter(new FileWriter("feedback.txt", true))) {
                writer.println(feedback);
                JOptionPane.showMessageDialog(this, "Feedback submitted successfully!");
           }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving feedback: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}





    private String getLecturerId(String lecturerName) {
    try (Scanner scanner = new Scanner(new File("lecturers.txt"))) {
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] parts = line.split(",");
            if (parts.length >= 4 && parts[3].equals(lecturerName)) {
                System.out.println("Lecturer Found: " + lecturerName + " -> ID: " + parts[0].trim());
                return parts[0].trim();
            }
        }
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    }
    System.out.println("Lecturer not found: " + lecturerName);
    return "";
}




    private void checkAndMovePastAppointments() {
        try {
            List<String> currentAppointments = new ArrayList<>();
            List<String> pastAppointments = new ArrayList<>();
            
            try (Scanner scanner = new Scanner(new File("pastAppointments.txt"))) {
                while (scanner.hasNextLine()) {
                    pastAppointments.add(scanner.nextLine());
                }
            } catch (FileNotFoundException e) {
            }
            
            try (Scanner scanner = new Scanner(new File(APPOINTMENTS_FILE))) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] parts = line.split(",");
                    if (parts.length >= 4) {
                        String appointmentDate = parts[2];
                        String appointmentTime = parts[3];
                        
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                        Date appointmentDateTime = sdf.parse(appointmentDate + " " + appointmentTime);
                        Date currentTime = new Date();

                        long hoursDiff = (currentTime.getTime() - appointmentDateTime.getTime()) / (60 * 60 * 1000);
                        if (hoursDiff > 1) {
                            // Move to past appointments
                            String pastAppointmentId = "P" + String.format("%03d", getNextPastAppointmentId());
                            String pastAppointmentLine = pastAppointmentId + line.substring(line.indexOf(",")) + ",Complete";
                            pastAppointments.add(pastAppointmentLine);
                        } else {
                            currentAppointments.add(line);
                        }
                    }
                }
            }
            
            // Write back current appointments
            try (PrintWriter writer = new PrintWriter(new FileWriter(APPOINTMENTS_FILE))) {
                for (String appointment : currentAppointments) {
                    writer.println(appointment);
                }
            }
            
            // Write back past appointments
            try (PrintWriter writer = new PrintWriter(new FileWriter("pastAppointments.txt"))) {
                for (String appointment : pastAppointments) {
                    writer.println(appointment);
                }
            }
            
            loadPastAppointmentsFromFile();
            
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private int getNextPastAppointmentId() {
        int maxId = 0;
        try (Scanner scanner = new Scanner(new File("pastAppointments.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.startsWith("P")) {
                    try {
                        int id = Integer.parseInt(line.substring(1, 4));
                        maxId = Math.max(maxId, id);
                    } catch (NumberFormatException e) {
                    }
                }
            }
        } catch (FileNotFoundException e) {
            return 1;
        }
        return maxId + 1;
    }

    private String convertDateFormat(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date parsedDate = inputFormat.parse(date);
            return outputFormat.format(parsedDate);
        } catch (ParseException e) {
            return date;
        }
    }

    // Add a public method to refresh the tables
    public void refreshTables() {
        DefaultTableModel model = (DefaultTableModel) appointmentsTable.getModel();
        DefaultTableModel pastModel = (DefaultTableModel) pastAppointmentsTable.getModel();
        model.setRowCount(0);
        pastModel.setRowCount(0);

        loadAppointmentsFromFile();
        loadPastAppointmentsFromFile();

        appointmentsTable.repaint();
        pastAppointmentsTable.repaint();
    }
}
