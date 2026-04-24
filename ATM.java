import java.util.HashMap;
import java.util.Map;

/**
 * Represents an ATM machine in the banking system.
 * Manages account registry and session creation.
 * Handles card insertion/ejection and session management.
 */
public class ATM {
    
    private final Map<String, Account> accountRegistry;
    private Account currentAccount;
    private Session currentSession;
    
    /**
     * Constructs an ATM with an empty account registry.
     */
    public ATM() {
        this.accountRegistry = new HashMap<>();
        this.currentAccount = null;
        this.currentSession = null;
    }
    
    /**
     * Registers a new account in the ATM system.
     * Account will be stored with cardId as key.
     *
     * @param account the account to register
     * @throws IllegalArgumentException if account with same cardId already exists
     */
    public void registerAccount(Account account) {
        if (accountRegistry.containsKey(account.getCardId())) {
            throw new IllegalArgumentException(
                "Account with card ID " + account.getCardId() + " already exists");
        }
        accountRegistry.put(account.getCardId(), account);
    }
    
    /**
     * Inserts a card into the ATM and initializes a session.
     * Creates a new session in PIN_ENTRY state.
     *
     * @param cardId the card ID being inserted
     * @throws IllegalStateException if a card is already inserted
     * @throws IllegalArgumentException if card is not registered
     */
    public void insertCard(String cardId) {
        if (currentAccount != null) {
            throw new IllegalStateException("A card is already inserted. Eject it first.");
        }
        
        if (!accountRegistry.containsKey(cardId)) {
            throw new IllegalArgumentException("Card ID not found in the system");
        }
        
        currentAccount = accountRegistry.get(cardId);
        currentSession = new Session(currentAccount);
    }
    
    /**
     * Ejects the current card from the ATM.
     * Ends the current session.
     *
     * @throws IllegalStateException if no card is inserted
     */
    public void ejectCard() {
        if (currentAccount == null) {
            throw new IllegalStateException("No card is currently inserted");
        }
        
        if (currentSession != null) {
            currentSession.end();
        }
        
        currentAccount = null;
        currentSession = null;
    }
    
    /**
     * Gets the current session.
     * Used to perform ATM operations like authenticate, withdraw, etc.
     *
     * @return the current session
     * @throws IllegalStateException if no card is inserted
     */
    public Session getSession() {
        if (currentSession == null) {
            throw new IllegalStateException("No card is inserted. Please insert a card first.");
        }
        return currentSession;
    }
    
    /**
     * Gets the currently inserted account.
     *
     * @return the current account, or null if no card is inserted
     */
    public Account getCurrentAccount() {
        return currentAccount;
    }
    
    /**
     * Checks if a card is currently inserted.
     *
     * @return true if a card is inserted, false otherwise
     */
    public boolean isCardInserted() {
        return currentAccount != null;
    }
}
