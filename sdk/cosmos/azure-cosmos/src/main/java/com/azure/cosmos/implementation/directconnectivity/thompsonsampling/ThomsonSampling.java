package com.azure.cosmos.implementation.directconnectivity.thompsonsampling;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;

import java.io.InputStream;
import java.util.*;
import java.util.stream.IntStream;

public class ThomsonSampling {

    public static final float EXPLORE_PROBABILITY = 0.1f;
    private static final int DEFAULT_WINDOW_SIZE = 10;
    private final Random random = new Random();
    private final Map<Integer, String> regionNameMap = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    List<Double> alpha;
    List<Double> beta;
    int numberOfArms;
    ArrayList<Double> theta;
    List<Deque<Double>> rewards;

    private int windowSize = DEFAULT_WINDOW_SIZE;
    private final EnumeratedIntegerDistribution armExplorationDist;
    private final EnumeratedIntegerDistribution shouldExploreDist;

    public ThomsonSampling(int numRegions, int windowSize) {
        alpha = new ArrayList<Double>(Collections.nCopies(numRegions, 1.0));
        alpha.set(0, 0.5);
        beta = new ArrayList<Double>(Collections.nCopies(numRegions, 1.0));
        beta.set(0, 0.5);
        numberOfArms = numRegions;
        theta = new ArrayList<Double>(Collections.nCopies(numberOfArms, 1.0));
        theta.set(0, 0.5);
        // Initialize the sliding window of rewards for each arm
        rewards = new ArrayList<>(numberOfArms);
        for (int i = 0; i < numberOfArms; i++) {
            rewards.add(new ArrayDeque<>());
        }
        this.windowSize = windowSize;
        int[] armIndices = IntStream.range(0, numRegions).toArray();
        armExplorationDist = new EnumeratedIntegerDistribution(armIndices, getPriorExplorationProbabilities(numRegions));
        shouldExploreDist = new EnumeratedIntegerDistribution(new int[]{0, 1}, new double[]{1 - EXPLORE_PROBABILITY, EXPLORE_PROBABILITY});
    }

    private double[] getPriorExplorationProbabilities(int numRegions){
        double[] probabilities = new double[numRegions];
        probabilities[0] = 0.9;
        for (int i = 1; i < numRegions; i++){
            probabilities[i] = 1.0/(numRegions-1);
        }
        return probabilities;
    }

    public int getSelection() {
        for (int j = 0; j < numberOfArms; j++) {
            theta.set(j, new GammaDistribution(alpha.get(j), 1 / beta.get(j)).sample());
        }
        int selection = this.theta.indexOf(Collections.min(this.theta));
        return selection;
    }

    public void updateReward(int arm, double latencyInMs) {
        rewards.get(arm).add(latencyInMs);
        //Update the posterior distribution for the parameters of the Gamma distribution for the selected arm
        if (rewards.get(arm).size() > windowSize) {
            double oldReward = rewards.get(arm).pop();
            alpha.set(arm, (alpha.get(arm) - oldReward));
            beta.set(arm, (beta.get(arm) - 1));
        }
        alpha.set(arm, (alpha.get(arm) + latencyInMs));
        beta.set(arm, (beta.get(arm) + 1));
    }

    public int getArmToExplore() {
        // Should return the next arm to explore. This is random with max weight on local
        return armExplorationDist.sample();
    }

    public int shouldExplore() {
        return shouldExploreDist.sample();
    }
}
