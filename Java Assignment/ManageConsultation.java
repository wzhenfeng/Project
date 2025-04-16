import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ManageConsultation extends JFrame {

    private JTable availableSlotsTable;
    private DefaultTableModel availableSlotsModel;
    private static final String FILE_NAME = "slots.txt";
    private String lecturerId;

    public ManageConsultation(String lecturerId) {
        this.lecturerId = lecturerId != null ? lecturerId : "";
        setTitle("Manage Consultation Slots");
        setSize(800, 500);
        setLayout(null);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel availableSlotsLabel = new JLabel("Available Slots");
        availableSlotsLabel.setBounds(20, 20, 150, 25);
        availableSlotsLabel.setFont(new Font("Times New Roman", Font.BOLD, 15)); 
        add(availableSlotsLabel);

        availableSlotsModel = new DefaultTableModel(new Object[]{"Date", "Time"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        availableSlotsTable = new JTable(availableSlotsModel);
        availableSlotsTable.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        availableSlotsTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 12));
        JScrollPane availableSlotsScrollPane = new JScrollPane(availableSlotsTable);
        availableSlotsScrollPane.setBounds(20, 50, 740, 300);
        add(availableSlotsScrollPane);
        loadSlots();

        JButton addSlotButton = new JButton("Add Slot");
        addSlotButton.setBounds(20, 370, 150, 35);
        addSlotButton.setFont(new Font("Times New Roman", Font.BOLD, 15)); 
        add(addSlotButton);

        JButton editSlotButton = new JButton("Edit Selected Slot");
        editSlotButton.setBounds(190, 370, 170, 35);
        editSlotButton.setFont(new Font("Times New Roman", Font.BOLD, 15)); 
        add(editSlotButton);

        JButton deleteSlotButton = new JButton("Delete Selected Slot");
        deleteSlotButton.setBounds(380, 370, 170, 35);
        deleteSlotButton.setFont(new Font("Times New Roman", Font.BOLD, 15)); 
        add(deleteSlotButton);

        JButton backButton = new JButton("Back");
        backButton.setBounds(20, 420, 100, 35);
        backButton.setFont(new Font("Times New Roman", Font.BOLD, 15));
        add(backButton);

        addSlotButton.addActionListener(e -> {
            try {
                addSlotDialog();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error opening Add Slot dialog: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        editSlotButton.addActionListener(e -> editSelectedSlot());

        deleteSlotButton.addActionListener(e -> {
            int selectedRow = availableSlotsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a slot to delete.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this slot?", 
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                availableSlotsModel.removeRow(selectedRow);
                try {
                    saveSlotsToFile();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error saving slots to file: " + ex.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
                JOptionPane.showMessageDialog(this, "Slot deleted successfully!");
            }
        });

        backButton.addActionListener(e -> {
            new LecturerDashboard("").setVisible(true);
            dispose();
        });
    }

    private String[] generateDateOptions() {
        String[] dates = new String[7];
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        java.util.Calendar cal = java.util.Calendar.getInstance();
        
        for (int i = 0; i < 7; i++) {
            dates[i] = sdf.format(cal.getTime());
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }
        return dates;
    }

    private String[] generateTimeOptions() {
        String[] times = new String[9];
        for (int hour = 9; hour <= 17; hour++) {
            times[hour - 9] = String.format("%02d:00", hour);
        }
        return times;
    }

    private void loadSlots() {
        availableSlotsModel.setRowCount(0);
        try (Scanner scanner = new Scanner(new File(FILE_NAME))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 4 && (lecturerId.isEmpty() || parts[1].equals(lecturerId))) {
                    String date = parts[2]; // Already in dd-MM-yyyy format
                    availableSlotsModel.addRow(new Object[]{date, convertTo12HourFormat(parts[3])});
                }
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error loading slots.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addSlotDialog() throws IOException {
        JDialog dialog = new JDialog(this, "Add New Slot", true);
        dialog.setLayout(null);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setBounds(20, 20, 150, 25);
        dialog.add(dateLabel);

        JComboBox<String> dateComboBox = new JComboBox<>(generateDateOptions());
        dateComboBox.setBounds(180, 20, 150, 25);
        dialog.add(dateComboBox);

        JLabel timeLabel = new JLabel("Time:");
        timeLabel.setBounds(20, 60, 150, 25);
        dialog.add(timeLabel);

        JComboBox<String> timeComboBox = new JComboBox<>(generateTimeOptions());
        timeComboBox.setBounds(180, 60, 150, 25);
        dialog.add(timeComboBox);

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(100, 200, 100, 30);
        dialog.add(submitButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(220, 200, 100, 30);
        dialog.add(cancelButton);

        submitButton.addActionListener(e -> {
            String selectedDate = (String) dateComboBox.getSelectedItem();
            String selectedTime = (String) timeComboBox.getSelectedItem();

            if (isDuplicateSlot(selectedDate, selectedTime)) {
                JOptionPane.showMessageDialog(dialog, "This slot already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                String slotId = generateNewSlotId();
                String newSlot = String.format("%s,%s,%s,%s", slotId, lecturerId, selectedDate, convertTo24HourFormat(selectedTime));
                
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
                    writer.write(newSlot);
                    writer.newLine();
                }

                // Update the table
                DefaultTableModel model = (DefaultTableModel) availableSlotsTable.getModel();
                model.addRow(new Object[]{selectedDate, convertTo12HourFormat(selectedTime)});
                
                JOptionPane.showMessageDialog(dialog, "Slot added successfully!");
                dialog.dispose();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog, "Error adding slot: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void editSelectedSlot() {
        int selectedRow = availableSlotsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a slot to edit.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String currentDate = (String) availableSlotsTable.getValueAt(selectedRow, 0);
        String currentTime = (String) availableSlotsTable.getValueAt(selectedRow, 1);

        JDialog dialog = new JDialog(this, "Edit Consultation Slot", true);
        dialog.setLayout(null);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setBounds(20, 20, 150, 25);
        dialog.add(dateLabel);

        JComboBox<String> dateComboBox = new JComboBox<>(generateDateOptions());
        dateComboBox.setBounds(180, 20, 150, 25);
        dateComboBox.setSelectedItem(currentDate);
        dialog.add(dateComboBox);

        JLabel timeLabel = new JLabel("Time:");
        timeLabel.setBounds(20, 60, 150, 25);
        dialog.add(timeLabel);

        JComboBox<String> timeComboBox = new JComboBox<>(generateTimeOptions());
        timeComboBox.setBounds(180, 60, 150, 25);
        timeComboBox.setSelectedItem(convertTo24HourFormat(currentTime));
        dialog.add(timeComboBox);

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(100, 200, 100, 30);
        dialog.add(submitButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(220, 200, 100, 30);
        dialog.add(cancelButton);

        submitButton.addActionListener(e -> {
            String editedDate = (String) dateComboBox.getSelectedItem();
            String editedTime = (String) timeComboBox.getSelectedItem();

            if (!editedDate.equals(currentDate) || !editedTime.equals(currentTime)) {
                if (isDuplicateSlot(editedDate, editedTime)) {
                    JOptionPane.showMessageDialog(dialog, "This slot already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            try {
                // Update the table model
                availableSlotsModel.setValueAt(editedDate, selectedRow, 0);
                availableSlotsModel.setValueAt(convertTo12HourFormat(editedTime), selectedRow, 1);
                
                // Save changes to file
                saveSlotsToFile();
                
                JOptionPane.showMessageDialog(dialog, "Slot updated successfully!");
                dialog.dispose();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog, "Error updating slot: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private boolean isDuplicateSlot(String date, String time) {
        try (Scanner scanner = new Scanner(new File(FILE_NAME))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    if (parts[1].equals(lecturerId) && 
                        parts[2].equals(date) && 
                        convertTo12HourFormat(parts[3]).equals(time)) {
                        return true;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void saveSlotsToFile() throws IOException {
        try (PrintWriter writer = new PrintWriter("slots.txt")) {
            for (int i = 0; i < availableSlotsTable.getRowCount(); i++) {
                String date = (String) availableSlotsTable.getValueAt(i, 0); // Already in dd-MM-yyyy format
                String time = (String) availableSlotsTable.getValueAt(i, 1);
                writer.println(String.format("%s,%s,%s,%s", 
                    generateNewSlotId(), lecturerId, date, convertTo24HourFormat(time)));
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error saving slots to file.", "Error", JOptionPane.ERROR_MESSAGE);
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

    private String convertTo24HourFormat(String time12) {
        try {
            String[] parts = time12.split(" ");
            if (parts.length == 2) {
                String[] timeParts = parts[0].split(":");
                if (timeParts.length == 2) {
                    int hour = Integer.parseInt(timeParts[0]);
                    String minutes = timeParts[1];
                    String period = parts[1];
                    
                    if (period.equals("PM") && hour != 12) {
                        hour += 12;
                    } else if (period.equals("AM") && hour == 12) {
                        hour = 0;
                    }
                    
                    return String.format("%02d:%s", hour, minutes);
                }
            }
        } catch (NumberFormatException e) {
            // If parsing fails, return the original time
        }
        return time12;
    }

    private String generateNewSlotId() {
        int maxId = 0;
        try (Scanner scanner = new Scanner(new File(FILE_NAME))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("S")) {
                    String[] parts = line.split(",", 2);
                    String idPart = parts[0].substring(1); // Remove "S" and parse the number
                    maxId = Math.max(maxId, Integer.parseInt(idPart));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error generating slot ID: " + e.getMessage());
        }
        return "S" + String.format("%03d", maxId + 1);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ManageConsultation app = new ManageConsultation("lecturerId");
            app.setVisible(true);
        });
    }
}