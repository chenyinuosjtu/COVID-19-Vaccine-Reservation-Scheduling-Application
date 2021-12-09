# COVID-19-Vaccine-Reservation-Scheduling-Application

● Patients: these are customers that want to receive the vaccine.
● Caregivers: these are employees of the health organization administering the vaccines.
● Vaccines: these are vaccine doses in the health organization’s inventory of medical
supplies that are on hand and ready to be given to the patients.

● src.main.resources
    ○ design.pdf: the design of your database schema.
    ○ create.sql: the create statement for your tables.
● src.main.scheduler.model
    ○ Caregiver.java: the data model for your caregivers.
    ○ Patient.java: the data model for your users.
    ○ Vaccine.java: the data model for vaccines.
    ○ Any other data models you have created.
● src.main.scheduler
    ○ Scheduler.java: the main runner for your command-line interface.


● search_caregiver_schedule <date>
    ○ Both patients and caregivers can perform this operation.
    ○ Output the username for the caregivers that are available for the date, along with
the number of available doses left for each vaccine.
● reserve <date> <vaccine>
    ○ Patients perform this operation to reserve an appointment.
    ○ You will be randomly assigned a caregiver for the reservation on that date.
    ○ Output the assigned caregiver and the appointment ID for the reservation.
● show_appointments
    ○ Output the scheduled appointments for the current user (both patients and
caregivers).
    ○ For caregivers, you should print the appointment ID, vaccine name, date, and
patient name.
    ○ For patients, you should print the appointment ID, vaccine name, date, and
caregiver name. 
● Logout
