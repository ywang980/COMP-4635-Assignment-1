package DatabaseServer;

/**
* Title: COMP4635 Task 2. Basic Socket Communication. Clients and Servers
* Usage: java BasicUDPTimeServer [port]
*/

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.nio.file.*;
import java.util.Random;

/**
 * Represents a database server that listens for incoming requests on a specified port.
 */
public class DatabaseServer {
    private static final String USAGE = "Usage: java DatabaseServer [port]";
    protected DatagramSocket socket = null;
    private static ArrayList<String> data;
    private Random randomizer = new Random();

    /**
     * Constructs a DatabaseServer object that listens on the specified port.
     * @param port - The port number to listen on.
     * @throws IOException - If an I/O error occurs while creating the DatagramSocket.
     */
    public DatabaseServer(int port) throws IOException {
        socket = new DatagramSocket(port);
    }

    /**
     * Listens for incoming requests and serves them indefinitely.
     */
    public void serve() {
        while (true) {
            try {
                System.out.println("Listening for incoming requests ...");
                byte[] inputbuf = new byte[1000];
                byte[] outputbuf = new byte[1000];

                DatagramPacket udpRequestPacket = new DatagramPacket(inputbuf, inputbuf.length);
                socket.receive(udpRequestPacket);

                String dataString = parsePacket(
                        new String(udpRequestPacket.getData(), 0, udpRequestPacket.getLength()));
                outputbuf = dataString.getBytes();

                System.out.println(dataString);

                InetAddress address = udpRequestPacket.getAddress();
                int port = udpRequestPacket.getPort();
                DatagramPacket udpReplyPacket = new DatagramPacket(outputbuf, outputbuf.length, address, port);
                socket.send(udpReplyPacket);
            } catch (SocketException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Parses the incoming packet command and performs corresponding actions.
     * @param command - The command received in the packet.
     * @return - The result of the command processing.
     */
    public String parsePacket(String command) {
        System.out.println(command);
        String commandParts[] = command.split(";", 2);
        char function = commandParts[0].toCharArray()[0];
        String word = commandParts[1];
        String result = "";

        switch (function) {
            case 'A':
                addWord(word);
                result = "Word added to database";
                break;
            case 'B':
                result = "Word removed from database";
                removeWord(word);
                break;
            case 'C':
                if (findWord(word) != null) {
                    result = "1";
                }
                else {
                    result = "0";
                }
                break;
            case 'D':
                result = randomWord(word);
                break;
            case 'E':
                result = randomWordLength(word);
                break;
            default:
                result = "error detected";
        }
        return result;
    }

    /**
     * Removes the specified word from the database.
     * @param word - The word to be removed.
     */
    public void removeWord(String word) {
        System.out.println(word);

        for (String a : data) {
            if (a.trim().equalsIgnoreCase(word.trim())) {
                System.out.println(a);
                data.remove(word.toLowerCase());
                break;
            }
        }
        updateDataBase();
    }

    /**
     * Searches for the specified word in the database.
     * @param word The word to search for.
     * @return The word if found in the database, otherwise null.
     */
    public String findWord(String word) {

        for (String a : data) {
            if (a.trim().equalsIgnoreCase(word.trim())) {
                System.out.println(a);
                return a;
            }
        }
        return null;
    }

    /**
     * Adds the specified word to the database if it does not already exist.
     * @param word The word to add to the database.
     */
    public void addWord(String word) {

        System.out.println(word);

        if (findWord(word) == null) {

            System.out.println("word not in database");
            data.add(word);
        }
        updateDataBase();
    }

    /**
     * Updates the database by writing the current data to a file.
     */
    public void updateDataBase() {
        String filename = "./words.txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename, false))) {
            for (String word : data) {
                writer.println(word);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates a random word from the database that contains the specified substring.
     * @param a - The substring to match in the generated word.
     * @return - A random word containing the specified substring, or an empty string if no such word is found.
     */
    public String randomWord(String a) {
        String word = "";

        ArrayList<String> filteredwords = data
                .stream()
                .filter(c -> c.contains(a))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        if (!filteredwords.isEmpty()) {

            word = filteredwords.get(randomizer.nextInt(filteredwords.size()));
        }
        return word;
    }

    /**
     * Generates a random word from the database with the specified length.
     * @param a - The length of the word to generate.
     * @return - A random word with the specified length, or an empty string if no such word is found.
     */
    public String randomWordLength(String a) {

        String word = "";

        int length = Integer.parseInt(a);

        ArrayList<String> filteredwords = data
                .stream()
                .filter(c -> c.length() == length)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        if (!filteredwords.isEmpty()) {

            word = filteredwords.get(randomizer.nextInt(filteredwords.size()));
        }

        return word;
    }

    /**
     * Starts the program.
     * @param args - The command line arguments. The first argument should be the port number.
     * @throws IOException - If an I/O error occurs.
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println(USAGE);
            System.exit(1);
        }

        int port = 0;
        DatabaseServer server = null;
        try {
            port = Integer.parseInt(args[0]);
            server = new DatabaseServer(port);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number: " + port + ".");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + port);
            System.out.println(e.getMessage());
        }

        getWords();

        server.serve();
        server.socket.close();
    }


    /**
     * Retrieves words from the database file.
     */
    public static void getWords() {
        try {
            String rawString = new String(Files.readAllBytes(Paths.get("./DatabaseServer/words.txt")));
            String[] words = rawString.split("\\s");
            data = new ArrayList<String>();

            for (String a : words) {

                data.add(a);
            }
            System.out.println(data.size());

        } catch (IOException e) {
            System.out.println("not found");
        }
    }
}
