



# COVID-19-Vaccine-Reservation-Scheduling-Application

## 1. Class

### Patients: 

these are customers that want to receive the vaccine.

### Caregivers: 

these are employees of the health organization administering the vaccines.

### Vaccines: 

these are vaccine doses in the health organization’s inventory of medical
supplies that are on hand and ready to be given to the patients.

## 2. files

### src.main.resources

​    design.pdf: the design of your database schema.
​    create.sql: the create statement for your tables.

### src.main.scheduler.model

​    Caregiver.java: the data model for caregivers.
​    Patient.java: the data model for users.
​    Vaccine.java: the data model for vaccines.
​    Appointment.java: the data model for appointments

### src.main.scheduler

​    Scheduler.java: the main runner for command-line interface.

## 3. function

### create_patient <username> <password>

Create a new patient with its password

### create_caregiver <username> <password>

Create a new caregiver with its password

### login_patient <username> <password>

Patient logs in

### login_caregiver <username> <password>

Caregiver logs in

### upload_availability <date>

Caregiver uploads his(her) availability

### cancel <appointment_id>

Patient or caregiver  can cancel his(her) appointment

### add_doses <vaccine> <number>

Caregiver adds doses

### search_caregiver_schedule <date>

​    Both patients and caregivers can perform this operation.
​    Output the username for the caregivers that are available for the date, 
​    along with the number of available doses left for each vaccine.

### reserve <date> <vaccine>

​    Patients perform this operation to reserve an appointment.
​    You will be randomly assigned a caregiver for the reservation on that date.
​    Output the assigned caregiver and the appointment ID for the reservation.

### show_appointments

​    Output the scheduled appointments for the current user 
​    (both patients and caregivers).
​    For caregivers, you should print 
​    the appointment ID, vaccine name, date, and patient name.
​    For patients, you should print 
​    the appointment ID, vaccine name, date, and caregiver name. 

### logout

Patient or caregiver logs out