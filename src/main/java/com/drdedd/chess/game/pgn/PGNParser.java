package com.drdedd.chess.game.pgn;

import com.drdedd.chess.game.GameLogic;
import com.drdedd.chess.game.Openings;
import com.drdedd.chess.game.ParsedGame;
import com.drdedd.chess.game.data.Regexes;
import com.drdedd.chess.game.gameData.ChessAnnotation;
import com.drdedd.chess.game.gameData.Player;
import com.drdedd.chess.game.gameData.Rank;
import com.drdedd.chess.game.pieces.King;
import com.drdedd.chess.game.pieces.Pawn;
import com.drdedd.chess.game.pieces.Piece;
import com.drdedd.chess.misc.Log;
import com.drdedd.chess.misc.MiscMethods;
import lombok.Getter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PGN parser to validate PGN moves and convert to game objects
 */
public class PGNParser extends Thread {
    private static final String TAG = "PGNParser";
    private final LinkedList<String> invalidWords;
    private final String pgnContent;
    private final PGNData pgnData;
    private GameLogic gameLogic;
    @Getter
    private ParsedGame parsedGame;

    /**
     * @param pgnContent PGN in <code>String</code> format
     */
    public PGNParser(String pgnContent) {
        this.pgnContent = pgnContent;
        pgnData = new PGNData();
        invalidWords = new LinkedList<>();
    }

    /**
     * Parse the given PGN content asynchronously
     */
    @Override
    public void run() {
        super.run();
        parse();
    }

    /**
     * Parse the given PGN content synchronously
     */
    public void parse() {
        boolean readResult;
        long start, end;

        start = System.nanoTime();
        readResult = readPGN();
        end = System.nanoTime();

        if (readResult) {
            Log.printTime(TAG + " Reading PGN", end - start);
            Log.d(TAG, " run: No syntax errors in PGN");

            gameLogic = new GameLogic(pgnData);

            start = System.nanoTime();
            boolean parseResult = parsePGN();
            end = System.nanoTime();

            if (parseResult) {
                Log.d(TAG, String.format(" run: Time to Parse: %,3d ns", end - start));
                Log.d(TAG, String.format(" run: Game valid and parsed!%nFinal position:%s", gameLogic.getBoardModel()));

                String opening, eco;
                int lastBookMove = -1;
                if (gameLogic.getPGN().isFENEmpty()) {
                    start = System.nanoTime();
                    Openings openings = Openings.getInstance();
                    String openingResult = openings.searchOpening(gameLogic.getPGN().getUCIMoves());
                    end = System.nanoTime();

                    String[] split = openingResult.split(Openings.separator);
                    lastBookMove = Integer.parseInt(split[0]);
                    if (lastBookMove != -1 && split.length == 3) {
                        Log.printTime(TAG + " searching opening", end - start);
                        eco = split[1];
                        opening = split[2];
                        gameLogic.getPGN().setLastBookMoveNo(lastBookMove);
                        gameLogic.getPGN().addTag(PGN.TAG_ECO, eco);
                        gameLogic.getPGN().addTag(PGN.TAG_OPENING, opening);
                        for (int i = 0; i <= lastBookMove; i++)
                            gameLogic.getPGN().getPGNData().addAnnotation(i, ChessAnnotation.BOOK);
                    } else {
                        opening = eco = "";
                        Log.d(TAG, String.format(" readPGN: Opening not found!\n%s\nMoves: %s", Arrays.toString(split), gameLogic.getPGN().getUCIMoves().subList(0, Math.min(gameLogic.getPGN().getUCIMoves().size(), 10))));
                    }
                } else opening = eco = "";

                parsedGame = new ParsedGame(gameLogic.getBoardModelStack(), gameLogic.getFENs(), gameLogic.getPGN(), eco, opening, lastBookMove);
            } else Log.d(TAG, " run: Game not parsed!");
        }
        Log.d(TAG, " run: Total invalid words: " + invalidWords.size());
    }

    /**
     * Reads each word in the PGN and checks syntax
     *
     * @return <code>true|false</code> - PGN is syntactically valid
     */
    private boolean readPGN() {
        int moveCount = -1;
        readTags(pgnContent);

        String pgnMoves = pgnContent.replaceAll(Regexes.tagsRegex, "").trim();

        String firstWord = pgnMoves.split(" ")[0];
        int initialPos = 0;
        if (!firstWord.matches(Regexes.singleMoveStrictRegex)) {
            Matcher startingMoveMatcher = Regexes.startingMovePattern.matcher(pgnMoves);
            boolean foundMoves = startingMoveMatcher.find();
            if (!foundMoves) {
                String error = "No moves in PGN!";
                Log.d(TAG, String.format(" readPGN: %s\n%s", error, pgnContent));
                return false;
            }
            initialPos = startingMoveMatcher.start();
        }

        Scanner PGNReader = new Scanner(pgnMoves.substring(initialPos));

//      Iterate through every word in the PGN
        while (PGNReader.hasNext()) {
            String word = null;
            try {
                word = PGNReader.next();

//              If comment is found, extract full comment
                if (word.startsWith("{")) {
                    StringBuilder commentBuilder = new StringBuilder(word);
                    while (PGNReader.hasNext()) {
                        if (word.endsWith("}")) break;
                        else if (word.contains("}")) {
                            commentBuilder = new StringBuilder(word.substring(0, word.indexOf('}') + 1));
                            if (word.contains("("))
                                extractAlternateMoves(moveCount, word.substring(word.indexOf('(')), PGNReader);
                            break;
                        }

                        word = PGNReader.next();
                        if (word.contains("}")) {
                            commentBuilder.append(' ').append(word, 0, word.indexOf('}') + 1);
                            if (word.contains("("))
                                extractAlternateMoves(moveCount, word.substring(word.indexOf('(')), PGNReader);
                            break;
                        }
                        commentBuilder.append(' ').append(word);
                    }
                    String comment = commentBuilder.toString();
                    pgnData.addComment(moveCount, comment);
                    findMoveFeedback(comment, moveCount);
                    findEval(comment, moveCount);
                    continue;
                }

                if (word.startsWith("(")) {
                    extractAlternateMoves(moveCount, word, PGNReader);
                    continue;
                }

//              If a move is found add move to the moves list
                if (word.matches(Regexes.singleMoveStrictRegex)) {
                    String move = word.replaceAll(Regexes.moveNumberRegex, "");
                    pgnData.addTempMove(move);
                    moveCount++;
                    findMoveFeedback(word, moveCount);
                    continue;
                }

                if (word.matches(Regexes.numberedAnnotationRegex)) {
                    pgnData.addAnnotation(moveCount, ChessAnnotation.getAnnotation(word));
                    continue;
                }

                if (word.matches(Regexes.resultRegex)) {
                    pgnData.addEval(moveCount, word);
                    continue;
                }

                if (word.matches(Regexes.moveNumberStrictRegex) || word.matches(Regexes.commentNumberStrictRegex))
                    continue;

                invalidWords.add(word + (pgnData.getTempMoves().isEmpty() ? "" : ", after move: " + pgnData.getLastTempMove()));
                throw new InvalidPGNException(pgnContent, "Invalid word: " + invalidWords.getLast());
            } catch (Exception e) {
                e.printStackTrace(System.err);
                String error = "Error at :" + word;
                Log.e(TAG, " readPGN: " + error, e);
            }
        }
        return true;
    }

    /**
     * Parses and converts PGN to game objects, by evaluating each move validity
     *
     * @return <code>true|false</code> - Whether all PGN moves are valid
     */
    private boolean parsePGN() {
        char ch;
        LinkedList<String> moves = pgnData.getTempMoves();
        int i, startRow, startCol, destRow, destCol, moveNo = 0;
        boolean promotion;
        Rank rank = null, promotionRank;
        Piece piece;
        Player player;

        for (String move : moves) {
            move = move.trim();
//            Log.d(TAG, " parsePGN: Move: " + move);
            startRow = -1;
            startCol = -1;
            destRow = -1;
            destCol = -1;
            promotion = false;
            promotionRank = null;
            piece = null;
            player = gameLogic.playerToPlay();

            try {
                if (move.startsWith(PGN.LONG_CASTLE)) {
                    King king = gameLogic.isWhiteToPlay() ? gameLogic.getBoardModel().getWhiteKing() : gameLogic.getBoardModel().getBlackKing();
                    if (king.canLongCastle(gameLogic)) {
                        gameLogic.move(king.getRow(), king.getCol(), king.getRow(), king.getCol() - 2);
                    }
                    continue;
                }
                if (move.startsWith(PGN.SHORT_CASTLE)) {
                    King king = gameLogic.isWhiteToPlay() ? gameLogic.getBoardModel().getWhiteKing() : gameLogic.getBoardModel().getBlackKing();
                    if (king.canShortCastle(gameLogic)) {
                        gameLogic.move(king.getRow(), king.getCol(), king.getRow(), king.getCol() + 2);
                    }
                    continue;
                }

                ch = move.charAt(0);
                if (Character.isLetter(ch)) rank = switch (ch) {
                    case 'K' -> Rank.KING;
                    case 'Q' -> Rank.QUEEN;
                    case 'R' -> Rank.ROOK;
                    case 'N' -> Rank.KNIGHT;
                    case 'B' -> Rank.BISHOP;
                    default -> Rank.PAWN;
                };

                label:
                for (i = 0; i < move.length(); i++) {
                    ch = move.charAt(i);
                    switch (ch) {
                        case 'a':
                        case 'b':
                        case 'c':
                        case 'd':
                        case 'e':
                        case 'f':
                        case 'g':
                        case 'h':
                            if (destCol != -1) startCol = destCol;
                            destCol = ch - 'a';
                            break;

                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                            if (destRow != -1) startRow = destRow;
                            destRow = ch - '1';
                            break;

                        case '=':
                            promotion = true;
                            ch = move.charAt(i + 1);
                            Log.d(TAG, " parsePGN: Promotion");
                            switch (ch) {
                                case 'Q':
                                    promotionRank = Rank.QUEEN;
                                    break;
                                case 'R':
                                    promotionRank = Rank.ROOK;
                                    break;
                                case 'N':
                                    promotionRank = Rank.KNIGHT;
                                    break;
                                case 'B':
                                    promotionRank = Rank.BISHOP;
                                    break;
                            }
                            Log.d(TAG, " parsePGN: Promotion rank: " + promotionRank);
                            if (promotionRank == null) return false;
                            break label;

                        case 'Q':
                        case 'R':
                        case 'N':
                        case 'B':
                            if (destCol != -1 && destRow != -1) {
                                promotion = true;
                                Log.d(TAG, " parsePGN: Promotion");
                                switch (ch) {
                                    case 'Q':
                                        promotionRank = Rank.QUEEN;
                                        break;
                                    case 'R':
                                        promotionRank = Rank.ROOK;
                                        break;
                                    case 'N':
                                        promotionRank = Rank.KNIGHT;
                                        break;
                                    case 'B':
                                        promotionRank = Rank.BISHOP;
                                        break;
                                }
                                Log.d(TAG, " parsePGN: Promotion rank: " + promotionRank);
                                break label;
                            }

                        case 'K':
                        case 'P':
                        case 'x':
                        case '+':
                        case '#':
                            break;
                    }
                }

                if (startRow != -1 && startCol != -1) {
                    piece = gameLogic.getBoardModel().pieceAt(startRow, startCol);
                    Log.d(TAG, String.format(" parsePGN: piece at %s piece: %s", MiscMethods.toNotation(startRow, startCol), piece));
                } else if (startCol != -1) {
                    piece = searchCol(player, rank, startCol, destRow, destCol);
                    Log.d(TAG, String.format(" parsePGN: searched col: %d piece:%s", startCol, piece));
                } else if (startRow != -1) {
                    piece = searchRow(player, rank, startRow, destRow, destCol);
                    Log.d(TAG, String.format(" parsePGN: searched row: %d piece: %s", startRow, piece));
                }

                if (piece == null) {
                    piece = searchPiece(player, rank, destRow, destCol);
                    Log.d(TAG, " parsePGN: piece searched");
                }

                if (piece != null && promotion) {
                    Pawn pawn = (Pawn) piece;
                    if (gameLogic.promote(pawn, destRow, destCol, pawn.getRow(), pawn.getCol(), promotionRank)) {
                        Log.d(TAG, String.format(" parsePGN: Promoted to %s at %s", promotionRank, MiscMethods.toNotation(destRow, destCol)));
                        continue;
                    }
                }

                if (piece != null) {
                    if (gameLogic.move(piece.getRow(), piece.getCol(), destRow, destCol)) {
                        Log.d(TAG, String.format(" parsePGN: Move success %s", move));
                    } else {
                        Log.d(TAG, " parsePGN: Second search!");
                        LinkedHashSet<Piece> pieces = gameLogic.getBoardModel().pieces, tempPieces = new LinkedHashSet<>();
                        for (Piece tempPiece : pieces)
                            if (tempPiece.getPlayer() == player && tempPiece.getRank() == rank) {
                                if (startRow != -1 && tempPiece.getRow() == startRow) {
                                    tempPieces.add(tempPiece);
                                } else if (startCol != -1 && tempPiece.getCol() == startCol) {
                                    tempPieces.add(tempPiece);
                                } else tempPieces.add(tempPiece);
                            }

                        for (Piece tempPiece : tempPieces)
                            if (gameLogic.getAllLegalMoves().containsKey(tempPiece.getSquare()) && Objects.requireNonNull(gameLogic.getAllLegalMoves().get(tempPiece.getSquare())).contains(destCol + destRow * 8)) {
                                piece = tempPiece;
                            }

                        if (gameLogic.move(piece.getRow(), piece.getCol(), destRow, destCol)) {
                            Log.d(TAG, " parsePGN: Move success after 2nd search! " + move);
                        } else {
                            StringBuilder legalMoves = new StringBuilder();
                            HashSet<Integer> pieceLegalMoves = gameLogic.getAllLegalMoves().get(piece.getSquare());
                            if (pieceLegalMoves != null) for (int legalMove : pieceLegalMoves)
                                legalMoves.append(MiscMethods.toNotation(legalMove)).append(' ');
                            System.err.println(TAG + String.format(" parsePGN: Move failed: %s%nPiece: %s%nLegalMoves: %s", move, piece, legalMoves));
                            return false;
                        }
                    }
                } else {
                    Log.d(TAG, String.format(" parsePGN: Move invalid! Piece not found! %s %s (%d,%d) -> %s move: %s", player, rank, startRow, startCol, MiscMethods.toNotation(destRow, destCol), move));
                    return false;
                }
            } catch (Exception e) {
                Log.e(TAG, " parsePGN: Error occurred after move " + move, e);
                return false;
            }
        }

        gameLogic.getPGN().addAllTags(pgnData.getTagsMap());
        return true;
    }

    /**
     * Search for piece from a specific row
     *
     * @param player  Player of the piece
     * @param rank    Rank of the piece
     * @param row     Row to be searched
     * @param destRow Destination row
     * @param destCol Destination column
     * @return <code>Piece|null</code>
     */
    public Piece searchRow(Player player, Rank rank, int row, int destRow, int destCol) {
        for (Piece piece : gameLogic.getBoardModel().pieces) {
            if (piece.getPlayer() != player || piece.isCaptured()) continue;
            if (piece.getRank() == rank && row == piece.getRow() && piece.canMoveTo(gameLogic, destRow, destCol))
                return piece;
        }
        return null;
    }

    /**
     * Search for piece from a specific column
     *
     * @param player  Player of the piece
     * @param rank    Rank of the piece
     * @param col     Column to be searched
     * @param destRow Destination row
     * @param destCol Destination column
     * @return <code>Piece|null</code>
     */
    public Piece searchCol(Player player, Rank rank, int col, int destRow, int destCol) {
        for (Piece piece : gameLogic.getBoardModel().pieces) {
            if (piece.getPlayer() != player || piece.isCaptured()) continue;
            if (piece.getRank() == rank && col == piece.getCol() && piece.canMoveTo(gameLogic, destRow, destCol))
                return piece;
        }
        return null;
    }

    /**
     * Search for piece from a specific position
     *
     * @param player Player of the piece
     * @param rank   Rank of the piece
     * @param row    Row to be searched
     * @param col    Col to be searched
     * @return <code>Piece|null</code>
     */
    public Piece searchPiece(Player player, Rank rank, int row, int col) {
        for (Piece piece : gameLogic.getBoardModel().pieces) {
            if (piece.getPlayer() != player || piece.isCaptured()) continue;
            if (piece.getRank() == rank && piece.canMoveTo(gameLogic, row, col)) return piece;
        }
        return null;
    }

    /**
     * Extracts the given alternate move sequence for the previous move
     *
     * @param moveCount Move number
     * @param word      Starting word of alternate move sequence
     * @param PGNReader PGN scanner
     */
    private void extractAlternateMoves(int moveCount, String word, Scanner PGNReader) {
        StringBuilder movesBuilder = new StringBuilder(word.substring(word.indexOf("(")));
        while (PGNReader.hasNext()) {
            if (word.endsWith(")")) break;
            word = PGNReader.next();
            movesBuilder.append(' ').append(word);
            if (word.endsWith(")")) break;
        }
        pgnData.addAlternateMoveSequence(moveCount, movesBuilder.toString());
    }

    /**
     * Extracts move feedback if any
     *
     * @param word      Move word or comment
     * @param moveCount Move number
     */
    private void findMoveFeedback(String word, int moveCount) {
        String feedback = null;
        Matcher feedbackMatcher = Regexes.moveAnnotationPattern.matcher(word);
        if (feedbackMatcher.find()) feedback = feedbackMatcher.group();
        if (feedback != null) pgnData.addAnnotation(moveCount, ChessAnnotation.getAnnotation(feedback));
    }

    /**
     * Extracts evaluation of the given move if any
     *
     * @param comment   Comment of the move
     * @param moveCount Move number
     */
    private void findEval(String comment, int moveCount) {
        Matcher matcher = Pattern.compile("\\[%eval [-+]?[#M]?-?[\\d.]+]").matcher(comment);
        if (matcher.find()) {
            String group = matcher.group().replace('#', 'M');
            group = group.substring(group.indexOf(" "), group.length() - 1).trim();
            if (group.contains("-")) group = "-" + group.replace("-", "");
            else if (!group.contains("+")) group = "+" + group;
            pgnData.addEval(moveCount, group);
        }
    }

    /**
     * Extracts Tags from the PGN
     *
     * @param pgn PGN in <code>String</code> format
     */
    private void readTags(String pgn) {
        Scanner tagReader = new Scanner(pgn);
        String word = null;
        while (tagReader.hasNext()) {
            try {
                word = tagReader.next();
                if (word.startsWith("1.")) return;
                if (word.startsWith("[")) {
                    String tag = word.substring(1);
                    StringBuilder tagBuilder = new StringBuilder();
                    while (tagReader.hasNext()) {
                        word = tagReader.next();
                        tagBuilder.append(word).append(' ');
                        if (word.endsWith("]")) break;
                    }
                    String value = tagBuilder.substring(tagBuilder.indexOf("\"") + 1, tagBuilder.lastIndexOf("\""));
                    pgnData.addTag(tag, value);
                }
            } catch (Exception e) {
                Log.e(TAG, " readTags: Error at : " + word, e);
            }
        }
    }
}
