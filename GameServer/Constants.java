package GameServer;

class Constants {
        public static final String USER_DATA_DIRECTORY = "./UserData/";

        public static final int MAX_WORD_COUNT = 15;
        public static final String WORD_REPOSITORY_PATH = "./Data/words.txt";

        public static final int port = 1000;
        public static final String MESSAGE_END_DELIM = "\n*End of Message*";
        public static final String EXIT_CODE = "*Exit*";
        public static final String SAVE_CODE = "*Save*";
        public static final String NO_SPECIAL_CHAR_REGEX = ".*[+\\-\\.].*";

        public static final String USER_MENU = "\nEnter a command from the list below " +
                        "(each command must adhere to the specified syntax - CASE SENSITIVE):\n" +
                        "Add;WordName              //Add a word to the database.\n" +
                        "Remove;WordName           //Remove a word from the database.\n" +
                        "New Game;x                //Start a new game with x words.\n" +
                        "Continue;*                //Continue existing game.\n" +
                        "*Exit*                    //Exit Game\n";

        public static final String GAME_MENU = "\nEnter a command from the list below " +
                        "(each command must adhere to the specified syntax - CASE SENSITIVE):\n" +
                        "Letter                    //Guess a letter.\n" +
                        "WordName                  //Guess a word.\n" +
                        "?WordName                 //Check if word exists in database.\n" +
                        "*Save*                    //Save and return to main menu.\n";

        public static final String IDLE_STATE = "Idle";
        public static final String PLAY_STATE = "Play";

        public static final String SOLUTION_GRID_DELIM = "$";

        public static final String NO_CLIENT_INPUT = "No client input.";
        public static final String DUPLICATE_LOGIN = "User already logged in.";

        public static final String CANT_CREATE_USER_FILE = "Could not create user data file.";
        public static final String PREMATURE_EOF = "End of file reached prematurely.";
        public static final String VALUE_PARSE_FAIL = "Failed to parse value in user data.";
        public static final String NON_INTEGER_VALUE = "Parsed value is not an integer.";

        public static final String INVALID_COMMAND_SYNTAX = "Invalid command syntax. Try again.";
        public static final String INVALID_WORD_COUNT = "Word count argument is not a number.";
        public static final String NO_EXISTING_GAME = "No existing game found.";
}