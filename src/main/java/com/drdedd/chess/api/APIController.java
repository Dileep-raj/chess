package com.drdedd.chess.api;

import com.drdedd.chess.api.data.*;
import com.drdedd.chess.api.error.exceptions.BadRequestException;
import com.drdedd.chess.api.error.exceptions.InternalServerErrorException;
import com.drdedd.chess.engine.FENEvaluator;
import com.drdedd.chess.engine.PGNAnalyzer;
import com.drdedd.chess.engine.stockfish.Stockfish;
import com.drdedd.chess.game.BoardModel;
import com.drdedd.chess.game.GameLogic;
import com.drdedd.chess.game.Openings;
import com.drdedd.chess.game.ParsedGame;
import com.drdedd.chess.game.data.Regexes;
import com.drdedd.chess.game.pgn.PGNParser;
import com.drdedd.chess.misc.Log;
import com.drdedd.chess.misc.MiscMethods;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api")
public class APIController {
    private static final String TAG = "APIController", appName = "chess";
    public static final String ABOUT = "Chess API for openings, gameplay, position evaluation and game analysis";
    private int legalMoves;

    /**
     * Brief description about the API
     *
     * @return {@link String}
     */
    @GetMapping(path = "/about")
    public ResponseEntity<Object> about() {
        Stockfish stockfish = new Stockfish(1);
        String stockfishVersion = stockfish.getStockfishVersion();
        stockfish.stopEngine();
        return new ResponseEntity<>("%s%nStockfish Version: %s".formatted(ABOUT, stockfishVersion), HttpStatus.OK);
    }

    /**
     * GET method for API validation
     *
     * @return {@link String}
     */
    @GetMapping(path = "/validate")
    public ResponseEntity<Object> validate() {
        return new ResponseEntity<>(new String(Base64.getEncoder().encode(appName.getBytes(StandardCharsets.UTF_8))), HttpStatus.OK);
    }

    /**
     * Evaluates the chess position
     *
     * @param FEN        FEN of the position to evaluate
     * @param depth      Depth of evaluation
     * @param variations Number of primary variations
     * @return <code>JSON</code>
     */
    @GetMapping(value = "/eval", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> evaluation(@RequestParam("fen") String FEN, @RequestParam(defaultValue = "-1") int depth, @RequestParam(defaultValue = "1") int variations) {
        try {
            long start = System.nanoTime();
            String error = validateFEN(FEN);
            if (error != null) throw new BadRequestException(error);
            FENEvaluator evaluator = new FENEvaluator(depth, variations);
            EvaluationData data = evaluator.evaluate(FEN.trim());
            data.setTime(MiscMethods.formatNanoseconds(System.nanoTime() - start));
            return new ResponseEntity<>(data, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error while evaluation position!");
            e.printStackTrace(System.err);
            throw new InternalServerErrorException("Unexpected error occurred during evaluation");
        }
    }

    /**
     * Analyzes the given game using stockfish engine
     *
     * @param payload Game data to analyze
     * @param accept  Type of response to accept
     * @return <code>JSON</code>
     */
    @PostMapping(value = "/analysis", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<Object> analysis(@RequestBody Map<String, Object> payload, @RequestHeader(value = "accept") String accept) {
        long start = System.nanoTime();
        int depth = (int) payload.getOrDefault("depth", PGNAnalyzer.NO_LIMIT);
        int time = (int) payload.getOrDefault("time", PGNAnalyzer.NO_LIMIT);
        if (!payload.containsKey("pgn")) throw new BadRequestException("Missing/Invalid pgn");
        String pgnString = payload.get("pgn").toString();
        boolean includeFENs = (boolean) payload.getOrDefault("fens", false);
        try {
            PGNAnalyzer analyzer = new PGNAnalyzer(depth, time);
            AnalysisData data = analyzer.analyzePGN(pgnString, includeFENs);
            data.setTime(MiscMethods.formatNanoseconds(System.nanoTime() - start));
            if (accept == null || accept.equalsIgnoreCase(MediaType.TEXT_PLAIN_VALUE))
                return new ResponseEntity<>(analyzer.getAnalyzedPGN(), data.getStatus());
            return new ResponseEntity<>(data, data.getStatus());
        } catch (Exception e) {
            System.err.println("Error occurred while analyzing PGN!");
            e.printStackTrace(System.err);
            throw new InternalServerErrorException("Unexpected error while analyzing PGN");
        }
    }

    /**
     * Unicode representation of the board
     *
     * @param FEN FEN of the position
     * @return {@link String}
     */
    @GetMapping(value = "/unicode", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Object> getUnicodeBoard(@RequestParam("fen") String FEN) {
        String error = validateFEN(FEN);
        if (error != null) throw new BadRequestException(error);

        BoardModel boardModel = BoardModel.parseFEN(FEN.trim());
        if (boardModel != null)
            return new ResponseEntity<>(String.format("<pre>%s</pre>", boardModel.unicode()), HttpStatus.OK);
        throw new InternalServerErrorException("Couldn't convert FEN to unicode board");
    }

    @Nullable
    private String validateFEN(String FEN) {
        if (FEN == null) return "Missing FEN parameter";
        FEN = FEN.trim();
        if (FEN.isEmpty()) return "Empty FEN parameter";
        if (!FEN.matches(Regexes.FENRegex)) return "Invalid FEN";
        return null;
    }

    /**
     * Legal moves for a given position
     *
     * @param FEN FEN of the position
     * @return <code>JSON</code>
     */
    @GetMapping(value = "/legalMoves", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> legalMoves(@RequestParam("fen") String FEN) {
        try {
            long start = System.nanoTime();
            String error = validateFEN(FEN);
            if (error != null) throw new BadRequestException(error);

            GameLogic gameLogic = new GameLogic(null, FEN);
            HashMap<String, HashSet<Integer>> allLegalMoves = gameLogic.getAllLegalMoves();
            LegalMovesData data = new LegalMovesData();
            data.setSuccess(false);

            HashMap<String, HashSet<String>> legalMoves = new HashMap<>();
            Set<Map.Entry<String, HashSet<Integer>>> entries = allLegalMoves.entrySet();
            for (Map.Entry<String, HashSet<Integer>> entry : entries) {
                String square = entry.getKey();
                HashSet<Integer> movesInt = entry.getValue();
                HashSet<String> moves = new HashSet<>();
                for (int move : movesInt) moves.add(MiscMethods.toNotation(move));
                legalMoves.put(square, moves);
            }

            data.setSuccess(true);
            data.setFen(FEN);
            data.setMessage("Legal moves computed successfully");
            data.setStatus(HttpStatus.OK);
            data.setUci(gameLogic.getAllLegalMovesUCI());
            data.setLegalMoves(legalMoves);
            data.setTime(MiscMethods.formatNanoseconds(System.nanoTime() - start));
            return new ResponseEntity<>(data, data.getStatus());
        } catch (Exception e) {
            System.err.println("Error while computing legal moves");
            e.printStackTrace(System.err);
            throw new InternalServerErrorException("Unexpected error occurred! Could not compute legal moves");
        }
    }

    /**
     * Picks a random move from all legal moves in the given position
     *
     * @param FEN FEN of the position
     * @return <code>JSON</code>
     */
    @GetMapping(value = "/randomMove", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> randomMove(@RequestParam("fen") String FEN) {
        try {
            long start = System.nanoTime();
            String error = validateFEN(FEN);
            if (error != null) throw new BadRequestException(error);

            BaseResponseData data = new BaseResponseData();
            data.setSuccess(false);
            GameLogic gameLogic = new GameLogic(null, FEN);
            String randomMove = gameLogic.getRandomMove();
            data.setSuccess(true);
            data.setMessage("Random move generated successfully");
            data.setStatus(HttpStatus.OK);
            data.setData(Map.of("move", randomMove));
            data.setTime(MiscMethods.formatNanoseconds(System.nanoTime() - start));
            return new ResponseEntity<>(data, data.getStatus());
        } catch (Exception e) {
            System.err.println("Error occurred while generating random move!");
            e.printStackTrace(System.err);
            throw new InternalServerErrorException("Unexpected error occurred! Could not generate random move");
        }
    }

    /**
     * Find opening with a given ECO code
     *
     * @param eco ECO code of the opening
     * @return <code>JSON</code>
     */
    @GetMapping(value = "/openings/{eco}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> opening(@PathVariable String eco) {
        try {
            long start = System.nanoTime();
            OpeningData data = new OpeningData();
            data.setSuccess(false);
            Openings openings = Openings.getInstance();
            ArrayList<String> moves = openings.getOpeningsFromEco(eco);
            if (moves == null) data.setMessage("Opening not found!");
            else {
                data.setSuccess(true);
                data.setMessage("Opening found successfully!");
                data.setEco(eco);
                data.setName(openings.getOpeningName(eco));
                data.setMoves(moves);
                data.setTime(MiscMethods.formatNanoseconds(System.nanoTime() - start));
            }
            data.setStatus(HttpStatus.OK);
            return new ResponseEntity<>(data, data.getStatus());
        } catch (Exception e) {
            throw new InternalServerErrorException("Error while loading openings");
        }
    }

    /**
     * Search opening from the list of moves
     *
     * @param moves List of moves in UCI/SAN format
     * @return <code>JSON</code>
     */
    @GetMapping(value = "/getOpening", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> searchOpenings(@RequestParam("moves") String moves) {
        long start = System.nanoTime();
        OpeningData data = new OpeningData();
        data.setSuccess(false);
        try {
            Openings openings = Openings.getInstance();

            PGNParser parser = new PGNParser(moves);
            parser.parse();
            ParsedGame parsedGame = parser.getParsedGame();
            String opening = parsedGame.opening();
            int lastBookMove = parsedGame.lastBookMove();
            if (opening != null) {
                ArrayList<String> openingMoves = openings.getOpeningFromName(parsedGame.eco() + " " + parsedGame.opening());
                PGNParser openingParser = new PGNParser(String.join(" ", openingMoves));
                openingParser.parse();
                ParsedGame openingGame = openingParser.getParsedGame();
                data.setSuccess(true);
                data.setMessage("Opening found successfully!");
                data.setUci(openingGame.pgn().getUCIMoves());
                data.setMoves(openingGame.pgn().getSanMoves());
                data.setLastMove(lastBookMove);
                data.setEco(parsedGame.eco());
                data.setName(opening);
                data.setTime(MiscMethods.formatNanoseconds(System.nanoTime() - start));
            } else data.setMessage("Opening not found!");
            data.setStatus(HttpStatus.OK);
            return new ResponseEntity<>(data, data.getStatus());
        } catch (Exception e) {
            Log.e(TAG, "searchOpenings: Error occurred while loading openings", e);
            throw new InternalServerErrorException("Error while loading openings");
        }
    }

    @GetMapping(value = "/checkmates")
    public ResponseEntity<Object> findMates(@RequestParam(value = "fen") String fen) {
        legalMoves = 0;
        List<String> list = new ArrayList<>();
        findCheckMates(list, fen, 0, "");
        System.out.println("Total positions calculated: " + legalMoves);
        Map<String, Object> map = new HashMap<>(Map.of("success", !list.isEmpty(), "fen", fen));
        if (!list.isEmpty()) map.put("mates", list);
        return ResponseEntity.ok(map);
    }

    private void findCheckMates(List<String> list, String FEN, int depth, String moves) {
        if (depth >= 2) return;        // Limit depth to 2 ply
//        System.out.println("Searching position " + FEN);

        GameLogic gameLogic = new GameLogic(null, FEN);
        HashSet<String> legalMoves = gameLogic.getAllLegalMovesUCI();

        // Check every legal move with new game logic instance
        for (String legalMove : legalMoves) {
            GameLogic temp = new GameLogic(null, gameLogic.getBoardModel().toFEN());
            if (temp.move(legalMove)) {
                this.legalMoves++;
//                String s = (moves + " " + temp.getPgn().getPgnMoves().getFirst()).trim();
                String s = (moves + " " + legalMove).trim();
                if (temp.isGameTerminated()) {
                    System.out.printf("%s Moves: %s  %n", temp.getResult(), s);
                    list.add(s);
                } else findCheckMates(list, temp.getBoardModel().toFEN(), depth + 1, s);
            } else System.out.printf("Move %s failed! FEN: %s%n", legalMove, temp.getBoardModel().toFEN());
        }
    }
}
