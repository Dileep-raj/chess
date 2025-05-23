package com.drdedd.chess.game.gameData;

import lombok.Getter;

/**
 * Collection of chess annotations
 */
@Getter
public enum ChessAnnotation {

    /**
     * Best move (Custom annotation of Simpli Chess)
     */
    BEST("$9", "$9"),
    /**
     * Blunder move <b color="#D02323">??</b>
     */
    BLUNDER("??", "$4"),
    /**
     * Book move (Custom annotation of Simpli Chess)
     */
    BOOK(" $0", " $0"),
    /**
     * Brilliant move <b color="#44DADA">!!</b>
     */
    BRILLIANT("!!", "$3"),
    /**
     * Dubious move <b color="#FDED30">?!</b>
     */
    INACCURACY("?!", "$6"),
    /**
     * Great move <b color="#22AAD0">!</b>
     */
    GREAT("!", "$1"),
    /**
     * Interesting move <b color="#2290A5">!?</b>
     */
    INTERESTING("!?", "!"),
    /**
     * Mistake <b color="#FFA600">?</b>
     */
    MISTAKE("?", "$2");
    private static final ChessAnnotation[] CHESS_ANNOTATIONS = values();

    /**
     * -- GETTER --
     *
     */
    private final String number, /**
     * -- GETTER --
     *
     */
            annotation;

    ChessAnnotation(String annotation, String number) {
        this.annotation = annotation;
        this.number = number;
    }

    /**
     * @param s Annotation or numbered annotation
     * @return <code>Annotation|null</code>
     */
    public static ChessAnnotation getAnnotation(String s) {
        s = s.trim();
        for (ChessAnnotation a : CHESS_ANNOTATIONS)
            if (a.getAnnotation().equals(s) || a.getNumber().trim().equals(s) || a.toString().equalsIgnoreCase(s))
                return a;
        return null;
    }
}