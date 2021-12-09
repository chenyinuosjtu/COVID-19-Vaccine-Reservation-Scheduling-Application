package scheduler.model;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Appointment {
    private int id;
    private String Patient_name;
    private String Caregiver_name;
    private String Vaccine_name;
    private Date Time;

    private Appointment(scheduler.model.Appointment.AppointmentBuilder builder) {
        this.id = builder.id;
        this.Patient_name = builder.Patient_name;
        this.Caregiver_name = builder.Caregiver_name;
        this.Vaccine_name = builder.Vaccine_name;
        this.Time = builder.Time;
    }

    private Appointment(scheduler.model.Appointment.AppointmentGetter getter) {
        this.id = getter.id;
        this.Patient_name = getter.Patient_name;
        this.Caregiver_name = getter.Caregiver_name;
        this.Vaccine_name = getter.Vaccine_name;
        this.Time = getter.Time;
    }

    // Getters
    public String getPatient_Name() {
        return Patient_name;
    }

    public String getVaccine_name() {
        return Vaccine_name;
    }

    public void saveToDB() throws SQLException {
        scheduler.db.ConnectionManager cm = new scheduler.db.ConnectionManager();
        Connection con = cm.createConnection();

        String addAppointment = "INSERT INTO Appointments VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement statement = con.prepareStatement(addAppointment);
            statement.setInt(1, this.id);
            statement.setString(2, this.Patient_name);
            statement.setString(3, this.Caregiver_name);
            statement.setString(4, this.Vaccine_name);
            statement.setDate(5, this.Time);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }


    public static class AppointmentBuilder {
        private int id;
        private String Patient_name;
        private String Caregiver_name;
        private String Vaccine_name;
        private Date Time;

        public AppointmentBuilder(int id, String Patient_name, String Caregiver_name, String Vaccine_name, Date Time) {
            this.id = id;
            this.Patient_name = Patient_name;
            this.Caregiver_name = Caregiver_name;
            this.Vaccine_name = Vaccine_name;
            this.Time = Time;
        }

        public scheduler.model.Appointment build() throws SQLException {
            return new scheduler.model.Appointment(this);
        }
    }

    public static class AppointmentGetter {
        private int id;
        private String Patient_name;
        private String Caregiver_name;
        private String Vaccine_name;
        private Date Time;

        public AppointmentGetter(int id, String Patient_name, String Caregiver_name, String Vaccine_name, Date Time) {
            this.id = id;
            this.Patient_name = Patient_name;
            this.Caregiver_name = Caregiver_name;
            this.Vaccine_name = Vaccine_name;
            this.Time = Time;
        }

        /*public scheduler.model.Appointment get_appointment() throws SQLException {
            scheduler.db.ConnectionManager cm = new scheduler.db.ConnectionManager();
            Connection con = cm.createConnection();

            String getAppointment = "SELECT id, Patient_name, Vaccine_name, Time FROM Appointments WHERE id = ?";
            try {
                PreparedStatement statement = con.prepareStatement(getAppointment);
                statement.setString(1, this.id);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    this.availableDoses = resultSet.getInt("Doses");
                    return new scheduler.model.Vaccine(this);
                }
                return null;
            } catch (SQLException e) {
                throw new SQLException();
            } finally {
                cm.closeConnection();
            }
        }*/
    }
}

