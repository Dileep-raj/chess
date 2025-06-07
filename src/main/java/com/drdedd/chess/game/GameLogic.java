package com.drdedd.chess.game;

import com.drdedd.chess.game.data.Regexes;
import com.drdedd.chess.game.gameData.ChessState;
import com.drdedd.chess.game.gameData.Player;
import com.drdedd.chess.game.gameData.Rank;
import com.drdedd.chess.game.interfaces.GameLogicInterface;
import com.drdedd.chess.game.interfaces.GameUI;
import com.drdedd.chess.game.pgn.PGN;
import com.drdedd.chess.game.pgn.PGNData;
import com.drdedd.chess.game.pieces.King;
import com.drdedd.chess.game.pieces.Pawn;
import com.drdedd.chess.game.pieces.Piece;
import com.drdedd.chess.misc.Log;
import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;

import static com.drdedd.chess.misc.MiscMethods.toColChar;

/**
 * {@inheritDoc}
 * Fragment to view, play, load and save chess game
 */
public class GameLogic implements GameLogicInterface {

    private final static String TAG = "GameLogic";
    @Getter
    private static ChessState gameState;
    @Getter
    private static String termination;
    private static boolean gameTerminated;
    private final GameUI gameUI;
    private final String FEN;
    private final Random random = new Random();
    private String white, black, app, date, fromSquare, toSquare;
    private PGN pgn;
    private BoardModel boardModel = null;
    @Getter
    private Stack<BoardModel> boardModelStack;
    @Getter
    private Stack<String> FENs;
    @Setter
    private Player playAs;
    private HashMap<String, HashSet<Integer>> allLegalMoves;
    private Thread randomMoveThread;
    private boolean whiteToPlay, onePlayer, infinitePlay;
    private int count, halfMove, fullMove;

    /**
     * GameLogic for normal game setup
     *
     * @param newGame Start new game or resume saved game
     */
    public GameLogic(GameUI gameUI, boolean newGame) {
        this.gameUI = gameUI;
        FEN = "";
        initializeData();
        if (newGame) reset();
        updateAll();
    }

    /**
     * GameLogic with a starting position
     *
     * @param FEN FEN of the starting position
     */
    public GameLogic(GameUI gameUI, String FEN) {
        this.gameUI = gameUI;
        this.FEN = FEN;
        initializeData();
        // Log.d(TAG, "GameLogic: Loading FEN: " + FEN);
        reset();
        updateAll();
    }

    /**
     * GameLogic to parse and validate PGN moves
     */
    public GameLogic(PGNData pgnData) {
        gameUI = null;
        initializeData();

        white = pgnData.getTagOrDefault(PGN.TAG_WHITE, "White");
        black = pgnData.getTagOrDefault(PGN.TAG_BLACK, "Black");
        date = pgnData.getTagOrDefault(PGN.TAG_DATE, "?");
        pgn.setWhiteBlack(white, black);
        pgn.addAllTags(pgnData.getTagsMap());

        FEN = pgnData.getTagOrDefault(PGN.TAG_FEN, "");
        reset();

        pgn.setPGNData(pgnData);
    }

    /**
     * Initializes game data and objects: MediaPlayer, Vibrator, PGN, BoardModel
     */
    private void initializeData() {

        gameTerminated = false;


        SimpleDateFormat pgnDate = new SimpleDateFormat("yyyy.MM.dd", Locale.ENGLISH);

        white = "White";
        black = "Black";

        app = null;
        date = pgnDate.format(new Date());

        if (boardModel == null || pgn == null) {
            boardModel = new BoardModel(true);
            boardModelStack = new Stack<>();
            FENs = new Stack<>();
            boardModelStack.push(boardModel);
            FENs.push(boardModel.toFEN(this));
            pgn = new PGN(app, white, black, date, true);
        }

        gameState = ChessState.ONGOING;
        whiteToPlay = pgn.isWhiteToPlay();
        pgn.setWhiteBlack(white, black);        //Set the white and the black players' names
    }

    public void reset() {
        stopInfinitePlay();
        gameTerminated = false;
        whiteToPlay = true;
        halfMove = 0;
        fullMove = 1;

        if (FEN.isEmpty()) {
            boardModel = new BoardModel(true);
            pgn = new PGN(app, white, black, date, true);
        } else {
            long start = System.nanoTime();
            boardModel = BoardModel.parseFEN(FEN);

            Matcher player = Regexes.activePlayerPattern.matcher(FEN);
            if (player.find()) whiteToPlay = player.group().trim().equals("w");
            long end = System.nanoTime();
            // Log.printTime(TAG, "parsing FEN", end - start, FEN.length());
            pgn = new PGN(app, white, black, date, whiteToPlay, FEN);
        }
        boardModelStack = new Stack<>();
        FENs = new Stack<>();
        fromSquare = "";
        toSquare = "";
        pushToStack();
    }

    public void playRandomMove() {
        try {
            try {
                Set<String> squares = allLegalMoves.keySet();
                ArrayList<String> array = new ArrayList<>(squares);
                Collections.shuffle(array);

                // Pick a random piece square
                String square = array.get(random.nextInt(array.size()));
                if (square != null) {
                    HashSet<Integer> moves = allLegalMoves.get(square);

                    // If piece has no legal moves pick another piece
                    if (moves == null || moves.isEmpty()) for (String p : squares) {
                        moves = allLegalMoves.get(p);
                        if (moves != null && !moves.isEmpty()) {
                            square = p;
                            break;
                        }
                    }

                    // If legal moves found for a piece
                    if (moves != null && !moves.isEmpty()) {
                        Piece piece = pieceAt(toRow(square), toCol(square));
                        ArrayList<Integer> legalMoves = new ArrayList<>(moves);
                        int position = legalMoves.get(random.nextInt(legalMoves.size()));
                        int fromRow = piece.getRow(), fromCol = piece.getCol(), row = position / 8, col = position % 8;

                        // If move is promotion promote to random rank
                        if (piece.getRank() == Rank.PAWN) {
                            Pawn pawn = (Pawn) piece;
                            Rank[] ranks = {Rank.QUEEN, Rank.ROOK, Rank.BISHOP, Rank.KNIGHT};
                            if (pawn.canPromote() && promote(pawn, row, col, fromRow, fromCol, ranks[random.nextInt(ranks.length)]))
                                return;
                        }

                        // Perform the randomly picked move
                        move(fromRow, fromCol, row, col);
//                        Log.d(TAG, String.format("run: Move %s: %s %s->%s", move(fromRow, fromCol, row, col) ? "played" : "failed!", piece.getUnicode(), square, piece.getSquare()));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "run: Exception occurred!", e);
            }
        } catch (Exception e) {
            Log.e(TAG, "playRandomMove: Exception!", e);
        }
    }

    public void toggleInfinitePlay() {
        if (infinitePlay) stopInfinitePlay();
        else {
            onePlayer = false;
            infinitePlay = true;
            playRandomMove();
        }
    }

    public void stopInfinitePlay() {
        infinitePlay = false;
        try {
            if (randomMoveThread != null) randomMoveThread.join();
        } catch (Exception e) {
            Log.e(TAG, "stopInfinitePlay: Exception occurred while stopping random move thread!", e);
        }
    }

    @Override
    public Piece pieceAt(int row, int col) {
        return boardModel.pieceAt(row, col);
    }

    @Override
    public boolean move(int fromRow, int fromCol, int toRow, int toCol) {
        if (gameTerminated) return false;

        Piece movingPiece = pieceAt(fromRow, fromCol);
        if (movingPiece == null) return false;

        // Check if the piece belongs to the active player
        if (isPieceToPlay(movingPiece) && allLegalMoves.get(movingPiece.getSquare()) != null) {
            HashSet<Integer> pieceLegalMoves = allLegalMoves.get(movingPiece.getSquare());
            if (pieceLegalMoves != null && !pieceLegalMoves.contains(toCol + toRow * 8))
                return false; // Return false if the move is an illegal move
        }
        boolean result = makeMove(movingPiece, fromRow, fromCol, toRow, toCol);
        if (result) {
            if (movingPiece.getRank() == Rank.PAWN || pgn.getMoves().getLast().contains(PGN.CAPTURE)) halfMove = 0;
            else halfMove = boardModel.getHalfMove() + 1;

            if (!whiteToPlay) fullMove = boardModel.getFullMove() + 1;

            boardModel.fromSquare = toNotation(fromRow, fromCol);
            boardModel.toSquare = toNotation(toRow, toCol);

            boardModel.enPassantPawn = null;
            boardModel.enPassantSquare = "";

            if (movingPiece.getRank() == Rank.PAWN && Math.abs(fromRow - toRow) == 2) {
                Pawn enPassantPawn = (Pawn) movingPiece;
                boardModel.enPassantPawn = enPassantPawn;
                boardModel.enPassantSquare = toNotation(enPassantPawn.getRow() - enPassantPawn.direction, enPassantPawn.getCol());
                //Log.d(TAG, "move: EnPassantPawn: " + boardModel.enPassantPawn.getPosition() + " EnPassantSquare: " + boardModel.enPassantSquare);
            }
            fromSquare = toNotation(fromRow, fromCol);
            toSquare = toNotation(toRow, toCol);
            toggleGameState();
            pushToStack();
            if (playerToPlay().isInCheck()) printLegalMoves();
        }
        return result;
    }

    /**
     * Checks for move validity and performs move
     *
     * @param fromRow Starting row of the piece
     * @param fromCol Starting column of the piece
     * @param toRow   Ending row of the piece
     * @param toCol   Ending column of the piece
     * @return Move result
     */
    private boolean makeMove(Piece movingPiece, int fromRow, int fromCol, int toRow, int toCol) {
        if (isGameTerminated()) return false;

//        Piece movingPiece = pieceAt(fromRow, fromCol);
        if (movingPiece == null || fromRow == toRow && fromCol == toCol || toRow < 0 || toRow > 7 || toCol < 0 || toCol > 7 || !isPieceToPlay(movingPiece))
            return false;

        Piece toPiece = pieceAt(toRow, toCol);
        String uciMove = getUCIMove(fromRow, fromCol, toRow, toCol, null);
        if (toPiece != null) if (toPiece.isKing()) return false;
        else if (movingPiece.getPlayer() != toPiece.getPlayer() && movingPiece.canCapture(this, toPiece)) {
            if (movingPiece.getRank() == Rank.PAWN) {
                Pawn pawn = (Pawn) movingPiece;
                if (pawn.canPromote()) {
                    promote(pawn, toRow, toCol, fromRow, fromCol);
                    //Log.d(TAG, "makeMove: Pawn promotion");
                    return false;
                }
            }
            String sanMove = getSANMove(movingPiece, fromRow, fromCol, toRow, toCol, PGN.CAPTURE, null);
            movingPiece.moveTo(toRow, toCol);
            capturePiece(toPiece);
            addMove(sanMove, uciMove);
//            addToPGN(movingPiece, PGN.CAPTURE, fromRow, fromCol);
            return true;
        }
        if (toPiece == null) {
            if (movingPiece.getRank() == Rank.KING) {
                King king = (King) movingPiece;
                if (!king.isCastled() && king.canMoveTo(this, toRow, toCol)) {
                    if (toCol - fromCol == -2 && king.canLongCastle(this)) {
                        king.longCastle(this);
                        String sanMove = PGN.LONG_CASTLE;
                        addMove(sanMove, uciMove);
//                        addToPGN(movingPiece, PGN.LONG_CASTLE, fromRow, fromCol);
                        return true;
                    }
                    if (toCol - fromCol == 2 && king.canShortCastle(this)) {
                        king.shortCastle(this);
                        String sanMove = PGN.SHORT_CASTLE;
                        addMove(sanMove, uciMove);
//                        addToPGN(movingPiece, PGN.SHORT_CASTLE, fromRow, fromCol);
                        return true;
                    }
                }
            }
            if (movingPiece.canMoveTo(this, toRow, toCol)) {
                if (movingPiece.getRank() == Rank.PAWN) {
                    Pawn pawn = (Pawn) movingPiece;
                    if (pawn.canCaptureEnPassant(this))
                        if (getBoardModel().enPassantSquare.equals(toNotation(toRow, toCol)))
                            if (capturePiece(pieceAt(toRow - pawn.direction, toCol))) {
                                //Log.d(TAG, "makeMove: EnPassant Capture");
                                String sanMove = getSANMove(pawn, fromRow, fromCol, toRow, toCol, PGN.CAPTURE, null);
                                movingPiece.moveTo(toRow, toCol);
                                addMove(sanMove, uciMove);
//                                addToPGN(pawn, PGN.CAPTURE, fromRow, fromCol);
                                return true;
                            }
                    if (pawn.canPromote()) {
                        promote(pawn, toRow, toCol, fromRow, fromCol);
                        //Log.d(TAG, "makeMove: Pawn promotion");
                        return false;
                    }
                }
                String sanMove = getSANMove(movingPiece, fromRow, fromCol, toRow, toCol, "", null);
                movingPiece.moveTo(toRow, toCol);
                addMove(sanMove, uciMove);
//                addToPGN(movingPiece, "", fromRow, fromCol);
                return true;
            }
        }
        //Log.d(TAG, "makeMove: Illegal move");
        return false;   //Default return false
    }

    public void saveGame() {
//        if (gameTerminated || loadingPGN) return;
//        dataManager.saveData(boardModel, pgn, boardModelStack, FENs);
    }

    private String getSANMove(Piece piece, int fromRow, int fromCol, int toRow, int toCol, String capture, Rank promotionRank) {
        LinkedHashSet<Piece> pieces = boardModel.pieces;
        String pieceChar;
        String startCol;
        if (piece.getRank() == Rank.PAWN) {
            pieceChar = "";
            if (!capture.isEmpty()) startCol = String.valueOf(toColChar(fromCol));
            else startCol = "";
        } else {
            pieceChar = String.valueOf(piece.getRank().getLetter());
            startCol = "";
        }
        String startRow = "";
        String promotion = promotionRank == null ? "" : "=" + promotionRank.getLetter();

        for (Piece tempPiece : pieces) {
            if (!startRow.isEmpty() && !startCol.isEmpty()) break;
            if (tempPiece.isCaptured() || tempPiece == piece) continue;
            if (tempPiece.getPlayer() == piece.getPlayer() && tempPiece.getRank() == piece.getRank()) {
                HashSet<Integer> tempPieceMoves = allLegalMoves.get(tempPiece.getSquare());
                if (tempPieceMoves != null && tempPieceMoves.contains(toRow * 8 + toCol))
                    if (piece.getRank() == Rank.KNIGHT) {
                        if (startCol.isEmpty() && piece.getCol() != tempPiece.getCol()) {
                            startCol = String.valueOf(toColChar(fromCol));
                            continue;
                        }
                        if (startRow.isEmpty() && piece.getRow() != tempPiece.getRow() && piece.getCol() == tempPiece.getCol())
                            startRow = String.valueOf(fromRow + 1);
                    } else {
                        if (startCol.isEmpty() && piece.getRow() == tempPiece.getRow())
                            startCol = String.valueOf(toColChar(fromCol));
                        if (startRow.isEmpty() && piece.getCol() == tempPiece.getCol())
                            startRow = String.valueOf(fromRow + 1);
                    }
            }
        }
        return String.format("%s%s%s%s%s%s", pieceChar, startCol, startRow, capture, toNotation(toRow, toCol), promotion);
    }

    private String getUCIMove(int fromRow, int fromCol, int toRow, int toCol, Rank promotionRank) {
        return String.format("%s%s%s", toNotation(fromRow, fromCol), toNotation(toRow, toCol), promotionRank == null ? "" : Character.toLowerCase(promotionRank.getLetter()));
    }

    private void addMove(String sanMove, String uciMove) {
        pgn.addMove(sanMove, uciMove);
    }

    @Override
    public boolean capturePiece(Piece piece) {
        return boardModel.capturePiece(piece);
    }

    @Override
    public void promote(Pawn pawn, int row, int col, int fromRow, int fromCol) {
//        PromoteDialog promoteDialog = new PromoteDialog(context);
//        promoteDialog.show();

//        Set image buttons as respective color pieces
//        Integer queenResID = boardModel.resIDs.get(pawn.getPlayer() + Rank.QUEEN.toString());
//        Integer rookResID = boardModel.resIDs.get(pawn.getPlayer() + Rank.ROOK.toString());
//        Integer bishopResID = boardModel.resIDs.get(pawn.getPlayer() + Rank.BISHOP.toString());
//        Integer knightResID = boardModel.resIDs.get(pawn.getPlayer() + Rank.KNIGHT.toString());
//        if (queenResID != null) promoteDialog.findViewById(R.id.promote_to_queen).setBackgroundResource(queenResID);
//        if (rookResID != null) promoteDialog.findViewById(R.id.promote_to_rook).setBackgroundResource(rookResID);
//        if (bishopResID != null) promoteDialog.findViewById(R.id.promote_to_bishop).setBackgroundResource(bishopResID);
//        if (knightResID != null) promoteDialog.findViewById(R.id.promote_to_knight).setBackgroundResource(knightResID);

//        Invalidate chess board to show new promoted piece
//        promoteDialog.setOnDismissListener(dialogInterface -> {
        Rank rank = null;
        System.out.print("Enter rank to be promoted (Q,R,N,B) or 'x' to cancel: ");
        Scanner sc = new Scanner(System.in);
        do {
            char c = Character.toUpperCase(sc.nextLine().charAt(0));
            switch (c) {
                case 'Q':
                    rank = Rank.QUEEN;
                    break;
                case 'R':
                    rank = Rank.ROOK;
                    break;
                case 'B':
                    rank = Rank.BISHOP;
                    break;
                case 'N':
                    rank = Rank.KNIGHT;
                    break;
                default:
                    System.out.println("Invalid input!");
            }
            if (c == 'X') return;
        } while (rank == null);

        Piece tempPiece = pieceAt(row, col);
        String sanMove = getSANMove(pawn, fromRow, fromCol, row, col, tempPiece == null ? "" : PGN.CAPTURE, rank);
        String uciMove = getUCIMove(fromRow, fromCol, row, col, rank);
        Piece promotedPiece = boardModel.promote(pawn, rank, row, col);
        if (tempPiece != null) {
            if (tempPiece.getPlayer() != promotedPiece.getPlayer()) {
                capturePiece(tempPiece);
//                    addToPGN(promotedPiece, PGN.PROMOTE + PGN.CAPTURE, fromRow, fromCol);
            }
        }
//            else addToPGN(promotedPiece, PGN.PROMOTE, fromRow, fromCol);
        addMove(sanMove, uciMove);
        // Log.v(TAG, String.format("promote: Promoted to %s, %s->%s", rank, toNotation(fromRow, fromCol), toNotation(row, col)));
        fromSquare = toNotation(fromRow, fromCol);
        toSquare = toNotation(row, col);

        halfMove = 0;
        if (!pawn.isWhite()) fullMove = boardModel.getFullMove() + 1;

        toggleGameState();
        pushToStack();
//        });
    }

    /**
     * Promotion of pawn to a higher rank
     *
     * @param pawn    Pawn to be promoted
     * @param row     Row of the promotion square
     * @param col     Column of the promotion square
     * @param fromRow Starting row of the pawn
     * @param fromCol Starting column of the pawn
     * @param rank    Rank to be promoted
     * @return <code>true|false</code> - Promotion result
     */
    public boolean promote(Pawn pawn, int row, int col, int fromRow, int fromCol, Rank rank) {
        boolean promoted = false;
        Piece tempPiece = pieceAt(row, col);
        String sanMove = getSANMove(pawn, fromRow, fromCol, row, col, tempPiece == null ? "" : PGN.CAPTURE, rank);
        String uciMove = getUCIMove(fromRow, fromCol, row, col, rank);
        Piece promotedPiece = boardModel.promote(pawn, rank, row, col);
        if (tempPiece != null) {
            if (tempPiece.getPlayer() != promotedPiece.getPlayer()) {
                capturePiece(tempPiece);
//                addToPGN(promotedPiece, PGN.PROMOTE + PGN.CAPTURE, fromRow, fromCol);
                promoted = true;
            }
        } else {
//            addToPGN(promotedPiece, PGN.PROMOTE, fromRow, fromCol);
            promoted = true;
        }
        if (promoted) {
            halfMove = 0;
            if (!pawn.isWhite()) fullMove = boardModel.getFullMove() + 1;
            addMove(sanMove, uciMove);
            fromSquare = toNotation(fromRow, fromCol);
            toSquare = toNotation(row, col);
            toggleGameState();
            pushToStack();
        }
        return promoted;
    }

    public boolean isGameTerminated() {
        return gameTerminated;
    }

    private void toggleGameState() {
        whiteToPlay = !whiteToPlay;
        pgn.setWhiteToPlay(whiteToPlay);
    }

    private void pushToStack() {
        boardModelStack.push(boardModel.clone());
        FENs.push(boardModel.toFEN(this));
        boardModel.setMoveClocks(halfMove, fullMove);
        boardModel.fromSquare = fromSquare;
        boardModel.toSquare = toSquare;
        fromSquare = "";
        toSquare = "";
        updateAll();
    }

    private void undoLastMove() {
        if (gameTerminated) return;
        pgn.removeLast();
        if (boardModelStack.size() > 1) {
            boardModelStack.pop();
            FENs.pop();
            boardModel = boardModelStack.peek().clone();
            if (boardModel.enPassantPawn != null) {
                //Log.d(TAG, "undoLastMove: EnPassantPawn: " + boardModel.enPassantPawn.getPosition() + " EnPassantSquare: " + boardModel.enPassantSquare);
            }
            toggleGameState();
            updateAll();
        }
    }

    /**
     * Updates all necessary fields and views
     */
    private void updateAll() {
        saveGame();

        count = 0;
//        long start = System.nanoTime();
        computeLegalMoves();
//        long end = System.nanoTime();
        isChecked();
//        printTime("updating LegalMoves", end - start);
        checkGameTermination();
//        Log.d(TAG, "updateAll: Updated game");
        if (gameUI != null) gameUI.updateViews();
        if (!gameTerminated) {
            randomMoveThread = null;
            if (onePlayer && playerToPlay() == playAs) playRandomMove();
        }
    }

    /**
     * Checks for termination of the game after each move
     */
    private void checkGameTermination() {
        ChessState terminationState;
//        if (!loadingPGN) {
//      Check for draw by insufficient material
        if (drawByInsufficientMaterial()) {
            termination = "Draw by insufficient material";
            terminationState = ChessState.DRAW;
            pgn.setTermination(termination);
            terminateGame(terminationState);
            return;
        }

//      Check for draw by repetition
        if (drawByRepetition()) {
            termination = "Draw by repetition";
            terminationState = ChessState.DRAW;
            pgn.setTermination(termination);
            terminateGame(terminationState);
            return;
        }
//        }
        if (noLegalMoves()) {
            //Log.d(TAG, "checkGameTermination: No Legal Moves for: " + playerToPlay());
            isChecked();

            if (!playerToPlay().isInCheck()) {
                termination = "Draw by Stalemate";
                terminationState = ChessState.STALEMATE;
            } else {
                termination = opponentPlayer(playerToPlay()).getName() + " won by Checkmate";
                terminationState = ChessState.CHECKMATE;
            }

            pgn.setTermination(termination);
            terminateGame(terminationState);
        }
    }

    private boolean noLegalMoves() {
        Set<Map.Entry<String, HashSet<Integer>>> legalMoves = allLegalMoves.entrySet();
        for (Map.Entry<String, HashSet<Integer>> entry : legalMoves)
            if (!entry.getValue().isEmpty()) return false;
        // Log.d(TAG, "noLegalMoves: No legal moves for " + playerToPlay());
        return true;
    }

    /**
     * Terminates the game
     *
     * @param terminationState State of the termination
     */
    private void terminateGame(ChessState terminationState) {
        saveGame();
        gameState = terminationState;
        if (termination == null || termination.isEmpty()) termination = pgn.getTermination();
        gameTerminated = true;

        pgn.setResult(getResult());

        if (gameUI != null) gameUI.terminateGame(termination);
    }

    @Override
    public void terminateByTimeOut(Player player) {
        termination = opponentPlayer(player) + " won on time";
        pgn.setTermination(termination);
        terminateGame(ChessState.TIMEOUT);
    }

    private boolean drawByInsufficientMaterial() {
        boolean KB = false, kb = false, BLight = false, bLight = false;
        LinkedHashSet<Piece> pieces = boardModel.pieces;
        HashSet<Piece> whitePieces = new HashSet<>(), blackPieces = new HashSet<>();
        for (Piece piece : pieces) {
            if (piece.isCaptured()) continue;
            if (piece.isWhite()) whitePieces.add(piece);
            else blackPieces.add(piece);
        }

        if (whitePieces.size() == 1 && blackPieces.size() == 1) return true;
        else if (whitePieces.size() <= 2 && blackPieces.size() == 1 || whitePieces.size() == 1 && blackPieces.size() <= 2) {
            for (Piece whitePiece : whitePieces)
                if (whitePiece.getRank() == Rank.BISHOP || whitePiece.getRank() == Rank.KNIGHT) return true;
            for (Piece blackPiece : blackPieces)
                if (blackPiece.getRank() == Rank.BISHOP || blackPiece.getRank() == Rank.KNIGHT) return true;
        } else if (whitePieces.size() <= 2 && blackPieces.size() <= 2) {
            for (Piece whitePiece : whitePieces)
                if (whitePiece.getRank() == Rank.BISHOP) {
                    KB = true;
                    BLight = (whitePiece.getRow() + whitePiece.getCol()) % 2 == 0;
                }
            for (Piece blackPiece : blackPieces)
                if (blackPiece.getRank() == Rank.BISHOP) {
                    kb = true;
                    bLight = (blackPiece.getRow() + blackPiece.getCol()) % 2 == 0;
                }
            return KB && kb && BLight == bLight;
        }
        return false;
    }

    private boolean drawByRepetition() {
        int i = 0, j, l = FENs.size();
        String[] positions = new String[l];
        for (String FEN : FENs) positions[l - i++ - 1] = FEN;

        String lastPosition = positions[l - 1];
        for (i = 0; i < l - 2; i++)
            if (lastPosition.equals(positions[i])) {
                //Log.d(TAG, String.format("drawByRepetition: One repetition found:\n%d: %s\n%d: %s", i / 2 + 1, positions[i], (l - 1) / 2 + 1, lastPosition));
                for (j = i + 1; j < l - 1; j++)
                    if (positions[i].equals(positions[j])) {
                        //Log.d(TAG, "Draw by repetition");
                        //Log.d(TAG, String.format("Position : %d, %d & %d", i / 2 + 1, j / 2 + 1, (l - 1) / 2 + 1));
                        //Log.d(TAG, String.format("Repeated moves FEN:\n%d - %s\n%d - %s\n%d - %s", i / 2 + 1, positions[i], j / 2 + 1, positions[j], (l - 1) / 2 + 1, lastPosition));
                        return true;
                    }
                positions[i] = "";
            }
        return false;
    }

    /**
     * Checks if any of the player is checked
     */
    private void isChecked() {
        boolean isChecked = false;
        King whiteKing = boardModel.getWhiteKing();
        King blackKing = boardModel.getBlackKing();
        Player.WHITE.setInCheck(false);
        Player.BLACK.setInCheck(false);
        if (whiteKing.isChecked(this)) {
            isChecked = true;
            Player.WHITE.setInCheck(true);
            //Log.d(TAG, "isChecked: White King checked");
        }
        if (blackKing.isChecked(this)) {
            isChecked = true;
            Player.BLACK.setInCheck(true);
            //Log.d(TAG, "isChecked: Black King checked");
        }
    }

    /**
     * Prints all legal moves for the player in check
     */
    private void printLegalMoves() {
        if (allLegalMoves == null) return;
        Set<Map.Entry<String, HashSet<Integer>>> pieces = allLegalMoves.entrySet();
        for (Map.Entry<String, HashSet<Integer>> entry : pieces) {
            String square = entry.getKey();
            HashSet<Integer> moves = entry.getValue();
            if (!moves.isEmpty()) {
                StringBuilder allMoves = new StringBuilder();
                for (int move : moves)
                    allMoves.append(toNotation(move)).append(" ");
                Log.d(TAG, "printLegalMoves: Legal Moves for " + square + ": " + allMoves);
            }
//            else //System.out.println(TAG+ "printLegalMoves: No legal moves for " + square);
        }
    }

    /**
     * Computes and updates all legal moves for the player to play
     */
    private void computeLegalMoves() {
        allLegalMoves = new HashMap<>();
        LinkedHashSet<Piece> pieces = boardModel.pieces;
        for (Piece piece : pieces) {
            if (!isPieceToPlay(piece) || piece.isCaptured()) continue;

            HashSet<Integer> possibleMoves = piece.getPossibleMoves(this), illegalMoves = new HashSet<>();
            for (int move : possibleMoves) {
                if (isIllegalMove(piece, move)) illegalMoves.add(move);
                count++;
            }

            possibleMoves.removeAll(illegalMoves);
            allLegalMoves.put(piece.getSquare(), possibleMoves);
        }
    }

    /**
     * Finds if a move is illegal for the given piece
     *
     * @param piece <code>Piece</code> to move
     * @param move  Move for the piece
     * @return <code>True|False</code>
     */
    private boolean isIllegalMove(Piece piece, int move) {
        TempGameLogicInterface tempBoardInterface = new TempGameLogicInterface();
        tempBoardInterface.tempBoardModel = boardModel.clone();
        boolean isChecked;
        int row = piece.getRow(), col = piece.getCol(), toRow = toRow(move), toCol = toCol(move);
        tempBoardInterface.move(row, col, toRow, toCol);
        if (piece.isWhite()) isChecked = tempBoardInterface.tempBoardModel.getWhiteKing().isChecked(tempBoardInterface);
        else isChecked = tempBoardInterface.tempBoardModel.getBlackKing().isChecked(tempBoardInterface);
        return isChecked;
    }

    /**
     * Returns result of the game
     *
     * @return <code> * | 0-1 | 1-0 | 1/2-1/2 </code>
     */
    public String getResult() {
        return switch (gameState) {
            case ONGOING -> PGN.RESULT_ONGOING;
            case RESIGN, TIMEOUT ->
                    termination.contains(Player.WHITE.getName()) ? PGN.RESULT_WHITE_WON : PGN.RESULT_BLACK_WON;
            case CHECKMATE -> Player.WHITE.isInCheck() ? PGN.RESULT_BLACK_WON : PGN.RESULT_WHITE_WON;
            case STALEMATE, DRAW -> PGN.RESULT_DRAW;
        };
    }

    @Override
    public boolean isWhiteToPlay() {
        return whiteToPlay;
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
     * Returns whether the piece belongs to the current player to play
     *
     * @param piece <code>Piece</code> to check
     * @return <code>True|False</code>
     */
    public boolean isPieceToPlay(Piece piece) {
        return piece.getPlayer() == playerToPlay();
    }

    /**
     * Returns the current player to play
     *
     * @return <code>White|Black</code>
     */
    public Player playerToPlay() {
        return whiteToPlay ? Player.WHITE : Player.BLACK;
    }

    @Override
    public BoardModel getBoardModel() {
        return boardModel;
    }

    private void setGameState(ChessState gameState) {
        GameLogic.gameState = gameState;
    }

    @Override
    public HashMap<String, HashSet<Integer>> getAllLegalMoves() {
        return allLegalMoves;
    }

    public PGN getPGN() {
        return pgn;
    }

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

    public void playRandomGame() {
        while (!gameTerminated) playRandomMove();
    }

    /**
     * Temporary GameLogicInterface for computing Legal Moves
     */
    static class TempGameLogicInterface implements GameLogicInterface {
        private static final String TAG = "TempGameLogicInterface";
        private BoardModel tempBoardModel;

        @Override
        public Piece pieceAt(int row, int col) {
            return tempBoardModel.pieceAt(row, col);
        }

        @Override
        public boolean move(int fromRow, int fromCol, int toRow, int toCol) {
            Piece opponentPiece = pieceAt(toRow, toCol), movingPiece = pieceAt(fromRow, fromCol);
            if (movingPiece != null) movingPiece.moveTo(toRow, toCol);
            else Log.d(TAG, " move: Error! movingPiece is null");
            if (opponentPiece != null) tempBoardModel.capturePiece(opponentPiece);
            return true;
        }

        @Override
        public boolean capturePiece(Piece piece) {
            return false;
        }

        @Override
        public void promote(Pawn pawn, int row, int col, int fromRow, int fromCol) {
        }

        @Override
        public void terminateByTimeOut(Player player) {
        }

        @Override
        public BoardModel getBoardModel() {
            return tempBoardModel;
        }

        @Override
        public HashMap<String, HashSet<Integer>> getAllLegalMoves() {
            return null;
        }

        @Override
        public boolean isWhiteToPlay() {
            return false;
        }

        @Override
        public boolean isGameTerminated() {
            return false;
        }

        @Override
        public boolean isPieceToPlay(Piece piece) {
            return false;
        }
    }
}