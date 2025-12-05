package models;

import java.sql.Date;

public class Contact {

    private int contactId;
    private int userId;

    private String name;
    private String surname;
    private String nickname;

    private String primaryPhone;
    private String secondaryPhone;   // NEW

    private Date birthdate;
    private String email;
    private String address;
    private String middleName;
    private String secondaryPhone;
    private String linkedinUrl;


    private String middleName;       // NEW
    private String linkedinUrl;      // NEW

    private Date created_at;
    private Date updated_at;

    // ====== CONSTRUCTORS ======

    // Parametresiz ctor – Tester.fetchContactsFromQuery() için gerekli
    public Contact() {
    }

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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPrimaryPhone() {
        return primaryPhone;
    }

    public void setPrimaryPhone(String primaryPhone) {
        this.primaryPhone = primaryPhone;
    }

    public String getSecondaryPhone() {         // NEW
        return secondaryPhone;
    }

    public void setSecondaryPhone(String secondaryPhone) { // NEW
        this.secondaryPhone = secondaryPhone;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

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

    public String getMiddleName() {             // NEW
        return middleName;
    }

    public void setMiddleName(String middleName) { // NEW
        this.middleName = middleName;
    }

    public String getLinkedinUrl() {            // NEW
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) { // NEW
        this.linkedinUrl = linkedinUrl;
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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getSecondaryPhone() {
        return secondaryPhone;
    }

    public void setSecondaryPhone(String secondaryPhone) {
        this.secondaryPhone = secondaryPhone;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "contactId=" + contactId +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", middleName='" + middleName + '\'' +
                ", surname='" + surname + '\'' +
                ", nickname='" + nickname + '\'' +
                ", primaryPhone='" + primaryPhone + '\'' +
                ", secondaryPhone='" + secondaryPhone + '\'' +
                ", email='" + email + '\'' +
                ", linkedinUrl='" + linkedinUrl + '\'' +
                ", birthdate='" + birthdate + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
