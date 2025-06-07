package com.drdedd.chess.game.pgn;

import com.drdedd.chess.game.gameData.ChessAnnotation;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

public class PGNData implements Serializable {
    @Getter
    private final LinkedList<String> sanMoves, uciMoves;
    @Getter
    private final LinkedHashMap<String, String> tagsMap;
    @Getter
    private final LinkedHashMap<Integer, String> evalMap, commentsMap, alternateMoveSequence;
    @Getter
    private final LinkedHashMap<Integer, ChessAnnotation> annotationMap;
    @Getter
    @Setter
    private LinkedList<String> tempMoves;

    public PGNData() {
        sanMoves = new LinkedList<>();
        uciMoves = new LinkedList<>();
        tagsMap = new LinkedHashMap<>();
        commentsMap = new LinkedHashMap<>();
        annotationMap = new LinkedHashMap<>();
        alternateMoveSequence = new LinkedHashMap<>();
        evalMap = new LinkedHashMap<>();
        tempMoves = new LinkedList<>();
    }

    public PGNData(LinkedList<String> sanMoves, LinkedList<String> uciMoves, LinkedHashMap<String, String> tagsMap, LinkedHashMap<Integer, String> commentsMap, LinkedHashMap<Integer, ChessAnnotation> annotationMap, LinkedHashMap<Integer, String> alternateMoveSequence, LinkedHashMap<Integer, String> evalMap) {
        this.sanMoves = sanMoves;
        this.uciMoves = uciMoves;
        this.tagsMap = tagsMap;
        this.commentsMap = commentsMap;
        this.annotationMap = annotationMap;
        this.alternateMoveSequence = alternateMoveSequence;
        this.evalMap = evalMap;
        tempMoves = new LinkedList<>();
    }

    public Set<String> getTags() {
        return tagsMap.keySet();
    }

    public String getTagOrDefault(String tagName, String defaultValue) {
        return tagsMap.getOrDefault(tagName, defaultValue);
    }

    public String getLastTempMove() {
        return tempMoves.getLast();
    }

    public void addTempMove(String move) {
        tempMoves.add(move);
    }

    public void addMove(String sanMove, String uciMove) {
        sanMoves.add(sanMove);
        uciMoves.add(uciMove);
    }

    public void addTag(String tag, String value) {
        tagsMap.put(tag, value);
    }

    public void addComment(int moveNo, String comment) {
        commentsMap.put(moveNo, comment);
    }

    public void addAnnotation(int moveNo, ChessAnnotation chessAnnotation) {
        if (chessAnnotation == null) return;
        annotationMap.put(moveNo, chessAnnotation);
    }

    public void addAlternateMoveSequence(int moveNo, String alternateMoveSequence) {
        this.alternateMoveSequence.put(moveNo, alternateMoveSequence);
    }

    public void addEval(int moveNo, String eval) {
        evalMap.put(moveNo, eval);
    }
}