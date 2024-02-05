import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Represents a word puzzle with a stem and leaves.
 */
public class Puzzle {
    private String stem;
    private int rows;
    private int columns;

    private char[][] puzzleGrid;
    private char[][] solutionGrid;

    /**
     * Constructs a Puzzle object based on an array of words.
     * @param words - An array of words including the stem and leaves.
     */
    public Puzzle(String[] words) {
        this.stem = words[0];
        this.rows = stem.length();
        this.columns = this.findLongestLeafLength(words) * 2 + 1;

        this.puzzleGrid = createDefaultGrid();
        this.solutionGrid = createDefaultGrid();
        populateSolutionGrid(words);
        initializePuzzleGrid();
    }

    /**
     * Finds the length of the longest leaf in the array of words.
     * @param words - An array of words.
     * @return - The length of the longest leaf.
     */
    private int findLongestLeafLength(String[] words) {
        int length = 0;
        for (int i = 1; i < words.length; i++)
            if (words[i].length() > length)
                length = words[i].length();
        return length;
    }

    /**
     * Creates a default character grid out of dots and plus signs.
     * @return - The default character grid.
     */
    private char[][] createDefaultGrid() {
        char[][] grid = new char[this.rows][this.columns];
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++)
                if (j == this.columns - 1) {
                    grid[i][j] = '+';
                } else {
                    grid[i][j] = '.';
                }
        }
        return grid;
    }

    /**
     * Populates the solution grid based on the given array of words.
     * @param words - An array of words including the stem and leaves.
     */
    private void populateSolutionGrid(String[] words) {
        char[] stemArray = words[0].toCharArray();
        int stemColumn = (this.columns - 1) / 2;
        for (int i = 0; i < stemArray.length; i++) {
            this.solutionGrid[i][stemColumn] = stemArray[i];
        }

        String[] leaves = new String[words.length - 1];
        for (int i = 1; i < words.length; i++) {
            leaves[i - 1] = words[i];
        }

        ArrayList<Leaf> matchingRows = findMatchingRows(stemArray, leaves);
        Collections.sort(matchingRows, Comparator.comparingInt(leaf -> leaf.getMatchingIndices().size()));

        for (int i = 0; i < matchingRows.size(); i++) {
            Leaf currentLeaf = matchingRows.get(i);
            ArrayList<Integer> currentMatches = currentLeaf.getMatchingIndices();

            int randomMatchingRow = currentMatches
                    .get(new Random().nextInt(currentMatches.size()));

            insertLeaf(currentLeaf, stemArray[randomMatchingRow], randomMatchingRow, stemColumn);

            for (int j = i; j < leaves.length; j++) {
                matchingRows.get(j).removeIndex(Integer.valueOf(randomMatchingRow));
            }
            Collections.sort(matchingRows, Comparator.comparingInt(leaf -> leaf.getMatchingIndices().size()));
        }
    }

    /**
     * Initializes the puzzle grid based on the solution grid.
     */
    private void initializePuzzleGrid() {
        for (int i = 0; i < this.rows; i++) {
            String row = new String(this.solutionGrid[i]);
            row = row.replaceAll("[^.+]", "-");
            this.puzzleGrid[i] = row.toCharArray();
        }
    }

    /**
     * Finds rows of leaves that match the stem and sorts them by the number of matching indices.
     * @param stemArray - The stem represented as an array of characters.
     * @param leaves - An array of leaves.
     * @return - An ArrayList of matching leaves sorted by the number of matching Indices.
     */
    private ArrayList<Leaf> findMatchingRows(char[] stemArray, String[] leaves) {
        ArrayList<Leaf> matchingRows = new ArrayList<>();

        for (int i = 0; i < leaves.length; i++) {
            matchingRows.add(new Leaf(stemArray, leaves[i]));
        }
        return matchingRows;
    }

    /**
     * Inserts a leaf into the solution grid at a random matching position.
     * @param leaf - The leaf object to insert.
     * @param matchingCharacter - The character to match in the leaf.
     * @param matchingRow - The row in which the leaf should be inserted.
     * @param stemColumn - The column of the stem in the grid.
     */
    private void insertLeaf(Leaf leaf, char matchingCharacter, int matchingRow, int stemColumn) {
        char[] leafArray = leaf.getWord().toCharArray();

        ArrayList<Integer> matchingIndices = new ArrayList<>();
        for (int i = 0; i < leafArray.length; i++) {
            if (leafArray[i] == matchingCharacter)
                matchingIndices.add(i);
        }

        int randomMatchingIndex = matchingIndices.get(new Random().nextInt(matchingIndices.size()));
        int offset = stemColumn - randomMatchingIndex;

        for (int i = 0; i < leafArray.length; i++) {
            this.solutionGrid[matchingRow][i + offset] = leafArray[i];
        }
    }

    /**
     * Updates the puzzle grid based on user input.
     * @param input - The user input.
     * @return - True if the puzzle grid is updated, False if not.
     */
    public boolean updatePuzzleGrid(String input) {
        boolean updated = false;
        char[] inputArray = input.toCharArray();

        if (input.length() == 1) {

            for (int i = 0; i < this.rows; i++) {
                for (int j = 0; j < this.columns; j++) {
                    if (this.solutionGrid[i][j] == inputArray[0]) {
                        puzzleGrid[i][j] = inputArray[0];
                        updated = true;
                    }
                }
            }
        } else {
            if (input.equals(this.stem)) {
                for (int i = 0; i < this.rows; i++) {
                    puzzleGrid[i][(this.columns - 1) / 2] = inputArray[i];
                }
                return true;
            }
            for (int i = 0; i < this.rows; i++) {
                if (new String(this.solutionGrid[i]).contains(input)) {
                    puzzleGrid[i] = solutionGrid[i];
                    return true;
                }
            }
        }

        return updated;
    }

    /**
     * Checks if the puzzle is solved by comparing the puzzle grid to the solution grid.
     * @return - True if the puzzle is solved, False if not.
     */
    public boolean checkPuzzleSolved(){
        return (gridToString(this.puzzleGrid).equals(gridToString(this.solutionGrid)));
    }

    /**
     * Converts the grid to a string for comparison.
     * @param grid - The character grid.
     * @return - A string representation of the grid.
     */
    private String gridToString(char[][] grid){
        String gridString = "";
        String row;
        for(int i = 0; i < this.rows; i++){
            row = new String(grid[i]);
            gridString += row;
        }

        return gridString;
    }

    /**
     * Prints the current state of the puzzle grid.
     */
    public void printPuzzle() {
        // System.out.println("Solution:");
        // for (int i = 0; i < this.rows; i++) {
        //     for (int j = 0; j < this.columns; j++) {
        //         System.out.print(this.solutionGrid[i][j]);
        //     }
        //     System.out.println();
        // }
        System.out.println("\n Puzzle:");
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                System.out.print(this.puzzleGrid[i][j]);
            }
            System.out.println();
        }
    }

    /**
     * Represents a leaf with a word and matching stem indices.
     */
    class Leaf {
        private String word;
        private ArrayList<Integer> matchingStemIndices;

        /**
         * Constructs a Leaf object based on the stem and leaf word.
         * @param stemArray - The stem represented as an array of characters.
         * @param leafWord - The word of the leaf.
         */
        public Leaf(char[] stemArray, String leafWord) {
            this.word = leafWord;
            this.matchingStemIndices = new ArrayList<>();

            for (int i = 0; i < stemArray.length; i++) {
                if (word.contains(String.valueOf(stemArray[i]))) {
                    this.matchingStemIndices.add(i);
                }
            }
        }

        /**
         * Gets the word of the leaf.
         * @return - The word of the leaf.
         */
        public String getWord() {
            return this.word;
        }

        /**
         * Gets the list of matching stem indices for the leaf.
         * @return - The list of matching stem indices.
         */
        public ArrayList<Integer> getMatchingIndices() {
            return this.matchingStemIndices;
        }

        /**
         * Removes a specific index from the list of matching stem indices.
         * @param Index - The index to remove.
         */
        public void removeIndex(Integer Index) {
            this.matchingStemIndices.remove(Index);
        }
    }
}