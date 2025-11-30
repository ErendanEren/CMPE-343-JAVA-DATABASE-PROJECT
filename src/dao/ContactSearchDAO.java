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

    // --- YARDIMCI METOD: ResultSet'i Arkadaşının Contact Sınıfına Çevirir ---
    private List<Contact> mapResultSetToContacts(ResultSet rs) throws SQLException {
        List<Contact> contacts = new ArrayList<>();
        while (rs.next()) {
            // 1. Veritabanından verileri çekiyoruz
            // Not: Veritabanı sütun isimlerinin (first_name, last_name vb.) doğru olduğundan emin ol.
            int id = rs.getInt("contact_id");
            String name = rs.getString("first_name");       // DB: first_name -> Java: name
            String surname = rs.getString("last_name");     // DB: last_name -> Java: surname
            String phone = rs.getString("phone_primary");   // DB: phone_primary -> Java: primaryPhone
            String email = rs.getString("email");
            String address = rs.getString("address");       // Adres araması için gerekli

            // 2. Arkadaşının Contact Constructor'ını kullanarak nesneyi oluşturuyoruz.
            // Search ekranında userId, company, nickname veya tarihlere ihtiyacımız olmadığı için
            // veya veritabanından o an çekmediğimiz için oralara 'null' veya '0' veriyoruz.
            Contact contact = new Contact(
                    id,
                    0,              // userId (Search için kritik değil)
                    name,
                    surname,
                    null,           // nickname
                    phone,
                    email,
                    address,
                    null,           // company
                    null,           // created_at
                    null            // updated_at
            );

            contacts.add(contact);
        }
        return contacts;
    }

    // ============================================================
    // 1. ARAMA METODLARI (SEARCH METHODS)
    // ============================================================

    public List<Contact> searchByFirstName(String query) {
        List<Contact> results = new ArrayList<>();
        // Veritabanında sütun adı 'first_name' olduğu varsayılıyor
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

    // Multi: Soyisim VE Şehir (Adres içinde arar)
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

    // Multi: İsim VE Doğum Ayı
    public List<Contact> searchByNameAndBirthMonth(String name, int month) {
        List<Contact> results = new ArrayList<>();
        // İsim hem ad hem soyad içinde aranır
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

    // Multi: Telefon Parçası VE Email Parçası
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

    // Tüm kişileri getir (Listeleme için)
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

    // ============================================================
    // 2. VALİDASYON METODLARI
    // ============================================================

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