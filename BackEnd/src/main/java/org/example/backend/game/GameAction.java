package org.example.backend.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameAction {
    public enum ActionType {
        PLAY,       // Tiến Lên / Phỏm (Discard)
        PASS,       // Tiến Lên (Skip)
        DRAW,       // Phỏm (Draw card)
        MELD,       // Phỏm (Declare Phỏm)
        STEAL,      // Phỏm (Eat/Steal card)
        LAYOUT,     // Mậu Binh (Set hands)
        BET,        // Poker
        RAISE,      // Poker
        CALL,       // Poker
        FOLD,       // Poker
        CHECK,      // Poker
        ATTACH      // Phỏm (Gửi bài)
    }

    private ActionType type;
    private Long playerId;
    private Long targetId; // NEW: For attaching cards to someone else's meld
    private List<Card> cards;
    private Integer amount; // For betting in Poker
    private List<List<Card>> layout; // For Mậu Binh (Chi 1, 2, 3) or Phỏm Melds
}
