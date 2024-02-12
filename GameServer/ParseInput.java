package GameServer;

import java.io.*;

class ParseInput {
    public static String parseStringValue(BufferedReader reader) throws IOException {
        String inputLine = reader.readLine();
        if (inputLine == null) {
            throw new IOException(Constants.PREMATURE_EOF);
        }

        String[] tokenizedInput = inputLine.split(";");
        if (tokenizedInput.length < 2) {
            throw new IOException(Constants.VALUE_PARSE_FAIL);
        }
        return tokenizedInput[1];
    }

    public static Integer parseIntValue(BufferedReader reader) throws IOException {
        try {
            return Integer.parseInt(parseStringValue(reader));
        } catch (NumberFormatException e) {
            throw new IOException(Constants.NON_INTEGER_VALUE);
        }
    }

    public static String parsePuzzle(BufferedReader reader, boolean stopOnDelimiter)
            throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String inputLine;
        do {
            inputLine = reader.readLine();
            if (inputLine == null ||
                    ((inputLine.equals(Constants.SOLUTION_GRID_DELIM)) && stopOnDelimiter)) {
                break;
            }
            stringBuilder.append(inputLine).append("\n");
        } while (true);

        String gridString = stringBuilder.toString();
        if (gridString.equals("")) {
            throw new IOException(Constants.PREMATURE_EOF);
        }

        return gridString;
    }
}