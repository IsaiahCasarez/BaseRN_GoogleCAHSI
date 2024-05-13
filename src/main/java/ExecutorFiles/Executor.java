package ExecutorFiles;

import java.util.Set;

public class Executor {

    public static double computeEucledianDistance(Area currentSeed, Set<Area> allSeeds) {
        double totalEucledianDistance = 0.0;
        double[] curSeedCentroid = currentSeed.getCentroid();
        double x1 = curSeedCentroid[0];
        double y1 = curSeedCentroid[0];

        for (Area curComparisonSeed: allSeeds) {
            double[] curComparionCentroid = curComparisonSeed.getCentroid();
            double x2 = curComparionCentroid[0];
            double y2 = curComparionCentroid[1];
            totalEucledianDistance += Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        }

        return totalEucledianDistance;
    }

}