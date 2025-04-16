import java.awt.Color;
import java.awt.Font;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.*; 
public class Ass3 extends JFrame {

    private JButton logInButton;
    private JButton registerButton;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private ImageIcon showIcon;
    private ImageIcon hideIcon;

    //LoginGUI
    public Ass3() {
        setTitle("APU Psychology Consultation Management System");
        setSize(950, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        JLabel titleLabel = new JLabel("APU Psychology Consultation Management System");
        titleLabel.setBounds(220, 10, 550, 40);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 25)); 
        add(titleLabel);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(370, 80, 80, 25);
        usernameLabel.setFont(new Font("Times New Roman",Font.BOLD,15));
        add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(450, 80, 160, 25);
        add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(370, 130, 80, 25);
        passwordLabel.setFont(new Font("Times New Roman",Font.BOLD,15));
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(450, 130, 160, 25);
        add(passwordField);

        logInButton = new JButton("Log In");
        logInButton.setBounds(360, 200, 100, 30);
        logInButton.setFont(new Font("Times New Roman",Font.BOLD,15));
        add(logInButton);

        registerButton = new JButton("Register");
        registerButton.setBounds(510, 200, 100, 30);
        registerButton.setFont(new Font("Times New Roman",Font.BOLD,15));
        add(registerButton);

        logInButton.addActionListener(e -> logIn());
        registerButton.addActionListener(e -> new RegisterGUI().setVisible(true));


        JCheckBox showPasswordCheckbox = new JCheckBox();
        showPasswordCheckbox.setBounds(610, 125, 30, 35);
        showPasswordCheckbox.setOpaque(false);
        showPasswordCheckbox.setContentAreaFilled(false);
        showPasswordCheckbox.setFocusPainted(false);
        add(showPasswordCheckbox);

    showPasswordCheckbox.addActionListener(e -> {
        if (showPasswordCheckbox.isSelected()) {
            passwordField.setEchoChar((char) 0);
        } else {
            passwordField.setEchoChar('\u2022');
        }
    });

    JLabel showPassword = new JLabel("Show Password");
        showPassword.setBounds(630, 130, 100, 25);
        showPassword.setFont(new Font("Times New Roman",Font.BOLD,15));
        showPassword.setForeground(Color.WHITE);
        add(showPassword);


        ImageIcon icon = new ImageIcon(getClass().getResource("/image/campus.jpg"));
        JLabel imageLabel = new JLabel(icon);
        imageLabel.setBounds(0, 0, icon.getIconWidth(), icon.getIconHeight());
        add(imageLabel);
        }
//Log In
private void logIn() {
    String username = usernameField.getText();
    String password = new String(passwordField.getPassword());

    System.out.println("Attempting login with username: " + username + " and password: " + password);

    if (username.isEmpty() || password.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Username or Password cannot be empty.");
        return;
    }

    List<User> students = FileHandler.loadUsers("Student");
    List<User> lecturers = FileHandler.loadUsers("Lecturer");

    if (students == null || lecturers == null) {
        JOptionPane.showMessageDialog(this, "Error loading user data.");
        return;
    }

    if (authenticate(username, password, students)) {
        JOptionPane.showMessageDialog(this, "Student login successful!");
        new StudentDashboard(username).setVisible(true);
        this.dispose();  
    } else if (authenticate(username, password, lecturers)) {
        JOptionPane.showMessageDialog(this, "Lecturer login successful!");
        new LecturerDashboard(username).setVisible(true);
        this.dispose();  
    } else {
        JOptionPane.showMessageDialog(this, "Invalid username or password.");
    }
}

private boolean authenticate(String username, String password, List<User> users) {
    for (User user : users) {
        if (user.login(username, password)) {
            System.out.println("Login successful for user: " + username); 
            return true;
        }
    }
    return false;
}



    public static void main(String[] args) {
        String[] requiredFiles = {
            "students.txt",
            "lecturers.txt",
            "slots.txt",
            "Appointments.txt",
            "pastAppointments.txt",
            "feedback.txt",
            "rescheduleRequests.txt",
            "Reschedule.txt"
        };

        for (String fileName : requiredFiles) {
            File file = new File(fileName);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    System.err.println("Error creating file: " + fileName);
                    e.printStackTrace();
                }
            }
        }

        SwingUtilities.invokeLater(() -> {
            new Ass3().setVisible(true);
        });
    }
}

abstract class User {
    protected String username;
    protected String password;
    protected String name;

    public User(String username, String password, String name) {
        this.username = username;
        this.password = password;
        this.name = name;
    }

    public boolean login(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }
}

class Student extends User {
    public Student(String username, String password, String name) {
        super(username, password, name);
    }
}

class Lecturer extends User {
    public Lecturer(String username, String password, String name) {
        super(username, password, name);
    }
}

class FileHandler {
    private static final String STUDENT_FILE = "students.txt";
    private static final String LECTURER_FILE = "lecturers.txt";

    @SuppressWarnings("CallToPrintStackTrace")
    public static boolean saveUser(User user) {
        String filePath = user instanceof Student ? STUDENT_FILE : LECTURER_FILE;
        try (FileWriter writer = new FileWriter(filePath, true)) {
            String id = generateNewId(filePath, user instanceof Student ? "ST" : "L");
            writer.write(id + "," + user.username + "," + user.password + "," + user.name + "\n");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String generateNewId(String fileName, String prefix) {
        int maxId = 0;
        try (Scanner scanner = new Scanner(new File(fileName))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.startsWith(prefix)) {
                    try {
                        int id = Integer.parseInt(line.substring(prefix.length(), prefix.length() + 3));
                        maxId = Math.max(maxId, id);
                    } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                    }
                }
            }
        } catch (FileNotFoundException e) {
            return String.format("%s%03d", prefix, 1);
        }
        return String.format("%s%03d", prefix, maxId + 1);
    }

    public static List<User> loadUsers(String userType) {
    List<User> users = new ArrayList<>();
    String fileName = userType.equals("Student") ? "students.txt" : "lecturers.txt";

    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",", 4);
            if (parts.length < 4) {
                System.out.println("Skipping invalid line: " + line);
                continue;
            }

            String username = parts[1].trim();
            String password = parts[2].trim();
            String name = parts[3].trim(); 

            if (userType.equals("Student")) {
                users.add(new Student(username, password, name));
            } else if (userType.equals("Lecturer")) {
                users.add(new Lecturer(username, password, name));
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
    return users;
}



}

class IDGenerator {
    public static String generateNewId(String fileName, String prefix) {
        int maxId = 0;

        try (Scanner scanner = new Scanner(new File(fileName))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith(prefix)) {
                    String[] parts = line.split(",", 2); // Extract ID part
                    String idPart = parts[0].substring(prefix.length()); // Extract numeric part
                    maxId = Math.max(maxId, Integer.parseInt(idPart));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error reading IDs from file: " + e.getMessage());
        }

        return prefix + String.format("%03d", maxId + 1);
    }
}
// Register GUI
class RegisterGUI extends JFrame {
    private JTextField usernameField, nameField;
    private JPasswordField passwordField;
    private JComboBox<String> userTypeComboBox;

    public RegisterGUI() {
        setTitle("Register");
        setSize(400, 300);
        setLayout(null);
        setLocationRelativeTo(null);

        JLabel userTypeLabel = new JLabel("User Type:");
        userTypeLabel.setBounds(10, 10, 80, 25);
        add(userTypeLabel);

        userTypeComboBox = new JComboBox<>(new String[]{"Student", "Lecturer"});
        userTypeComboBox.setBounds(100, 10, 160, 25);
        add(userTypeComboBox);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(10, 50, 80, 25);
        add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(100, 50, 160, 25);
        add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 90, 80, 25);
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(100, 90, 160, 25);
        add(passwordField);

        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setBounds(10, 130, 80, 25);
        add(nameLabel);

        nameField = new JTextField();
        nameField.setBounds(100, 130, 160, 25);
        add(nameField);

        JLabel reminderLabel = new JLabel("Please to ensure that the name");
        reminderLabel.setBounds(100,150,160,40);
        reminderLabel.setFont(new Font("Times New Roman",Font.BOLD,10));
        add(reminderLabel);

        JLabel remindersLabel = new JLabel("you write is your full name!!");
        remindersLabel.setBounds(100,165,160,40);
        remindersLabel.setFont(new Font("Times New Roman",Font.BOLD,10));
        add(remindersLabel);


        JButton registerButton = new JButton("Register");
        registerButton.setBounds(100, 200, 100, 30);
        add(registerButton);

        registerButton.addActionListener(e -> registerUser());
    }

    private void registerUser() {
        String userType = (String) userTypeComboBox.getSelectedItem();
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String name = nameField.getText();

        if (username.isEmpty() || password.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        List<User> users = FileHandler.loadUsers(userType);
    if (users != null) {
        for (User user : users) {
            if (user.username.equals(username)) {
                JOptionPane.showMessageDialog(this, "Username already exists. Please choose a different username.");
                return;
            }
        }
    }

        User newUser;
        if (userType.equals("Student")) {
            newUser = new Student(username, password, name);
        } else {
            newUser = new Lecturer(username, password, name);
        }

        if (FileHandler.saveUser(newUser)) {
            JOptionPane.showMessageDialog(this, "Registration successful!");
            this.dispose(); 
        } else {
            JOptionPane.showMessageDialog(this, "Error during registration.");
        }
    }
}
