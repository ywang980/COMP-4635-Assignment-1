package GameServer;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class Game {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(Constants.port);
            runServer(serverSocket);
        } catch (IOException e) {
            System.out.println("Could not create server.");
        }
    }

    private static void runServer(ServerSocket serverSocket) {
        while (true) {
            try {
                System.out.println("Listening for incoming requests...");

                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            } catch (IOException e) {
                System.out.println("Error: could not communicate with client.");
            }
        }
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintStream out = new PrintStream(clientSocket.getOutputStream());

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
            clientSocket.close();
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
                checkValidUser(username);
                validUserName = true;
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

    private static void checkValidUser(String username)
            throws Exceptions.DuplicateLoginException {
        // Replace with call to account microservice
        boolean offline = true;
        if (!offline) {
            throw new Exceptions().new DuplicateLoginException(Constants.DUPLICATE_LOGIN);
        }
    }

    private static UserData validateUserData(PrintStream out, String username) {
        UserData userData = null;
        try {
            userData = loadUserData(username);
        } catch (IOException e) {
            out.println("Error in parsing user data. " + e.getMessage());
            out.println("Connection will now close." + Constants.MESSAGE_END_DELIM);
        }
        return userData;
    }

    private static UserData loadUserData(String username) throws IOException {
        String filePath = Constants.USER_DATA_DIRECTORY + username + ".txt";
        File userDataFile = new File(filePath);
        try {
            if (!userDataFile.exists()) {
                userDataFile.createNewFile();
                return new UserData(username);
            }
        } catch (IOException e) {
            throw new IOException(Constants.CANT_CREATE_USER_FILE);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(userDataFile))) {
            reader.readLine();
            Integer score = ParseInput.parseIntValue(reader);
            return new UserData(username, score.intValue(), reader);
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
                        saveGame(userData);
                        break;
                    }
                    boolean existingGame = userData.getGameState().getState()
                            .equals(Constants.PLAY_STATE);

                    processUserInput(in, out, userData, input, existingGame);
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
                // Replace with call to word microservice
                out.println("Sending to database microservice: " + "A;" + argument);
                break;
            }
            case "Remove": {
                // Replace with call to word microservice
                out.println("Sending to database microservice: " + "B;" + argument);
                break;
            }
            case "New Game": {
                try {
                    int wordCount = Integer.parseInt(argument);
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

    private static void createNewGame(UserData userData, int wordCount) throws IOException {
        String words[] = generateWordList(wordCount);
        int attempts = Math.min(words.length * 2, 15);
        userData.setGameState(new GameState(attempts, words));
        saveGame(userData);
    }

    private static String[] generateWordList(int wordCount) {
        // Delete once connected to word microservice
        ArrayList<String> wordRepository = readWordRepository(Constants.WORD_REPOSITORY_PATH);

        while (true) {
            ArrayList<String> wordsList = new ArrayList<>();
            String stem = fetchStem(wordRepository, wordCount - 1);
            wordsList.add(stem);

            ArrayList<Integer> leafIndicesList = generateLeafIndices(wordCount, stem);
            if (populateLeaves(leafIndicesList, stem, wordRepository, wordsList)) {
                return wordsList.toArray(new String[0]);
            }
        }
    }

    private static ArrayList<String> readWordRepository(String filePath) {
        ArrayList<String> wordRepository = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(filePath))) {
            while (scanner.hasNextLine()) {
                String word = scanner.nextLine();
                wordRepository.add(word);
            }
        } catch (FileNotFoundException e) {
        }
        return wordRepository;
    }

    // Rewrite function with call to word microservice
    private static String fetchStem(ArrayList<String> wordRepository, int length) {
        String stem = "";
        int wordRepositoryLength = wordRepository.size();
        int randomOffset = new Random().nextInt(wordRepositoryLength);

        for (int i = 0; i < wordRepositoryLength; i++) {
            String randomWord = wordRepository
                    .get((i + randomOffset) % wordRepositoryLength);
            if (randomWord.length() >= length) {
                stem = randomWord;
                break;
            }
        }
        return stem;
    }

    private static ArrayList<Integer> generateLeafIndices(int wordCount, String stem) {
        Set<Integer> leafIndices = new HashSet<>();
        while (leafIndices.size() < wordCount - 1) {
            leafIndices.add(new Random().nextInt(stem.length()));
        }
        return new ArrayList<>(leafIndices);
    }

    // Edit function prototype to exclude wordRepository
    private static boolean populateLeaves(ArrayList<Integer> leafIndicesList, String stem,
            ArrayList<String> wordRepository, ArrayList<String> wordsList) {
        String leaf = "";
        int consecutiveDuplicateLeaf = 0;

        for (int i = 0; i < leafIndicesList.size(); i++) {
            int index = leafIndicesList.get(i).intValue();
            char connectingCharacter = stem.toCharArray()[index];

            do {
                leaf = fetchLeaf(wordRepository, connectingCharacter);
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

    // Rewrite function with call to word microservice
    private static String fetchLeaf(ArrayList<String> wordRepository, char matchingCharacter) {
        String leaf = "";
        int wordRepositoryLength = wordRepository.size();
        int randomOffset = new Random().nextInt(wordRepositoryLength);

        for (int i = 0; i < wordRepositoryLength; i++) {
            String randomWord = wordRepository.get((i + randomOffset) % wordRepositoryLength);
            if (randomWord.contains(String.valueOf(matchingCharacter))) {
                leaf = randomWord;
                break;
            }
        }
        return leaf;
    }

    private static void playGame(BufferedReader in, PrintStream out, UserData userData)
            throws IOException {
        GameState gameState = userData.getGameState();
        gameState.setState(Constants.PLAY_STATE);

        String input;
        boolean gameOver;

        do {
            out.print("\n" + gameState.getPuzzle().getPuzzleString());
            input = getValidInput(in, out, gameState);
            gameOver = processGameInput(in, out, gameState, input);
        } while (!gameOver);

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

    private static boolean processGameInput(BufferedReader in, PrintStream out,
            GameState gameState, String input) {
        if (input.equals(Constants.SAVE_CODE)) {
            return true;
        } else if (input.toCharArray()[0] == '?') {
            processWordQuery(in, out, gameState, input.substring(1));
            return false;
        } else {
            return processPuzzleGuess(in, out, gameState, input);
        }
    }

    // Replace with call to word microservice
    private static void processWordQuery(BufferedReader in, PrintStream out,
            GameState gameState, String input) {
        out.println("Sending to database microservice: " + "C;" + input);
    }

    private static boolean processPuzzleGuess(BufferedReader in, PrintStream out,
            GameState gameState, String input) {
        boolean successfulGuess = gameState.getPuzzle().updatePuzzleGrid(input);
        gameState.decrementAttempts();

        if (successfulGuess) {
            out.println("\n*Successful guess: '" + input + "'. Puzzle updated.");
            if (gameState.getPuzzle().checkPuzzleSolved()) {
                gameState.setState(Constants.IDLE_STATE);
                out.println("You win!");
                return true;
            }
        } else {
            out.println("\n*Unsuccessful guess: '" + input + "'.");
            if (gameState.getAttempts() == 0) {
                gameState.setState(Constants.IDLE_STATE);
                out.println("You lose!");
                return true;
            }
        }
        return false;
    }

    private static void saveGame(UserData userData) throws IOException {
        File userDataFile = new File(Constants.USER_DATA_DIRECTORY +
                userData.getUsername() + ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(userDataFile))) {
            writer.write(userData.getUserDataString());
        } catch (IOException e) {
            throw new IOException("Couldn't save game.");
        }
    }

    private static void handleError(PrintStream out, UserData userData, Exception e) {
        out.println("Error: " + (e.getMessage()));
        String saveStatus = "";
        try {
            saveGame(userData);
        } catch (IOException saveError) {
        } finally {
        }
    }
}