package games.loveletter.actions;

import core.AbstractGameState;
import core.actions.IAction;
import core.components.Card;
import core.components.Deck;
import core.observations.IPrintable;
import games.explodingkittens.actions.PlayCard;
import games.loveletter.LoveLetterGameState;
import games.loveletter.cards.LoveLetterCard;


public class KingAction extends PlayCard<LoveLetterCard> implements IAction, IPrintable {

    private final Deck<LoveLetterCard> opponentDeck;
    private final Deck<LoveLetterCard> playerDeck;
    private final int opponentID;

    public KingAction(LoveLetterCard card, Deck<LoveLetterCard> playerHand, Deck<LoveLetterCard> discardPile,
                       Deck<LoveLetterCard> opponentDeck, int opponentID){
        super(card, playerHand, discardPile);
        this.opponentDeck = opponentDeck;
        this.playerDeck = playerHand;
        this.opponentID = opponentID;
    }

    @Override
    public boolean execute(AbstractGameState gs) {
        super.execute(gs);
        if (((LoveLetterGameState) gs).isNotProtected(opponentID)){
            Deck<LoveLetterCard> tmpDeck = new Deck<>("tmp");
            while (opponentDeck.getComponents().size() > 0)
                tmpDeck.add(opponentDeck.draw());
            while (playerDeck.getComponents().size() > 0)
                opponentDeck.add(playerDeck.draw());
            while (tmpDeck.getComponents().size() > 0)
                playerDeck.add(tmpDeck.draw());
        }

        return false;
    }

    @Override
    public Card getCard() {
        return null;
    }

    @Override
    public String toString(){
        return "King - trade hands with player "+ opponentID;
    }

    @Override
    public void printToConsole() {
        System.out.println(toString());
    }
}
