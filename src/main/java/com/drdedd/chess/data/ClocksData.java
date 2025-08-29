package com.drdedd.chess.data;

import lombok.Data;

import java.util.List;

@Data
public class ClocksData {
    List<Long> white, black;
}
