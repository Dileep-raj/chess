package com.drdedd.chess.misc;

import com.drdedd.chess.game.gameData.Player;
import com.drdedd.chess.game.pieces.Piece;

public class MiscMethods {


    /**
     * Converts absolute position to column number
     */
    public static int toCol(int position) {
        return position % 8;
    }

    /**
     * Converts absolute position to row number
     */
    public static int toRow(int position) {
        return position / 8;
    }

    /**
     * Converts notation to column number
     */
    public static int toCol(String position) {
        return position.charAt(0) - 'a';
    }

    /**
     * Converts notation to row number
     */
    public static int toRow(String position) {
        return position.charAt(1) - '1';
    }

    /**
     * Converts absolute position to Standard Notation
     */
    public static String toNotation(int position) {
        return "" + (char) ('a' + position % 8) + (position / 8 + 1);
    }

    /**
     * Converts row and column numbers to Standard Notation
     */
    public static String toNotation(int row, int col) {
        return "" + (char) ('a' + col) + (row + 1);
    }

    /**
     * Converts column number to the corresponding file character
     */
    public static char toColChar(int col) {
        return (char) ('a' + col);
    }

    /**
     * Opponent player for the given <code>Player</code>
     *
     * @return <code>White|Black</code>
     */
    public static Player opponentPlayer(Player player) {
        return player == Player.WHITE ? Player.BLACK : Player.WHITE;
    }

    /**
     * Converts piece to letter for FEN representation
     *
     * @param piece Piece to be converted
     * @return <code>K|Q|R|N|B|P</code> - Uppercase or Lowercase
     * @see <a href="https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation">More about FEN</a>
     */
    public static char getPieceChar(Piece piece) {
        char ch = piece.getRank().getLetter();
        if (!piece.isWhite()) ch = Character.toLowerCase(ch);
        return ch;
    }

    public static String convertNumber(long number) {
        if (number == 0) return "0 ";
        String sign = number < 0 ? "-" : "";
        number = Math.abs(number);
        int pow = (int) Math.log10(number) / 3;
        double convertedNumber = number / Math.pow(1000, pow);
        String suffix = switch (pow) {
            case 1 -> "K";
            case 2 -> "M";
            case 3 -> "G";
            case 4 -> "T";
            case 5 -> "P";
            case 6 -> "E";
            default -> "";
        };
        return "%s%.2f%s".formatted(sign, convertedNumber, suffix);
    }

    public static String convertBytes(long bytes) {
        String sign = "";
        if (bytes < 0) sign = "-";
        if (bytes == 0) return "0 ";
        bytes = Math.abs(bytes);
        int pow = (int) (Math.log(bytes) / Math.log(1024));
        double convertedNumber = bytes / Math.pow(1024, pow);
        String suffix = switch (pow) {
            case 1 -> "K";
            case 2 -> "M";
            case 3 -> "G";
            case 4 -> "T";
            case 5 -> "P";
            case 6 -> "E";
            default -> "";
        };
        return "%s%.2f %s".formatted(sign, convertedNumber, suffix);
    }

    public static double convertToHigherBase(long number, long base, int power) {
        return number / Math.pow(base, power);
    }

    public static String formatNanoseconds(long time) {
        StringBuilder builder = new StringBuilder();
        int ns = (int) (time % 1e3);
        int us = (int) (time / 1e3 % 1e3);
        int ms = (int) (time / 1e6 % 1e3);
        int sec = (int) (time / 1e9);
        int min = sec / 60;
        sec = sec % 60;
        if (min > 0) builder.append(String.format("%dm", min));
        if (sec > 0) builder.append(String.format(" %ds", sec));
        else if (ms > 0) builder.append(String.format(" %dms", ms));
        else if (us > 0) builder.append(String.format(" %dµs", us));
        else if (ns > 0) builder.append(String.format(" %dns", ns));
        else return "0ns";
        return builder.toString().trim();
    }
}
