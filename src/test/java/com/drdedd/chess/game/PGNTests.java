package com.drdedd.chess.game;

import com.drdedd.chess.game.data.ChessAnnotation;
import com.drdedd.chess.game.pgn.PGN;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PGNTests {

    private PGN pgn;

    @BeforeEach
    void setup() {
        // Set up a default PGN instance for testing
        pgn = new PGN("ChessApp", "Magnus Carlsen", "Hikaru Nakamura", "2023-10-01", true);
    }

    @Test
    void testAddMove() {
        String sanMove = "e4", uciMove = "e2e4";

        pgn.addMove(sanMove, uciMove);

        LinkedList<String> sanMoves = pgn.getSanMoves();
        LinkedList<String> uciMoves = pgn.getUCIMoves();

        assertEquals(1, sanMoves.size());
        assertEquals("e4", sanMoves.getFirst());

        assertEquals(1, uciMoves.size());
        assertEquals("e2e4", uciMoves.getFirst());
    }

    @Test
    void testRemoveLastMove() {
        String sanMove1 = "e4";
        String uciMove1 = "e2e4";
        pgn.addMove(sanMove1, uciMove1);

        pgn.removeLast();

        assertTrue(pgn.getSanMoves().isEmpty());
        assertTrue(pgn.getUCIMoves().isEmpty());
    }

    @Test
    void testSetWhiteBlack() {
        String white = "Magnus Carlsen";
        String black = "Hikaru Nakamura";

        pgn.setWhiteBlack(white, black);

        assertEquals(white, pgn.getWhite());
        assertEquals(black, pgn.getBlack());
    }

    @Test
    void testGetSanMoves() {
        pgn.addMove("e4", "e2e4");
        pgn.addMove("e5", "e7e5");

        String pgnMoves = pgn.getPGNMoves();

        assertEquals("1. e4 e5", pgnMoves.trim());
    }

    @Test
    void testGetPGNCommented() {
        pgn.addMove("e4", "e2e4");
        pgn.addMove("e5", "e7e5");

        // Mocking annotations and comments for the moves
        pgn.getData().addAnnotation(0, ChessAnnotation.BEST);
        pgn.getData().addComment(1, "Nice response!");

        String pgnCommented = pgn.getPGNCommented();

        assertTrue(pgnCommented.contains(ChessAnnotation.BEST.getAnnotation()));
        assertTrue(pgnCommented.contains("Nice response!"));
    }

    @Test
    void testAddTag() {
        pgn.addTag(PGN.TAG_ECO, "A01");

        assertEquals("A01", pgn.getTag(PGN.TAG_ECO, null));
    }

    @Test
    void testSetResult() {
        pgn.setResult(PGN.RESULT_WHITE_WON);

        assertEquals(PGN.RESULT_WHITE_WON, pgn.getResult());
    }

    @Test
    void testGetTags() {
        String tags = pgn.getTags();

        assertTrue(tags.contains("[White \"Magnus Carlsen\"]"));
        assertTrue(tags.contains("[Black \"Hikaru Nakamura\"]"));
        assertTrue(tags.contains("[Date \"2023-10-01\"]"));
    }

    @Test
    void testToString() {
        pgn.addMove("e4", "e2e4");
        pgn.addMove("e5", "e7e5");
        pgn.setResult(PGN.RESULT_ONGOING);

        String pgnString = pgn.toString();

        assertTrue(pgnString.contains("[White \"Magnus Carlsen\"]"));
        assertTrue(pgnString.contains("[Black \"Hikaru Nakamura\"]"));
        assertTrue(pgnString.contains("1. e4 e5"));
        assertTrue(pgnString.contains("[Result \"*\"]"));
    }

    @Test
    void testHasNoEval() {
        assertTrue(pgn.hasNoEval());
    }

    @Test
    void testIsFENEmpty() {
        assertTrue(pgn.isFENEmpty());
    }

    @Test
    void testAddAllTags() {
        HashMap<String, String> newTags = new HashMap<>();
        newTags.put(PGN.TAG_ECO, "A01");
        newTags.put(PGN.TAG_OPENING, "Ruy Lopez");

        pgn.addAllTags(newTags);

        assertEquals("A01", pgn.getData().getTag(PGN.TAG_ECO, null));
        assertEquals("Ruy Lopez", pgn.getData().getTag(PGN.TAG_OPENING, null));
    }

    @Test
    void testGetMoveAt() {
        pgn.addMove("e4", "e2e4");
        pgn.addMove("e5", "e7e5");

        assertEquals("e5", pgn.getMoveAt(1));
    }

    @Test
    void testGetUCIMoveAt() {
        pgn.addMove("e4", "e2e4");
        pgn.addMove("e5", "e7e5");

        assertEquals("e7e5", pgn.getUCIMoveAt(1));
    }

    @Test
    void testGetPlyCount() {
        pgn.addMove("e4", "e2e4");
        pgn.addMove("e5", "e7e5");

        assertEquals(2, pgn.getPlyCount());
    }

}
