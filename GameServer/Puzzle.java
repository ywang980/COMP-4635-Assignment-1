package GameServer;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

class Puzzle {
    private String stem;

    // Puzzle height and width (i.e., rows/columns, respectively)
    private int rows;
    private int columns;

    /*
     * Each puzzle has 2 2-D char "grids"
     * 1 to represent the current puzzle state displayed to the player
     * 1 to represent the solved puzzle
     */
    private char[][] puzzleGrid;
    private char[][] solutionGrid;

    public Puzzle(String[] words) {
        this.stem = words[0];
        this.rows = stem.length();
        this.columns = this.findLongestLeafLength(words) * 2 + 1;
        this.puzzleGrid = createDefaultGrid();
        this.solutionGrid = createDefaultGrid();
        populateSolutionGrid(words);
        initializePuzzleGrid();
    }

    public Puzzle(String stem, String puzzleData) {
        this.stem = stem;

        String[] gridStrings = puzzleData.split("\\$");

        String gridString = gridStrings[0].trim();
        this.rows = gridString.split("\n").length;
        this.columns = gridString.indexOf('\n') - 1;
        this.puzzleGrid = convertStringToGrid(gridString, this.rows, this.columns);

        if (gridStrings.length > 1) {
            String solutionString = gridStrings[1].trim();
            this.solutionGrid = convertStringToGrid(solutionString, this.rows, this.columns);
        }
    }

    private int findLongestLeafLength(String[] words) {
        int length = 0;
        for (int i = 1; i < words.length; i++)
            if (words[i].length() > length)
                length = words[i].length();
        return length;
    }

    /*
     * Purpose: Initialize a grid where each row contains a series of '*',
     * terminated with a '+'
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

    /*
     * Purpose: populate the solution grid using an array of words.
     * 
     * Details:
     * The solution grid is populated as follows:
     * 1. The stem is inserted into the middle column
     * 
     * 2. An ArrayList of "Leaf" objects is constructed
     * -Each Leaf object has 2 members:
     * -A String for the associated word
     * -An arrayList of Integers, representing which indices of the leaf
     * contain a shared character with the Stem
     * 
     * 3. The Leaf ArrayList is sorted by the Leaves' matching indices count
     * in ascending order to ensure Leaves are inserted into the puzzle properly
     * 
     * 4. Each Leaf will choose a random valid index to "connect" to the Stem
     * -2 step process: find row in Stem -> find column in Leaf; if multiple
     * choices possible, randomly choose from valid list
     * 
     * 5. This index is now invalid for all other Leaves, and must be removed
     * from any other Leaf's valid indices list
     * 
     * 6. Repeat from Step 3 until all Leaves are inserted
     */
    private void populateSolutionGrid(String[] words) {

        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].toLowerCase(); // For case-insensitive search
        }

        // Insert stem
        char[] stemArray = words[0].toCharArray();
        int stemColumn = (this.columns - 1) / 2;
        for (int i = 0; i < stemArray.length; i++) {
            this.solutionGrid[i][stemColumn] = stemArray[i];
        }

        // Construct/sort Leaf ArrayList
        String[] leaves = new String[words.length - 1];
        for (int i = 1; i < words.length; i++) {
            leaves[i - 1] = words[i];
        }
        ArrayList<Leaf> matchingRows = findMatchingRows(stemArray, leaves);
        Collections.sort(matchingRows, Comparator
                .comparingInt(leaf -> leaf.getMatchingIndices().size()));

        for (int i = 0; i < matchingRows.size(); i++) {
            // Find random valid row in Stem
            Leaf currentLeaf = matchingRows.get(i);
            ArrayList<Integer> currentMatches = currentLeaf.getMatchingIndices();
            int randomMatchingRow = currentMatches
                    .get(new Random().nextInt(currentMatches.size()));

            // Insert Leaf at random valid column, remove index, resort Leaf ArrayList
            insertLeaf(currentLeaf, stemArray[randomMatchingRow],
                    randomMatchingRow, stemColumn);
            for (int j = i; j < leaves.length; j++) {
                matchingRows.get(j).removeIndex(Integer.valueOf(randomMatchingRow));
            }
            Collections.sort(matchingRows, Comparator
                    .comparingInt(leaf -> leaf.getMatchingIndices().size()));
        }
    }

    private class Leaf {
        private String word;
        private ArrayList<Integer> matchingStemIndices;

        public Leaf(char[] stemArray, String leafWord) {
            this.word = leafWord;
            this.matchingStemIndices = new ArrayList<>();

            /*
             * Find all indices through which the Leaf
             * may be connected to the Stem
             */
            for (int i = 0; i < stemArray.length; i++) {
                if (word.contains(String.valueOf(stemArray[i]))) {
                    this.matchingStemIndices.add(i);
                }
            }
        }

        public String getWord() {
            return this.word;
        }

        public ArrayList<Integer> getMatchingIndices() {
            return this.matchingStemIndices;
        }

        public void removeIndex(Integer Index) {
            this.matchingStemIndices.remove(Index);
        }
    }

    // Purpose: Construct an ArrayList of Leaves
    private ArrayList<Leaf> findMatchingRows(char[] stemArray, String[] leaves) {
        ArrayList<Leaf> matchingRows = new ArrayList<>();

        for (int i = 0; i < leaves.length; i++) {
            matchingRows.add(new Leaf(stemArray, leaves[i]));
        }
        return matchingRows;
    }

    // Purpose: Insert a Leaf at a specified row and random valid column
    private void insertLeaf(Leaf leaf, char matchingCharacter,
            int matchingRow, int stemColumn) {
        char[] leafArray = leaf.getWord().toCharArray();
        ArrayList<Integer> matchingIndices = new ArrayList<>();
        for (int i = 0; i < leafArray.length; i++) {
            if (leafArray[i] == matchingCharacter)
                matchingIndices.add(i);
        }
        int randomMatchingIndex = matchingIndices
                .get(new Random().nextInt(matchingIndices.size()));

        /*
         * (Horizontal) offset is x indices left of the Stem (central)
         * column, where x is the number of characters preceding the
         * connecting character
         */
        int offset = stemColumn - randomMatchingIndex;
        for (int i = 0; i < leafArray.length; i++) {
            this.solutionGrid[matchingRow][i + offset] = leafArray[i];
        }
    }

    /*
     * Purpose: Construct the initial puzzle grid
     * 
     * Details:
     * The initial puzzle grid is a copy of the solution grid, where every
     * "word" character (i.e., every character that isn't a '.' or a '+')
     * is replaced with a '-'
     */
    private void initializePuzzleGrid() {
        for (int i = 0; i < this.rows; i++) {
            String row = new String(this.solutionGrid[i]);
            row = row.replaceAll("[^.+]", "-");
            this.puzzleGrid[i] = row.toCharArray();
        }
    }

    private char[][] convertStringToGrid(String gridString, int rows, int columns) {
        String[] grid1D = gridString.split("\n");
        char[][] grid2D = new char[rows][columns];
        for (int i = 0; i < grid1D.length; i++) {
            grid2D[i] = grid1D[i].toCharArray();
        }

        return grid2D;
    }

    /*
     * Purpose: Update the puzzle grid in response to user input.
     * 
     * Details: The user may guess either a character or a word.
     * -Case 1: user input is a single character - character guess - find/reveal
     * all occurrences of the character
     * -Case 2: user input is 2+ characters - word guess - check the Stem/Leaves,
     * reveal a match if it exists
     * 
     * A flag indicating a successful reveal (i.e., a puzzle update) is returned.
     * Note: this flag to also set to true if the user guesses something that is
     * already revealed
     */
    public boolean updatePuzzleGrid(String input) {
        input = input.toLowerCase(); // For case-insensitive search
        char[] inputArray = input.toCharArray();
        boolean updated = false;
        // Single character input
        if (input.length() == 1) {
            for (int i = 0; i < this.rows; i++) {
                for (int j = 0; j < this.columns; j++) {
                    if (this.solutionGrid[i][j] == inputArray[0]) {
                        this.puzzleGrid[i][j] = inputArray[0];
                        updated = true;
                    }
                }
            }
        } else {
            // Multiple character input - stem match
            if (input.equals(this.stem)) {
                for (int i = 0; i < this.rows; i++) {
                    this.puzzleGrid[i][(this.columns - 1) / 2] = inputArray[i];
                }
                return true;
            }

            // Multiple character input - leaf match
            for (int i = 0; i < this.rows; i++) {
                String leaf = new String(this.solutionGrid[i])
                        .replaceAll("[.+]", "");
                if (leaf.equals(input)) {
                    this.puzzleGrid[i] = this.solutionGrid[i];
                    return true;
                }
            }
        }
        return updated;
    }

    // Purpose: check if the puzzle is complete (i.e. puzzle grid == solution grid)
    public boolean checkPuzzleSolved() {
        return (gridToString(this.puzzleGrid).equals(gridToString(this.solutionGrid)));
    }

    // Purpose: export the puzzle as a string
    public String getPuzzleString() {
        return gridToString(this.puzzleGrid);
    }

    // Purpose: export the solved puzzle as a string
    public String getSolutionString() {
        return gridToString(this.solutionGrid);
    }

    private String gridToString(char[][] grid) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < this.rows; i++) {
            stringBuilder.append(grid[i]).append("\n");
        }
        return stringBuilder.toString();
    }
}