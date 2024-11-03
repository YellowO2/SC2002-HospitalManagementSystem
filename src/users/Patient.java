/* 
 * The Patient class represents a patient who has access to a medical record and can schedule appointments.
 * It should contain methods that manage and manipulate patient data, 
 * but not handle interaction with the user as that is done by the menu.
 */
package users;

import medicalrecords.Diagnosis;
import medicalrecords.Prescription;
import medicalrecords.Treatment;
import medicalrecords.MedicalRecordManager;

import java.time.LocalDate;

public class Patient extends User {
    private String patientId; // Only store the patient ID

    public Patient(String id, String name, String password, String phoneNumber, String emailAddress, String bloodType,
            LocalDate dateOfBirth, String gender) {
        super(id, name, "Patient", password, phoneNumber, emailAddress, dateOfBirth, gender);
        this.patientId = id;
    }

}
