import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bank account in the ATM system.
 * Stores account details, PIN, balance, and transaction history.
 * Implements security features including PIN verification and account locking.
 */
public class Account {
    
    /** Maximum number of failed PIN attempts before account locks */
    private static final int MAX_FAILED_ATTEMPTS = 3;
    
    /** Minimum deposit amount (100 currency units) */
    private static final double MIN_DEPOSIT = 100.0;
    
    /** Withdrawal must be in multiples of 100 */
    private static final double WITHDRAWAL_MULTIPLE = 100.0;
    
    private final String cardId;
    private final String holderName;
    private final String hashedPin;
    private double balance;
    private int failedAttempts;
    private boolean isLocked;
    private final List<Transaction> transactionHistory;
    
    /**
     * Constructs an Account with initial details.
     *
     * @param cardId unique card identification number
     * @param holderName name of the account holder
     * @param pin the PIN as a plain string (will be hashed)
     * @param initialBalance the initial account balance
     */
    public Account(String cardId, String holderName, String pin, double initialBalance) {
        this.cardId = cardId;
        this.holderName = holderName;
        this.hashedPin = hashPin(pin);
        this.balance = initialBalance;
        this.failedAttempts = 0;
        this.isLocked = false;
        this.transactionHistory = new ArrayList<>();
    }
    
    /**
     * Hashes a PIN using a simple masking approach.
     * In production, use BCrypt or similar.
     *
     * @param pin the plain PIN
     * @return hashed PIN
     */
    private String hashPin(String pin) {
        // Simple hashing: repeat each character and shift
        StringBuilder hashed = new StringBuilder();
        for (char c : pin.toCharArray()) {
            hashed.append((char) (c + 5)); // Simple shift cipher
        }
        return hashed.toString();
    }
    
    /**
     * Verifies if the provided PIN matches the stored PIN.
     * Increments failed attempt counter on mismatch.
     * Locks account after 3 failed attempts.
     *
     * @param pin the PIN to verify
     * @return true if PIN is correct, false otherwise
     */
    public boolean verifyPin(String pin) {
        if (isLocked) {
            return false;
        }
        
        String hashedInput = hashPin(pin);
        if (hashedInput.equals(hashedPin)) {
            failedAttempts = 0; // Reset counter on successful verification
            return true;
        } else {
            failedAttempts++;
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                isLocked = true;
            }
            return false;
        }
    }
    
    /**
     * Deposits money into the account.
     * Minimum deposit is 100 currency units.
     * Records the transaction in history.
     *
     * @param amount the amount to deposit
     * @throws IllegalArgumentException if amount is invalid
     */
    public void deposit(double amount) {
        if (amount < MIN_DEPOSIT) {
            throw new IllegalArgumentException(
                "Minimum deposit amount is " + MIN_DEPOSIT + " currency units");
        }
        
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        
        this.balance += amount;
        transactionHistory.add(new Transaction("DEPOSIT", amount, LocalDateTime.now()));
    }
    
    /**
     * Withdraws money from the account.
     * Withdrawal must be in multiples of 100.
     * Checks for sufficient funds before withdrawal.
     * Records the transaction in history.
     *
     * @param amount the amount to withdraw
     * @throws IllegalArgumentException if amount is invalid or insufficient funds
     */
    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        
        if (amount % WITHDRAWAL_MULTIPLE != 0) {
            throw new IllegalArgumentException(
                "Withdrawal amount must be in multiples of " + (int) WITHDRAWAL_MULTIPLE);
        }
        
        if (amount > balance) {
            throw new IllegalArgumentException(
                "Insufficient funds. Current balance: " + balance);
        }
        
        this.balance -= amount;
        transactionHistory.add(new Transaction("WITHDRAWAL", amount, LocalDateTime.now()));
    }
    
    /**
     * Gets the current account balance.
     *
     * @return the current balance
     */
    public double getBalance() {
        return balance;
    }
    
    /**
     * Gets the transaction history in reverse chronological order.
     *
     * @return a list of transactions, most recent first
     */
    public List<Transaction> getHistory() {
        List<Transaction> reversedHistory = new ArrayList<>(transactionHistory);
        reversedHistory.sort((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp()));
        return reversedHistory;
    }
    
    /**
     * Checks if the account is locked due to multiple failed PIN attempts.
     *
     * @return true if account is locked, false otherwise
     */
    public boolean isLocked() {
        return isLocked;
    }
    
    /**
     * Gets the card ID.
     *
     * @return the card ID
     */
    public String getCardId() {
        return cardId;
    }
    
    /**
     * Gets the account holder's name.
     *
     * @return the holder name
     */
    public String getHolderName() {
        return holderName;
    }
    
    /**
     * Gets the number of failed PIN attempts.
     *
     * @return the failed attempt count
     */
    public int getFailedAttempts() {
        return failedAttempts;
    }
    
    /**
     * Inner class representing a single transaction.
     */
    public static class Transaction {
        private final String type; // "DEPOSIT" or "WITHDRAWAL"
        private final double amount;
        private final LocalDateTime timestamp;
        
        /**
         * Constructs a Transaction.
         *
         * @param type the transaction type
         * @param amount the transaction amount
         * @param timestamp the transaction timestamp
         */
        public Transaction(String type, double amount, LocalDateTime timestamp) {
            this.type = type;
            this.amount = amount;
            this.timestamp = timestamp;
        }
        
        /**
         * Gets the transaction type.
         *
         * @return the type
         */
        public String getType() {
            return type;
        }
        
        /**
         * Gets the transaction amount.
         *
         * @return the amount
         */
        public double getAmount() {
            return amount;
        }
        
        /**
         * Gets the transaction timestamp.
         *
         * @return the timestamp
         */
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        /**
         * Returns a formatted string representation of the transaction.
         *
         * @return formatted transaction string
         */
        @Override
        public String toString() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return String.format("%s | Amount: %.2f | Time: %s", 
                type, amount, timestamp.format(formatter));
        }
    }
}
