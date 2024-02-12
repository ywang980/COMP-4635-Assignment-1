package GameServer;

import java.io.*;

public class GameState {
    private String state;
    private int attempts;
    private String[] words;
    private Puzzle puzzle;

    public GameState() {
        this.state = Constants.IDLE_STATE;
    }

    public GameState(int attempts, String[] words) {
        this.state = Constants.PLAY_STATE;
        this.attempts = attempts;
        this.words = words;
        this.puzzle = new Puzzle(this.words);
    }

    public GameState(String data) throws IOException {
        try (BufferedReader reader = new BufferedReader(new StringReader(data))) {
            this.state = ParseInput.parseStringValue(reader);
            if (this.state.equals(Constants.PLAY_STATE)) {
                this.attempts = ParseInput.parseIntValue(reader);
                String wordsData = ParseInput.parseStringValue(reader);
                this.words = wordsData.split(",");
                this.puzzle = new Puzzle(this.words[0], reader);
            }
        }
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getAttempts() {
        return this.attempts;
    }

    public void decrementAttempts() {
        this.attempts--;
    }

    public String[] getWords() {
        return this.words;
    }

    public Puzzle getPuzzle() {
        return this.puzzle;
    }

    public String getGameStateString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("State;").append(this.state).append("\n");

        if (this.state.equals(Constants.PLAY_STATE)) {
            stringBuilder.append("Attempts;").append(this.attempts).append("\n");

            stringBuilder.append("Words;");
            for (int i = 0; i < this.words.length; i++) {
                stringBuilder.append(this.words[i]);
                if (i < words.length - 1) {
                    stringBuilder.append(", ");
                }
            }

            stringBuilder.append("\n").append(this.puzzle.getPuzzleString());
            stringBuilder.append("$\n").append(this.puzzle.getSolutionString());
        }
        String gameStateString = stringBuilder.toString();
        return gameStateString;
    }
}