package com.drdedd.chess.api;

import com.drdedd.chess.api.data.AnalysisData;
import com.drdedd.chess.api.data.JSONConstants;
import com.drdedd.chess.engine.PGNAnalyzer;
import com.drdedd.chess.game.BoardModel;
import com.drdedd.chess.game.data.Regexes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class APIController {

    @GetMapping(path = "/about")
    public String about() {
        return "Chess API for position evaluation and game analysis";
    }

    /**
     * Evaluates the chess position
     *
     * @param FEN FEN of the position to evaluate (passed as GET param)
     * @return <code>JSON</code>
     */
    @GetMapping(value = "/eval", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> evaluation(@RequestParam("fen") String FEN) {
        try {
            FEN = FEN.trim();

            String error = validateFEN(FEN);
            if (error != null) return ErrorResponse.badRequest(error);

            return new ResponseEntity<>(Map.of(JSONConstants.fen, FEN), HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error while evaluation position!");
            e.printStackTrace(System.err);
            return ErrorResponse.internalErrorResponse("Unexpected error");
        }
    }

    @PostMapping(value = "/analysis", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> analysis(@RequestBody Map<String, Object> payload, @RequestHeader(value = "accept") String accept) {
        int depth = (int) payload.getOrDefault("depth", -1);
        if (!payload.containsKey("pgn")) return ErrorResponse.badRequest("Missing/Invalid pgn");
        String pgnString = payload.get("pgn").toString();
        try {
            PGNAnalyzer analyzer = new PGNAnalyzer(depth, PGNAnalyzer.NO_LIMIT, PGNAnalyzer.NO_LIMIT);
            AnalysisData analysisData = analyzer.analyzePGN(pgnString);
            if (accept == null || accept.equalsIgnoreCase(MediaType.TEXT_PLAIN_VALUE))
                return new ResponseEntity<>(analyzer.getAnalyzedPGN(), HttpStatus.OK);
            return new ResponseEntity<>(analysisData, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error occurred while analyzing PGN!");
            e.printStackTrace(System.err);
            return ErrorResponse.internalErrorResponse("Unexpected error while analyzing PGN");
        }
    }

    @GetMapping(value = "/unicode", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getUnicodeBoard(@RequestParam("fen") String FEN) {
        FEN = FEN.trim();
        String error = validateFEN(FEN);
        if (error != null) return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);

        BoardModel boardModel = BoardModel.parseFEN(FEN);
        if (boardModel != null)
            return new ResponseEntity<>(String.format("<pre>%s</pre>", boardModel.unicode()), HttpStatus.OK);
        else return new ResponseEntity<>("Couldn't convert FEN to unicode board", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String validateFEN(String FEN) {
        if (FEN == null) return "Missing FEN parameter";
        FEN = FEN.trim();
        if (FEN.isEmpty()) return "Empty FEN parameter";
        if (!FEN.matches(Regexes.FENRegex)) return "Invalid FEN";
        return null;
    }

}
