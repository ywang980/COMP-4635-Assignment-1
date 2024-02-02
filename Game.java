import java.util.Scanner;
import java.util.Random;

public class Game {
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

    public static class GameState {
        private String state;
        private String[] words;
        private Puzzle puzzle;
        private int lives;

        public GameState(String[] words, int lives) {
            this.words = words;
            this.state = "Play";
            this.puzzle = new Puzzle(this.words);
            this.lives = lives;
        }

        public String getState() {
            return this.state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public int getLives() {
            return this.lives;
        }

        public void decrementLives() {
            this.lives--;
        }
    }
}