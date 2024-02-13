package DatabaseServer;

/**
* Title: COMP4635 Task 2. Basic Socket Communication. Clients and Servers
* Usage: java BasicUDPTimeServer [port]
*/

import java.net.*;
import java.io.*;
import java.util.ArrayList;

import java.util.Date;
import java.nio.file.*;
import java.util.Locale;
import java.util.Random;

public class DatabaseServer {
    private static final String USAGE = "Usage: java DatabaseServer [port]";
    protected DatagramSocket socket = null;
    protected BufferedReader in = null;
    private static ArrayList<String> data;
    private Random randomizer = new Random();

    public DatabaseServer(int port) throws IOException {
        socket = new DatagramSocket(port);
    }

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

    public String parsePacket(String command) {
        System.out.println(command);
        String commandParts[] = command.split(";", 2);
        char function = commandParts[0].toCharArray()[0];
        String word = commandParts[1];
        String result = "";

        // A;bird Add
        // B;bird Remove
        // C;bird Query
        // D;d Find with letter
        // E;10 Find with length

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

    public String findWord(String word) {

        for (String a : data) {
            if (a.trim().equalsIgnoreCase(word.trim())) {
                System.out.println(a);
                return a;
            }
        }
        return null;
    }

    public void addWord(String word) {

        System.out.println(word);

        if (findWord(word) == null) {

            System.out.println("word not in database");
            data.add(word);
        }
        updateDataBase();


    }

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
     * starts the program
     * @param args
     * @throws IOException
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


    /*
    *
    * Gets words from database
    * */
    public static void getWords() throws IOException {

        try {
            String rawString = new String(Files.readAllBytes(Paths.get("./DatabaseServer/words.txt")));
            String[] words = rawString.split("\\s");
            data = new ArrayList<String>();

            for (String a : words) {

                data.add(a);
            }
            System.out.println(data.size());
        }

        catch (IOException e) {

            System.out.println("not found");

        }
    };
}
