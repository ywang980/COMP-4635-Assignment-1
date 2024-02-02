import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Puzzle {
    private String stem;
    private int rows;
    private int columns;

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

    private int findLongestLeafLength(String[] words) {
        int length = 0;
        for (int i = 1; i < words.length; i++)
            if (words[i].length() > length)
                length = words[i].length();
        return length;
    }

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

    private void initializePuzzleGrid() {
        for (int i = 0; i < this.rows; i++) {
            String row = new String(this.solutionGrid[i]);
            row = row.replaceAll("[^.+]", "-");
            this.puzzleGrid[i] = row.toCharArray();
        }
    }

    private ArrayList<Leaf> findMatchingRows(char[] stemArray, String[] leaves) {
        ArrayList<Leaf> matchingRows = new ArrayList<>();

        for (int i = 0; i < leaves.length; i++) {
            matchingRows.add(new Leaf(stemArray, leaves[i]));
        }
        return matchingRows;
    }

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

    public boolean checkPuzzleSolved(){
        return (gridToString(this.puzzleGrid).equals(gridToString(this.solutionGrid)));
    }

    private String gridToString(char[][] grid){
        String gridString = "";
        String row;
        for(int i = 0; i < this.rows; i++){
            row = new String(grid[i]);
            gridString += row;
        }

        return gridString;
    }

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

    class Leaf {
        private String word;
        private ArrayList<Integer> matchingStemIndices;

        public Leaf(char[] stemArray, String leafWord) {
            this.word = leafWord;
            this.matchingStemIndices = new ArrayList<>();

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
}