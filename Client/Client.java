package Client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    static final String MESSAGE_END_DELIM = "*End of Message*";
    static final String host = "localhost";
    static final int port = 8080;

    public static void main(String[] args) {
        Socket clientSocket = null;
        try {
            clientSocket = new Socket(host, port);
            /*
             * Generate timeout exception if 5 seconds have elapsed since
             * request and no response received
             */
            clientSocket.setSoTimeout(5000);

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintStream out = new PrintStream(clientSocket.getOutputStream());
            System.out.println("Connected!");

            Scanner scanner = new Scanner(System.in);
            String clientInput;

            /*
             * Read server response, send request, repeat.
             * Request is a single line command.
             */

            do {
                printServerOutput(in);
                clientInput = scanner.nextLine();
                out.println(clientInput);
            } while (!clientInput.equals("*Exit*"));

        } catch (IOException e) {
            System.err.println("Error: could not communicate with server");
        }
    }

    /*
     * Parse the server response using a string builder.
     * The server response is a string terminated with an exit line:
     * "*End of Message*".
     */
    private static void printServerOutput(BufferedReader in) {
        StringBuilder stringBuilder = new StringBuilder();
        String serverOutputLine;
        try {
            serverOutputLine = in.readLine();
            while (serverOutputLine != null && !serverOutputLine.equals(MESSAGE_END_DELIM)) {
                stringBuilder.append(serverOutputLine).append("\n");
                serverOutputLine = in.readLine();
            }
            System.out.println(stringBuilder.toString());
        } catch (IOException e) {
            System.err.println("Error: could not print server output. Exiting");
            System.exit(0);
            return;

        }
    }
}