import java.awt.*;
import java.io.*;
import java.util.Scanner;
import javax.swing.*;

class StudentDashboard extends JFrame {
    public StudentDashboard(String username) {
        setTitle("Student Dashboard");
        setSize(800, 550);
        setLayout(null);
        setLocationRelativeTo(null);

        JLabel welcomeLabel = new JLabel("Student Dashboard");
        welcomeLabel.setBounds(15,15,300,30);
        welcomeLabel.setFont(new Font("Times New Roman",Font.BOLD,30));
        welcomeLabel.setForeground(Color.WHITE);
        add(welcomeLabel);

        JButton ConsultationButton = new JButton("Make Consultation");
        ConsultationButton.setBounds(130,200,250,45);
        ConsultationButton.setFont(new Font("Times New Roman",Font.BOLD,20));
        add(ConsultationButton);

        JButton viewAppointmentsButton = new JButton("View Appointments");
        viewAppointmentsButton.setBounds(400,200,250,45);
        viewAppointmentsButton.setFont(new Font("Times New Roman",Font.BOLD,20));
        add(viewAppointmentsButton);

        JButton viewRescheduleButton = new JButton("View Reschedule");
        viewRescheduleButton.setBounds(130,300,250,45);
        viewRescheduleButton.setFont(new Font("Times New Roman",Font.BOLD,20));
        viewRescheduleButton.addActionListener(e -> {
            RescheduleStudent rescheduleStudent = new RescheduleStudent(username);
            rescheduleStudent.setVisible(true);
        });
        add(viewRescheduleButton);

        JButton feedbackButton = new JButton("Feedback");
        feedbackButton.setBounds(400, 300, 250, 45);
        feedbackButton.setFont(new Font("Times New Roman", Font.BOLD, 20));
        add(feedbackButton);

        JButton logoutButton = new JButton("Log Out");
        logoutButton.setBounds(670,15,100,30);
        logoutButton.setFont(new Font("Times New Roman",Font.BOLD,15));
        add(logoutButton);

        ConsultationButton.addActionListener(e -> {
            MakeConsultation makeConsultationApp = new MakeConsultation(getStudentId(username));
            makeConsultationApp.setVisible(true);
            this.dispose(); 
        });

        viewAppointmentsButton.addActionListener(e -> {
            ViewAppointmentStudent viewAppointmentapp = new ViewAppointmentStudent(username);
            viewAppointmentapp.setVisible(true);
            this.dispose();
        });

        feedbackButton.addActionListener(e -> {
            dispose();
            FeedbackStudent feedbackApp = new FeedbackStudent(getStudentId(username));
            feedbackApp.setVisible(true);
        });

        logoutButton.addActionListener(e -> {
            new Ass3().setVisible(true);
            this.dispose();
        });

        ImageIcon icon = new ImageIcon(getClass().getResource("/image/psyco.jpg"));
        JLabel imageLabel = new JLabel(icon);
        imageLabel.setBounds(0, 0, icon.getIconWidth(), icon.getIconHeight());
        add(imageLabel);
    }

    private String getStudentId(String studentName) {
        try (Scanner scanner = new Scanner(new File("students.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 4 && parts[1].equals(studentName)) {
                    return parts[0];
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
}
