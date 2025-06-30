package com.drdedd.chess.game;

import com.drdedd.chess.game.data.FENs;
import com.drdedd.chess.game.gameData.Rank;
import com.drdedd.chess.game.pieces.King;
import com.drdedd.chess.game.pieces.Pawn;
import com.drdedd.chess.game.pieces.Piece;
import com.drdedd.chess.game.pieces.Queen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BoardModelTests {

    private BoardModel boardModel;

    @BeforeEach
    void setUp() {
        boardModel = new BoardModel(true); // initializing the board with default setup
    }

    @Test
    void testResetBoard() {
        boardModel.resetBoard();

        // Check the number of pieces (should be 32 at the start)
        assertEquals(32, boardModel.pieces.size());

        // Check if the White King is at the correct position
        King whiteKing = boardModel.getWhiteKing();
        assertNotNull(whiteKing);
        assertEquals(0, whiteKing.getRow());
        assertEquals(4, whiteKing.getCol());

        // Check if the Black King is at the correct position
        King blackKing = boardModel.getBlackKing();
        assertNotNull(blackKing);
        assertEquals(7, blackKing.getRow());
        assertEquals(4, blackKing.getCol());
    }

    @Test
    void testCapturePiece() {
        boardModel.resetBoard();
        Piece whitePawn = boardModel.pieceAt(1, 0); // The white pawn at row 1, column 0

        assertFalse(whitePawn.isCaptured());
        boardModel.capturePiece(whitePawn);
        assertTrue(whitePawn.isCaptured());
    }

    @Test
    void testEnPassant() {
        // Setting up En Passant position
        boardModel = BoardModel.parseFEN("5k2/8/8/8/2Pp4/8/8/K7 b - c3 0 1");

        assertNotNull(boardModel);

        assertEquals("c3", boardModel.enPassantSquare);
        assertNotNull(boardModel.enPassantPawn, boardModel.unicode() + "\n" + boardModel.toFEN());
    }

    @Test
    void testPromotePawnToQueen() {
        Pawn pawn = (Pawn) boardModel.pieceAt(1, 0); // The white pawn at row 1, column 0
        assertNotNull(pawn);
        Piece promotedPiece = boardModel.promote(pawn, Rank.QUEEN, 0, 0); // Promote to Queen
        assertInstanceOf(Queen.class, promotedPiece);
        assertEquals(0, promotedPiece.getRow());
        assertEquals(0, promotedPiece.getCol());
    }

    @Test
    void testToFEN() {
        boardModel.resetBoard();
        String fen = boardModel.toFEN();

        String expectedFEN = FENs.defaultPosition;
        assertEquals(expectedFEN, fen);
    }

    @Test
    void testParseFEN() {
        String validFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1";
        BoardModel parsedBoard = BoardModel.parseFEN(validFEN);

        assertNotNull(parsedBoard);
        assertEquals(32, parsedBoard.pieces.size());
    }

    @Test
    void testInvalidFEN() {
        String invalidFEN = "invalidFENString";
        BoardModel parsedBoard = BoardModel.parseFEN(invalidFEN);

        assertNull(parsedBoard);
    }

    @Test
    void testGetCapturedPieces() {
        boardModel.resetBoard();
        Piece whitePawn = boardModel.pieceAt(1, 0); // White pawn at (1, 0)
        boardModel.capturePiece(whitePawn);

        List<Piece> capturedPieces = boardModel.getCapturedPieces();
        assertEquals(1, capturedPieces.size());
        assertTrue(capturedPieces.contains(whitePawn));
    }

    @Test
    void testClone() {
        boardModel.resetBoard();
        BoardModel clonedBoard = boardModel.clone();

        assertNotSame(boardModel, clonedBoard);
        assertEquals(boardModel.pieces.size(), clonedBoard.pieces.size());
        assertNotSame(boardModel.pieces, clonedBoard.pieces);

        // Check if the pieces are also cloned properly
        for (Piece piece : boardModel.pieces) {
            Piece clonedPiece = clonedBoard.pieceAt(piece.getRow(), piece.getCol());
            assertNotSame(piece, clonedPiece);
            assertEquals(piece.getRank(), clonedPiece.getRank());
        }
    }

    @Test
    void testToString() {
        boardModel.resetBoard();
        String expected = "Board:\n 8 r n b q k b n r \n 7 p p p p p p p p \n 6 - - - - - - - - \n 5 - - - - - - - - \n 4 - - - - - - - - \n 3 - - - - - - - - \n 2 P P P P P P P P \n 1 R N B Q K B N R \n   a b c d e f g h";

        assertEquals(expected, boardModel.toString().trim());
    }
}
