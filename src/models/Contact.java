package models;

import java.sql.Date;

public class Contact {

    private int contactId;
    private int userId;

    private String name;
    private String surname;
    private String nickname;
    private String primaryPhone;
    private Date birthdate;
    private String email;
    private String address;

    private Date created_at;
    private Date updated_at;

    // ====== CONSTRUCTORS ======

    public Contact(int contactId, int userId,
                   String name, String surname, String nickname,
                   String phone, Date birthdate, String email,
                   String address, Date created_at, Date updated_at) {

        this.contactId = contactId;
        this.userId = userId;
        this.name = name;
        this.surname = surname;
        this.nickname = nickname;
        this.primaryPhone = phone;
        this.birthdate = birthdate;
        this.email = email;
        this.address = address;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    // ====== GETTERS / SETTERS ======
    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPrimaryPhone() {
        return primaryPhone;
    }

    public void setPrimaryPhone(String primaryPhone) {
        this.primaryPhone = primaryPhone;
    }

    public Date getBirthdate() {return birthdate;}

    public void setBirthdate(Date birthdate) {this.birthdate = birthdate;}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }

    // ====== OPTIONAL: DEBUG PRINT ======
    @Override
    public String toString() {
        return "Contact{" +
                "contactId=" + contactId +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", nickname='" + nickname + '\'' +
                ", phone='" + primaryPhone + '\'' +
                ", email='" + email + '\'' +
                ", birthdate='" + birthdate + '\'' +
                '}';
    }
}
