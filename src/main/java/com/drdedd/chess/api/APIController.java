package com.drdedd.chess.api;

import com.drdedd.chess.api.data.AnalysisData;
import com.drdedd.chess.api.data.LegalMovesData;
import com.drdedd.chess.api.data.OpeningData;
import com.drdedd.chess.api.data.RandomMoveData;
import com.drdedd.chess.api.error.exceptions.BadRequestException;
import com.drdedd.chess.api.error.exceptions.InternalServerErrorException;
import com.drdedd.chess.engine.FENEvaluator;
import com.drdedd.chess.engine.PGNAnalyzer;
import com.drdedd.chess.game.BoardModel;
import com.drdedd.chess.game.GameLogic;
import com.drdedd.chess.game.Openings;
import com.drdedd.chess.game.ParsedGame;
import com.drdedd.chess.game.data.Regexes;
import com.drdedd.chess.game.pgn.PGNParser;
import com.drdedd.chess.misc.Log;
import com.drdedd.chess.misc.MiscMethods;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api")
public class APIController {
    private final static String TAG = "APIController";

    @Value("${spring.application.name}")
    private String appName;

    @GetMapping(path = "/about")
    public String about() {
        return "Chess API for position evaluation and game analysis";
    }

    @GetMapping(path = "/validate")
    public ResponseEntity<Object> validate() {
        return new ResponseEntity<>(new String(Base64.getEncoder().encode(appName.getBytes(StandardCharsets.UTF_8))), HttpStatus.OK);
    }

    /**
     * Evaluates the chess position
     *
     * @param FEN        FEN of the position to evaluate (passed as GET param)
     * @param depth      Depth of evaluation
     * @param variations Number of primary variations
     * @return <code>JSON</code>
     */
    @GetMapping(value = "/eval", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> evaluation(@RequestParam("fen") String FEN, @RequestParam(defaultValue = "-1") int depth, @RequestParam(defaultValue = "1") int variations) {
        try {
            String error = validateFEN(FEN);
            if (error != null) throw new BadRequestException(error);
            FENEvaluator evaluator = new FENEvaluator(depth, variations);
            return new ResponseEntity<>(evaluator.evaluate(FEN.trim()), HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error while evaluation position!");
            e.printStackTrace(System.err);
            throw new InternalServerErrorException("Unexpected error occurred during evaluation");
        }
    }

    @PostMapping(value = "/analysis", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<Object> analysis(@RequestBody Map<String, Object> payload, @RequestHeader(value = "accept") String accept) {
        int depth = (int) payload.getOrDefault("depth", PGNAnalyzer.NO_LIMIT);
        int time = (int) payload.getOrDefault("time", PGNAnalyzer.NO_LIMIT);
        if (!payload.containsKey("pgn")) throw new BadRequestException("Missing/Invalid pgn");
        String pgnString = payload.get("pgn").toString();
        boolean includeFENs = (boolean) payload.getOrDefault("fens", false);
        try {
            PGNAnalyzer analyzer = new PGNAnalyzer(depth, time);
            AnalysisData analysisData = analyzer.analyzePGN(pgnString, includeFENs);
            if (accept == null || accept.equalsIgnoreCase(MediaType.TEXT_PLAIN_VALUE))
                return new ResponseEntity<>(analyzer.getAnalyzedPGN(), HttpStatus.CREATED);
            return new ResponseEntity<>(analysisData, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("Error occurred while analyzing PGN!");
            e.printStackTrace(System.err);
            throw new InternalServerErrorException("Unexpected error while analyzing PGN");
        }
    }

    @GetMapping(value = "/unicode", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Object> getUnicodeBoard(@RequestParam("fen") String FEN) {
        String error = validateFEN(FEN);
        if (error != null) throw new BadRequestException(error);

        BoardModel boardModel = BoardModel.parseFEN(FEN.trim());
        if (boardModel != null)
            return new ResponseEntity<>(String.format("<pre>%s</pre>", boardModel.unicode()), HttpStatus.OK);
        throw new InternalServerErrorException("Couldn't convert FEN to unicode board");
    }

    private String validateFEN(String FEN) {
        if (FEN == null) return "Missing FEN parameter";
        FEN = FEN.trim();
        if (FEN.isEmpty()) return "Empty FEN parameter";
        if (!FEN.matches(Regexes.FENRegex)) return "Invalid FEN";
        return null;
    }

    @GetMapping(value = "/legalMoves", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> legalMoves(@RequestParam("fen") String FEN) {
        try {
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
            data.setMessage("Legal moves computed successfully");
            data.setUci(gameLogic.getAllLegalMovesUCI());
            data.setLegalMoves(legalMoves);
            return new ResponseEntity<>(data, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error while computing legal moves");
            e.printStackTrace(System.err);
            throw new InternalServerErrorException("Unexpected error occurred! Could not compute legal moves");
        }
    }

    @GetMapping(value = "/randomMove", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> randomMove(@RequestParam("fen") String FEN) {
        try {
            String error = validateFEN(FEN);
            if (error != null) throw new BadRequestException(error);

            RandomMoveData data = new RandomMoveData();
            data.setSuccess(false);
            GameLogic gameLogic = new GameLogic(null, FEN);
            String randomMove = gameLogic.getRandomMove();
            data.setSuccess(true);
            data.setMessage("Random move generated successfully");
            data.setMove(randomMove);
            return new ResponseEntity<>(data, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error occurred while generating random move!");
            e.printStackTrace(System.err);
            throw new InternalServerErrorException("Unexpected error occurred! Could not generate random move");
        }
    }

    @GetMapping(value = "/openings/{eco}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> opening(@PathVariable String eco) {
        try {
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
            }
            return new ResponseEntity<>(data, HttpStatus.OK);
        } catch (Exception e) {
            throw new InternalServerErrorException("Error while loading openings");
        }
    }

    @GetMapping(value = "/getOpening", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> searchOpenings(@RequestParam("moves") String moves) {
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
                data.setMoves(openingGame.pgn().getMoves());
                data.setLastMove(lastBookMove);
                data.setEco(parsedGame.eco());
                data.setName(opening);
            } else data.setMessage("Opening not found!");
            return new ResponseEntity<>(data, HttpStatus.OK);
        } catch (Exception e) {
            Log.e(TAG, "searchOpenings: Error occurred while loading openings", e);
            throw new InternalServerErrorException("Error while loading openings");
        }
    }
}
