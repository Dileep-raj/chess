package com.drdedd.chess.game.pgn;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

/**
 * PGN (Portable Game Notation) is a standard text format used to record Chess game moves with standard notations
 *
 * @see <a href="https://en.wikipedia.org/wiki/Portable_Game_Notation"> More about PGN </a>
 */
public class PGN implements Serializable {
    /**
     * Constant String for special moves
     */
    public static final String APP_NAME = "";
    public static final String LONG_CASTLE = "O-O-O", SHORT_CASTLE = "O-O", CAPTURE = "x";
    public static final String RESULT_DRAW = "1/2-1/2", RESULT_WHITE_WON = "1-0", RESULT_BLACK_WON = "0-1", RESULT_ONGOING = "*";
    public static final String TAG_APP = "App", TAG_WHITE = "White", TAG_DATE = "Date", TAG_BLACK = "Black", TAG_SET_UP = "SetUp", TAG_FEN = "FEN", TAG_RESULT = "Result", TAG_TERMINATION = "Termination";
    public static final String TAG_ECO = "ECO", TAG_OPENING = "Opening", TAG_WHITE_TITLE = "WhiteTitle", TAG_BLACK_TITLE = "BlackTitle", TAG_WHITE_ELO = "WhiteElo", TAG_BLACK_ELO = "BlackElo";
    public static final String TAG_ANALYZED_BY = "AnalyzedBy", TAG_ANNOTATOR = "Annotator", UNKNOWN = "?";
    private final String startingPosition;
    /**
     * Termination message
     */
    @Getter
    @Setter
    private String termination = "";
    private PGNData data;
    /**
     * Flag - White to play
     */
    @Setter
    @Getter
    private boolean whiteToPlay;
    @Getter
    @Setter
    private int lastBookMoveNo;

    /**
     * @param app         Name of app
     * @param white       Name of White player
     * @param black       Name of Black player
     * @param date        Date of the game
     * @param whiteToPlay Player to play
     */
    public PGN(String app, String white, String black, String date, boolean whiteToPlay) {
        data = new PGNData();
        data.addTag(TAG_APP, app);
        data.addTag(TAG_WHITE, white);
        data.addTag(TAG_BLACK, black);
        data.addTag(TAG_DATE, date);
        this.whiteToPlay = whiteToPlay;
        startingPosition = "";
//        moves.clear();
//        uciMoves.clear();
//        commentsMap = new LinkedHashMap<>();
//        moveAnnotationMap = new LinkedHashMap<>();
//        annotationMap = new LinkedHashMap<>();
//        alternateMoveSequence = new LinkedHashMap<>();
//        evalMap = new LinkedHashMap<>();
        lastBookMoveNo = -1;
    }

    /**
     * @param app              Name of app
     * @param white            Name of White player
     * @param black            Name of Black player
     * @param date             Date of the game
     * @param whiteToPlay      Player to play
     * @param startingPosition Starting position of the game
     */
    public PGN(String app, String white, String black, String date, boolean whiteToPlay, String startingPosition) {
        data = new PGNData();
        data.addTag(TAG_APP, app);
        data.addTag(TAG_WHITE, white);
        data.addTag(TAG_BLACK, black);
        data.addTag(TAG_DATE, date);
        this.whiteToPlay = whiteToPlay;
        this.startingPosition = startingPosition;
//        moves.clear();
//        uciMoves.clear();
//        commentsMap = new LinkedHashMap<>();
//        moveAnnotationMap = new LinkedHashMap<>();
//        annotationMap = new LinkedHashMap<>();
//        alternateMoveSequence = new LinkedHashMap<>();
//        evalMap = new LinkedHashMap<>();
        lastBookMoveNo = -1;
    }

    /**
     * Adds moves to the PGN moves list
     *
     * @param sanMove Move in Standard Algebraic Notation
     * @param uciMove Move in UCI format
     */
    public void addMove(String sanMove, String uciMove) {
        data.addMove(sanMove, uciMove);
//        data.getSanMoves().addLast(sanMove);
//        data.getUciMoves().addLast(uciMove);
    }

    /**
     * Removes last move from PGN
     */
    public void removeLast() {
        if (!data.getSanMoves().isEmpty()) {
            data.getSanMoves().removeLast();
            data.getUciMoves().removeLast();
        }
    }

    /**
     * Set White and Black player names
     *
     * @param white Name of white player
     * @param black Name of black player
     */
    public void setWhiteBlack(String white, String black) {
        data.addTag(TAG_WHITE, white);
        data.addTag(TAG_BLACK, black);
    }

    /**
     * @return <code>String</code> - White player name
     */
    public String getWhite() {
        return data.getTag(TAG_WHITE, PGN.UNKNOWN);
    }

    /**
     * @return <code>String</code> - Black player name
     */
    public String getBlack() {
        return data.getTag(TAG_BLACK, PGN.UNKNOWN);
    }

    /**
     * Converts PGN to standard text format
     *
     * @return String - PGN with tags and moves
     */
    @Override
    public String toString() {
        return getTags() + getPGNCommented();
    }

    /**
     * PGN tags with their values
     *
     * @return String - PGN Tags text
     */
    public String getTags() {
        StringBuilder tags = new StringBuilder();
        data.addTag(TAG_RESULT, getResult());
        if (!termination.isEmpty()) data.addTag(TAG_TERMINATION, termination);
        if (startingPosition != null && !startingPosition.isEmpty()) {
            data.addTag(TAG_SET_UP, "1");
            data.addTag(TAG_FEN, startingPosition);
        }
        Set<String> tagSet = data.getTagNames();
        for (String tag : tagSet) tags.append(String.format("[%s \"%s\"]\n", tag, data.getTag(tag, PGN.UNKNOWN)));
        return tags.toString();
    }

    /**
     * Returns result of the game
     *
     * @return <code> * | 0-1 | 1-0 | 1/2-1/2 </code>
     */
    public String getResult() {
        return data.getTag(TAG_RESULT, "");
    }

    /**
     * Adds game result to the tag data
     *
     * @param result Result of the game
     */
    public void setResult(String result) {
        if (!result.isEmpty()) addTag(TAG_RESULT, result);
    }

    /**
     * @return <code>String</code> - PGN moves without tags, comments and annotations
     */
    public String getPGNMoves() {
        StringBuilder pgn = new StringBuilder();
        int length = data.getSanMoves().size();
        for (int i = 0; i < length; i++) {
            if (i % 2 == 0) pgn.append(i / 2 + 1).append(". ");
            pgn.append(data.getSanMoves().get(i)).append(' ');
        }
        return pgn.toString();
    }

    /**
     * @return <code>String</code> - PGN moves with comments and annotation
     */
    public String getPGNCommented() {
        StringBuilder pgn = new StringBuilder();
        int length = data.getSanMoves().size();
        for (int i = 0; i < length; i++) {
            if (i % 2 == 0) pgn.append(i / 2 + 1).append(". ");
            pgn.append(data.getSanMoves().get(i));
            if (data.getAnnotationMap().containsKey(i)) pgn.append(data.getAnnotationMap().get(i).getAnnotation());
            pgn.append(' ');
            if (data.getCommentsMap().containsKey(i)) pgn.append(data.getCommentsMap().get(i)).append(' ');
            if (data.getAlternateMoveSequence().containsKey(i))
                pgn.append(data.getAlternateMoveSequence().get(i)).append(' ');
        }
        return pgn.toString();
    }

    /**
     * @return Number of half moves played
     */
    public int getPlyCount() {
        return data.getSanMoves().size();
    }

    /**
     * @param moveNo Move number
     * @return <code>String|null</code> - Move at given position
     */
    public String getMoveAt(int moveNo) {
        if (moveNo < data.getSanMoves().size()) return data.getSanMoves().get(moveNo);
        return null;
    }

    /**
     * @param moveNo Move number
     * @return <code>String|null</code> - UCI move at given position
     */
    public String getUCIMoveAt(int moveNo) {
        if (moveNo < data.getUciMoves().size()) return data.getUciMoves().get(moveNo);
        return null;
    }

    /**
     * @return List of moves
     */
    public LinkedList<String> getMoves() {
        return data.getSanMoves();
    }

    /**
     * @return List of UCI moves
     */
    public LinkedList<String> getUCIMoves() {
        return data.getUciMoves();
    }

    /**
     * Adds tag to PGN tags
     *
     * @param tag   Tag name
     * @param value Tag value
     */
    public void addTag(String tag, String value) {
        data.addTag(tag, value);
    }

    public void addAllTags(HashMap<String, String> tags) {
        data.addTags(tags);
    }

    public void setPGNData(PGNData pgnData) {
        this.data = pgnData;
    }

    public PGNData getPGNData() {
        return data;
    }

    public boolean hasNoEval() {
        return data.getEvalMap().isEmpty() || data.getEvalMap().size() == 1;
    }

    public boolean isFENEmpty() {
        return startingPosition.isEmpty();
    }

}