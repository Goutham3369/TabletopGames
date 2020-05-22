package games.explodingkittens.actions;

import core.components.Card;
import core.AbstractGameState;
import core.components.Component;
import core.components.Deck;
import core.observations.IPrintable;
import games.explodingkittens.ExplodingKittenTurnOrder;
import core.turnorder.TurnOrder;


public class AttackAction<T extends Component> extends PlayCard<T> implements IsNopeable, IPrintable {
    int attackTargetID;

    public AttackAction(T card, Deck<T> sourceDeck, Deck<T> targetDeck, int attackTargetID) {
        super(card, sourceDeck, targetDeck);
        this.attackTargetID = attackTargetID;
    }

    @Override
    public boolean execute(AbstractGameState gs) {
        super.execute(gs);
        ((ExplodingKittenTurnOrder) gs.getTurnOrder()).registerAttackAction(attackTargetID);
        return false;
    }

    @Override
    public Card getCard() {
        return null;
    }

    @Override
    public String toString(){//overriding the toString() method
        return String.format("Player attacks player %d", attackTargetID);
    }

    @Override
    public boolean nopedExecute(AbstractGameState gs, TurnOrder turnOrder) {
        return super.execute(gs);
    }

    @Override
    public void printToConsole() {
        System.out.println(this.toString());
    }
}
