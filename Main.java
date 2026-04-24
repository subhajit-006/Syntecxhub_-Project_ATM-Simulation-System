import java.util.List;
import java.util.Scanner;

/**
 * Main entry point for the ATM Simulation System.
 * Provides a console-based user interface with menu-driven operations.
 * Initializes demo accounts for testing.
 */
public class Main {
    
    private static final ATM atm = new ATM();
    private static final Scanner scanner = new Scanner(System.in);
    
    /**
     * Main method - entry point for the ATM system.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("     Welcome to ATM Simulation System    ");
        System.out.println("========================================\n");
        
        // Initialize demo accounts
        initializeDemoAccounts();
        
        // Main ATM loop
        boolean running = true;
        while (running) {
            if (!atm.isCardInserted()) {
                running = handleCardSelection();
            } else {
                running = handleSessionMenu();
            }
        }
        
        System.out.println("\nThank you for using our ATM. Goodbye!");
        scanner.close();
    }
    
    /**
     * Initializes two demo accounts for testing.
     * Account 1: Card ID "1001", PIN "1234", Initial Balance 5000
     * Account 2: Card ID "1002", PIN "5678", Initial Balance 3000
     */
    private static void initializeDemoAccounts() {
        Account account1 = new Account("1001", "John Doe", "1234", 5000.0);
        Account account2 = new Account("1002", "Jane Smith", "5678", 3000.0);
        
        atm.registerAccount(account1);
        atm.registerAccount(account2);
        
        System.out.println("Demo Accounts Initialized:");
        System.out.println("  Card ID: 1001 | Holder: John Doe | PIN: 1234 | Balance: 5000");
        System.out.println("  Card ID: 1002 | Holder: Jane Smith | PIN: 5678 | Balance: 3000");
        System.out.println();
    }
    
    /**
     * Handles card insertion and PIN verification.
     * Displays menu to insert card or exit.
     *
     * @return true to continue, false to exit
     */
    private static boolean handleCardSelection() {
        System.out.println("========== MAIN MENU ==========");
        System.out.println("1. Insert Card");
        System.out.println("2. Exit");
        System.out.print("Select an option: ");
        
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                insertCard();
                return true;
            case "2":
                return false;
            default:
                System.out.println("Invalid option. Please try again.\n");
                return true;
        }
    }
    
    /**
     * Handles card insertion process.
     * Prompts for card ID and initiates PIN entry state.
     */
    private static void insertCard() {
        System.out.print("Enter Card ID: ");
        String cardId = scanner.nextLine().trim();
        
        try {
            atm.insertCard(cardId);
            System.out.println("\nCard inserted successfully.");
            System.out.println("Welcome, " + atm.getCurrentAccount().getHolderName() + "!");
            enterPin();
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
        System.out.println();
    }
    
    /**
     * Handles PIN entry process.
     * Allows up to 3 attempts. Locks account after 3 failures.
     */
    private static void enterPin() {
        Session session = atm.getSession();
        int attempts = 0;
        final int MAX_ATTEMPTS = 3;
        
        while (attempts < MAX_ATTEMPTS && session.getCurrentState() == Session.State.PIN_ENTRY) {
            System.out.print("Enter PIN: ");
            String pin = scanner.nextLine().trim();
            
            try {
                if (session.authenticate(pin)) {
                    System.out.println("Authentication successful!\n");
                    return;
                } else {
                    attempts++;
                    int remaining = MAX_ATTEMPTS - attempts;
                    if (remaining > 0) {
                        System.out.println("Incorrect PIN. " + remaining + " attempt(s) remaining.");
                    }
                }
            } catch (IllegalStateException e) {
                System.out.println("Error: " + e.getMessage());
                break;
            }
        }
        
        if (session.getCurrentState() == Session.State.LOCKED) {
            System.out.println("Account locked due to multiple failed PIN attempts.");
        }
        System.out.println();
    }
    
    /**
     * Handles the main ATM operations menu.
     * Displays options: Balance Check, Withdraw, Deposit, Transaction History, Exit.
     *
     * @return true to continue, false to exit
     */
    private static boolean handleSessionMenu() {
        Session session = atm.getSession();
        
        // Check if session is still active
        if (!session.isActive()) {
            try {
                atm.ejectCard();
            } catch (IllegalStateException e) {
                // Already ejected
            }
            return true;
        }
        
        // Check if authenticated
        if (session.getCurrentState() != Session.State.AUTHENTICATED) {
            System.out.println("Session is no longer active.");
            try {
                atm.ejectCard();
            } catch (IllegalStateException e) {
                // Already ejected
            }
            return true;
        }
        
        System.out.println("========== ATM MENU ==========");
        System.out.println("1. Check Balance");
        System.out.println("2. Withdraw");
        System.out.println("3. Deposit");
        System.out.println("4. Transaction History");
        System.out.println("5. Exit/Eject Card");
        System.out.print("Select an option: ");
        
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                checkBalance(session);
                break;
            case "2":
                withdraw(session);
                break;
            case "3":
                deposit(session);
                break;
            case "4":
                displayTransactionHistory(session);
                break;
            case "5":
                System.out.println("Ejecting card...");
                try {
                    atm.ejectCard();
                } catch (IllegalStateException e) {
                    System.out.println("Error: " + e.getMessage());
                }
                System.out.println();
                return true;
            default:
                System.out.println("Invalid option. Please try again.\n");
        }
        
        return true;
    }
    
    /**
     * Displays the current account balance.
     *
     * @param session the current session
     */
    private static void checkBalance(Session session) {
        try {
            double balance = session.checkBalance();
            System.out.printf("Current Balance: %.2f%n%n", balance);
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * Handles withdrawal operation.
     * Validates amount and checks sufficient funds.
     *
     * @param session the current session
     */
    private static void withdraw(Session session) {
        System.out.print("Enter withdrawal amount (must be multiple of 100): ");
        
        try {
            String input = scanner.nextLine().trim();
            double amount = Double.parseDouble(input);
            
            session.withdraw(amount);
            System.out.printf("Withdrawal successful! Amount withdrawn: %.2f%n", amount);
            double newBalance = session.checkBalance();
            System.out.printf("New Balance: %.2f%n%n", newBalance);
        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter a valid number.\n");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * Handles deposit operation.
     * Validates amount (minimum 100).
     *
     * @param session the current session
     */
    private static void deposit(Session session) {
        System.out.print("Enter deposit amount (minimum 100): ");
        
        try {
            String input = scanner.nextLine().trim();
            double amount = Double.parseDouble(input);
            
            session.deposit(amount);
            System.out.printf("Deposit successful! Amount deposited: %.2f%n", amount);
            double newBalance = session.checkBalance();
            System.out.printf("New Balance: %.2f%n%n", newBalance);
        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter a valid number.\n");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * Displays the transaction history in reverse chronological order.
     *
     * @param session the current session
     */
    private static void displayTransactionHistory(Session session) {
        System.out.println("\n========== TRANSACTION HISTORY ==========");
        
        try {
            List<Account.Transaction> history = session.getTransactionHistory();
            
            if (history.isEmpty()) {
                System.out.println("No transactions found.\n");
            } else {
                for (Account.Transaction transaction : history) {
                    System.out.println(transaction);
                }
                System.out.println();
            }
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }
    }
}
