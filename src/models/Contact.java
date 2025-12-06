package models;

import java.sql.Date;

/**
 * Represents a contact entity in the Contact Management System.
 * <p>
 * This class maps directly to the <b>contacts</b> table in the database.
 * It serves as a Data Transfer Object (DTO) to hold information about a single person,
 * including personal details, contact information, and audit timestamps.
 * </p>
 *
 * @author Eren Çakır Bircan
 */
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
    private String middleName;
    private String secondaryPhone;
    private String linkedinUrl;


    private Date created_at;
    private Date updated_at;

    /**
     * Parameterized constructor to initialize a Contact object with specific details.
     * <p>
     * Note: This constructor initializes the core fields used in earlier versions of the system.
     * Newer fields like middleName or secondaryPhone should be set using setter methods.
     * </p>
     *
     * @param contactId    The unique identifier for the contact.
     * @param userId       The ID of the user associated with this contact.
     * @param name         The first name of the contact.
     * @param surname      The last name of the contact.
     * @param nickname     The nickname or alias of the contact.
     * @param phone        The primary phone number.
     * @param birthdate    The date of birth.
     * @param email        The email address.
     * @param address      The physical address.
     * @param created_at   The timestamp when the record was created.
     * @param updated_at   The timestamp when the record was last updated.
     */
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

    /**
     * Default no-argument constructor.
     * <p>
     * Creates an empty Contact object. Useful when fetching data from the database
     * column by column and setting fields via setter methods.
     * </p>
     */
    public Contact() {

    }

    /**
     * Retrieves the unique ID of the contact.
     * @return The contact ID.
     */
    public int getContactId() {
        return contactId;
    }

    /**
     * Sets the unique ID of the contact.
     * @param contactId The contact ID to set.
     */
    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    /**
     * Retrieves the ID of the user who owns this contact record.
     * @return The user ID.
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the ID of the user associated with this contact.
     * @param userId The user ID to set.
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Retrieves the first name of the contact.
     * @return The first name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the first name of the contact.
     * @param name The first name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the last name of the contact.
     * @return The last name.
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Sets the last name of the contact.
     * @param surname The last name to set.
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * Retrieves the primary phone number.
     * @return The primary phone number.
     */
    public String getPrimaryPhone() {
        return primaryPhone;
    }

    /**
     * Sets the primary phone number.
     * @param primaryPhone The phone number to set.
     */
    public void setPrimaryPhone(String primaryPhone) {
        this.primaryPhone = primaryPhone;
    }

    /**
     * Retrieves the birthdate of the contact.
     * @return The birthdate as a SQL Date object.
     */
    public Date getBirthdate() {return birthdate;}

    /**
     * Sets the birthdate of the contact.
     * @param birthdate The birthdate to set.
     */
    public void setBirthdate(Date birthdate) {this.birthdate = birthdate;}

    /**
     * Retrieves the email address.
     * @return The email string.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address.
     * @param email The email to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Retrieves the physical address.
     * @return The address string.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the physical address.
     * @param address The address to set.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Retrieves the creation timestamp.
     * @return The creation date.
     */
    public Date getCreated_at() {
        return created_at;
    }

    /**
     * Sets the creation timestamp.
     * @param created_at The creation date to set.
     */
    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    /**
     * Retrieves the last update timestamp.
     * @return The last update date.
     */
    public Date getUpdated_at() {
        return updated_at;
    }

    /**
     * Sets the last update timestamp.
     * @param updated_at The last update date to set.
     */
    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }

    /**
     * Retrieves the nickname of the contact.
     * @return The nickname.
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Sets the nickname of the contact.
     * @param nickname The nickname to set.
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Retrieves the middle name of the contact.
     * @return The middle name.
     */
    public String getMiddleName() {
        return middleName;
    }

    /**
     * Sets the middle name of the contact.
     * @param middleName The middle name to set.
     */
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    /**
     * Retrieves the secondary phone number.
     * @return The secondary phone number.
     */
    public String getSecondaryPhone() {
        return secondaryPhone;
    }

    /**
     * Sets the secondary phone number.
     * @param secondaryPhone The secondary phone number to set.
     */
    public void setSecondaryPhone(String secondaryPhone) {
        this.secondaryPhone = secondaryPhone;
    }

    /**
     * Retrieves the LinkedIn profile URL.
     * @return The LinkedIn URL.
     */
    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    /**
     * Sets the LinkedIn profile URL.
     * @param linkedinUrl The URL to set.
     */
    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    @Override
    /**
     * Returns a string representation of the Contact object.
     * <p>
     * Useful for debugging purposes to see all field values at once.
     * </p>
     *
     * @return A string containing all contact details.
     * @author Eren Çakır Bircan
     */
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
