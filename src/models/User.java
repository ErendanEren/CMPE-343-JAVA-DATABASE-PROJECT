package models;

import java.sql.Date;
import java.util.Scanner;

/**
 * Base abstract model representing an authenticated user of the system.
 * <p>
 * Concrete roles such as {@code Manager}, {@code Senior}, {@code Junior}
 * and {@code Tester} extend this class and implement their own menu logic
 * in {@link #showMenu(Scanner)}.
 * </p>
 *
 * @author Eren Çakır Bircan
 */
public abstract class User {

    /**
     * Unique identifier of the user in the database.
     */
    private int userId;

    /**
     * Login name used for authentication.
     */
    private String username;

    /**
     * Stored password representation (typically a hash).
     */
    private String password_hash;

    /**
     * First name of the user.
     */
    private String name;

    /**
     * Last name (surname) of the user.
     */
    private String surname;

    /**
     * Role of the user (e.g. "Manager", "Senior Developer").
     */
    private String role;

    /**
     * Timestamp at which the user record was created.
     */
    private Date created_at;

    /**
     * Timestamp of the last update to the user record.
     */
    private Date updated_at;

    /**
     * Displays the role-specific console menu for this user.
     * Implementations are responsible for handling user input
     * and controlling the interaction loop.
     *
     * @param scanner a {@link Scanner} used to read input from the console
     */
    public abstract void showMenu(Scanner scanner);

    /**
     * Returns the database identifier of this user.
     *
     * @return the user id
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the database identifier of this user.
     *
     * @param userId the user id to set
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Returns the username used for login.
     *
     * @return the username value
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username used for login.
     *
     * @param username the username value to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the stored password representation (hash or legacy plain-text).
     *
     * @return the stored password value
     */
    public String getPassword_hash() {
        return password_hash;
    }

    /**
     * Sets the stored password representation.
     *
     * @param password_hash the password value (typically hashed) to set
     */
    public void setPassword_hash(String password_hash) {
        this.password_hash = password_hash;
    }

    /**
     * Returns the first name of the user.
     *
     * @return the first name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the first name of the user.
     *
     * @param name the first name value to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the last name (surname) of the user.
     *
     * @return the surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Sets the last name (surname) of the user.
     *
     * @param surname the surname value to set
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * Returns the logical role of the user.
     *
     * @return the role name
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the logical role of the user.
     *
     * @param role the role name to set (e.g. "Tester", "Manager")
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Returns the creation timestamp of this user record.
     *
     * @return the creation date
     */
    public Date getCreated_at() {
        return created_at;
    }

    /**
     * Sets the creation timestamp of this user record.
     *
     * @param created_at the creation date to set
     */
    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    /**
     * Returns the last update timestamp of this user record.
     *
     * @return the last update date
     */
    public Date getUpdated_at() {
        return updated_at;
    }

    /**
     * Sets the last update timestamp of this user record.
     *
     * @param updated_at the last update date to set
     */
    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }
}
