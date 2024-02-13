package UserAccountServer;

import GameServer.Constants;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Represents a server managing user accounts and handling client interactions.
 */
public class UserAccountServer {

    private static final String USER_DATA_DIR = "./UserData/";
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
    private static synchronized int login(String username) {
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

    private static synchronized String load(String username) throws IOException {
        String filePath = USER_DATA_DIR + username + ".txt";
        File userDatafile = new File(filePath);
        try {
            if (!userDatafile.exists()) {
                userDatafile.createNewFile();
                UserData userData = new UserData(username, true);
                return userData.getUserDataString();
            }
        } catch (IOException e) {
            throw new IOException(Constants.CANT_CREATE_USER_FILE);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(userDatafile))) {
            StringBuilder userDataBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                userDataBuilder.append(line).append("\n");
            }
            return userDataBuilder.toString();
        }
    }

    private static synchronized int save(String username, String data) {
        File userDataFile = new File(Constants.USER_DATA_DIRECTORY +
                username + ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(userDataFile))) {
            writer.write(data);
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static void handleConnection(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            int result = -1;
            String stringResult = "";

            String input = in.readLine();
            String[] parts = input.split(";");
            if (parts.length == 2) {
                String operation = parts[0].trim();
                String username = parts[1].trim();
                switch (operation) {
                    case "login" -> result = login(username);
                    case "logout" -> result = logout(username);
                    case "load" -> stringResult = load(username);
                    case "save" -> {
                        StringBuilder dataBuilder = new StringBuilder();
                        String line;
                        while ((line = in.readLine()) != null && !line.isEmpty()) {
                            dataBuilder.append(line).append("\n");
                        }
                        System.out.println("Received line: testing");
                        result = save(username, dataBuilder.toString());
                    }
                }
                if (result != -1) {
                    out.write(String.valueOf(result));
                } else {
                    out.write(stringResult);
                }
                out.newLine();
                out.flush();
            }
        } catch (SocketException e) {
          System.out.println("Connection closed");
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
                threadPool.submit(() -> handleConnection(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
