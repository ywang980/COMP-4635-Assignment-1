package GameServer;

import java.io.*;

class UserData {
    private String username;
    private int score;
    private GameState gameState;

    public UserData(String username) {
        this.username = username;
        this.score = 0;
        this.gameState = new GameState();
    }

    public UserData(String username, int score, BufferedReader reader) throws IOException {
        this.username = username;
        this.score = score;
        this.gameState = new GameState(reader);
    }

    public String getUsername() {
        return this.username;
    }

    public int getScore() {
        return this.score;
    }

    public void incrementScore() {
        this.score += 1;
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public String getUserDataString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Username;").append(this.username).append("\n");
        stringBuilder.append("Score;").append(this.score).append("\n");
        stringBuilder.append(this.gameState.getGameStateString());

        String userDataString = stringBuilder.toString();
        return userDataString;
    }
}