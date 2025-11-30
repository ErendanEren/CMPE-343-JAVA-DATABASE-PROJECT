package models;

import util.ConsoleUI;
import java.util.Scanner;
import dao.ContactSearchDAO;
import java.sql.Connection;
import java.util.List;

/**
 * Represents the "tester".
 * This class extends the {@link User} class to gain certain functions.
 * For the tester who has the read-only access.
 * <p>
 * Testers are allowed list, search, and sort contacts but cannot add, update, or delete them.
 * Uses {@link ContactSearchDAO} to perform database searches securely
 * </p>
 * @author Arda Dulger
 */

public class Tester extends User {
    private ContactSearchDAO searchDAO;


    public Tester(int userId, String username, String name, String surname, Connection connection) {
        super();

        this.setUserId(userId);
        this.setUsername(username);
        this.setName(name);
        this.setSurname(surname);
        this.setRole("Tester");

        this.searchDAO = new ContactSearchDAO(connection);
    }

    @Override
    public void showMenu(Scanner scanner) {
        boolean running = true;

        while (running) {
            String choice = ConsoleUI.showMenu(
                    ConsoleUI.BLUE_BOLD + "Tester Menu" + ConsoleUI.RESET,
                    new String[]{
                            "1) Change password",
                            "2) List all contacts",
                            "3) Search contacts",
                            "4) Sort contacts",
                            "",
                            "0) Logout"
                    },
                    scanner
            );

            switch (choice) {
                case "1" :
                    changePassword(scanner);
                    break;

                case "2" :
                    listAllContacts(scanner);
                    break;

                case "3" :
                    searchContacts(scanner);
                    break;

                case "4" :
                    sortContacts(scanner);
                    break;

                case "0" :
                    running = false;
                    break;

                default :
                    ConsoleUI.printInvalidChoice();
                    break;
            }
        }
    }


    protected void changePassword(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println("Change password (Tester)...");
        ConsoleUI.pause();
    }

    protected void listAllContacts(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println("List all contacts (Tester)...");
        ConsoleUI.pause();
    }

    /**
     * Performs various person search operations and allows viewing the search sub-menu.
     * <p>
     * This method allows users to perform searches without having to return to the main menu.
     * Two types of search criteria can be specified.
     * <ul>
     * <li><b>Single-Field Search:</b> Search by First Name, Last Name, or Phone Number.</li>
     * <li><b>Multi-Field Search:</b> Advanced search combinations (e.g., Name + Birth Month).</li>
     * </ul>
     * </p>
     * <p>
     * Before sending a query to the database via Dao, it performs <b>input validation</b>
     * For example, checking if the entered month is between 1-12 or if the entered phone number is a digit.
     * @param scanner {@code Scanner } object is used to receive input.
     * @author Arda Dulger
     */
    protected void searchContacts(Scanner scanner) {
        boolean searching = true;
        while (searching) {
            ConsoleUI.clearConsole();

            System.out.println(ConsoleUI.YELLOW_BOLD + "=== Search Contacts (Tester) ===" + ConsoleUI.RESET);
            System.out.println("1) Search by First Name (Partial)");
            System.out.println("2) Search by Last Name (Partial)");
            System.out.println("3) Search by Phone Number (Validated)");
            System.out.println("4) [Multi] Name AND Birth Month");
            System.out.println("5) [Multi] Lastname AND City/Address");
            System.out.println("6) [Multi] Phone Part AND Email Part");
            System.out.println("0) Back to Tester Menu");
            System.out.print("Select search type: ");

            String choice = scanner.nextLine();
            List<Contact> results = null;

            switch (choice) {
                case "1":

                    System.out.print("Enter first name (or part of it): ");
                    String firstNameQuery = scanner.nextLine();
                    results = searchDAO.searchByFirstName(firstNameQuery);


                case "2":

                    System.out.print("Enter last name (or part of it): ");
                    String lastNameQuery = scanner.nextLine();
                    results = searchDAO.searchByLastName(lastNameQuery);
                    break;

                case "3":

                    System.out.print("Enter phone number (digits only): ");
                    String phone = scanner.nextLine();
                    if (searchDAO.isValidPhoneNumber(phone)) {
                        results = searchDAO.searchByPhoneNumber(phone);
                    } else {
                        System.out.println(ConsoleUI.RED + "Invalid format! Phone should contain only digits/spaces." + ConsoleUI.RESET);
                    }
                    break;

                case "4":

                    System.out.print("Enter name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter birth month (1-12): ");
                    try {
                        int month = Integer.parseInt(scanner.nextLine());
                        if (searchDAO.isValidMonth(month)) {
                            results = searchDAO.searchByNameAndBirthMonth(name, month);
                        } else {
                            System.out.println(ConsoleUI.RED + "Invalid month! Please enter between 1-12." + ConsoleUI.RESET);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(ConsoleUI.RED + "Invalid input! Please enter a number for month." + ConsoleUI.RESET);
                    }
                    break;

                case "5":

                    System.out.print("Enter lastname: ");
                    String lname = scanner.nextLine();
                    System.out.print("Enter city/address part: ");
                    String city = scanner.nextLine();
                    results = searchDAO.searchByLastnameAndCity(lname, city);
                    break;

                case "6":

                    System.out.print("Enter phone part (e.g. 555): ");
                    String phonePart = scanner.nextLine();
                    System.out.print("Enter email part (e.g. gmail): ");
                    String emailPart = scanner.nextLine();
                    results = searchDAO.searchByPhonePartAndEmailPart(phonePart, emailPart);
                    break;

                case "0":
                    searching = false;
                    break;

                default:
                    ConsoleUI.printInvalidChoice();
                    break;
            }

            //
            if (results != null) {
                if (results.isEmpty()) {
                    System.out.println(ConsoleUI.RED + "No contacts found matching criteria." + ConsoleUI.RESET);
                } else {
                    System.out.println(ConsoleUI.GREEN + "Found " + results.size() + " record(s):" + ConsoleUI.RESET);
                    printSearchResults(results);
                }
                ConsoleUI.pause();
            } else if (!choice.equals("0")) {

                ConsoleUI.pause();
            }
        }
    }

    /**
     * Prints the list of found people in tabular form.
     * <p>
     * This helper allows to iterate and display the contact list.
     * key attributes (ID, Name, Surname, Phone, Email) in arranged columns using {@code printf}.
     * </p>
     * @param contacts A {@code List} of {@link Contact} objects retrieved from the database.
     * If the list empty, it will be not printed.(handled by caller)
     * @author Arda Dulger
     */
    private void printSearchResults(List<Contact> contacts) {
        System.out.println("-------------------------------------------------------------------------");
        System.out.printf("%-5s %-15s %-15s %-15s %-20s\n", "ID", "First Name", "Last Name", "Phone", "Email");
        System.out.println("-------------------------------------------------------------------------");
        for (Contact c : contacts) {
            System.out.printf("%-5d %-15s %-15s %-15s %-20s\n",
                    c.getContactId(), c.getName(), c.getSurname(), c.getPrimaryPhone(), c.getEmail());
        }
        System.out.println("-------------------------------------------------------------------------");
    }
    protected void sortContacts(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println("Sort contacts (Tester)...");
        ConsoleUI.pause();
    }
}
