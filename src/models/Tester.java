package models;

import util.ConsoleUI;
import java.util.Scanner;
import dao.ContactSearchDAO;
import java.sql.Connection;
import java.util.List;

public class Tester extends User { // SENİN ALANIN: Veritabanı işlemleri için DAO nesnesi
    private ContactSearchDAO searchDAO;

    // Constructor güncellendi: Connection alıp DAO'yu başlatıyoruz
    public Tester(int userId, String username, String name, String surname, Connection connection) {
        super();
        // User sınıfında parametreli constructor olmadığı için
        // verileri Setter metodları ile yüklüyoruz:
        this.setUserId(userId);
        this.setUsername(username);
        this.setName(name);
        this.setSurname(surname);
        this.setRole("Tester"); // Rolü elle sabitliyoruz

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
                case "1" -> changePassword(scanner);
                case "2" -> listAllContacts(scanner);
                case "3" -> searchContacts(scanner);
                case "4" -> sortContacts(scanner);
                case "0" -> running = false;
                default -> ConsoleUI.printInvalidChoice();
            }
        }
    }

    // Junior / Senior da kullanacağı için protected ve imzaları aynı
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

    protected void searchContacts(Scanner scanner) {
        boolean searching = true;
        while (searching) {
            ConsoleUI.clearConsole();
            // Proje isterlerine göre: 3 tekil, 3 çoklu arama seçeneği [cite: 29]
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
                case "1" -> {
                    // Tekil Arama 1: İsim
                    System.out.print("Enter first name (or part of it): ");
                    String query = scanner.nextLine();
                    results = searchDAO.searchByFirstName(query);
                }
                case "2" -> {
                    // Tekil Arama 2: Soyisim
                    System.out.print("Enter last name (or part of it): ");
                    String query = scanner.nextLine();
                    results = searchDAO.searchByLastName(query);
                }
                case "3" -> {
                    // Tekil Arama 3: Telefon (Validasyonlu) [cite: 31, 38]
                    System.out.print("Enter phone number (digits only): ");
                    String phone = scanner.nextLine();

                    // VALIDATION KONTROLÜ: Veri mantıklı mı? [cite: 28, 51]
                    if (searchDAO.isValidPhoneNumber(phone)) {
                        results = searchDAO.searchByPhoneNumber(phone);
                    } else {
                        System.out.println(ConsoleUI.RED + "Invalid format! Phone should contain only digits/spaces." + ConsoleUI.RESET);
                    }
                }
                case "4" -> {
                    // Çoklu Arama 1: İsim ve Ay [cite: 33]
                    System.out.print("Enter name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter birth month (1-12): ");

                    // VALIDATION: Ay girişi sayısal mı ve 1-12 arasında mı? [cite: 38]
                    try {
                        int month = Integer.parseInt(scanner.nextLine());
                        if (searchDAO.isValidMonth(month)) {
                            results = searchDAO.searchByNameAndBirthMonth(name, month);
                        } else {
                            System.out.println(ConsoleUI.RED + "Invalid month! Please enter between 1-12." + ConsoleUI.RESET);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(ConsoleUI.RED + "Invalid input! Please enter a number for month." + ConsoleUI.RESET); //
                    }
                }
                case "5" -> {
                    // Çoklu Arama 2: Soyisim ve Şehir [cite: 32]
                    System.out.print("Enter lastname: ");
                    String lname = scanner.nextLine();
                    System.out.print("Enter city/address part: ");
                    String city = scanner.nextLine();
                    results = searchDAO.searchByLastnameAndCity(lname, city);
                }
                case "6" -> {
                    // Çoklu Arama 3: Telefon parçası ve Email parçası [cite: 33]
                    System.out.print("Enter phone part (e.g. 555): ");
                    String phonePart = scanner.nextLine();
                    System.out.print("Enter email part (e.g. gmail): ");
                    String emailPart = scanner.nextLine();
                    results = searchDAO.searchByPhonePartAndEmailPart(phonePart, emailPart);
                }
                case "0" -> searching = false;
                default -> ConsoleUI.printInvalidChoice();
            }

            // Sonuçları Ekrana Basma İşlemi
            if (results != null) {
                if (results.isEmpty()) {
                    System.out.println(ConsoleUI.RED + "No contacts found matching criteria." + ConsoleUI.RESET);
                } else {
                    System.out.println(ConsoleUI.GREEN + "Found " + results.size() + " record(s):" + ConsoleUI.RESET);
                    printSearchResults(results);
                }
                ConsoleUI.pause();
            } else if (!choice.equals("0")) {
                // Hata mesajı varsa (validasyon hatası gibi) beklet
                ConsoleUI.pause();
            }
        }
    }
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
