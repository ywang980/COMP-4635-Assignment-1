import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Represents a server managing user accounts and handling client interactions.
 */
public class UserAccountServer {

    private static final String USER_ACCOUNTS_FP = "UserAccounts.txt";
    private static final String USAGE = "Usage: java UserAccountServer [port]";
    private static Map<String, User> userAccounts;

    /**
     * Static initializer block to load user accounts from file.
     */
    static {
        loadUserAccounts();
    }

    /**
     * Loads user accounts from the file into the userAccounts map.
     */
    private static void loadUserAccounts() {
        userAccounts = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(USER_ACCOUNTS_FP))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String username = parts[0].trim();
                    int lifetimeWins = Integer.parseInt(parts[1].trim());
                    userAccounts.put(username, new User(username, lifetimeWins));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves user account information to the file.
     * @param user - The User object containing account information.
     */
    private static void saveUserAccount(User user) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USER_ACCOUNTS_FP, true))) {
            writer.println(user.getUsername() + "," + user.getLifetimeWins());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if a user is already registered
     * @param username - The username to check for registration
     * @return - True if the user is registered, False if not.
     */
    public static boolean login(String username) {
        return userAccounts.containsKey(username.trim());
    }

    /**
     * Creates a new user and updates userAccounts map and file.
     * @param username - The username of the new user.
     */
    private static void createUser(String username) {
        User newUser = new User(username, 0);
        userAccounts.put(username, newUser);
        saveUserAccount(newUser);
    }

    /**
     * Handles communication with a connected client.
     * @param clientSocket - The Socket representing the connection to the client.
     */
    public static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String username = in.readLine().trim();

            if (login(username)) {
                out.println("Logging in as: " + username);
            } else {
                out.println("Creating new user: " + username);
                createUser(username);
            }
            out.println("Welcome to the server " + username + "!");
            out.println("Enter 'q' to quit.");
            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                //this area is for back and forth communication with client
                if ("q".equalsIgnoreCase(clientMessage)) {
                    out.println("Ending communication");
                    break;
                } else {
                    out.println("Server received: " + clientMessage);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Main entry point for running the UserAccountServer.
     * @param args - Command-line arguments, expects a single argument representing the port number to use.
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println(USAGE);
            System.exit(1);
        }

        int port = 0;
        ServerSocket server = null;

        try {
            port = Integer.parseInt(args[0]);
            server = new ServerSocket(port);
            System.out.println("The UserAccount server is running...");
            ExecutorService fixedThreadPool = Executors.newFixedThreadPool(20);
            while (true) {
                Socket clientSocket = server.accept();
                System.out.println("Connection from " + clientSocket.getInetAddress());
                fixedThreadPool.execute(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            System.out.println(
                    "Exception caught when trying to listen on port " + port + " or listening for a connection");
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (server != null) {
                    server.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
