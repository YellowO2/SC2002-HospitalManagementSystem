package database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import users.Administrator;
import users.Doctor;
import users.Patient;
import users.Pharmacist;
import users.User;

public class UserDB extends Database<User> {

    private List<User> users;
    private static final String USER_FILE = "csv_data/User_List.csv";
    private static final String USER_HEADER = "ID,Name,Date of Birth,Gender,Phone Number,Email Address,Password,Role";

    public UserDB() {
        super(USER_FILE);
        users = new ArrayList<>();
    }

    @Override
    public boolean create(User user) {
        if (exists(user.getId())) {
            System.out.println("A user with ID " + user.getId() + " already exists. No new entry created.");
            return false; // Prevent adding a duplicate
        }
        users.add(user);
        return true;
    }

    public boolean exists(String id) {
        for (User user : users) {
            if (user.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public User getById(String id) {
        for (User user : users) {
            if (user.getId().equals(id)) {
                return user; // Return the matching user
            }
        }
        return null; // Return null if not found
    }

    @Override
    public List<User> getAll() {
        return users;
    }

    @Override
    public boolean update(User updatedUser) {
        User existingUser = getById(updatedUser.getId());
        if (existingUser != null) {
            users.remove(existingUser);
            users.add(updatedUser);
            try {
                save(); // Save changes
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false; // User not found
    }

    @Override
    public boolean delete(String id) {
        User user = getById(id);
        if (user != null) {
            users.remove(user);
            try {
                save();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean save() throws IOException {
        saveData(USER_FILE, users, USER_HEADER);
        return true;
    }

    @Override
    public boolean load() throws IOException {
        List<String> lines = readFile(USER_FILE);

        for (String line : lines) {
            String[] tokens = splitLine(line);

            if (tokens.length == 8) { // Ensure all necessary fields are present
                String id = tokens[0];
                String name = tokens[1];
                String dob = tokens[2];
                String gender = tokens[3];
                String phoneNumber = tokens[4];
                String emailAddress = tokens[5];
                String password = tokens[6];
                String role = tokens[7];

                // Create specific User objects based on the role
                switch (role) {
                    case "Patient":
                        users.add(new Patient(id, name, dob, gender, phoneNumber, emailAddress, password));
                        break;
                    case "Doctor":
                        users.add(new Doctor(id, name, dob, gender, phoneNumber, emailAddress, password));
                        break;
                    case "Pharmacist":
                        users.add(new Pharmacist(id, name, dob, gender, phoneNumber, emailAddress, password));
                        break;
                    case "Administrator":
                        users.add(new Administrator(id, name, dob, gender, phoneNumber, emailAddress, password));
                        break;
                    default:
                        System.out.println("Unknown user role for ID: " + id);
                        break;
                }
            } else {
                System.out.println("Invalid line in " + USER_FILE + ": " + line);
            }
        }
        return true;
    }

    // Method to add a CSV entry directly
    public void addCsvEntry(String csvLine) {
        String[] tokens = splitLine(csvLine);
        if (tokens.length == 8) { // Ensure all fields are present
            String id = tokens[0];
            String name = tokens[1];
            String dob = tokens[2];
            String gender = tokens[3];
            String phoneNumber = tokens[4];
            String emailAddress = tokens[5];
            String password = tokens[6];
            String role = tokens[7];

            switch (role) {
                case "Doctor":
                    users.add(new Doctor(id, name, dob, gender, phoneNumber, emailAddress, password));
                    break;
                case "Pharmacist":
                    users.add(new Pharmacist(id, name, dob, gender, phoneNumber, emailAddress, password));
                    break;
                case "Administrator":
                    users.add(new Administrator(id, name, dob, gender, phoneNumber, emailAddress, password));
                    break;
                default:
                    System.out.println("Unknown role. Entry not added.");
                    break;
            }
        } else {
            System.out.println("Invalid CSV entry. Not added to the list.");
        }
    }
        public List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        for (User user : users) {
            if (user instanceof Doctor) {
                // downcast to Doctor
                doctors.add((Doctor) user);
            }
        }
        return doctors;
    }

    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        for (User user : users) {
            if (user instanceof Patient) {
                // downcast to Patient
                patients.add((Patient) user);
            }
        }
        return patients;
    }
}
