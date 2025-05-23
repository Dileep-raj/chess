package com.drdedd.chess.game.pgn;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    public static final String TAG_ANALYZED_BY = "AnalyzedBy", TAG_ANNOTATOR = "Annotator";
    private final String FEN;
    private final LinkedHashMap<String, String> allTags = new LinkedHashMap<>();
    /**
     * -- SETTER --
     * Set termination message for the game
     * <p>
     * <p>
     * -- GETTER --
     * Get termination message of the game
     *
     * @param termination Termination message
     * @return <code>String</code> - Termination message
     */
    @Getter
    @Setter
    private String termination = "";
    private PGNData data;
    private AnalysisReport report;
    /**
     * -- GETTER --
     * <p>
     * <p>
     * -- SETTER --
     * Sets white to play
     *
     * @return <code>true|false</code> - White to play
     * @param whiteToPlay White to play
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
        allTags.put(TAG_APP, app);
        allTags.put(TAG_WHITE, white);
        allTags.put(TAG_BLACK, black);
        allTags.put(TAG_DATE, date);
        this.whiteToPlay = whiteToPlay;
        FEN = "";
//        moves.clear();
//        uciMoves.clear();
//        commentsMap = new LinkedHashMap<>();
//        moveAnnotationMap = new LinkedHashMap<>();
//        annotationMap = new LinkedHashMap<>();
//        alternateMoveSequence = new LinkedHashMap<>();
//        evalMap = new LinkedHashMap<>();
        lastBookMoveNo = -1;
        data = new PGNData();
    }

    /**
     * @param app         Name of app
     * @param white       Name of White player
     * @param black       Name of Black player
     * @param date        Date of the game
     * @param whiteToPlay Player to play
     * @param FEN         Starting position of the game
     */
    public PGN(String app, String white, String black, String date, boolean whiteToPlay, String FEN) {
        allTags.put(TAG_APP, app);
        allTags.put(TAG_WHITE, white);
        allTags.put(TAG_BLACK, black);
        allTags.put(TAG_DATE, date);
        this.whiteToPlay = whiteToPlay;
        this.FEN = FEN;
//        moves.clear();
//        uciMoves.clear();
//        commentsMap = new LinkedHashMap<>();
//        moveAnnotationMap = new LinkedHashMap<>();
//        annotationMap = new LinkedHashMap<>();
//        alternateMoveSequence = new LinkedHashMap<>();
//        evalMap = new LinkedHashMap<>();
        lastBookMoveNo = -1;
        data = new PGNData();
    }

    /**
     * Adds moves to the PGN moves list
     *
     * @param sanMove Move in Standard Algebraic Notation
     * @param uciMove Move in UCI format
     */
    public void addMove(String sanMove, String uciMove) {
        data.addMove(sanMove, uciMove);
//        data.getSANMoves().addLast(sanMove);
//        data.getUCIMoves().addLast(uciMove);
    }

    /**
     * Removes last move from PGN
     */
    public void removeLast() {
        if (!data.getSANMoves().isEmpty()) {
            data.getSANMoves().removeLast();
            data.getUCIMoves().removeLast();
        }
    }

    /**
     * Set White and Black player names
     *
     * @param white Name of white player
     * @param black Name of black player
     */
    public void setWhiteBlack(String white, String black) {
        allTags.put(TAG_WHITE, white);
        allTags.put(TAG_BLACK, black);
    }

    /**
     * @return <code>String</code> - White player name
     */
    public String getWhite() {
        return allTags.get(TAG_WHITE);
    }

    /**
     * @return <code>String</code> - Black player name
     */
    public String getBlack() {
        return allTags.get(TAG_BLACK);
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
    private String getTags() {
        StringBuilder tags = new StringBuilder();
        allTags.put(TAG_RESULT, getResult());
        if (!termination.isEmpty()) allTags.put(TAG_TERMINATION, termination);
        if (FEN != null && !FEN.isEmpty()) {
            allTags.put(TAG_SET_UP, "1");
            allTags.put(TAG_FEN, FEN);
        }
        Set<String> tagSet = allTags.keySet();
        for (String tag : tagSet) {
            String value = allTags.get(tag);
            if (value == null || value.isEmpty()) value = "?";
            tags.append(String.format("[%s \"%s\"]\n", tag, value));
        }
        return tags.toString();
    }

    /**
     * Returns result of the game
     *
     * @return <code> * | 0-1 | 1-0 | 1/2-1/2 </code>
     */
    public String getResult() {
        return allTags.getOrDefault(TAG_RESULT, "");
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
        int length = data.getSANMoves().size();
        for (int i = 0; i < length; i++) {
            if (i % 2 == 0) pgn.append(i / 2 + 1).append(". ");
            pgn.append(data.getSANMoves().get(i)).append(' ');
        }
        return pgn.toString();
    }

    /**
     * @return <code>String</code> - PGN moves with comments and annotation
     */
    public String getPGNCommented() {
        StringBuilder pgn = new StringBuilder();
        int length = data.getSANMoves().size();
        for (int i = 0; i < length; i++) {
            if (i % 2 == 0) pgn.append(i / 2 + 1).append(". ");
            pgn.append(data.getSANMoves().get(i));
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
        return data.getSANMoves().size();
    }

    /**
     * @param moveNo Move number
     * @return <code>String|null</code> - Move at given position
     */
    public String getMoveAt(int moveNo) {
        if (moveNo < data.getSANMoves().size()) return data.getSANMoves().get(moveNo);
        return null;
    }

    /**
     * @param moveNo Move number
     * @return <code>String|null</code> - UCI move at given position
     */
    public String getUCIMoveAt(int moveNo) {
        if (moveNo < data.getUCIMoves().size()) return data.getUCIMoves().get(moveNo);
        return null;
    }

    /**
     * @return List of moves
     */
    public LinkedList<String> getMoves() {
        return data.getSANMoves();
    }

    /**
     * @return List of UCI moves
     */
    public LinkedList<String> getUCIMoves() {
        return data.getUCIMoves();
    }

    /**
     * Adds tag to PGN tags
     *
     * @param tag   Tag name
     * @param value Tag value
     */
    public void addTag(String tag, String value) {
        allTags.put(tag, value);
    }

    public void addAllTags(HashMap<String, String> tags) {
        allTags.putAll(tags);
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
        return FEN.isEmpty();
    }

}