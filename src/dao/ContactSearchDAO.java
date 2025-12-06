package dao;

import Database.DatabaseConnection;
import models.Contact;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
/**
 * Data Access Object (DAO) class responsible for performing search operations on Contact data.
 * <p>
 * This class provides methods to search contacts by various criteria such as name, phone,
 * address, and complex combinations (e.g., name and birth month). It handles the database
 * connection and maps SQL results to {@link Contact} objects.
 * </p>
 *
 * @author Eren Çakır Bircan, Arda Dülger
 */
public class ContactSearchDAO {

    public ContactSearchDAO() {
    }

    /**
     * Maps a generic SQL {@code ResultSet} to a list of {@link Contact} objects.
     * <p>
     * This helper method iterates through the ResultSet, extracts column values including
     * primary/secondary phones and timestamps, and populates Contact instances.
     * </p>
     *
     * @param rs The {@code ResultSet} returned from a database query.
     * @return A {@code List} of {@link Contact} objects populated with the data.
     * @throws SQLException If a database access error occurs or column labels are invalid.
     * @author Eren Çakır Bircan
     */
    private List<Contact> mapResultSetToContacts(ResultSet rs) throws SQLException {
        List<Contact> contacts = new ArrayList<>();
        while (rs.next()) {
            int id = rs.getInt("contact_id");
            String firstName = rs.getString("first_name");
            String middleName = rs.getString("middle_name");
            String lastName  = rs.getString("last_name");
            String nickname  = rs.getString("nickname");
            String phone     = rs.getString("phone_primary");
            String phoneSecondary = rs.getString("phone_secondary");
            String email     = rs.getString("email");
            String linkedinUrl = rs.getString("linkedin_url");
            String address   = rs.getString("address");
            java.sql.Date birthdate = rs.getDate("birthdate");

            Timestamp createdTs = rs.getTimestamp("created_at");
            Timestamp updatedTs = rs.getTimestamp("updated_at");

            Contact contact = new Contact(
                    id,
                    0,
                    firstName,
                    lastName,
                    nickname,
                    phone,
                    birthdate,
                    email,
                    address,
                    createdTs != null ? new java.sql.Date(createdTs.getTime()) : null,
                    updatedTs != null ? new java.sql.Date(updatedTs.getTime()) : null
            );
            contact.setNickname(nickname);
            contact.setMiddleName(middleName);
            contact.setSecondaryPhone(phoneSecondary);
            contact.setLinkedinUrl(linkedinUrl);
            contact.setBirthdate(birthdate);
            contacts.add(contact);
        }
        return contacts;
    }
    /**
     * Searches for contacts where the first name matches the given query pattern.
     * <p>
     * This method uses the SQL {@code LIKE} operator with wildcards to find partial matches.
     * </p>
     *
     * @param nameQuery The partial or full string to search for within the first name.
     * @return A {@code List} of {@link Contact} objects matching the criteria.
     * @author Eren Çakır Bircan, Arda Dülger
     */
    public List<Contact> searchByFirstName(String nameQuery) {
        List<Contact> results = new ArrayList<>();

        String sql = "SELECT * FROM contacts WHERE first_name LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {


            ps.setString(1, "%" + nameQuery + "%");

            try (ResultSet rs = ps.executeQuery()) {
                return mapResultSetToContacts(rs);
            }
        } catch (SQLException e) {
            System.err.println("Search Error (First Name): " + e.getMessage());

        }
        return results;
    }
    /**
     * Searches for contacts where the middle name matches the given query pattern.
     *
     * @param query The partial or full string to search for within the middle name.
     * @return A {@code List} of {@link Contact} objects matching the criteria.
     * @author Eren Çakır Bircan, Arda Dülger
     */
    public List<Contact> searchByMiddleName(String query) {
        List<Contact> results = new ArrayList<>();

        String sql = "SELECT * FROM contacts WHERE middle_name LIKE ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, "%" + query + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                results = mapResultSetToContacts(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
    /**
     * Searches for contacts where the last name matches the given query pattern.
     *
     * @param query The partial or full string to search for within the last name.
     * @return A {@code List} of {@link Contact} objects matching the criteria.
     * @author Eren Çakır Bircan, Arda Dülger
     */
    public List<Contact> searchByLastName(String query) {
        List<Contact> results = new ArrayList<>();
        String sql = "SELECT * FROM contacts WHERE last_name LIKE ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1,"%"+  query + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                results = mapResultSetToContacts(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
    /**
     * Searches for contacts based on their primary phone number.
     *
     * @param query The phone number (or part of it) to search for.
     * @return A {@code List} of {@link Contact} objects matching the phone number.
     * @author Eren Çakır Bircan, Arda Dülger
     */
    public List<Contact> searchByPhoneNumber(String query) {
        List<Contact> results = new ArrayList<>();
        String sql = "SELECT * FROM contacts WHERE phone_primary LIKE ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, "%" + query + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                results = mapResultSetToContacts(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
    /**
     * Searches for contacts based on their secondary phone number.
     * <p>
     * Specifically queries the 'phone_secondary' column using the LIKE operator.
     * </p>
     *
     * @param query The secondary phone number (or part of it) to search for.
     * @return A {@code List} of {@link Contact} objects matching the secondary phone number.
     * @author Eren Çakır Bircan, Arda Dülger
     */
    public List<Contact> searchBySecondaryPhoneNumber(String query) {
        List<Contact> results = new ArrayList<>();

        String sql = "SELECT * FROM contacts WHERE phone_secondary LIKE ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, "%" + query + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                results = mapResultSetToContacts(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
    /**
     * Performs a multi-criteria search using both last name and city (address).
     * <p>
     * A contact must match BOTH the last name pattern AND the address pattern to be returned.
     * </p>
     *
     * @param lastname The partial string for the last name.
     * @param city The partial string for the address/city.
     * @return A {@code List} of matching {@link Contact} objects.
     * @author Eren Çakır Bircan, Arda Dülger
     */
    public List<Contact> searchByLastnameAndCity(String lastname, String city) {
        List<Contact> results = new ArrayList<>();
        String sql = "SELECT * FROM contacts WHERE last_name LIKE ? AND address LIKE ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, "%" + lastname + "%");
            pstmt.setString(2, "%" + city + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                results = mapResultSetToContacts(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
    /**
     * Searches for contacts matching a name (First or Middle) and a specific birth month.
     * <p>
     * This method extracts the month part of the 'birthdate' column using the SQL
     * {@code MONTH()} function.
     * </p>
     *
     * @param name The name to search for (checks both first and middle names).
     * @param month The integer representation of the month (1-12).
     * @return A {@code List} of matching {@link Contact} objects.
     * @author Eren Çakır Bircan, Arda Dülger
     */
    public List<Contact> searchByNameAndBirthMonth(String name, int month) {
        List<Contact> results = new ArrayList<>();


        String sql =
                "SELECT * FROM contacts " +
                        "WHERE (first_name LIKE ? OR middle_name LIKE ?) " +
                        "AND MONTH(birthdate) = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {


            pstmt.setString(1, "%" + name + "%");
            pstmt.setString(2, "%" + name + "%");
            pstmt.setInt(3, month);

            try (ResultSet rs = pstmt.executeQuery()) {
                results = mapResultSetToContacts(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
    /**
     * Searches for contacts using partial matches for phone numbers and email addresses.
     * <p>
     * The logic checks if (Primary Phone OR Secondary Phone matches) AND (Email matches).
     * </p>
     *
     * @param phonePart The partial phone number string.
     * @param emailPart The partial email string.
     * @return A {@code List} of matching {@link Contact} objects.
     * @author  Arda Dülger
     */
    public List<Contact> searchByPhonePartAndEmailPart(String phonePart, String emailPart) {
        List<Contact> results = new ArrayList<>();
        String sql = "SELECT * FROM contacts WHERE (phone_primary LIKE ? OR phone_secondary LIKE ?) AND email LIKE ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, "%" + phonePart + "%");
            pstmt.setString(2, "%" + phonePart + "%");
            pstmt.setString(3, "%" + emailPart + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                results = mapResultSetToContacts(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
    /**
     * Retrieves all contact records from the database.
     *
     * @return A {@code List} containing all {@link Contact} objects in the database.
     * @author Eren Çakır Bircan
     */
    public List<Contact> getAllContacts() {
        List<Contact> results = new ArrayList<>();
        String sql = "SELECT * FROM contacts";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            results = mapResultSetToContacts(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
    /**
     * Validates if the provided phone string contains only allowed characters (digits, +, -, space).
     *
     * @param phone The phone string to validate.
     * @return {@code true} if valid, {@code false} otherwise.
     * @author Eren Çakır Bircan
     */
    public boolean isValidPhoneNumber(String phone) {
        return phone.matches("[0-9\\+\\-\\s]*");
    }
    /**
     * Validates if the provided string follows the ISO Local Date format (YYYY-MM-DD).
     *
     * @param dateStr The date string to check.
     * @return {@code true} if the date format is valid.
     * @author Eren Çakır Bircan
     */
    public boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    /**
     * Validates if the provided integer is a valid month (1-12).
     *
     * @param month The month number to check.
     * @return {@code true} if the month is between 1 and 12 inclusive.
     * @author Eren Çakır Bircan
     */
    public boolean isValidMonth(int month) {
        return month >= 1 && month <= 12;
    }
}
