package scheduler.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class Patient {
    public final String username;
    private final byte[] salt;
    private final byte[] hash;

    private Patient(PatientBuilder builder) {
        this.username = builder.username;
        this.salt = builder.salt;
        this.hash = builder.hash;
    }

    private Patient(PatientGetter getter) {
        this.username = getter.username;
        this.salt = getter.salt;
        this.hash = getter.hash;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getHash() {
        return hash;
    }

    public void saveToDB() throws SQLException {
        scheduler.db.ConnectionManager cm = new scheduler.db.ConnectionManager();
        Connection con = cm.createConnection();

        String addPatient = "INSERT INTO Patients VALUES (? , ?, ?)";
        try {
            PreparedStatement statement = con.prepareStatement(addPatient);
            statement.setString(1, this.username);
            statement.setBytes(2, this.salt);
            statement.setBytes(3, this.hash);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    /*public void uploadAvailability(Date d) throws SQLException {
        scheduler.db.ConnectionManager cm = new scheduler.db.ConnectionManager();
        Connection con = cm.createConnection();

        String addAvailability = "INSERT INTO Availabilities VALUES (? , ?)";
        try {
            PreparedStatement statement = con.prepareStatement(addAvailability);
            statement.setDate(1, d);
            statement.setString(2, this.username);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }*/

    public static class PatientBuilder {
        private final String username;
        private final byte[] salt;
        private final byte[] hash;

        public PatientBuilder(String username, byte[] salt, byte[] hash) {
            this.username = username;
            this.salt = salt;
            this.hash = hash;
        }

        public scheduler.model.Patient build() {
            return new scheduler.model.Patient(this);
        }
    }

    public static class PatientGetter {
        private final String username;
        private final String password;
        private byte[] salt;
        private byte[] hash;

        public PatientGetter(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public scheduler.model.Patient get() throws SQLException {
            scheduler.db.ConnectionManager cm = new scheduler.db.ConnectionManager();
            Connection con = cm.createConnection();

            String getPatient = "SELECT Salt, Hash FROM Patients WHERE Username = ?";
            try {
                PreparedStatement statement = con.prepareStatement(getPatient);
                statement.setString(1, this.username);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    byte[] salt = resultSet.getBytes("Salt");
                    // we need to call Util.trim() to get rid of the paddings,
                    // try to remove the use of Util.trim() and you'll see :)
                    byte[] hash = scheduler.util.Util.trim(resultSet.getBytes("Hash"));
                    // check if the password matches
                    byte[] calculatedHash = scheduler.util.Util.generateHash(password, salt);
                    if (!Arrays.equals(hash, calculatedHash)) {
                        return null;
                    } else {
                        this.salt = salt;
                        this.hash = hash;
                        return new scheduler.model.Patient(this);
                    }
                }
                return null;
            } catch (SQLException e) {
                throw new SQLException();
            } finally {
                cm.closeConnection();
            }
        }
    }
}
