package com.aris.mardiyanto.controller.dto;

import com.aris.mardiyanto.model.Player;
import lombok.Data;

@Data
public class ConnectRequest {
    private Player player;
    private String gameId;
}
