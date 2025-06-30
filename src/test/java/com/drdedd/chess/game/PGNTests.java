package com.drdedd.chess.game;

import com.drdedd.chess.game.gameData.ChessAnnotation;
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
        // Arrange
        String sanMove = "e4";
        String uciMove = "e2e4";

        // Act
        pgn.addMove(sanMove, uciMove);

        // Assert
        LinkedList<String> sanMoves = pgn.getMoves();
        LinkedList<String> uciMoves = pgn.getUCIMoves();

        assertEquals(1, sanMoves.size());
        assertEquals("e4", sanMoves.getFirst());

        assertEquals(1, uciMoves.size());
        assertEquals("e2e4", uciMoves.getFirst());
    }

    @Test
    void testRemoveLastMove() {
        // Arrange
        String sanMove1 = "e4";
        String uciMove1 = "e2e4";
        pgn.addMove(sanMove1, uciMove1);

        // Act
        pgn.removeLast();

        // Assert
        assertTrue(pgn.getMoves().isEmpty());
        assertTrue(pgn.getUCIMoves().isEmpty());
    }

    @Test
    void testSetWhiteBlack() {
        // Arrange
        String white = "Magnus Carlsen";
        String black = "Hikaru Nakamura";

        // Act
        pgn.setWhiteBlack(white, black);

        // Assert
        assertEquals(white, pgn.getWhite());
        assertEquals(black, pgn.getBlack());
    }

    @Test
    void testGetPGNMoves() {
        // Arrange
        pgn.addMove("e4", "e2e4");
        pgn.addMove("e5", "e7e5");

        // Act
        String pgnMoves = pgn.getPGNMoves();

        // Assert
        assertEquals("1. e4 e5", pgnMoves.trim());
    }

    @Test
    void testGetPGNCommented() {
        // Arrange
        pgn.addMove("e4", "e2e4");
        pgn.addMove("e5", "e7e5");

        // Mocking annotations and comments for the moves
        pgn.getPGNData().getAnnotationMap().put(0, ChessAnnotation.BEST);
        pgn.getPGNData().getCommentsMap().put(1, "Nice response!");

        // Act
        String pgnCommented = pgn.getPGNCommented();

        // Assert
        assertTrue(pgnCommented.contains(ChessAnnotation.BEST.getAnnotation()));
        assertTrue(pgnCommented.contains("Nice response!"));
    }

    @Test
    void testAddTag() {
        // Act
        pgn.addTag(PGN.TAG_ECO, "A01");

        // Assert
        assertEquals("A01", pgn.getPGNData().getTag(PGN.TAG_ECO, null));
    }

    @Test
    void testSetResult() {
        // Act
        pgn.setResult(PGN.RESULT_WHITE_WON);

        // Assert
        assertEquals(PGN.RESULT_WHITE_WON, pgn.getResult());
    }

    @Test
    void testGetTags() {
        // Act
        String tags = pgn.getTags();

        // Assert
        assertTrue(tags.contains("[White \"Magnus Carlsen\"]"));
        assertTrue(tags.contains("[Black \"Hikaru Nakamura\"]"));
        assertTrue(tags.contains("[Date \"2023-10-01\"]"));
    }

    @Test
    void testToString() {
        // Arrange
        pgn.addMove("e4", "e2e4");
        pgn.addMove("e5", "e7e5");
        pgn.setResult(PGN.RESULT_ONGOING);

        // Act
        String pgnString = pgn.toString();

        // Assert
        assertTrue(pgnString.contains("[White \"Magnus Carlsen\"]"));
        assertTrue(pgnString.contains("[Black \"Hikaru Nakamura\"]"));
        assertTrue(pgnString.contains("1. e4 e5"));
        assertTrue(pgnString.contains("[Result \"*\"]"));
    }

    @Test
    void testHasNoEval() {
        // Act
        boolean hasNoEval = pgn.hasNoEval();

        // Assert
        assertTrue(hasNoEval);
    }

    @Test
    void testIsFENEmpty() {
        // Act
        boolean isFENEmpty = pgn.isFENEmpty();

        // Assert
        assertTrue(isFENEmpty);
    }

    @Test
    void testAddAllTags() {
        // Arrange
        HashMap<String, String> newTags = new HashMap<>();
        newTags.put(PGN.TAG_ECO, "A01");
        newTags.put(PGN.TAG_OPENING, "Ruy Lopez");

        // Act
        pgn.addAllTags(newTags);

        // Assert
        assertEquals("A01", pgn.getPGNData().getTag(PGN.TAG_ECO, null));
        assertEquals("Ruy Lopez", pgn.getPGNData().getTag(PGN.TAG_OPENING, null));
    }

    @Test
    void testGetMoveAt() {
        // Arrange
        pgn.addMove("e4", "e2e4");
        pgn.addMove("e5", "e7e5");

        // Act
        String move = pgn.getMoveAt(1);

        // Assert
        assertEquals("e5", move);
    }

    @Test
    void testGetUCIMoveAt() {
        // Arrange
        pgn.addMove("e4", "e2e4");
        pgn.addMove("e5", "e7e5");

        // Act
        String uciMove = pgn.getUCIMoveAt(1);

        // Assert
        assertEquals("e7e5", uciMove);
    }

    @Test
    void testGetPlyCount() {
        // Arrange
        pgn.addMove("e4", "e2e4");
        pgn.addMove("e5", "e7e5");

        // Act
        int plyCount = pgn.getPlyCount();

        // Assert
        assertEquals(2, plyCount);
    }

}
