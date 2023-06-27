package players.rl;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.actions.AbstractAction;


public class RLPlayer extends AbstractPlayer {

    public static final String resourcesPath = "src/main/java/players/rl/resources/";

    enum RLType {
        TABULAR,
        LINEAR_APPROX,
    }

    private final Random rng;

    final public RLParams params;
    private QWeightsDataStructure qWeights;

    private RLTrainer trainer;

    public RLPlayer(QWeightsDataStructure qWeights, RLParams params) {
        this.rng = new Random(params.getRandomSeed());
        this.params = params;
        this.qWeights = qWeights;
        this.qWeights.setParams(params);
    }

    RLPlayer(QWeightsDataStructure qWeights, RLParams params, RLTrainer trainer) {
        this(qWeights, params);
        this.trainer = trainer;
        this.qWeights.setTrainingParams(trainer.params);
    }

    @Override
    public void initializePlayer(AbstractGameState gameState) {
        this.params.features.linkPlayer(this);
        this.qWeights.initialize(gameState.getGameType().name());
        if (this.trainer != null)
            this.trainer.initializeTrainer(gameState);
    }

    @Override
    public void finalizePlayer(AbstractGameState gameState) {
        if (trainer == null)
            return;
        trainer.train(this, gameState);
    }

    @Override
    public AbstractAction _getAction(AbstractGameState gameState, List<AbstractAction> possibleActions) {
        AbstractAction chosenAction = trainer == null || rng.nextFloat() > params.epsilon
                ? randArgmaxEvaluation(gameState, possibleActions)
                : possibleActions.get(rng.nextInt(possibleActions.size()));

        // TODO implement better methods for reward (score, etc.?)
        if (trainer != null)
            trainer.addTurn(this, gameState, chosenAction, possibleActions);
        return chosenAction;
    }

    private AbstractAction randArgmaxEvaluation(AbstractGameState gameState, List<AbstractAction> possibleActions) {
        // Choose an action that maximizes the Q-function
        List<AbstractAction> maximizingActions = new LinkedList<AbstractAction>();
        double qMax = -Double.MAX_VALUE;
        for (AbstractAction a : possibleActions) {
            // Apply the action to the state
            double q = qWeights.evaluateQ(this, gameState, a);
            // Keep all actions that maximize Q
            if (q > qMax) {
                maximizingActions.clear();
                maximizingActions.add(a);
                qMax = q;
            } else if (q == qMax)
                maximizingActions.add(a);
        }
        // Choose a random action that maximizes Q
        return maximizingActions.get(rng.nextInt(maximizingActions.size()));
    }

    @Override
    public RLPlayer copy() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'copy'");
    }

}
