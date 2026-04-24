/**
 * Manages a user session with the ATM.
 * Implements a finite state machine with states: PIN_ENTRY, AUTHENTICATED, LOCKED, ENDED.
 * Enforces valid state transitions and guards operations accordingly.
 */
public class Session {
    
    /**
     * Enumeration of possible session states.
     */
    public enum State {
        PIN_ENTRY,      // Initial state after card insertion
        AUTHENTICATED,  // State after successful PIN verification
        LOCKED,         // State when account is locked (3 failed PINs)
        ENDED           // State after logout/eject
    }
    
    private final Account account;
    private State currentState;
    
    /**
     * Constructs a Session for the given account.
     * Session starts in PIN_ENTRY state.
     *
     * @param account the account associated with this session
     */
    public Session(Account account) {
        this.account = account;
        this.currentState = State.PIN_ENTRY;
    }
    
    /**
     * Authenticates the user by verifying the provided PIN.
     * Transitions to AUTHENTICATED state on success.
     * Transitions to LOCKED state on 3 failed attempts.
     *
     * @param pin the PIN to verify
     * @return true if authentication successful, false otherwise
     * @throws IllegalStateException if session is not in PIN_ENTRY state or account is already locked
     */
    public boolean authenticate(String pin) {
        if (currentState != State.PIN_ENTRY) {
            throw new IllegalStateException(
                "Cannot authenticate in " + currentState + " state");
        }
        
        if (account.isLocked()) {
            currentState = State.LOCKED;
            throw new IllegalStateException(
                "Account is locked due to multiple failed PIN attempts");
        }
        
        boolean isValid = account.verifyPin(pin);
        
        if (isValid) {
            currentState = State.AUTHENTICATED;
            return true;
        } else {
            if (account.isLocked()) {
                currentState = State.LOCKED;
            }
            return false;
        }
    }
    
    /**
     * Checks the current account balance.
     * Operation only allowed in AUTHENTICATED state.
     *
     * @return the current balance
     * @throws IllegalStateException if not authenticated
     */
    public double checkBalance() {
        if (currentState != State.AUTHENTICATED) {
            throw new IllegalStateException(
                "Balance check requires AUTHENTICATED state. Current state: " + currentState);
        }
        return account.getBalance();
    }
    
    /**
     * Withdraws money from the account.
     * Operation only allowed in AUTHENTICATED state.
     * Withdrawal must be in multiples of 100 and not exceed balance.
     *
     * @param amount the amount to withdraw
     * @throws IllegalStateException if not authenticated
     * @throws IllegalArgumentException if amount is invalid or insufficient funds
     */
    public void withdraw(double amount) {
        if (currentState != State.AUTHENTICATED) {
            throw new IllegalStateException(
                "Withdrawal requires AUTHENTICATED state. Current state: " + currentState);
        }
        account.withdraw(amount);
    }
    
    /**
     * Deposits money into the account.
     * Operation only allowed in AUTHENTICATED state.
     * Minimum deposit is 100 currency units.
     *
     * @param amount the amount to deposit
     * @throws IllegalStateException if not authenticated
     * @throws IllegalArgumentException if amount is invalid
     */
    public void deposit(double amount) {
        if (currentState != State.AUTHENTICATED) {
            throw new IllegalStateException(
                "Deposit requires AUTHENTICATED state. Current state: " + currentState);
        }
        account.deposit(amount);
    }
    
    /**
     * Retrieves the transaction history for the account.
     * Operation only allowed in AUTHENTICATED state.
     *
     * @return the transaction history
     * @throws IllegalStateException if not authenticated
     */
    public java.util.List<Account.Transaction> getTransactionHistory() {
        if (currentState != State.AUTHENTICATED) {
            throw new IllegalStateException(
                "History access requires AUTHENTICATED state. Current state: " + currentState);
        }
        return account.getHistory();
    }
    
    /**
     * Ends the session and ejects the card.
     * Transitions to ENDED state.
     */
    public void end() {
        currentState = State.ENDED;
    }
    
    /**
     * Gets the current state of the session.
     *
     * @return the current state
     */
    public State getCurrentState() {
        return currentState;
    }
    
    /**
     * Gets the account holder's name.
     *
     * @return the holder name
     */
    public String getAccountHolderName() {
        return account.getHolderName();
    }
    
    /**
     * Checks if the session is active (not ended).
     *
     * @return true if session is active, false otherwise
     */
    public boolean isActive() {
        return currentState != State.ENDED;
    }
}
