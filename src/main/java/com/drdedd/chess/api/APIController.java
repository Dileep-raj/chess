package com.drdedd.chess.api;

import com.drdedd.chess.api.data.AnalysisData;
import com.drdedd.chess.api.error.exceptions.BadRequestException;
import com.drdedd.chess.api.error.exceptions.InternalServerErrorException;
import com.drdedd.chess.engine.FENEvaluator;
import com.drdedd.chess.engine.PGNAnalyzer;
import com.drdedd.chess.game.BoardModel;
import com.drdedd.chess.game.data.Regexes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class APIController {

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
     * @param FEN FEN of the position to evaluate (passed as GET param)
     * @return <code>JSON</code>
     */
    @GetMapping(value = "/eval", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> evaluation(@RequestParam("fen") String FEN, @RequestParam(defaultValue = "-1") int depth, @RequestParam(defaultValue = "1") int variations) {
        try {
            FEN = FEN.trim();
            String error = validateFEN(FEN);
            if (error != null) throw new BadRequestException(error);
            FENEvaluator evaluator = new FENEvaluator(depth, variations);
            return new ResponseEntity<>(evaluator.evaluate(FEN), HttpStatus.OK);
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
                return new ResponseEntity<>(analyzer.getAnalyzedPGN(), HttpStatus.OK);
            return new ResponseEntity<>(analysisData, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error occurred while analyzing PGN!");
            e.printStackTrace(System.err);
            throw new InternalServerErrorException("Unexpected error while analyzing PGN");
        }
    }

    @GetMapping(value = "/unicode", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Object> getUnicodeBoard(@RequestParam("fen") String FEN) {
        FEN = FEN.trim();
        String error = validateFEN(FEN);
        if (error != null) throw new BadRequestException(error);

        BoardModel boardModel = BoardModel.parseFEN(FEN);
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
}
