import authentication.AuthenticationManager;
import database.DatabaseManager;
import database.UserDB;
import java.io.IOException;
import java.util.Scanner;

import authentication.AuthenticationManager;
import appointments.AppointmentManager;
import medicalrecords.MedicalRecordManager;
import menus.AdministratorMenu;
import menus.PatientMenu;
import users.Doctor;
import menus.DoctorMenu;
import database.DatabaseManager;
import menus.PharmacistMenu;
import users.Administrator;
import users.Patient;
import users.Pharmacist;
import users.User;

public class HospitalManagementSystem {

    private static DatabaseManager databaseManager = new DatabaseManager();
    private static AuthenticationManager loginSystem = new AuthenticationManager(databaseManager.getUserDB());
    private static MedicalRecordManager medicalRecordManager = new MedicalRecordManager(
            databaseManager.getMedicalRecordDB());
    private static AppointmentManager appointmentManager = new AppointmentManager(
            databaseManager.getdoctorAvailabilityDB(), databaseManager.getAppointmentDB(), databaseManager.getUserDB());

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            // Initialize the database
            System.out.println("Loading database...");
            databaseManager.initialize();

            User currentUser = loginSystem.handleLogin();

            if (currentUser != null) {
                handleUserRole(scanner, currentUser);
            }
            
            // Save any changes to the database before exiting
            // hmsDatabase.savePatients();
            // hmsDatabase.saveMedicalRecords();

        } catch (Exception e) {
            System.out.println("Error loading the database: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }

        try {
            databaseManager.save();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Thank you for using the Hospital Management System.");
    }

    // Handles the actions based on the user's role
    private static void handleUserRole(Scanner scanner, User currentUser) {
        String role = currentUser.getRole();

        if (role.equals("Patient")) {
            Patient patient = (Patient) currentUser;
            PatientMenu patientMenu = new PatientMenu(patient, medicalRecordManager, appointmentManager);
            patientMenu.displayMenu();
        } else if (role.equals("Doctor")) {
            Doctor doctor = (Doctor) currentUser;
            DoctorMenu doctorMenu = new menus.DoctorMenu(doctor, medicalRecordManager, appointmentManager,
                    databaseManager.getdoctorAvailabilityDB());
            doctorMenu.displayMenu();
        } else if (role.equals("Pharmacist")) {
            Pharmacist pharmacist = (Pharmacist) currentUser;
            PharmacistMenu pharmacistMenu = new PharmacistMenu(pharmacist);
            pharmacistMenu.displayMenu();
        } else if (role.equals("Administrator")) {
            Administrator administrator = (Administrator) currentUser;
            AdministratorMenu administratorMenu = new AdministratorMenu(administrator);
            administratorMenu.displayMenu();
        } else {
            System.out.println("Invalid role. Logging out.");
        }
    }
}
