public class User {
    private String username;
    private int lifetimeWins;

    public User(String username, int lifetimeWins) {
        this.username = username;
        this.lifetimeWins = lifetimeWins;
    }

    public String getUsername() {
        return username;
    }

    public int getLifetimeWins() {
        return lifetimeWins;
    }

    public void addWin() {
        lifetimeWins += 1;
    }
}
