package users;

import medicalrecords.Diagnosis;
import medicalrecords.MedicalRecord;
import medicalrecords.Prescription;
import medicalrecords.Treatment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import appointments.Appointment;

public class Doctor extends User {
    private List<Appointment> appointmentSchedule;

    public Doctor(String id, String name, String password, String phoneNumber, String emailAddress,
            LocalDate dateOfBirth, String gender){
        super(id, name, "Doctor", password, phoneNumber, emailAddress, dateOfBirth, gender);
        this.appointmentSchedule = new ArrayList<>();  
    }

    public String viewPatientMedicalRecord(Patient patient){
        return patient.getMedicalRecord().getMedicalRecordDescription();
    }

    public void addDiagnosis(Patient patient, Diagnosis diagnosis) {
        patient.getMedicalRecord().addDiagnosis(diagnosis); // Ensure method access control
    }

    public void addPrescription(Patient patient, Prescription prescription) {
        patient.getMedicalRecord().addPrescription(prescription); // Ensure method access control
    }

    public void addTreatment(Patient patient, Treatment treatment) {
        patient.getMedicalRecord().addTreatment(treatment); // Ensure method access control
    }

    //Work In Progress
    // public void acceptAppointment(Appointment appointment){
    //     appointment.setStatus("Accepted");
    // }

    // public void declineAppointment(Appointment appointment) {
    //     // Logic to decline the appointment and update the status
    //     appointment.setStatus("Declined");
    // }

    // public List<Appointment> getUpcomingAppointments() {
    //     return upcomingAppointments;
    // }
}