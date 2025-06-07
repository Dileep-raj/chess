package com.drdedd.chess.engine.stockfish;

import com.drdedd.chess.engine.HardwareInfo;
import com.drdedd.chess.game.data.Regexes;
import com.drdedd.chess.misc.MiscMethods;
import lombok.Getter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stockfish chess engine
 */
public class Stockfish {
    private static final String TAG = "Stockfish";
    private static final int timeout = 7500;
    private static final String enginePathUbuntu = "assets/engine/sf17", enginePathWindows = "assets/engine/sf17.exe";

    private static final String uciBoardRegex = "^\\s*\\+[\\w\\s|+-]*?h", uciFENRegex = "Fen:.*", multiPVRegex = "multipv \\d";
    private static final Pattern uciBoardPattern = Pattern.compile(uciBoardRegex), uciFENPattern = Pattern.compile(uciFENRegex), multiPVPattern = Pattern.compile(multiPVRegex);

    // UCI Commands
    private static final String readyResult = "readyok", noBestMove = "bestmove (none)";
    private static final char newLine = '\n';
    private static final int defaultMoveTime = 7500, defaultMoveDepth = 30;
    private static final int defaultHash = 256;

    private Process stockfishEngine;
    private BufferedReader engineReader, errorReader;
    private OutputStreamWriter engineWriter;

    private final HashMap<String, StockfishOption> stockfishOptions;
    private final HashSet<String> infoParameters = EngineLine.getInfoParameters();
    /**
     * Stockfish engine version
     */
    @Getter
    private String stockfishVersion = "";
    @Getter
    private boolean engineStarted;
    /**
     * Engine is currently executing a command
     */
    @Getter
    private boolean engineRunning;
    private boolean whiteToPlay;
    /**
     * Number of variation lines set in the engine
     */
    @Getter
    private int variations;
    private Timer timer;

    public static void main(String[] args) {
        Stockfish stockfish = new Stockfish("11");
        if (stockfish.isEngineStarted()) {
            System.out.println(stockfish.getBoard());

//            System.out.printf("%nDefault position: %s%n", stockfish.getEngineLines(FENs.defaultPosition, "", -1, -1, -1).getFirst());
//            System.out.printf("%nWhite to move losing: %s%n", stockfish.getEngineLines(FENs.whiteToMoveLosing, "", -1, -1, -1).getFirst());
//            System.out.printf("%nBlack to move losing: %s%n", stockfish.getEngineLines(FENs.blackToMoveLosing, "", -1, -1, -1).getFirst());
//            System.out.printf("%nStalemate: %s%n", stockfish.getEngineLines(FENs.stalemate, "", -1, -1, -1).getFirst());
//            System.out.printf("%nWhite won: %s%n", stockfish.getEngineLines(FENs.whiteWon, "", -1, -1, -1).getFirst());
//            System.out.printf("%nBlack won: %s%n", stockfish.getEngineLines(FENs.blackWon, "", -1, -1, -1).getFirst());
//            System.out.printf("%nM2: %s%n", stockfish.getEngineLines(FENs.M2.getFirst(), "", -1, -1, -1).getFirst());
//            System.out.printf("%nM1: %s%n", stockfish.getEngineLines(FENs.testFoolsM1, "", -1, -1, -1).getFirst());
//            System.out.printf("%nM1: %s%n", stockfish.getEngineLines(FENs.M1.getFirst(), "", -1, -1, -1).getFirst());
//
//            System.out.println("\nTest MultiPV:");
//            stockfish.setOption(StockfishOption.optionMultiPV, String.valueOf(3));
//            ArrayList<EngineLine> engineLines = stockfish.getEngineLines(FENs.M4.getFirst(), "", -1, -1, -1);
//            for (EngineLine engineLine : engineLines) System.out.println("\n" + engineLine);
//            System.out.println("Eval: " + stockfish.getEngineLines("8/R7/2q5/8/6k1/8/1P5p/K6R w - - 0 124", "", 10000, -1, -1));

            System.out.println("\nBench results:");
            System.out.println(stockfish.getBench());
            stockfish.quitEngine();
        }
    }

    public Stockfish(String threadCount) {
        HardwareInfo hardwareInfo = new HardwareInfo();

        stockfishOptions = new HashMap<>();
        whiteToPlay = true;
        variations = 1;

        System.out.println("\nEngine starting");
        String path = String.format("src%smain%sresources%s%s", File.separator, File.separator, File.separator, hardwareInfo.getOSName().toLowerCase().contains("linux") ? enginePathUbuntu : enginePathWindows);
        try {
//            ClassPathResource classPathResource = new ClassPathResource(path);
            System.out.println("Engine path: " + path);
            File file = new File(path);
            if (!file.exists()) {
                System.err.println("Stockfish engine not found!\n" + file.getAbsolutePath());
                return;
            }
            stockfishEngine = new ProcessBuilder(path).start();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        engineReader = new BufferedReader(new InputStreamReader(stockfishEngine.getInputStream()));
        errorReader = new BufferedReader(new InputStreamReader(stockfishEngine.getErrorStream()));
        engineWriter = new OutputStreamWriter(stockfishEngine.getOutputStream());

        runUCICommand();

        long maxMemory = hardwareInfo.getMaxMemory();
        int threads, hash, maxThreadCount = Integer.parseInt(hardwareInfo.getProperty(HardwareInfo.LOGICAL_CORES));

        if (threadCount != null && !threadCount.isEmpty()) {
            threads = Integer.parseInt(threadCount);
            if (threads > hardwareInfo.maximumSafeThreads()) threads = (int) Math.max(1, maxThreadCount * 0.75);
        } else threads = (int) Math.max(1, maxThreadCount * 0.75);

        hash = MiscMethods.convertToHigherBase(maxMemory, 1024, 2) > defaultHash ? defaultHash : 64;
        setOption(StockfishOption.optionThreads, String.valueOf(threads));
        setOption(StockfishOption.optionHash, String.valueOf(hash));
        if (isReady()) {
            System.out.println("\nStockfish engine is started");
            engineStarted = true;
        } else System.err.println("Engine failed to start");

    }

    /**
     * Stops engine if its running
     */
    public void stopEngine() {
        try {
            sendCommand(Command.stopCommand.toString());
            engineRunning = false;
            stockfishEngine = null;
            System.out.println("\nEngine stopped");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Stops and quits the engine process
     */
    public void quitEngine() {
        try {
            sendCommand(Command.stopCommand.toString());
            sendCommand(Command.exitCommand.toString());
            engineRunning = false;
            System.out.println("\nEngine stopped and exited");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Sets the given stockfish option
     *
     * @param name  Name of the stockfish option
     * @param value Value for the option
     */
    public void setOption(String name, String value) {
        if (stockfishOptions.containsKey(name)) {
            StockfishOption option = stockfishOptions.get(name);
            if (!option.isValidValue(value)) {
                String type = option.getType();
                String expected = "";
                if (!type.equals(StockfishOption.typeButton) && !type.equals(StockfishOption.typeString))
                    expected = "\nExpected " + (type.equals(StockfishOption.typeCheck) ? "true|false" : "value between " + option.getMin() + " and " + option.getMax());
                System.err.printf("Invalid value for option \"%s\": \"%s\"%s%n", name, value, expected);
                return;
            }
            option.setValue(value);
            sendCommand(commandBuilder(Command.setOptionCommand.command, StockfishOption.attributeName, name, StockfishOption.attributeValue, value));
            if (name.equals(StockfishOption.optionMultiPV)) variations = Integer.parseInt(value);
            System.out.printf("Set %s = %s%n", name, value);
        } else System.err.println("Unknown stockfish option: " + name);
    }

    /**
     * @return <code>true|false</code> - Engine is ready
     */
    private boolean isReady() {
        if (engineRunning) return false;
        sendCommand(Command.readyCommand.toString());
        while (true) {
            String output = readLine(engineReader);
            if (output.equals(readyResult)) return true;
        }
    }

    /**
     * Tells engine to use UCI and check options supported by the engine
     */
    private void runUCICommand() {
        String commandResult = getCommandResult(Command.uciCommand.toString());
        String[] lines = commandResult.split("\\n");
//        try {
        for (String line : lines)
            if (line.startsWith("id name")) stockfishVersion = line.replace("id name", "").trim();
            else if (line.startsWith("option ")) {
                String name, type, defaultValue = "", min = "", max = "";
                name = extractAttribute(line, StockfishOption.attributeName, line.indexOf(StockfishOption.attributeType));
                int index = line.indexOf(StockfishOption.attributeDefault);
                type = extractAttribute(line, (StockfishOption.attributeType), index == -1 ? line.length() : index);
                switch (type) {
                    case StockfishOption.typeString, StockfishOption.typeCheck ->
                            defaultValue = extractAttribute(line, StockfishOption.attributeDefault, line.length());
                    case StockfishOption.typeSpin -> {
                        defaultValue = extractAttribute(line, StockfishOption.attributeDefault, line.lastIndexOf(StockfishOption.attributeMin));
                        min = extractAttribute(line, StockfishOption.attributeMin, line.indexOf(StockfishOption.attributeMax));
                        max = extractAttribute(line, (StockfishOption.attributeMax), line.length());
                    }
                }
                stockfishOptions.put(name, new StockfishOption(name, type, defaultValue, min, max));
            }
//        } catch (Exception e) {
//            e.printStackTrace(System.err);
//        }
        System.out.printf("Stockfish version: %s%n%n", stockfishVersion);
        System.out.printf("%-20s %-10s %-10s %-10s %-10s%n", StockfishOption.attributeName, StockfishOption.attributeType, StockfishOption.attributeDefault, StockfishOption.attributeMin, StockfishOption.attributeMax);
        for (String optionName : stockfishOptions.keySet()) {
            StockfishOption option = stockfishOptions.get(optionName);
            System.out.printf("%-20s %-10s %-10s %-10s %-10s%n", option.getName(), option.getType(), option.getDefaultValue(), option.getMin(), option.getMax());
        }
        System.out.println();
    }

    /**
     * Extracts the attribute value from the given line
     *
     * @param line      Line currently reading
     * @param attribute Name of the attribute
     * @param end       End index of substring
     * @return <code>String</code> - Value of the attribute
     */
    private String extractAttribute(String line, String attribute, int end) {
        return line.substring(line.indexOf(attribute), end).replace(attribute, "").trim();
    }

    /**
     * Initialize the board to default position
     */
    public void initializeBoard() {
        sendCommand(Command.newGameCommand.toString());
        sendCommand(Command.defaultPositionCommand.toString());
        whiteToPlay = true;
    }

    /**
     * Set the board position to the given FEN
     *
     * @param FEN FEN of the position
     */
    public void setPosition(String FEN) {
//        sendCommand(newGameCommand);
        sendCommand(commandBuilder("", Command.positionFENCommand.command, FEN));
        whiteToPlay = FEN.contains(" w ");
    }

    /**
     * Play the given moves from the default initial position or from the given FEN
     *
     * @param moves Moves to play from initial position
     */
    public void playMoves(String FEN, String moves) {
        String[] split = moves.split(" ");
        if (FEN.isEmpty()) {
            sendCommand(commandBuilder(Command.defaultPositionCommand.command, Command.movesCommand.command, moves));
            whiteToPlay = split.length % 2 == 0;
        } else {
            sendCommand(commandBuilder("", Command.positionFENCommand.command, FEN, Command.movesCommand.toString(), moves));
            whiteToPlay = FEN.contains(" w ");
            if (!moves.isEmpty() && split.length % 2 != 0) whiteToPlay = !whiteToPlay;
        }
    }

    /**
     * Write a command to the engine
     *
     * @param command Command to write to the engine
     */
    private void sendCommand(String command) {
        try {
            engineWriter.write(command + newLine);
            engineWriter.flush();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Evaluates the current position and finds best move<br>
     *
     * @param FEN   FEN of the board
     * @param time  Time limit of evaluation in ms
     * @param depth Depth of evaluation
     * @param nodes Nodes size of the evaluation
     * @return {@code HashMap<String,String>} - Map of info parameters and value
     */
    public ArrayList<EngineLine> getEngineLines(String FEN, String moves, int time, int depth, int nodes) {
        if (isInvalidFEN(FEN)) {
            System.out.println("Invalid FEN!: " + FEN);
            return null;
        }
        boolean gameOver;
        String line, command;
        ArrayList<EngineLine> engineLines = new ArrayList<>();
        String[] lines = new String[variations];

        playMoves(FEN, moves);

        if (time != -1 || depth != -1 || nodes != -1)
            command = commandBuilder(Command.goCommand.command, Command.moveDepth.command, String.valueOf(depth), Command.moveTime.command, String.valueOf(time), Command.moveNodes.command, String.valueOf(nodes));
        else
            command = commandBuilder(Command.goCommand.command, Command.moveDepth.command, String.valueOf(defaultMoveDepth), Command.moveTime.command, String.valueOf(defaultMoveTime));

        if (isReady()) sendCommand(command);
        sendCommand(Command.readyCommand.command);
        engineRunning = true;
        while (true) {
            line = readLine(engineReader);
            if (line.startsWith("bestmove")) {
                gameOver = line.equals(noBestMove);
                break;
            }
            if ((lines[0] == null || lines[0].isEmpty()) && line.contains("score")) lines[0] = line;
            if (line.contains("multipv")) {
                Matcher matcher = multiPVPattern.matcher(line);
                if (matcher.find()) {
                    int variation = Integer.parseInt(matcher.group().split(" ")[1]);
                    lines[variation - 1] = line;
                }
            }
        }
        lines[0] = lines[0] + ' ' + line;

        engineRunning = false;

        for (String pv : lines) engineLines.add(new EngineLine(pv, whiteToPlay, gameOver));
        return engineLines;
    }

    public String getBench() {
        String line;
        sendCommand(Command.stopCommand.command);
        sendCommand(Command.benchCommand.command);
        engineRunning = true;
        long ms = 0, nodes = 0, nps = 0;
        while ((line = readLine(engineReader)) != null) if (line.equals("bestmove f1g1 ponder f8g8")) break;
        while ((line = readLine(errorReader)) != null) {
            if (line.contains(":") && !line.startsWith("Position")) {
//                System.out.println(line);
                String trim = line.substring(line.lastIndexOf(':') + 1).trim();
                if (line.startsWith("Total time")) ms = Long.parseLong(trim);
                if (line.startsWith("Nodes searched")) nodes = Long.parseLong(trim);
                if (line.startsWith("Nodes/second")) {
                    nps = Long.parseLong(trim);
                    break;
                }
            }
        }
        System.out.println("Bench completed");
        engineRunning = false;
        return "%s nodes searched in %d s at %sn/s".formatted(MiscMethods.convertNumber(nodes), ms, MiscMethods.convertNumber(nps));
    }

    /**
     * Parses engine score to evaluation score
     *
     * @param score    Score string
     * @param gameOver If there are no legal moves
     * @return Evaluation of the position {@code (+|-|<empty>)<evaluation>|Draw|1-0|0-1}
     */
    private String parseScore(String score, boolean gameOver) {
//        System.out.println("white to move: " + whiteToPlay);
        String[] split = score.split(" ");
        if (gameOver) {
            if (split[0].equals("cp")) return "Draw";
            else if (split[0].equals("mate")) return whiteToPlay ? "0-1" : "1-0";
        }
        int no = Integer.parseInt(split[1]);
        if (!whiteToPlay) no = -no;
        String prefix = no == 0 ? "" : no > 0 ? "+" : "-";
        if (split[0].equals("mate")) return prefix + "M" + Math.abs(no);
        return prefix + Math.abs((float) no / 100);
    }

    private String commandBuilder(String mainCommand, String... args) {
        StringBuilder commandBuilder = new StringBuilder(mainCommand.trim());
        String parameter = null;
        for (String arg : args) {
            if (parameter == null || parameter.isEmpty()) {
                parameter = arg;
                continue;
            }
            if (arg == null || arg.isEmpty() || arg.equals("-1")) {
                parameter = null;
                continue;
            }
            commandBuilder.append(' ').append(parameter.trim()).append(' ').append(arg.trim());
            parameter = null;
        }
        return commandBuilder.toString();
    }

    /**
     * @param command Command to execute
     * @return <code>String</code> - Complete output after executing the command
     */
    private String getCommandResult(String command) {
        StringBuilder result = new StringBuilder();
        String line;
        if (isReady()) engineRunning = true;

        sendCommand(command);
        sendCommand(Command.readyCommand.command);
        while (true) {
            line = readLine(engineReader);
            if (line.equals(readyResult)) break;
            result.append(line).append(newLine);
        }
        engineRunning = false;
        return result.toString();
    }

    /**
     * @return <code>String</code> - UCI board
     */
    public String getBoard() {
        String commandResult = getCommandResult(Command.displayBoardCommand.command);
        Matcher uciBoardMatcher = uciBoardPattern.matcher(commandResult);
        if (uciBoardMatcher.find()) return uciBoardMatcher.group() + newLine;
        else System.err.println("UCI Board not found!");
        return "";
    }

    /**
     * @return <code>String</code> - FEN of the current position
     */
    public String getFEN() {
        String commandResult = getCommandResult(Command.displayBoardCommand.command);
        Matcher FENMatcher = uciFENPattern.matcher(commandResult);
        if (FENMatcher.find()) return FENMatcher.group().replace("Fen: ", "").trim();
        else System.err.println("FEN not found!");
        return "";
    }

    private boolean isInvalidFEN(String FEN) {
        return !FEN.matches(Regexes.FENRegex);
    }

    private String readLine(BufferedReader reader) {
        setTimeout(reader);
        try {
            var line = reader.readLine();
            stopTimeout();
            return line;
        } catch (IOException e) {
            e.printStackTrace(System.err);
//            throw new RuntimeException(e);
            return null;
        }
    }

    private void setTimeout(BufferedReader reader) {
        timer = new Timer(timeout, reader);
        timer.start();
    }

    private void stopTimeout() {
        timer.interrupt();
    }

    private static class Timer extends Thread {
        private final long ms;
        private final BufferedReader reader;

        private Timer(long ms, BufferedReader reader) {
            this.ms = ms;
            this.reader = reader;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(ms);
                reader.close();
                throw new RuntimeException(String.format("Timeout! Time exceeded %d ms\nCouldn't read response from engine", ms));
            } catch (InterruptedException ignored) {
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Commands for stockfish engine
     *
     * @see <a href='https://official-stockfish.github.io/docs/stockfish-wiki/UCI-&-Commands.html'>Official stockfish docs</a>
     */
    private enum Command {
        /**
         * Bench engine
         */
        benchCommand("bench"),
        /**
         * Get compiler information
         */
        compilerCommand("compiler"),
        /**
         * Set board to default start position
         */
        defaultPositionCommand("position startpos"),
        /**
         * Displays board in terminal
         */
        displayBoardCommand("d"),
        /**
         * Quit engine
         */
        exitCommand("quit"),
        /**
         * Start searching
         */
        goCommand("go"),
        /**
         * Depth limit for search
         */
        moveDepth("depth"),
        /**
         * Node limit for search
         */
        moveNodes("nodes"),
        /**
         * Time limit for search
         */
        moveTime("movetime"),
        /**
         * Make specified moves in the given position
         */
        movesCommand("moves"),
        /**
         * New UCI game
         */
        newGameCommand("ucinewgame"),
        /**
         * Set board to given fen position
         */
        positionFENCommand("position fen"),
        /**
         * Is engine ready
         */
        readyCommand("isready"),
        /**
         * Change UCI option
         */
        setOptionCommand("setoption"),
        /**
         * Stop search
         */
        stopCommand("stop"),
        /**
         * Set UCI mode for engine
         */
        uciCommand("uci");

        private final String command;

        Command(String command) {
            this.command = command;
        }

        @Override
        public String toString() {
            return command;
        }

    }
}