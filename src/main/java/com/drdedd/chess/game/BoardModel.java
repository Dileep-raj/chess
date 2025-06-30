package com.drdedd.chess.game;

import com.drdedd.chess.game.data.Regexes;
import com.drdedd.chess.game.gameData.Player;
import com.drdedd.chess.game.gameData.Rank;
import com.drdedd.chess.game.gameData.Unicodes;
import com.drdedd.chess.game.pieces.*;
import com.drdedd.chess.misc.MiscMethods;
import lombok.Getter;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Stores pieces location, enPassant square and other board UI data<br>
 */
public class BoardModel implements Serializable, Cloneable {
    private static final String TAG = "BoardModel";
    public final HashMap<String, Integer> resIDs = new HashMap<>();
    private final HashMap<String, String> unicodes = new HashMap<>();
    /**
     * Set of all the pieces on the board
     */
    public LinkedHashSet<Piece> pieces = new LinkedHashSet<>();
    private King whiteKing = null, blackKing = null;
    public Pawn enPassantPawn = null;
    public String enPassantSquare = "", fromSquare = "", toSquare = "";
    @Getter
    private int halfMove, fullMove;
    @Getter
    private Player turn;

    public BoardModel(boolean initializeBoard) {
        Player.WHITE.setInCheck(false);
        Player.BLACK.setInCheck(false);

        unicodes.put("QW", Unicodes.QW);
        unicodes.put("RW", Unicodes.RW);
        unicodes.put("BW", Unicodes.BW);
        unicodes.put("NW", Unicodes.NW);

        unicodes.put("QB", Unicodes.QB);
        unicodes.put("RB", Unicodes.RB);
        unicodes.put("BB", Unicodes.BB);
        unicodes.put("NB", Unicodes.NB);

        if (initializeBoard) resetBoard();

        resIDs.put(Player.WHITE + Rank.QUEEN.toString(), -1);
        resIDs.put(Player.WHITE + Rank.ROOK.toString(), -1);
        resIDs.put(Player.WHITE + Rank.BISHOP.toString(), -1);
        resIDs.put(Player.WHITE + Rank.KNIGHT.toString(), -1);

        resIDs.put(Player.BLACK + Rank.QUEEN.toString(), -1);
        resIDs.put(Player.BLACK + Rank.ROOK.toString(), -1);
        resIDs.put(Player.BLACK + Rank.BISHOP.toString(), -1);
        resIDs.put(Player.BLACK + Rank.KNIGHT.toString(), -1);
    }

    /**
     * Resets the board to initial state
     */
    public void resetBoard() {
        int i;
        pieces.clear();
        for (i = 0; i <= 1; i++) {
            addPiece(new Rook(Player.WHITE, 0, i * 7, Unicodes.RW));
            addPiece(new Knight(Player.WHITE, 0, 1 + i * 5, Unicodes.NW));
            addPiece(new Bishop(Player.WHITE, 0, 2 + i * 3, Unicodes.BW));


            addPiece(new Rook(Player.BLACK, 7, i * 7, Unicodes.RB));
            addPiece(new Knight(Player.BLACK, 7, 1 + i * 5, Unicodes.NB));
            addPiece(new Bishop(Player.BLACK, 7, 2 + i * 3, Unicodes.BB));

        }

//        King and Queen pieces
        addPiece(new King(Player.WHITE, 0, 4, Unicodes.KW));
        addPiece(new Queen(Player.WHITE, 0, 3, Unicodes.QW));

        addPiece(new King(Player.BLACK, 7, 4, Unicodes.KB));
        addPiece(new Queen(Player.BLACK, 7, 3, Unicodes.QB));

//        Pawn pieces
        for (i = 0; i < 8; i++) {
            addPiece(new Pawn(Player.WHITE, 1, i, Unicodes.PW));
            addPiece(new Pawn(Player.BLACK, 6, i, Unicodes.PB));
        }

        halfMove = 0;
        fullMove = 1;

        turn = Player.WHITE;
    }

    /**
     * Returns Black king from <code>{@link BoardModel#pieces}</code> set
     *
     * @return <code>{@link King}</code>
     */
    public King getBlackKing() {
        for (Piece piece : pieces)
            if (piece.isKing() && !piece.isWhite()) {
                blackKing = (King) piece;
                break;
            }
        return blackKing;
    }

    /**
     * Returns White king from <code>{@link BoardModel#pieces}</code> set
     *
     * @return <code>{@link  King}</code>
     */
    public King getWhiteKing() {
        for (Piece piece : pieces)
            if (piece.isKing() && piece.isWhite()) {
                whiteKing = (King) piece;
                break;
            }
        return whiteKing;
    }

    /**
     * Returns piece at a given position
     *
     * @param row Row number
     * @param col Column number
     * @return <code>{@link Piece}|null</code>
     */
    public Piece pieceAt(int row, int col) {
        if (row < 0 || row > 7 || col < 0 || col > 7) return null;
        for (Piece piece : pieces)
            if (!piece.isCaptured() && piece.getCol() == col && piece.getRow() == row) return piece;
        return null;
    }

    /**
     * Captures the piece and removes it from board view
     *
     * @param piece <code>Piece</code> to be captured
     */
    public boolean capturePiece(Piece piece) {
        piece.setCaptured(true);
        return piece.isCaptured();
    }

    /**
     * Adds the given piece into <code>{@link BoardModel#pieces}</code> set
     *
     * @param piece <code>Piece</code> to be added
     */
    public void addPiece(Piece piece) {
        pieces.add(piece);
    }

    /**
     * Promotes a pawn to higher rank on reaching last rank
     *
     * @param pawn <code>Pawn</code> to be promoted
     * @param rank <code>Queen|Rook|Knight|Bishop</code>
     * @param row  Row of the pawn
     * @param col  Column of the pawn
     * @return <code>{@link Piece}</code> - Promoted piece
     */
    public Piece promote(Piece pawn, Rank rank, int row, int col) {
        Piece piece = null;

        if (rank == Rank.QUEEN)
            piece = new Queen(pawn.getPlayer(), row, col, unicodes.get("Q" + pawn.getPlayer().toString().charAt(0)));
        if (rank == Rank.ROOK)
            piece = new Rook(pawn.getPlayer(), row, col, unicodes.get("R" + pawn.getPlayer().toString().charAt(0)));
        if (rank == Rank.BISHOP)
            piece = new Bishop(pawn.getPlayer(), row, col, unicodes.get("B" + pawn.getPlayer().toString().charAt(0)));
        if (rank == Rank.KNIGHT)
            piece = new Knight(pawn.getPlayer(), row, col, unicodes.get("N" + pawn.getPlayer().toString().charAt(0)));

        if (piece != null) {
            addPiece(piece);
//            System.out.println(TAG+" promote: Promoted " + pawn.getPosition().charAt(1) + " file pawn to " + piece.getRank());
        }
        pieces.remove(pawn);
//        removePiece(pawn);
        return piece;
    }

    /**
     * Converts the <code>BoardModel</code> to <code>String</code> type <br>
     * <ul>
     * <li>UpperCase letter represents White Piece <br></li>
     * <li>LowerCase letter represents Black Piece <br></li>
     * <li>Hyphen (-) represents empty square</li>
     * </ul>
     *
     * @return Standard board notation
     */
    @Override
    public String toString() {
        StringBuilder board = new StringBuilder("Board:\n");
        int i, j;
        for (i = 7; i >= 0; i--) {
            board.append(' ').append(i + 1).append(' ');
            for (j = 0; j < 8; j++) {
                Piece tempPiece = pieceAt(i, j);
                if (tempPiece == null) board.append("- ");
                else board.append(MiscMethods.getPieceChar(tempPiece)).append(' ');
            }
            board.append("\n");
        }
        board.append("   a b c d e f g h");
        return String.valueOf(board);
    }

    /**
     * Converts the <code>BoardModel</code> to <code>String</code> type <br>
     * Uses Unicode to represent pieces and hyphen (-) for empty square
     *
     * @return Unicode board
     */
    public String unicode() {
        StringBuilder board = new StringBuilder("Board:\n");
        int i, j;
        for (i = 7; i >= 0; i--) {
            board.append(' ').append(i + 1).append(' ');
            for (j = 0; j < 8; j++) {
                Piece tempPiece = pieceAt(i, j);
                if (tempPiece == null) board.append("- ");
                else board.append(tempPiece.getUnicode()).append(' ');
            }
            board.append("\n");
        }
        board.append("   a b c d e f g h");
        return String.valueOf(board);
    }

    @Override
    public BoardModel clone() {
        try {
            BoardModel boardModelClone = (BoardModel) super.clone();
            boardModelClone.pieces = new LinkedHashSet<>();
            for (Piece piece : pieces) boardModelClone.pieces.add(piece.clone());

            if (enPassantPawn != null) boardModelClone.enPassantPawn = (Pawn) enPassantPawn.clone();
            else boardModelClone.enPassantPawn = null;

            return boardModelClone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /**
     * Converts current position to FEN Notation <br>
     *
     * @return <code>String</code> - FEN of the <code>BoardModel</code>
     * @see <a href="https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation">More about FEN</a>
     */
    public String toFEN() {
        String[] fenStrings = toFENStrings();
        return String.format(Locale.ENGLISH, "%s %s %s %s %s %s", fenStrings[0], fenStrings[1], fenStrings[2], fenStrings[3], fenStrings[4], fenStrings[5]);
    }

    private String[] toFENStrings() {
        String[] FEN = new String[6];

        StringBuilder position = new StringBuilder();
        int i, j, c = 0;
        for (i = 7; i >= 0; i--) {
            for (j = 0; j < 8; j++) {
                Piece tempPiece = pieceAt(i, j);
                if (tempPiece == null) c++;
                else {
                    if (c > 0) {
                        position.append(c);
                        c = 0;
                    }
                    position.append(MiscMethods.getPieceChar(tempPiece));
                }
            }
            if (c > 0) {
                position.append(c);
                c = 0;
            }
            if (i != 0) position.append("/");
        }

        FEN[0] = String.valueOf(position);

        if (isWhiteToPlay()) FEN[1] = "w";
        else FEN[1] = "b";

        StringBuilder castleRights = getCastleRights();
        if (castleRights.isEmpty()) FEN[2] = "-";
        else FEN[2] = String.valueOf(castleRights);

        if (enPassantSquare.isEmpty()) FEN[3] = "-";
        else FEN[3] = enPassantSquare;

        FEN[4] = String.valueOf(halfMove);
        FEN[5] = String.valueOf(fullMove);

        return FEN;
    }

    private StringBuilder getCastleRights() {
        King whiteKing = getWhiteKing(), blackKing = getBlackKing();
        StringBuilder castleRights = new StringBuilder();
        if (whiteKing != null) {
            if (whiteKing.isNotShortCastled()) castleRights.append('K');
            if (whiteKing.isNotLongCastled()) castleRights.append('Q');
        }
        if (blackKing != null) {
            if (blackKing.isNotShortCastled()) castleRights.append('k');
            if (blackKing.isNotLongCastled()) castleRights.append('q');
        }
        return castleRights;
    }

    /**
     * Parses the valid FEN to BoardModel
     *
     * @param FEN Valid FEN String
     * @return <code>BoardModel|null</code>
     */
    public static BoardModel parseFEN(String FEN) {
        BoardModel boardModel = new BoardModel(false);
        Matcher matcher = Regexes.FENPattern.matcher(FEN);
        if (!matcher.find()) {
            System.out.println(TAG + " parseFEN: Invalid FEN! FEN didn't match the pattern");
            return null;
        }
        StringTokenizer FENTokens = new StringTokenizer(FEN, " ");
        int tokens = FENTokens.countTokens();
        if (tokens > 6 || tokens < 4) {
            System.out.println(TAG + " parseFEN: Invalid FEN! found " + tokens + " fields");
            return null;
        }
        String board = FENTokens.nextToken();
        String nextPlayer = FENTokens.nextToken();
        String castlingAvailability = FENTokens.nextToken();
        String enPassantSquare = FENTokens.nextToken();
        String halfMoveClock = "", fullMoveNumber = "";
        Player activePlayer;
        boolean whiteShortCastle = false, whiteLongCastle = false, blackShortCastle = false, blackLongCastle = false;

        if (FENTokens.hasMoreTokens()) halfMoveClock = FENTokens.nextToken();
        if (FENTokens.hasMoreTokens()) fullMoveNumber = FENTokens.nextToken();

        StringTokenizer boardTokens = new StringTokenizer(board, "/");
        if (boardTokens.countTokens() != 8) {
            System.out.println(TAG + " parseFEN: Invalid FEN! found " + tokens + " rows in board field");
            return null;
        }

//        Convert pieces to BoardModel
        int i, row = 7, col;
        while (boardTokens.hasMoreTokens()) {
            String rank = boardTokens.nextToken();
            for (i = 0, col = 0; i < rank.length(); i++) {
                Player player;
                Piece piece;
                char ch = rank.charAt(i);
                if (Character.isDigit(ch)) {
                    col += ch - '0';
                    continue;
                }
                if (i > 8) {
                    System.out.println(TAG + " parseFEN: Invalid FEN! found " + col + " columns in rank " + (i + 1));
                    return null;
                }
                player = Character.isUpperCase(ch) ? Player.WHITE : Player.BLACK;
                boolean isWhite = player == Player.WHITE;
                switch (Character.toLowerCase(ch)) {
                    case 'k':
                        piece = new King(player, row, col, isWhite ? Unicodes.KW : Unicodes.KB);
                        break;
                    case 'q':
                        piece = new Queen(player, row, col, isWhite ? Unicodes.QW : Unicodes.QB);
                        break;
                    case 'r':
                        piece = new Rook(player, row, col, isWhite ? Unicodes.RW : Unicodes.RB);
                        break;
                    case 'b':
                        piece = new Bishop(player, row, col, isWhite ? Unicodes.BW : Unicodes.BB);
                        break;
                    case 'n':
                        piece = new Knight(player, row, col, isWhite ? Unicodes.NW : Unicodes.NB);
                        break;
                    case 'p':
                        piece = new Pawn(player, row, col, isWhite ? Unicodes.PW : Unicodes.PB);
                        if (row != (isWhite ? 1 : 6)) piece.setMoved(true);
                        break;
                    default:
                        System.out.println(TAG + " parseFEN: Invalid FEN! found invalid character " + ch);
                        return null;
                }
                boardModel.addPiece(piece);
                col++;
            }
            row--;
        }

//        Player to play next move
        activePlayer = nextPlayer.equals("w") ? Player.WHITE : Player.BLACK;

//        Castling availability for each player
        if (!castlingAvailability.equals("-")) {
            whiteShortCastle = castlingAvailability.contains("K");
            whiteLongCastle = castlingAvailability.contains("Q");
            blackShortCastle = castlingAvailability.contains("k");
            blackLongCastle = castlingAvailability.contains("q");
        }

        if (!enPassantSquare.equals("-")) {
            boardModel.enPassantSquare = enPassantSquare;
            boardModel.enPassantPawn = (Pawn) boardModel.pieceAt(MiscMethods.toRow(enPassantSquare) - (activePlayer == Player.WHITE ? 1 : -1), MiscMethods.toCol(enPassantSquare));
        }
//        System.out.println(TAG + " parseFEN: Successfully parsed FEN to BoardModel");

//        System.out.println(TAG+ " "+String.format(Locale.ENGLISH, "\nGiven FEN: %s\nConverted BoardModel:%s\nConverted BoardModel FEN: %s\nPlayer to play: %s\nWhite ShortCastle: %b\tLongCastle: %b\nBlack ShortCastle: %b\tLongCastle: %b\nEnPassantPawn: %s", FEN, boardModel, boardModel.toFENStrings()[0], activePlayer, whiteShortCastle, whiteLongCastle, blackShortCastle, blackLongCastle, boardModel.enPassantPawn == null ? "-" : boardModel.enPassantPawn.getPosition()));
//        if (!halfMoveClock.isEmpty() && !fullMoveNumber.isEmpty())
//            System.out.println(TAG + " " + String.format("parseFEN: Half move clock: %s, Full move count: %s", halfMoveClock, fullMoveNumber));

        boardModel.setMoveClocks(Integer.parseInt(halfMoveClock), Integer.parseInt(fullMoveNumber));

//        System.out.println(TAG + " parseFEN: Valid FEN\n");
        return boardModel;
    }

    public void setMoveClocks(int halfMove, int fullMove) {
        this.halfMove = halfMove;
        this.fullMove = fullMove;
    }

    /**
     * @return List of all captured pieces
     */
    public ArrayList<Piece> getCapturedPieces() {
        ArrayList<Piece> capturedPieces = new ArrayList<>();
        for (Piece piece : pieces) if (piece.isCaptured()) capturedPieces.add(piece);
        return capturedPieces;
    }

    public boolean isWhiteToPlay() {
        return turn == Player.WHITE;
    }

    public void setWhiteToPlay(boolean whiteToPlay) {
        turn = whiteToPlay ? Player.WHITE : Player.BLACK;
    }
}