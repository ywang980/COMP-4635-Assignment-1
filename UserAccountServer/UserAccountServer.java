package UserAccountServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Represents a server managing user accounts and handling client interactions.
 */
public class UserAccountServer {

    private static final String USER_DATA_DIR = "UserAccountServer/UserData";
    private static final Integer THREAD_COUNT = 20;
    private static List<String> userAccounts;
    private static Set<String> loggedInUsers;

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
        userAccounts = new ArrayList<>();
        loggedInUsers = new HashSet<>();

        File directory = new File(USER_DATA_DIR);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".txt")) {
                    userAccounts.add(file.getName().replace(".txt",""));
                }
            }
        }
    }

    /**
     * Checks if a user is already registered
     * @param username - The username to check for registration
     * @return - 1 if the user is registered and not currently logged in, 2 if the user is not registered and
     * not logged in, and 0 if the user is not logged in and not registered.
     */
    public static synchronized int login(String username) {
        if (userAccounts.contains(username.trim()) && !loggedInUsers.contains(username.trim())) {
            loggedInUsers.add(username);
            return 1;
        } else if (!userAccounts.contains(username.trim()) && !loggedInUsers.contains(username.trim())) {
            return 2;
        } else {
            return 0;
        }
    }

    private static synchronized int logout(String username) {
        if (loggedInUsers.contains(username.trim())) {
            loggedInUsers.remove(username.trim());
            return 1;
        } else {
            return 0;
        }
    }

    private static void handleConnection(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String input = in.readLine();
            System.out.println(input);
            String[] parts = input.split(";");
            if (parts.length == 2) {
                String operation = parts[0].trim();
                String argument = parts[1].trim();
                int result = switch (operation) {
                    case "login" -> login(argument);
                    case "logout" -> logout(argument);
                    default -> 0;
                };

                out.println(result);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Main entry point for running the UserAccountServer.
     */
    public static void main(String[] args) {

        int port = 8081;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("UserAccountServer is running...");
            ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Connection established with game server.");
                handleConnection(socket);
                threadPool.submit(() -> handleConnection(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}