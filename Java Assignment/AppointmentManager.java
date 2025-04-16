import java.io.*;
import java.util.*;
import javax.swing.table.DefaultTableModel;

public class AppointmentManager {
    private static final String PAST_APPOINTMENTS_FILE = "pastAppointments.txt";

    public static void cleanupDuplicateAppointments() {
        Set<String> uniqueAppointments = new LinkedHashSet<>(); // LinkedHashSet to maintain order
        
        // Read all appointments and keep only unique ones
        try (Scanner scanner = new Scanner(new File(PAST_APPOINTMENTS_FILE))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    uniqueAppointments.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            return;
        }

        // Write back unique appointments
        try (PrintWriter writer = new PrintWriter(new FileWriter(PAST_APPOINTMENTS_FILE))) {
            for (String appointment : uniqueAppointments) {
                writer.println(appointment);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadPastAppointments(String studentId, DefaultTableModel model) {
        loadAppointmentsForStudent(studentId, model);
    }

    public static void loadPastAppointmentsForLecturer(String lecturerId, DefaultTableModel model) {
        loadAppointmentsForLecturer(lecturerId, model);
    }

    private static void loadAppointmentsForStudent(String studentId, DefaultTableModel model) {
        model.setRowCount(0);
        Set<String> uniqueAppointments = new HashSet<>();
        List<String[]> appointments = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(PAST_APPOINTMENTS_FILE))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length >= 5 && parts[4].equals(studentId)) {
                    // Create a unique key for this appointment
                    String key = parts[1] + "," + parts[2] + "," + parts[3];
                    if (!uniqueAppointments.contains(key)) {
                        uniqueAppointments.add(key);
                        appointments.add(parts);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // File might not exist yet, which is fine
            return;
        }

        // Sort appointments by date and time
        appointments.sort((a, b) -> {
            int dateCompare = a[2].compareTo(b[2]);
            if (dateCompare != 0) return dateCompare;
            return a[3].compareTo(b[3]);
        });

        // Add sorted appointments to the model
        for (String[] appointment : appointments) {
            String lecturerName = getLecturerName(appointment[1]);
            String time12Hour = convertTo12HourFormat(appointment[3]);
            model.addRow(new Object[]{lecturerName, appointment[2], time12Hour});
        }
    }

    private static void loadAppointmentsForLecturer(String lecturerId, DefaultTableModel model) {
        model.setRowCount(0);
        Set<String> uniqueAppointments = new HashSet<>();
        List<String[]> appointments = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(PAST_APPOINTMENTS_FILE))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length >= 5 && parts[1].equals(lecturerId)) {
                    // Create a unique key for this appointment
                    String key = parts[1] + "," + parts[2] + "," + parts[3];
                    if (!uniqueAppointments.contains(key)) {
                        uniqueAppointments.add(key);
                        appointments.add(parts);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // File might not exist yet, which is fine
            return;
        }

        // Sort appointments by date and time
        appointments.sort((a, b) -> {
            int dateCompare = a[2].compareTo(b[2]);
            if (dateCompare != 0) return dateCompare;
            return a[3].compareTo(b[3]);
        });

        // Add sorted appointments to the model
        for (String[] appointment : appointments) {
            String studentName = getStudentName(appointment[4]);
            String time12Hour = convertTo12HourFormat(appointment[3]);
            model.addRow(new Object[]{studentName, appointment[2], time12Hour});
        }
    }

    private static String getLecturerName(String lecturerId) {
        try (Scanner scanner = new Scanner(new File("lecturers.txt"))) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",");
                if (parts.length >= 4 && parts[0].equals(lecturerId)) {
                    return parts[3];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lecturerId;
    }

    private static String getStudentName(String studentId) {
        try (Scanner scanner = new Scanner(new File("students.txt"))) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",");
                if (parts.length >= 4 && parts[0].equals(studentId)) {
                    return parts[3];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return studentId;
    }

    private static String convertTo12HourFormat(String time24) {
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
            // Return original if parsing fails
        }
        return time24;
    }
}
