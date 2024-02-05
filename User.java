/**
 * Represents a user with a username and lifetime wins.
 */
public class User {
    private String username;
    private int lifetimeWins;

    /**
     * Constructs a User object with the given username and lifetime wins.
     * @param username - The username of the user.
     * @param lifetimeWins - The number of lifetime wins for the user.
     */
    public User(String username, int lifetimeWins) {
        this.username = username;
        this.lifetimeWins = lifetimeWins;
    }

    /**
     * Gets the username of the user
     * @return - The username of the user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the number of lifetime wins for the user.
     * @return - The number of lifetime wins for the user.
     */
    public int getLifetimeWins() {
        return lifetimeWins;
    }

    /**
     * Increases the lifetime wins count by one.
     */
    public void addWin() {
        lifetimeWins += 1;
    }
}
