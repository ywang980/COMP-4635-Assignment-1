import java.util.Scanner;
import java.util.Random;

/**
 * Represents a word puzzle game with a main entry point and game state management.
 */
public class Game {

    /**
     * The main entry point for the word puzzle game.
     * @param args - not used.
     */
    public static void main(String[] args) {

        String[] words = { "elephant", "leopard", "cat", "rabbit", "monkey", "dolphin" };
        int lives = 5;
        GameState gameState = new GameState(words, lives);

        Scanner scanner = new Scanner(System.in);
        String input = "";
        final String exitCode = "**";
        boolean successfulGuess, solved = false;

        do {
            gameState.puzzle.printPuzzle();
            System.out.println("\nGuess something, or enter '**' to quit");
            System.out.println("Lives remaining: :" + gameState.getLives());

            input = scanner.nextLine();
            successfulGuess = gameState.puzzle.updatePuzzleGrid(input);
            if (successfulGuess) {
                System.out.println("Valid guess: '" + input + "'. Puzzle updated");
                solved = gameState.puzzle.checkPuzzleSolved();

                if (solved) {
                    System.out.println("You win!");
                    break;
                }
            } else {
                if (!input.equals(exitCode)) {
                    System.out.println("Invalid guess: '" + input + "''");
                    gameState.decrementLives();
                    if (gameState.getLives() == 0) {
                        System.out.println("You lose!");
                    }
                }
            }
        } while (!input.equals(exitCode) && gameState.getLives() > 0);

        scanner.close();
    }

    /**
     * Represents the state of the word puzzle game.
     */
    public static class GameState {
        private String state;
        private String[] words;
        private Puzzle puzzle;
        private int lives;

        /**
         * Constructs a game state with a given set of words and initial lives.
         * @param words - An array of words for the puzzle.
         * @param lives - The initial number of lives for the player.
         */
        public GameState(String[] words, int lives) {
            this.words = words;
            this.state = "Play";
            this.puzzle = new Puzzle(this.words);
            this.lives = lives;
        }

        /**
         * Gets the state of the game.
         * @return - The game state.
         */
        public String getState() {
            return this.state;
        }

        /**
         * Sets the state of the game.
         * @param state - The game state.
         */
        public void setState(String state) {
            this.state = state;
        }

        /**
         * Gets the current number of lives.
         * @return - Current number of lives.
         */
        public int getLives() {
            return this.lives;
        }

        /**
         * Decreases the number of lives by one.
         */
        public void decrementLives() {
            this.lives--;
        }
    }
}