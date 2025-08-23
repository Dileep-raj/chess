package com.drdedd.chess.api.data;

import lombok.Data;

import java.util.List;

@Data
public class ClocksData {
    List<Long> white, black;
}
