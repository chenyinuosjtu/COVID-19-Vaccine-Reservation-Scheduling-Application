package scheduler;

import scheduler.db.ConnectionManager;
import scheduler.model.Appointment;
import scheduler.model.Caregiver;
import scheduler.model.Patient;
import scheduler.model.Vaccine;
import scheduler.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;

public class Scheduler {

    // objects to keep track of the currently logged-in user
    // Note: it is always true that at most one of currentCaregiver and currentPatient is not null
    //       since only one user can be logged-in at a time
    private static Caregiver currentCaregiver = null;
    private static Patient currentPatient = null;

    public static void main(String[] args) {
        // printing greetings text
        System.out.println();
        System.out.println("Welcome to the COVID-19 Vaccine Reservation Scheduling Application!");
        System.out.println("*** Please enter one of the following commands ***");
        System.out.println("> create_patient <username> <password>");  //TODO: implement create_patient (Part 1)
        System.out.println("> create_caregiver <username> <password>");
        System.out.println("> login_patient <username> <password>");  // TODO: implement login_patient (Part 1)
        System.out.println("> login_caregiver <username> <password>");
        System.out.println("> search_caregiver_schedule <date>");  // TODO: implement search_caregiver_schedule (Part 2)
        System.out.println("> reserve <date> <vaccine>");  // TODO: implement reserve (Part 2)
        System.out.println("> upload_availability <date>");
        System.out.println("> cancel <appointment_id>");  // TODO: implement cancel (extra credit)
        System.out.println("> add_doses <vaccine> <number>");
        System.out.println("> show_appointments");  // TODO: implement show_appointments (Part 2)
        System.out.println("> logout");  // TODO: implement logout (Part 2)
        System.out.println("> quit");
        System.out.println();

        // read input from user
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            String response = "";
            try {
                response = r.readLine();
            } catch (IOException e) {
                System.out.println("Please try again!");
            }
            // split the user input by spaces
            String[] tokens = response.split(" ");
            // check if input exists
            if (tokens.length == 0) {
                System.out.println("Please try again!");
                continue;
            }
            // determine which operation to perform
            String operation = tokens[0];
            if (operation.equals("create_patient")) {
                createPatient(tokens);
            } else if (operation.equals("create_caregiver")) {
                createCaregiver(tokens);
            } else if (operation.equals("login_patient")) {
                loginPatient(tokens);
            } else if (operation.equals("login_caregiver")) {
                loginCaregiver(tokens);
            } else if (operation.equals("search_caregiver_schedule")) {
                searchCaregiverSchedule(tokens);
            } else if (operation.equals("reserve")) {
                reserve(tokens);
            } else if (operation.equals("upload_availability")) {
                uploadAvailability(tokens);
            } else if (operation.equals("cancel")) {
                cancel(tokens);
            } else if (operation.equals("add_doses")) {
                addDoses(tokens);
            } else if (operation.equals("show_appointments")) {
                showAppointments(tokens);
            } else if (operation.equals("logout")) {
                logout(tokens);
            } else if (operation.equals("quit")) {
                System.out.println("Bye!");
                return;
            } else {
                System.out.println("Invalid operation name!");
            }
        }
    }

    private static void createPatient(String[] tokens) {
        // TODO: Part 1
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];
        if (usernameExistsPatient(username)) {
            System.out.println("Username taken, try again!");
            return;
        }
        // add salt and generate hash
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the patient
        try {
            currentPatient = new Patient.PatientBuilder(username, salt, hash).build();
            // save to patient information to our database
            currentPatient.saveToDB();
            System.out.println(" *** Account created successfully *** ");
        } catch (SQLException e) {
            System.out.println("Create failed");
            e.printStackTrace();
        }
    }

    private static boolean usernameExistsPatient(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Patients WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void createCaregiver(String[] tokens) {
        // create_caregiver <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];
        // check 2: check if the username has been taken already
        if (usernameExistsCaregiver(username)) {
            System.out.println("Username taken, try again!");
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the caregiver
        try {
            currentCaregiver = new Caregiver.CaregiverBuilder(username, salt, hash).build();
            // save to caregiver information to our database
            currentCaregiver.saveToDB();
            System.out.println(" *** Account created successfully *** ");
        } catch (SQLException e) {
            System.out.println("Create failed");
            e.printStackTrace();
        }
    }

    private static boolean usernameExistsCaregiver(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Caregivers WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void loginPatient(String[] tokens) {
        // TODO: Part 1
        // login_caregiver <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("Already logged-in!");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Patient patient = null;
        try {
            patient = new Patient.PatientGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when logging in");
            e.printStackTrace();
        }
        // check if the login was successful
        if (patient == null) {
            System.out.println("Please try again!");
        } else {
            System.out.println("Patient logged in as: " + username);
            currentPatient = patient;
        }
    }

    private static void loginCaregiver(String[] tokens) {
        // login_caregiver <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("Already logged-in!");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Caregiver caregiver = null;
        try {
            caregiver = new Caregiver.CaregiverGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when logging in");
            e.printStackTrace();
        }
        // check if the login was successful
        if (caregiver == null) {
            System.out.println("Please try again!");
        } else {
            System.out.println("Caregiver logged in as: " + username);
            currentCaregiver = caregiver;
        }
    }

    private static void searchCaregiverSchedule(String[] tokens) {
        // TODO: Part 2
        // searchCaregiverSchedule <date>
        if (currentPatient == null && currentCaregiver == null) {
            System.out.println("Please login first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 2 to include all information (with the operation name)
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }
        String date = tokens[1];
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String getAvailabilities = "Select * from Availabilities where Time = ?";
        try {
            PreparedStatement statement = con.prepareStatement(getAvailabilities);
            Date d = Date.valueOf(date);
            statement.setDate(1, d);
            ResultSet rs = statement.executeQuery();
            if (!rs.isBeforeFirst()) {
                System.out.println("There is no available caregiver!");
            } else {
                while (rs.next()) {
                    System.out.print("Time: " + rs.getDate("Time") + "\t");
                    System.out.print("Caregiver: " + rs.getString("Username") + "\t");
                    System.out.println("");
                }
            }
            showDoses();
        } catch (SQLException e) {
            System.out.println("Error occurred when getting availabilities");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }

    }

    private static void reserve(String[] tokens) {
        // TODO: Part 2
        // reserve <date> <vaccine>
        // check 1: check if the current logged-in user is a patient
        if (currentPatient == null) {
            System.out.println("Please login as a patient first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 2 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String date = tokens[1];
        String vaccineName = tokens[2];


        Vaccine vaccine = null;
        try {
            vaccine = new Vaccine.VaccineGetter(vaccineName).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when adding doses");
            e.printStackTrace();
        }
        if (vaccine == null) {
            System.out.println("There is no " + vaccineName + " now!");
            return;
        }
        if (vaccine.getAvailableDoses() == 0) {
            System.out.println("There is not enough " + vaccineName + " now!");
            return;
        }


        scheduler.model.Appointment appoint = null;
        try {
            Date d = Date.valueOf(date);
            String assigned_caregiver = getRandomCaregiver(d);
            if (assigned_caregiver == null) {
                System.out.println("There is no available caregiver!");
                return;
            }
            int id = getMAXid() + 1;
            appoint = new Appointment.AppointmentBuilder(id, currentPatient.username, assigned_caregiver, vaccineName, d).build();
            // todo: minus doses
            minusDoses(vaccineName, 1);
            removeAvailability(assigned_caregiver, d);
            appoint.saveToDB();
            System.out.println(" *** Reservation created successfully! *** ");
            System.out.println("Your appointment_id is " + id + "!");
            System.out.println(assigned_caregiver + " is assigned to vaccine you!");
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
        } catch (SQLException e) {
            System.out.println("Error occurred when reserving");
            e.printStackTrace();
        }


    }

    public static String getRandomCaregiver(Date time) throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String getRandom = "Select * from Availabilities where Time = ? order by RAND()";
        try {
            PreparedStatement statement = con.prepareStatement(getRandom);
            statement.setDate(1, time);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                return rs.getString("Username");
            }
            return null;
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    public static int getMAXid() throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String getMAX = "Select MAX(ID) as max from Appointments";
        try {
            PreparedStatement statement = con.prepareStatement(getMAX);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                return rs.getInt("max");
            }
            return 0;
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }


    private static void uploadAvailability(String[] tokens) {
        // upload_availability <date>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 2 to include all information (with the operation name)
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }
        String date = tokens[1];

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        String getAppointments = "SELECT * FROM Appointments WHERE Caregiver_name = ? and Time = ?";

        try {
            PreparedStatement statement = con.prepareStatement(getAppointments);
            statement.setString(1, currentCaregiver.getUsername());
            statement.setString(2, date);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                System.out.println("The date is appointed by a patient");
                return;
            }
        } catch (SQLException e) {
            System.out.println("Error occurred when search appointments");
            e.printStackTrace();
        }

        String getAvailability = "SELECT * FROM Availabilities WHERE Username = ? and Time = ?";
        try {
            PreparedStatement statement = con.prepareStatement(getAvailability);
            statement.setString(1, currentCaregiver.getUsername());
            statement.setString(2, date);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                System.out.println("The date already exists!");
                return;
            }
        } catch (SQLException e) {
            System.out.println("Error occurred when search Availabilities");
            e.printStackTrace();
        }

        try {
            Date d = Date.valueOf(date);
            currentCaregiver.uploadAvailability(d);
            System.out.println("Availability uploaded!");
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
        } catch (SQLException e) {
            System.out.println("Error occurred when uploading availability");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
    }


    private static void removeAvailability(String username, Date date) {
        // remove_availability <username> <date>
        // check 1: check if the current logged-in user is a caregiver
        if (currentPatient == null) {
            System.out.println("Please login first!");
            return;
        }
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        String remove = "DELETE from Availabilities where Username = ? and Time = ?";

        try {
            PreparedStatement statement = con.prepareStatement(remove);
            statement.setString(1, username);
            statement.setDate(2, date);
            int rs = statement.executeUpdate();
            //System.out.println("Line: " + rs + "is removed!");
            //System.out.println("remove availabilities successfully!");
            return;
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
        } catch (SQLException e) {
            System.out.println("Error occurred when removing availability");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
    }

    private static void cancel(String[] tokens) {
        // TODO: Extra credit
        if (currentPatient == null && currentCaregiver == null) {
            System.out.println("Please login first!");
            return;
        }

        if (tokens.length != 2) {
            System.out.println("Please input the appointment id and try again!");
            return;
        }
        int id = Integer.parseInt(tokens[1]);
        String vaccineName = "";

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        String getAppointments = "SELECT * FROM Appointments WHERE ID = ?";

        // update availabilities
        if (currentPatient != null || currentCaregiver != null) {
            try {
                PreparedStatement statement1 = con.prepareStatement(getAppointments);
                statement1.setInt(1, id);
                ResultSet resultSet = statement1.executeQuery();
                while (resultSet.next()) {
                    String name = resultSet.getString("Caregiver_name");
                    Date d = resultSet.getDate("Time");
                    vaccineName = resultSet.getString("Vaccine_name");
                    String addAvailability = "INSERT INTO Availabilities VALUES (? , ?)";
                    statement1 = con.prepareStatement(addAvailability);
                    statement1.setDate(1, d);
                    statement1.setString(2, name);
                    statement1.executeUpdate();
                    System.out.println("Availabilities updated!");
                }
            } catch (SQLException e) {
                System.out.println("Error occurred when add Availabilities");
                e.printStackTrace();
            }
        }

        // remove id
        String remove = "DELETE from Appointments where ID = ?";
        if (currentPatient != null || currentCaregiver != null) {
            try {
                PreparedStatement statement2 = con.prepareStatement(remove);
                statement2.setInt(1, id);
                int rs = statement2.executeUpdate();
                //add doses
                Vaccine vaccine = new Vaccine.VaccineGetter(vaccineName).get();
                vaccine.increaseAvailableDoses(1);
                System.out.println("Cancel appointments successfully!");
                return;
            } catch (IllegalArgumentException e) {
                System.out.println("Please enter a valid id!");
            } catch (SQLException e) {
                System.out.println("Error occurred when cancelling appointments");
                e.printStackTrace();
            } finally {
                cm.closeConnection();
            }
        }


    }

    private static void addDoses(String[] tokens) {
        // add_doses <vaccine> <number>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String vaccineName = tokens[1];
        int doses = Integer.parseInt(tokens[2]);
        Vaccine vaccine = null;
        try {
            vaccine = new Vaccine.VaccineGetter(vaccineName).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when adding doses");
            e.printStackTrace();
        }
        // check 3: if getter returns null, it means that we need to create the vaccine and insert it into the Vaccines
        //          table
        if (vaccine == null) {
            try {
                vaccine = new Vaccine.VaccineBuilder(vaccineName, doses).build();
                vaccine.saveToDB();
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        } else {
            // if the vaccine is not null, meaning that the vaccine already exists in our table
            try {
                vaccine.increaseAvailableDoses(doses);
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        }
        System.out.println("Doses updated!");
    }

    private static void minusDoses(String vaccineName, int doses) {
        // minus_doses <vaccine> <number>
        // check 1: check if the current logged-in user is a caregiver
        if (currentPatient == null) {
            System.out.println("Please login first!");
            return;
        }

        Vaccine vaccine = null;
        try {
            vaccine = new Vaccine.VaccineGetter(vaccineName).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when minusing doses");
            e.printStackTrace();
        }
        // check 3: if getter returns null, it means that we need to create the vaccine and insert it into the Vaccines
        //          table
        if (vaccine == null) {
            return;
        } else {
            // if the vaccine is not null, meaning that the vaccine already exists in our table
            try {
                vaccine.decreaseAvailableDoses(doses);
            } catch (SQLException e) {
                System.out.println("Error occurred when minusing doses");
                e.printStackTrace();
            }

        }
        // System.out.println("Doses updated!");
    }

    private static void showDoses() {
        // TODO: Part 2
        // check 1: check login

        if (currentPatient == null && currentCaregiver == null) {
            System.out.println("Please login first!");
            return;
        }
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        String getVaccine = "SELECT * FROM Vaccines";

        if (currentPatient != null || currentCaregiver != null) {
            try {
                PreparedStatement statement = con.prepareStatement(getVaccine);
                ResultSet resultSet = statement.executeQuery();
                System.out.println("*** Vaccine and Available Doses ***");
                while (resultSet.next()) {
                    System.out.print("Vaccine: " + resultSet.getString("Name") + "   " + "\t");
                    System.out.print("Available Doses: " + resultSet.getInt("Doses") + "   " + "\t");
                    System.out.println();
                }
                return;
            } catch (SQLException e) {
                System.out.println("Error occurred when show appointments");
                e.printStackTrace();
            } finally {
                cm.closeConnection();
            }
        }
    }

    private static void showAppointments(String[] tokens) {
        // TODO: Part 2
        // check 1: check if the current logged-in user is a patient
        if (currentPatient == null && currentCaregiver == null) {
            System.out.println("Please login first!");
            return;
        }

        if (tokens.length != 1) {
            System.out.println("Please try again, you typed in too much arguments!");
            return;
        }

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        String getVaccine = "SELECT * FROM Appointments WHERE Patient_Name = ?";

        // patient login
        if (currentPatient != null && currentCaregiver == null) {
            try {
                PreparedStatement statement = con.prepareStatement(getVaccine);
                statement.setString(1, currentPatient.getUsername());
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    System.out.print("Appointment ID: " + resultSet.getInt("ID") + "\t");
                    // System.out.print("Patient_name: " + resultSet.getString("Patient_name") + "\t");
                    System.out.print("Caregiver_name: " + resultSet.getString("Caregiver_name") + "\t");
                    System.out.print("Vaccine_name: " + resultSet.getString("Vaccine_name") + "\t");
                    System.out.print("Time: " + resultSet.getDate("Time") + "\t");
                    System.out.println("");
                }
                return;
            } catch (SQLException e) {
                System.out.println("Error occurred when show appointments");
                e.printStackTrace();
            } finally {
                cm.closeConnection();
            }
        }
        // caregiver login
        else {
            String getVaccine_caregiver = "SELECT * FROM Appointments WHERE Caregiver_Name = ?";
            try {
                PreparedStatement statement = con.prepareStatement(getVaccine_caregiver);
                statement.setString(1, currentCaregiver.getUsername());
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    System.out.print("Appointment ID: " + resultSet.getInt("ID") + "\t");
                    System.out.print("Patient_name: " + resultSet.getString("Patient_name") + "\t");
                    // System.out.print("Caregiver_name: " + resultSet.getString("Caregiver_name") + "\t");
                    System.out.print("Vaccine_name: " + resultSet.getString("Vaccine_name") + "\t");
                    System.out.print("Time: " + resultSet.getDate("Time") + "\t");
                    System.out.println("");
                }
                return;
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            } finally {
                cm.closeConnection();
            }
        }
    }

    private static void logout(String[] tokens) {
        // TODO: Part 2
        // login_out
        // check 1: if nobody has already logged-in, they do not need to log out
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Nobody has already logged-in!");
            return;
        }
        // check 2: the length for tokens need to be exactly 1 to include all information (with the operation name)
        if (tokens.length != 1) {
            System.out.println("Please try again, you just need to type 'logout'!");
            return;
        }

        currentCaregiver = null;
        currentPatient = null;
        // check if the logout was successful
        System.out.println("You have successfully logged out!");
    }
}
