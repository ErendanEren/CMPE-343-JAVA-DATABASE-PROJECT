package dao;

import models.Contact;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ContactSearchDAO {

    private Connection connection;

    public ContactSearchDAO(Connection connection) {
        this.connection = connection;
    }


    private List<Contact> mapResultSetToContacts(ResultSet rs) throws SQLException {
        List<Contact> contacts = new ArrayList<>();
        while (rs.next()) {

            int id = rs.getInt("contact_id");
            String name = rs.getString("first_name");
            String surname = rs.getString("last_name");
            String phone = rs.getString("phone_primary");
            String email = rs.getString("email");
            String address = rs.getString("address");


            Contact contact = new Contact(
                    id,
                    0,
                    name,
                    surname,
                    null,
                    phone,
                    email,
                    address,
                    null,
                    null,
                    null
            );

            contacts.add(contact);
        }
        return contacts;
    }



    public List<Contact> searchByFirstName(String query) {
        List<Contact> results = new ArrayList<>();

        String sql = "SELECT * FROM contacts WHERE first_name LIKE ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query + "%");
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
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query + "%");
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
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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

        String sql = "SELECT * FROM contacts WHERE (first_name LIKE ? OR last_name LIKE ?) AND MONTH(birth_date) = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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
        try (Statement stmt = connection.createStatement();
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