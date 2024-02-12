package UserAccountServer;

import GameServer.GameState;

import java.io.*;

public class UserData {
    private final String username;
    private int score;
    private GameState gameState;

    public UserData(String data) throws IOException {
        int usernameStartIndex = data.indexOf("Username;") + "Username;".length();
        int usernameEndIndex = data.indexOf("\n", usernameStartIndex);
        this.username = data.substring(usernameStartIndex, usernameEndIndex);

        int scoreStartIndex = data.indexOf("Score;") + "Score;".length();
        int scoreEndIndex = data.indexOf("\n", scoreStartIndex);
        this.score = Integer.parseInt(data.substring(scoreStartIndex, scoreEndIndex));

        if (data.contains("State;Play")) {
            this.gameState = new GameState(data);
        } else {
            this.gameState = new GameState();
        }
    }

    public UserData(String username, boolean defaultAccount) {
        this.username = username;
        this.score = 0;
        this.gameState = new GameState();
    }

     /**
    public UserData(String username, int score, String data) throws IOException {
        this.username = username;
        this.score = score;
        this.gameState = new GameState(data);
    }*/

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