package GameServer;

import UserAccountServer.UserData;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Game {

    private static DatagramSocket wordSocket;
    private static int wordServerPort;

    public static void main(String[] args) {

        if (args.length != 3) {
            System.out.println(Constants.USAGE);
            System.exit(1);
        }
        int port = Integer.parseInt(args[1]);
        wordServerPort = Integer.parseInt(args[2]);

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            wordSocket = new DatagramSocket();
            wordSocket.setSoTimeout(2000);
            ExecutorService fixedThreadPool = Executors.newFixedThreadPool(20);
            System.out.println("Listening for incoming requests...");

            while (true) {
                fixedThreadPool.execute(new newGameHandler(serverSocket.accept()));
            }
        } catch (SocketException e) {
            System.out.println("Could not create socket to word database microservice.");
        } catch (IOException e) {
            System.out.println("Could not create server.");
        }
    }

    private static class newGameHandler implements Runnable {
        private Socket clientSocket;

        public newGameHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                handleClient(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error while attempting to close socket.\n" +
                        e.getMessage() + "\n");
                e.printStackTrace();
            }
        }

        private static void handleClient(Socket clientSocket) throws IOException {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintStream out = new PrintStream(clientSocket.getOutputStream());
            System.out.println("Client Connected");

            try {
                String username = validateUsername(clientSocket, in, out);
                UserData userData = validateUserData(out, username);

                if (userData != null) {
                    serveUser(in, out, userData);
                }
            } catch (SocketException e) {
                System.out.println("Lost connection with client.");
            } catch (IOException e) {
                System.out.println("Error: could not communicate with client.");
            } finally {
                in.close();
                out.close();
                System.out.println("Connection successfully closed.");
            }
        }

        private static String validateUsername(Socket clientSocket,
                BufferedReader in, PrintStream out)
                throws IOException {

            String username = "";
            boolean validUserName = false;

            while (!validUserName) {
                username = promptUserName(clientSocket, in, out);
                try {
                    validUserName = checkValidUser(username, out);
                } catch (Exceptions.DuplicateLoginException e) {
                    out.println("Error:" + e.getMessage());
                    out.println("Try again.");
                }
            }
            return username;
        }

        private static String promptUserName(Socket clientSocket,
                BufferedReader in, PrintStream out) throws IOException {

            out.println("Welcome to the crossword puzzle game. Please enter your username."
                    + Constants.MESSAGE_END_DELIM);
            String username = in.readLine();

            if (username == null) {
                throw new IOException(Constants.NO_CLIENT_INPUT);
            }
            return username;
        }

        private static boolean checkValidUser(String username, PrintStream out)
                throws Exceptions.DuplicateLoginException {

            try (Socket accountSocket = new Socket("localhost", Constants.UAS_PORT)) {
                accountSocket.setSoTimeout(5000);
                BufferedWriter dataOut = new BufferedWriter(new OutputStreamWriter(accountSocket.getOutputStream()));

                String output = "login;" + username.trim();
                dataOut.write(output);
                dataOut.newLine();
                dataOut.flush();

                BufferedReader in = new BufferedReader(new InputStreamReader(accountSocket.getInputStream()));
                int loginResult = Integer.parseInt(in.readLine());
                if (loginResult == 0) {
                    throw new Exceptions().new DuplicateLoginException(Constants.DUPLICATE_LOGIN);
                } else {
                    if (loginResult == 1) {
                        out.println("Logging in as: " + username);
                    } else {
                        out.println("Creating new account: " + username);
                    }
                    return true;
                }
            } catch (SocketTimeoutException e) {
                return false;
            } catch (IOException e) {
                return false;
            }
        }

        private static UserData validateUserData(PrintStream out, String username) {
            UserData userData = null;
            try (Socket accountSocket = new Socket("localhost", Constants.UAS_PORT)) {
                accountSocket.setSoTimeout(5000);
                BufferedWriter dataOut = new BufferedWriter(new OutputStreamWriter(accountSocket.getOutputStream()));

                String output = "load;" + username;
                dataOut.write(output);
                dataOut.newLine();
                dataOut.flush();

                BufferedReader in = new BufferedReader(new InputStreamReader(accountSocket.getInputStream()));
                StringBuilder userDataBuilder = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    userDataBuilder.append(line).append("\n");
                }
                userData = new UserData(userDataBuilder.toString());
            }
            catch (IOException e) {
                out.println("Error: Could not communicate with user account server.");
                e.printStackTrace();
            }
            return userData;
        }

        private static void logoutUser(String username, PrintStream out) {
            try (Socket accountSocket = new Socket("localhost", Constants.UAS_PORT)) {
                accountSocket.setSoTimeout(5000);
                BufferedWriter dataOut = new BufferedWriter(new OutputStreamWriter(accountSocket.getOutputStream()));

                String output = "logout;" + username.trim();
                dataOut.write(output);
                dataOut.newLine();
                dataOut.flush();

                BufferedReader inLogout = new BufferedReader(new InputStreamReader(accountSocket.getInputStream()));
                int logoutResult = Integer.parseInt(inLogout.readLine());
                if (logoutResult == 0) {
                    out.println("Failed to log out user: " + username);
                } else {
                    out.println("Logging out: " + username);
                }
            } catch (IOException e) {
                out.println("Error: Could not communicate with user account server.");
                e.printStackTrace();
            }
        }

        private static void serveUser(BufferedReader in, PrintStream out, UserData userData)
                throws IOException {
            String input;

            try {
                do {
                    out.println(Constants.USER_MENU + Constants.MESSAGE_END_DELIM);
                    input = in.readLine().trim();
                    try {
                        if (input.equals(Constants.EXIT_CODE)) {
                            logoutUser(userData.getUsername(), out);
                            saveGame(userData);
                            break;
                        }
                        boolean existingGame = userData.getGameState().getState()
                                .equals(Constants.PLAY_STATE);

                        processUserInput(in, out, userData, input, existingGame);
                    } catch (SocketTimeoutException e) {
                        handleError(out, userData, e);
                    } catch (IOException e) {
                        handleError(out, userData, e);
                    }
                } while (true);
            } catch (IOException e) {
                System.out.println("Error: could not communicate with client.");
            }
        }

        private static void processUserInput(BufferedReader in, PrintStream out,
                UserData userData, String input, boolean existingGame) throws IOException {
            String[] tokenizedInput = input.split(";");
            if (tokenizedInput.length <= 1)
                throw new IOException(Constants.INVALID_COMMAND_SYNTAX);

            String command = tokenizedInput[0];
            String argument = tokenizedInput[1];

            switch (command) {
                case "Add": {
                    System.out.println(contactDatabase('A', argument));
                    break;
                }
                case "Remove": {
                    System.out.println(contactDatabase('B', argument));
                    break;
                }
                case "New Game": {
                    try {
                        int wordCount = Integer.parseInt(argument);
                        if (wordCount < 2 || wordCount > Constants.MAX_WORD_COUNT) {
                            throw new IOException(Constants.WORD_COUNT_NOT_IN_RANGE);
                        }

                        createNewGame(userData, wordCount);
                        playGame(in, out, userData);
                    } catch (NumberFormatException e) {
                        throw new IOException(Constants.INVALID_WORD_COUNT);
                    }
                    break;
                }
                case "Continue": {
                    if (existingGame) {
                        playGame(in, out, userData);
                    } else {
                        throw new IOException(Constants.NO_EXISTING_GAME);
                    }
                    break;
                }
                default:
                    throw new IOException(Constants.INVALID_COMMAND_SYNTAX);
            }
        }

        private static String contactDatabase(char command, String payload) {
            try {
                String request = String.valueOf(command) + ";" + payload;
                byte[] requestBuf = new byte[Constants.BUFFER_LIMIT];
                requestBuf = request.getBytes();

                InetAddress address = InetAddress.getByName("localhost");
                DatagramPacket packet = new DatagramPacket(requestBuf, requestBuf.length, address, wordServerPort);
                // Set timeout: value
                wordSocket.send(packet);

                byte[] responseBuf = new byte[Constants.BUFFER_LIMIT];
                packet = new DatagramPacket(responseBuf, responseBuf.length);
                // Set timeout: 0
                wordSocket.receive(packet);

                String word = new String(packet.getData(), 0, packet.getLength());
                System.out.println(word);
                return word;
            }

            catch (UnknownHostException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private static void createNewGame(UserData userData, int wordCount) throws IOException {
            String words[] = generateWordList(wordCount);
            int attempts = Math.min(words.length * 2, 15);
            userData.setGameState(new GameState(attempts, words));
            saveGame(userData);
        }

        private static String[] generateWordList(int wordCount) {
            while (true) {
                ArrayList<String> wordsList = new ArrayList<>();
                String stem = fetchStem(wordCount - 1);
                wordsList.add(stem);

                ArrayList<Integer> leafIndicesList = generateLeafIndices(wordCount, stem);
                if (populateLeaves(leafIndicesList, stem, wordsList)) {
                    return wordsList.toArray(new String[0]);
                }
            }
        }

        private static String fetchStem(int a) {
            return contactDatabase('E', String.valueOf(a));
        }

        private static String fetchLeaf(char matchingCharacter) {
            String charToString = String.valueOf(matchingCharacter);
            return contactDatabase('D', charToString);
        }

        private static ArrayList<Integer> generateLeafIndices(int wordCount, String stem) {
            Set<Integer> leafIndices = new HashSet<>();
            while (leafIndices.size() < wordCount - 1) {
                leafIndices.add(new Random().nextInt(stem.length()));
            }
            return new ArrayList<>(leafIndices);
        }

        private static boolean populateLeaves(ArrayList<Integer> leafIndicesList, String stem,
                ArrayList<String> wordsList) {

            String leaf = "";
            int consecutiveDuplicateLeaf = 0;

            for (int i = 0; i < leafIndicesList.size(); i++) {
                int index = leafIndicesList.get(i).intValue();
                char connectingCharacter = stem.toCharArray()[index];

                do {
                    leaf = fetchLeaf(connectingCharacter);
                    consecutiveDuplicateLeaf++;

                    if (consecutiveDuplicateLeaf > 5 || leaf.equals("")) {
                        return false;
                    }
                } while (wordsList.contains(leaf));

                wordsList.add(leaf);
                consecutiveDuplicateLeaf = 0;
            }
            return true;
        }

        private static void playGame(BufferedReader in, PrintStream out, UserData userData)
                throws IOException {
            GameState gameState = userData.getGameState();
            gameState.setState(Constants.PLAY_STATE);

            String input;
            int gameOver = -1;

            do {
                try {
                    out.print("\n" + gameState.getPuzzle().getPuzzleString());
                    input = getValidInput(in, out, gameState);
                    gameOver = processGameInput(in, out, gameState, input);
                } catch (SocketTimeoutException e) {
                    handleError(out, userData, e);
                }
            } while (gameOver == 0);

            if (gameOver == 2) {
                userData.incrementScore();
            }
            saveGame(userData);
        }

        private static String getValidInput(BufferedReader in, PrintStream out, GameState gameState)
                throws IOException {
            String input = "";

            do {
                out.println(Constants.GAME_MENU);
                out.println("Attempts remaining: " + gameState.getAttempts() + "\n"
                        + Constants.MESSAGE_END_DELIM);
                input = in.readLine().trim();
                if (input.matches(Constants.NO_SPECIAL_CHAR_REGEX)) {
                    throw new IOException("\nInvalid guess: " + input + ". Try again.");
                } else
                    return input;
            } while (true);
        }

        private static int processGameInput(BufferedReader in, PrintStream out,
                GameState gameState, String input) {
            if (input.equals(Constants.SAVE_CODE)) {
                return 1;
            }

            else if (input.toCharArray()[0] == '?') {
                if (processWordQuery(in, out, gameState, input.substring(1))) {
                    out.println("This word is in the database");
                } else {
                    out.println("This is not in the database");
                }
                return 0;
            }

            else {
                return processPuzzleGuess(in, out, gameState, input);
            }
        }

        private static Boolean processWordQuery(BufferedReader in, PrintStream out,
                GameState gameState, String input) {

            return contactDatabase('C', input.replaceAll("\\?", "")).equals("1");
        }

        private static int processPuzzleGuess(BufferedReader in, PrintStream out,
                GameState gameState, String input) {
            boolean successfulGuess = gameState.getPuzzle().updatePuzzleGrid(input);
            gameState.decrementAttempts();

            if (successfulGuess) {
                out.println("\n*Successful guess: '" + input + "'. Puzzle updated.");
                if (gameState.getPuzzle().checkPuzzleSolved()) {
                    gameState.setState(Constants.IDLE_STATE);
                    out.println("You win!");
                    return 2;
                }
            } else {
                out.println("\n*Unsuccessful guess: '" + input + "'.");
                if (gameState.getAttempts() == 0) {
                    gameState.setState(Constants.IDLE_STATE);
                    out.println("You lose!");
                    return 1;
                }
            }
            return 0;
        }

        private static void saveGame(UserData userData) throws IOException {

            try (Socket accountSocket = new Socket("localhost", Constants.UAS_PORT)) {
                accountSocket.setSoTimeout(5000);
                BufferedWriter dataOut = new BufferedWriter(new OutputStreamWriter(accountSocket.getOutputStream()));

                String output = "save;" + userData.getUsername();
                dataOut.write(output + "\n" + userData.getUserDataString());
                dataOut.newLine();
                dataOut.flush();
                
                BufferedReader in = new BufferedReader(new InputStreamReader(accountSocket.getInputStream()));
                int saveResult = Integer.parseInt(in.readLine());
                if (saveResult == 0) {
                    throw new IOException("Couldn't save game.");
                }
            } catch (IOException e) {
                throw new IOException("Couldn't save game.");
            }
        }

        private static void handleError(PrintStream out, UserData userData, Exception e) {
            out.println("Error: " + (e.getMessage()));
            try {
                saveGame(userData);
            } catch (IOException saveError) {
                out.println("Could not save user data.");
            }
        }
    }
}