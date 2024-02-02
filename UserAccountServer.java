import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserAccountServer {

    private static final String USERNAMES_FP = "usernames.txt";
    private static final String USAGE = "Usage: java UserAccountServer [port]";
    private static List<String> existingUserNames;

    static {
        loadUsernames();
    }

    private static void loadUsernames() {
        existingUserNames = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(USERNAMES_FP))) {
            String line;
            while ((line = reader.readLine()) != null) {
                existingUserNames.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean login(String username) {
        return existingUserNames.contains(username.trim());
    }

    private static void updateUsernameFile(String newUsername) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERNAMES_FP, true))) {
            writer.println(newUsername);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String username = in.readLine().trim();

            if (login(username)) {
                out.println("Logging in as: " + username);
            } else {
                out.println("Creating new user: " + username);
                existingUserNames.add(username);
                updateUsernameFile(username);
            }
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
