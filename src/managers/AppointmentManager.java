package managers;

import database.AppointmentDB;
import database.DoctorUnavailabilityDB;
import database.UserDB;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import appointments.Appointment;
import appointments.DoctorUnavailableSlots;
import users.Doctor;

public class AppointmentManager {
    private AppointmentDB appointmentDB;
    private DoctorUnavailabilityDB availabilityDB;
    private UserDB userDB;

    // Constructor
    public AppointmentManager(DoctorUnavailabilityDB availabilityDB, AppointmentDB appointmentDB, UserDB userDB) {
        this.appointmentDB = appointmentDB;
        this.availabilityDB = availabilityDB;
        this.userDB = userDB;
    }

    // Get all doctors with their number in the list
    public List<String> getAllAvailableDoctors() {
        List<Doctor> doctorList = userDB.getAllDoctors();
        List<String> doctorListFormatted = new ArrayList<>();
        for (int i = 0; i < doctorList.size(); i++) {
            Doctor doctor = doctorList.get(i);
            doctorListFormatted.add(doctor.getName() + " - " + doctor.getId());
        }
        return doctorListFormatted;
    }

    public void showAvailableSlots(String doctorId, LocalDate startDate) {
        System.out.println("Viewing available appointment slots for Doctor " + userDB.getById(doctorId).getName());
        System.out.println("===================================================");

        // Iterate through the next 7 days
        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            System.out.print((i + 1) + ". Date: " + currentDate + "  "); // Print the date first

            // Use the helper function to get available slots
            List<LocalTime> availableSlots = getAvailableSlotsForDoctor(doctorId, currentDate);

            // Display the available slots
            if (availableSlots.isEmpty()) {
                System.out.print("No available slots.");
            } else {
                int slotNumber = 1; // Counter for numbering slots
                for (LocalTime slot : availableSlots) {
                    System.out.printf("%d. %s  ", slotNumber++, slot); // Print slots on the same line, separated by
                                                                       // spaces
                }
            }
            System.out.println(); // Move to the next line after printing all slots for the day
            System.out.println("---------------------------------------------------");
        }

        System.out.println("===================================================");
    }

    // Get personal schedule for a doctor on a given date
    public List<String> getPersonalSchedule(String doctorId, LocalDate date) {
        // Define working hours
        LocalTime startOfWork = LocalTime.of(9, 0);
        LocalTime endOfWork = LocalTime.of(17, 0);

        // Create a list of all potential time slots
        List<LocalTime> allPossibleSlots = new ArrayList<>();
        for (LocalTime time = startOfWork; !time.isAfter(endOfWork); time = time.plusHours(1)) {
            allPossibleSlots.add(time);
        }

        // Retrieve unavailable slots and existing appointments
        Set<LocalTime> unavailableTimes = getUnavailableTimes(doctorId, date);

        // Filter out unavailable and booked times
        List<String> availableSlots = new ArrayList<>();
        for (LocalTime slot : allPossibleSlots) {
            if (!unavailableTimes.contains(slot)) {
                availableSlots.add(slot.toString());
            }
        }
        return availableSlots;
    }

    public List<String> getPatientAppointments(String patientId) {
        List<Appointment> patientAppointments = appointmentDB.getPatientAppointments(patientId);
        List<String> patientAppointmentsFormatted = new ArrayList<>();
        for (int i = 0; i < patientAppointments.size(); i++) {
            Appointment appointment = patientAppointments.get(i);
            patientAppointmentsFormatted.add(appointment.toString());
        }
        return patientAppointmentsFormatted;
    }

    public List<String> getDoctorAppointments(String doctorId, String statusFilter) {
        List<Appointment> doctorAppointments = appointmentDB.getDoctorAppointments(doctorId);
        List<String> doctorAppointmentsFormatted = new ArrayList<>();

        for (Appointment appointment : doctorAppointments) {
            if (statusFilter.equalsIgnoreCase("All") ||
                    (statusFilter.equalsIgnoreCase("Pending") && appointment.getStatus().equalsIgnoreCase("Pending")) ||
                    (statusFilter.equalsIgnoreCase("Accepted")
                            && appointment.getStatus().equalsIgnoreCase("Accepted"))) {
                doctorAppointmentsFormatted.add(appointment.toString());
            }
        }
        return doctorAppointmentsFormatted;
    }

    // Helper method to retrieve unavailable times
    private Set<LocalTime> getUnavailableTimes(String doctorId, LocalDate date) {
        List<DoctorUnavailableSlots> unavailableSlots = availabilityDB.getDoctorUnavailability(doctorId, date);
        Set<LocalTime> unavailableTimes = new HashSet<>();
        for (DoctorUnavailableSlots unavailable : unavailableSlots) {
            unavailableTimes.add(unavailable.getTime());
        }
        return unavailableTimes;
    }

    // Helper method to retrieve booked times
    private Set<LocalTime> getBookedTimes(String doctorId, LocalDate date) {
        List<Appointment> doctorAppointments = appointmentDB.getDoctorAppointments(doctorId);
        Set<LocalTime> bookedTimes = new HashSet<>();
        for (Appointment appointment : doctorAppointments) {
            if (appointment.getAppointmentDate().equals(date)) {
                bookedTimes.add(appointment.getAppointmentTime());
            }
        }
        return bookedTimes;
    }

    public List<LocalTime> getAvailableSlotsForDoctor(String doctorId, LocalDate date) {
        // Define working hours
        LocalTime startOfWork = LocalTime.of(9, 0);
        LocalTime lastAppointmentTime = LocalTime.of(16, 0); // Changed from 17 to 16

        // Create a list of all potential time slots for the day
        List<LocalTime> allPossibleSlots = new ArrayList<>();
        for (LocalTime time = startOfWork; !time.isAfter(lastAppointmentTime); time = time.plusHours(1)) {
            allPossibleSlots.add(time);
        }

        // Retrieve unavailable slots and existing appointments
        Set<LocalTime> unavailableTimes = getUnavailableTimes(doctorId, date);
        Set<LocalTime> bookedTimes = getBookedTimes(doctorId, date);

        // System.out.println("DEBUG DAY: " + date);
        // System.out.println("DEBUG unavailable times: " + unavailableTimes);
        // System.out.println("DEBUG booked times: " + bookedTimes);

        // Filter out unavailable and booked times
        List<LocalTime> availableSlots = new ArrayList<>();
        for (LocalTime slot : allPossibleSlots) {
            if (!unavailableTimes.contains(slot) && !bookedTimes.contains(slot)) {
                availableSlots.add(slot);
            }
        }

        return availableSlots;
    }

    // Schedule appointment based on user-selected doctor index
    public boolean scheduleAppointment(String patientId, String doctorId, LocalDate date, int slotIndex) {

        // Use the helper function to get available slots
        List<LocalTime> availableSlots = getAvailableSlotsForDoctor(doctorId, date);
        // System.out.println("DEBUG available slots: " + availableSlots);
        // System.out.println("DEBUG schedule appointment slot index: " + slotIndex);
        if (!validateSlotSelection(slotIndex, availableSlots)) {
            return false;
        }

        LocalTime appointmentTime = availableSlots.get(slotIndex);
        String appointmentId = UUID.randomUUID().toString();
        Appointment appointment = new Appointment(appointmentId, doctorId, patientId, date, appointmentTime,
                "Pending");
        if (appointmentDB.create(appointment)) {
            System.out.println(
                    "Appointment scheduled to " + date + " at " + appointmentTime + ".");
            return true;
        } else {
            return false;
        }
    }

    // Helper method to validate slot selection
    private boolean validateSlotSelection(int slotIndex, List<LocalTime> availableSlots) {
        if (slotIndex < 0 || slotIndex >= availableSlots.size()) {
            System.out.println("Invalid slot selection. Please choose a valid slot index.");
            return false;
        }
        return true;
    }

    // Reschedule an appointment
    public void rescheduleAppointment(String doctorId, String originalAppointmentId, LocalDate newDate,
            int newSlotIndex) {
        // Retrieve the original appointment
        Appointment originalAppointment = appointmentDB.getById(originalAppointmentId);
        if (originalAppointment == null) {
            System.out.println("Error: Appointment with ID " + originalAppointmentId + " not found.");
            return;
        }

        String patientId = originalAppointment.getPatientId();

        // Attempt to schedule the new appointment first
        boolean isNewAppointmentScheduled = scheduleAppointment(patientId, doctorId, newDate, newSlotIndex);
        if (!isNewAppointmentScheduled) {
            System.out.println("Error: Failed to schedule the new appointment. Rescheduling aborted.");
            return;
        }

        // If new appointment is successfully scheduled, cancel the old appointment
        boolean isOldAppointmentCancelled = cancelAppointment(originalAppointmentId);
        if (isOldAppointmentCancelled) {
            System.out.println("Success: Appointment rescheduled successfully.");
        } else {
            System.out.println("Warning: Failed to cancel the original appointment. Please check manually.");
        }
    }

    public boolean cancelAppointment(String appointmentId) {
        Appointment appointment = appointmentDB.getById(appointmentId);
        if (appointment == null) {
            System.out.println("Appointment not found.");
            return false;
        }

        // Remove the appointment from the database
        return appointmentDB.delete(appointment.getAppointmentId());
    }

    // TODO: Change boolean to void upon validation of method
    public boolean updateAppointmentStatus(String appointmentId, String status) {
        Appointment appointment = appointmentDB.getById(appointmentId);
        if (appointment == null) {
            System.out.println("Appointment not found.");
            return false;
        }

        appointment.setStatus(status);
        return appointmentDB.update(appointment); // Test and make sure that this is working
        // return true;
    }

    public boolean isValidDoctorId(String doctorId) {
        List<Doctor> doctorList = userDB.getAllDoctors();

        return doctorList.stream().anyMatch(doctor -> doctor.getId().equals(doctorId));
    }

    public boolean isValidAppointmentId(String appointmentId) {
        Appointment appointment = appointmentDB.getById(appointmentId);
        if (appointment != null) {
            return true;
        }
        return false;
    }

    public List<String> viewAllAppointments() {
        List<Appointment> allAppointments = appointmentDB.getAll();
        List<String> allAppointmentsFormatted = new ArrayList<>();

        for (Appointment appointment : allAppointments) {
            allAppointmentsFormatted.add(appointment.toString());
        }

        return allAppointmentsFormatted;
    }
}
