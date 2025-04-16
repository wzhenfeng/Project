import java.awt.*;
import java.io.*;
import java.util.Scanner;
import javax.swing.*;

class LecturerDashboard extends JFrame {
    private String lecturerId;

    public LecturerDashboard(String lecturerName) {
        setTitle("Lecturer Dashboard");
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        lecturerId = getLecturerId(lecturerName);

        JLabel lecturerDashboardLabel = new JLabel("Lecturer Dashboard");
        lecturerDashboardLabel.setBounds(15,15,300,30);
        lecturerDashboardLabel.setFont(new Font("Times New Roman",Font.BOLD,30));
        lecturerDashboardLabel.setForeground(Color.WHITE);
        add(lecturerDashboardLabel);

        JButton ConsultationButton = new JButton("Manage Consultation");
        ConsultationButton.setBounds(130,200,250,45);
        ConsultationButton.setFont(new Font("Times New Roman",Font.BOLD,20));
        add(ConsultationButton);


        ConsultationButton.addActionListener(e -> {
            dispose();
            ManageConsultation manageConsultationApp = new ManageConsultation(lecturerId);
            manageConsultationApp.setVisible(true);
        });

        JButton viewAppointmentsButton = new JButton("View Appointments");
        viewAppointmentsButton.setBounds(400,200,250,45);
        viewAppointmentsButton.setFont(new Font("Times New Roman",Font.BOLD,20));
        add(viewAppointmentsButton);

        viewAppointmentsButton.addActionListener(e -> {
            ViewAppointmentLecturer viewAppointmentapp = new ViewAppointmentLecturer();
            viewAppointmentapp.setVisible(true);
            this.dispose();  // Close dashboard when viewing appointments
        });

        JButton rescheduleLecturerButton = new JButton("Reschedule");
        rescheduleLecturerButton.setBounds(130,300,250,45);
        rescheduleLecturerButton.setFont(new Font("Times New Roman",Font.BOLD,20));
        add(rescheduleLecturerButton);

        rescheduleLecturerButton.addActionListener(e -> {
            RescheduleLecturer Rescheduleapp = new RescheduleLecturer();
            Rescheduleapp.setVisible(true);
            this.dispose();  // Close dashboard when viewing reschedule
        });

        JButton feedbackButton = new JButton("Feedback");
        feedbackButton.setBounds(400, 300, 250, 45);
        feedbackButton.setFont(new Font("Times New Roman", Font.BOLD, 20));
        add(feedbackButton);

        feedbackButton.addActionListener(e -> {
            dispose();
            FeedbackLecturer feedbackApp = new FeedbackLecturer(lecturerId);
            feedbackApp.setVisible(true);
        });

        JButton logoutButton = new JButton("Log Out");
        logoutButton.setBounds(670,15,100,30);
        logoutButton.setFont(new Font("Times New Roman",Font.BOLD,15));
        add(logoutButton);


        logoutButton.addActionListener(e -> {
            new Ass3().setVisible(true);
            this.dispose();
        });

        ImageIcon icon = new ImageIcon(getClass().getResource("/image/psyco.jpg"));
        JLabel imageLabel = new JLabel(icon);
        imageLabel.setBounds(0, 0, icon.getIconWidth(), icon.getIconHeight());
        add(imageLabel);
    }

    private String getLecturerId(String lecturerName) {
        try (Scanner scanner = new Scanner(new File("lecturers.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 4 && parts[1].equals(lecturerName)) {
                    return parts[0];
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
}
