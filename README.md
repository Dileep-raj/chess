# Chess - Spring Boot Application

### About

Plain description of the API

<details>
 <summary><code>GET</code> <code><b>/api/about</b></code></summary>

#### Response:

> | http code | content-type             | response                                                     |
> |-----------|--------------------------|--------------------------------------------------------------|
> | `200`     | `text/plain;charset=UTF-8`| `Chess API for position evaluation and game analysis`        |

#### Example cURL:

```bash
curl -X GET http://localhost:8080/api/about
```

</details>

---

### Evaluation

Evaluate a chess position from FEN

<details>
 <summary><code>GET</code> <code><b>/api/eval</b></code></summary>

#### Parameters:

> | name         | type     | data type | description                                  |
> | ------------ | -------- | --------- | -------------------------------------------- |
> | `fen`        | required | string    | FEN string representing the chess position   |
> | `depth`      | optional | int       | Depth of evaluation (15 to 30)               |
> | `variations` | optional | int       | Number of variations to evaluate (default 1) |

#### Response:

> | http code | content-type       | response                                                          |
> | --------- | ------------------ | ----------------------------------------------------------------- |
> | `200`     | `application/json` | JSON with evaluation data, best move, engine line, and variations |

#### Example cURL:

```bash
curl -X GET "http://localhost:8080/api/eval?fen=8/8/2R1kpp1/3pp2p/8/1P2PPP1/P3K1P1/8 b - - 0 35&depth=20"
```

#### Response:

```json
{
  "success": true,
  "status": null,
  "message": "Evaluation successful",
  "eval": "+8.36",
  "bestmove": "e6f5",
  "engineLine": "info depth 20 seldepth 40 multipv 1 score cp -836 nodes 4037228 nps 20810453 hashfull 59 tbhits 0 time 194 pv e6f5 c6d6 f5g5 ...",
  "engine": "Stockfish 17",
  "variations": [
    [
      "e6f5",
      "c6d6",
      "f5g5",
      "d6d5",
      "h5h4",
      "g3h4",
      "g5h4",
      "e2f2",
      "h4h5",
      "g2g3",
      "h5h6",
      "f2e2",
      "h6h5",
      "d5c5",
      "h5h6"
    ]
  ],
  "fen": "8/8/2R1kpp1/3pp2p/8/1P2PPP1/P3K1P1/8 b - - 0 35"
}
```

</details>

---

### Analysis

Analyze a PGN game

<details>
 <summary><code>POST</code> <code><b>/api/analysis</b></code></summary>

#### Parameters:

> | name    | type     | data type | description                                |
> | ------- | -------- | --------- | ------------------------------------------ |
> | `pgn`   | required | string    | PGN string of the game                     |
> | `depth` | optional | int       | Depth of analysis (15 to 30)               |
> | `time`  | optional | int       | Time per move evaluation (in milliseconds) |

#### Response:

> | http code | content-type       | response                                            |
> | --------- | ------------------ | --------------------------------------------------- |
> | `200`     | `application/json` | JSON with move-by-move analysis and accuracy report |

#### Example cURL:

```bash
curl -X POST -H "Content-Type: application/json" --data '{"pgn": "1.e3 a5 2.Qh5 Ra6 3.Qxa5 h5 4.Qxc7 Rah6 5.h4 f6 6.Qxd7+ Kf7 7.Qxb7 Qd3 8.Qxb8 Qh7 9.Qxc8 Kg6 10.Qe6"}' http://localhost:8080/api/analysis
```

#### Response:

```json
{
  "success": true,
  "status": null,
  "message": "Analysis successful",
  "depth": 15,
  "engine": "Stockfish 17",
  "pgn": "[App \"?\"]\n[White \"White\"]\n[Black \"Black\"]\n[Date \"?\"]\n[Result \"1/2-1/2\"]\n[AnalyzedBy \"Stockfish 17, depth 15, 0ns\"]\n1. e3 { [%eval +0.11] } a5 { [%eval +0.41] } ...",
  "whiteAnalysis": {
    "mistake": 1,
    "acpl": 688,
    "accuracy": 36,
    "blunder": 5
  },
  "blackAnalysis": {
    "mistake": 0,
    "acpl": 896,
    "accuracy": 33,
    "blunder": 8
  }
}
```

</details>

---

### Unicode Board

Get a Unicode chessboard representation from FEN

<details>
 <summary><code>GET</code> <code><b>/api/unicode</b></code></summary>

#### Parameters:

> | name  | type     | data type | description                                |
> | ----- | -------- | --------- | ------------------------------------------ |
> | `fen` | required | string    | FEN string representing the chess position |

#### Response:

> | http code | content-type | response                            |
> | --------- | ------------ | ----------------------------------- |
> | `200`     | `text/html`  | HTML string with Unicode chessboard |

#### Example cURL:

```bash
curl -X GET "http://localhost:8080/api/unicode?fen=8/8/2R1kpp1/3pp2p/8/1P2PPP1/P3K1P1/8 b - - 0 35"
```

#### Response:

```html

<pre>Board:

8 - - - - - - - -
7 - - - - - - - -
6 - - ♖ - ♚ ♟ ♟ -
5 - - - ♟ ♟ - - ♟
4 - - - - - - - -
3 - ♙ - - ♙ ♙ ♙ -
2 ♙ - - - ♔ - ♙ -
1 - - - - - - - -
a b c d e f g h</pre>
```

</details>

---

### **General Notes**

* All endpoints are designed for ease of use with **RESTful** principles.
* Responses are typically returned as **JSON** for most endpoints, and **HTML** for Unicode board rendering.
* The **evaluation depth** and **variations** can be adjusted based on the user's preference for analysis.

### **Example cURL Usage**:

Below is a quick reference to how the endpoints are called via `cURL`:

1. **About endpoint**:

   ```bash
   curl -X GET http://localhost:8080/api/about
   ```

2. **Evaluation endpoint**:

   ```bash
   curl -X GET "http://localhost:8080/api/eval?fen=8/8/2R1kpp1/3pp2p/8/1P2PPP1/P3K1P1/8 b - - 0 35&depth=20"
   ```

3. **Analysis endpoint**:

   ```bash
   curl -X POST -H "Content-Type: application/json" --data '{"pgn": "1.e3 a5 2.Qh5 Ra6 3.Qxa5 h5 4.Qxc7 Rah6 5.h4 f6 6.Qxd7+ Kf7 7.Qxb7 Qd3 8.Qxb8 Qh7 9.Qxc8 Kg6 10.Qe6"}' http://localhost:8080/api/analysis
   ```

4. **Unicode endpoint**:

   ```bash
   curl -X GET "http://localhost:8080/api/unicode?fen=8/8/2R1kpp1/3pp2p/8/1P2PPP1/P3K1P1/8 b - - 0 35"
   ```

## Game Logic

MVC application with BoardModel, GameUI and GameLogic classes

### BoardModel (Model)

Logical board with the data of the position

- Set of pieces
- Player turn
- Castling rights
- EnPassant pawn
- Half move and full move clock

### GameUI (View)

UI class for the game view

- Updates game data on the UI after each move
- Displays game termination

### 