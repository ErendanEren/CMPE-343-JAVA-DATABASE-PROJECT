package dao;

import Database.DatabaseConnection;
import models.Contact;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ContactSearchDAO {

    public ContactSearchDAO() {
    }


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

    public List<Contact> searchByFirstOrMiddleName(String query) {
        List<Contact> results = new ArrayList<>();


        String sql = "SELECT * FROM contacts WHERE first_name LIKE ? OR middle_name LIKE ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            String searchPattern = "%" + query + "%";


            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {

                results = mapResultSetToContacts(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

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

    public List<Contact> searchByPhonePartAndEmailPart(String phonePart, String emailPart) {
        List<Contact> results = new ArrayList<>();
        String sql = "SELECT * FROM contacts WHERE phone_primary LIKE ? AND email LIKE ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, "%" + phonePart + "%");
            pstmt.setString(2, "%" + emailPart + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                results = mapResultSetToContacts(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

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

    public boolean isValidPhoneNumber(String phone) {
        return phone.matches("[0-9\\+\\-\\s]*");
    }

    public boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public boolean isValidMonth(int month) {
        return month >= 1 && month <= 12;
    }
}
