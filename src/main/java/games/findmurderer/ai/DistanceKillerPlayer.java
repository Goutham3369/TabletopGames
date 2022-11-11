package games.findmurderer.ai;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.actions.AbstractAction;
import core.actions.DoNothing;
import games.findmurderer.MurderGameState;
import games.findmurderer.actions.Kill;
import games.findmurderer.components.Person;
import utilities.Distance;
import utilities.Vector2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

public class DistanceKillerPlayer extends AbstractPlayer {
    // Parameters
    double passProbability = 0;
    BiFunction<Vector2D, Vector2D, Double> distanceFunction = Distance::manhattan_distance;

    // Constant
    static final DoNothing passAction = new DoNothing();

    // Other variables
    long randomSeed;
    Random r;
    HashMap<Integer, Double> distanceToKiller;  // maps component ID of person -> distance to killer

    public DistanceKillerPlayer() {
        randomSeed = System.currentTimeMillis();
        r = new Random(randomSeed);
    }

    @Override
    public void initializePlayer(AbstractGameState gameState) {
        // Assumes constant grid with no movement, precomputes distances to killer for each person in the grid
        // If grid not static, then this will have to be done for every decision

        MurderGameState mgs = (MurderGameState) gameState;
        List<Person> people = mgs.getGrid().getNonNullComponents();

        distanceToKiller = new HashMap<>();
        Vector2D killerPosition = mgs.getPersonToPositionMap().get(mgs.getKiller().getComponentID());
        for (Person p: people) {
            Vector2D pos = mgs.getPersonToPositionMap().get(p.getComponentID());
            distanceToKiller.put(p.getComponentID(), distanceFunction.apply(killerPosition, pos));
        }
    }

    @Override
    public AbstractAction getAction(AbstractGameState gameState, List<AbstractAction> possibleActions) {

        if (possibleActions.contains(passAction) && r.nextDouble() < passProbability) {
            // If a pass probability is set, then pass action will be returned with that probability
            return passAction;
        }
        else {
            // If not passing, have to choose which Kill action to do (i.e. which person to kill).

            // Get a list of all possible targets, keeping a mapping from target ID to action object used to kill them
            HashMap<Integer, Kill> targetToActionMap = new HashMap<>();
            double probSum = 0;
            ArrayList<Integer> possibleTargets = new ArrayList<>();
            for (AbstractAction aa: possibleActions) {
                if (aa instanceof Kill) {
                    Kill a = (Kill)aa;
                    targetToActionMap.put(a.target, a);
                    probSum += distanceToKiller.get(a.target);
                    possibleTargets.add(a.target);
                }
            }

            // Calculate probabilities to kill each target based on their distance to the killer
            double[] probabilities = new double[possibleTargets.size()];
            for (int i = 0; i < probabilities.length; i++) {
                probabilities[i] = distanceToKiller.get(possibleTargets.get(i))/probSum;
            }

            // Choose random target based on probabilities
            double p = r.nextDouble();
            double sum = 0.0;
            int i = 0;
            while(sum < p){
                sum += probabilities[i];
                i++;
            }

            // Return action that kills selected target
            return targetToActionMap.get(possibleTargets.get(i-1));
        }
    }

    @Override
    public DistanceKillerPlayer copy() {
        DistanceKillerPlayer player = new DistanceKillerPlayer();
        player.passProbability = passProbability;
        player.distanceFunction = distanceFunction;
        player.randomSeed = randomSeed;
        player.r = r;
        player.distanceToKiller = new HashMap<>(distanceToKiller);
        return player;
    }
}
