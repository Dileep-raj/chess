package com.drdedd.chess.api.data;

import lombok.Data;
import lombok.Getter;

@Data
public class PlayerData {
    public PlayerData() {
    }

    public PlayerData(String name, Title title, int elo) {
        this.name = name;
        this.title = title;
        this.elo = elo;
    }

    String name;
    Title title;
    int elo;

    /**
     * Chess titles according to FIDE rules
     *
     * @see <a href="https://en.wikipedia.org/wiki/FIDE_titles">FIDE titles</a>
     */
    @Getter
    public enum Title {
        None(""), GM("Grandmaster"), IM("International Master"), FM("FIDE Master"), CM("Candidate Master"), WGM("Woman Grandmaster"), WIM("Woman International Master"), WFM("Woman FIDE Master"), WCM("Woman Candidate Master");
        private final String name;

        Title(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            if (this == None) return "";
            else return super.toString();
        }
    }
}
