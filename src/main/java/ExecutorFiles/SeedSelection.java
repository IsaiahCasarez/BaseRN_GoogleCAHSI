package ExecutorFiles;

import ParserFiles.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import static ExecutorFiles.PolygonGraph.printPolygons;

public class SeedSelection {
    public static void main(String[] args) throws InvalidQueryInformation {
        List<Area> areaList = createGridAreas();

        Parser parseQuery = new Parser();

        String query = " SELECT REGIONS, REGIONS.p;"
                + "FROM NYC_census_tracts;"
                + "WHERE p=10 ,"
                + "5000 <= MIN ON population, OBJECTIVE COMPACT,"
                + "OPTIMIZATION CONNECTED, HEURISTIC TABU;";
        QuerySpecifics queryInfo = null;
        try {
            parseQuery.validateQuery(query);
            queryInfo = parseQuery.getQueryInfo();
            System.out.println(queryInfo.toString());
        }
        catch (InvalidRSqlSyntaxException e) {
            System.out.println(e);
        }

        Set<Area> seedSet = SeedSelection(areaList, (int)queryInfo.getPValueDouble(), 50, true, parseQuery.getQueryInfo(), true);

    }

    //Dummy Data of a 10 by 10 polygons to represent a grid
    public static List<Area> createGridAreas() {
        List<Area> areaList = new ArrayList<>();

        int cellSize = 50;

        // Create a 10x10 grid of polygons
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                // coordinates for the polygon
                int[] xCoords = {x * cellSize, (x + 1) * cellSize, (x + 1) * cellSize, x * cellSize};
                int[] yCoords = {y * cellSize, y * cellSize, (y + 1) * cellSize, (y + 1) * cellSize};
                Polygon polygon = new Polygon(xCoords, yCoords, 4);

                int minValue = 5000;
                int maxValue = 10000;
                Random rand = new Random();

                // right now the spatially extensive attribute is the population
                Area area = new Area(areaList.size() + 1, polygon, rand.nextInt(maxValue - minValue + 1) + minValue, 0.0);

                // Add the area to the list
                areaList.add(area);
            }
        }

        return areaList;
    }


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
    public static void createImageOfState(Set<Area> seedSet, String filename, String title) {
        List<Area> seedList = new ArrayList<>(seedSet);
        printPolygons(seedList, filename + ".png", title);
    }

    public static Set<Area> SeedSelection(List<Area> areaList, int pRegions, int mIterations, boolean Scattered, QuerySpecifics querySpecifics, boolean printDebugImages) throws InvalidQueryInformation {
        //initialize empty set we will fill with our seeds when done
        Set<Area> seedSet = new HashSet<>();

        //if areas do not satisfy the max and min constraint in C then we do not want to add them to our set (value is above max or below min)
        for (Area area : areaList) {
            if (satifiesConstraints(area, querySpecifics)) {
                seedSet.add(area);
            }
        }

        if (seedSet.size() < pRegions) {
            throw new InvalidQueryInformation("The pRegions you specified: " + pRegions + " is greater than the number of valid seed areas: " + seedSet.size());
        }

       // createImageOfState(seedSet, "maxMinfiltered", "inital");

        //only choose the seeds if we dont want the max number of seeds
        double finalSeedsEucledian = 0;
        if (querySpecifics.getPValueEnum() != QueryEnums.pType.PMAX) {

            //Srandom = p seed areas selected randomly from S
            Random random = new Random();
            while (seedSet.size() > pRegions) {
                int randomIndex = random.nextInt(seedSet.size());
                Area[] array = seedSet.toArray(new Area[0]);
                seedSet.remove(array[randomIndex]);
            }

            //createImageOfState(seedSet, "firstRandomSeeds", "firstRandomSeeds");
            //areas are replaced with the areas that are not in S to ensure that the seeds in S are as far away from possible from each other
            if (Scattered) {


                //S notseeds = S- S random
                Set<Area> notSeedSet = new HashSet<>(areaList);
                notSeedSet.removeAll(seedSet);

                while (mIterations > 0) {

                    //initlize our minAre and eudlidan distance in the seedSet to be invalid
                    Area minArea = null;
                    double minEucledianDistance = Double.POSITIVE_INFINITY;
                    double originalTotalEucleidan = 0;

                    //find smallest euclidan distance among all of our seeds in seedSet
                    for (Area seed : seedSet) {
                        double currentEucledian = computeEucledianDistance(seed, seedSet);
                        //for this current seedSet we need to know what the total euldian distance is for all our seeds
                        originalTotalEucleidan += currentEucledian;
                        seed.setDissimilarityAttribute(currentEucledian);
                        //figure out the seed with the mimnum eucldian distance to all the other seeds
                        if (currentEucledian < minEucledianDistance) {
                            minArea = seed;
                            minEucledianDistance = currentEucledian;
                        }
                    }
                    minArea.setCustomColor(Color.RED);

                    Set<Area> modifiedSeedSet = new HashSet<>(seedSet);
                    Area randomArea = getRandomElement(notSeedSet);

                    if (printDebugImages) {
                        createImageOfState(seedSet, "seedSetIteration" + mIterations, "Euclidian " + originalTotalEucleidan);
                    }
                    //modified Seed set will no be the exact same seed set except remove min eudlidan an
                    modifiedSeedSet.remove(minArea);
                    modifiedSeedSet.add(randomArea);
                    randomArea.setCustomColor(Color.YELLOW);

                    //find out if adding in this random area will improve our seeds!!
                    double newTotalEucledian = 0;
                    for (Area seed : modifiedSeedSet) {
                        double currentEucledian = computeEucledianDistance(seed, modifiedSeedSet);
                        seed.setDissimilarityAttribute(currentEucledian);
                        newTotalEucledian += currentEucledian;
                    }

                    if (printDebugImages) {
                        createImageOfState(modifiedSeedSet, "modifiedseedSetIteration" + mIterations, "Euclidian " + newTotalEucledian);
                    }

                    // if random improves quality of S
                    if (newTotalEucledian > originalTotalEucleidan) {
                        //make the seedset equal to the
                        seedSet = modifiedSeedSet;

                        //remove the random area that got added into the seedset
                        notSeedSet.remove(randomArea);
                        ///TODO: ask if i should remove this entirely or put it back into the
                        //   notSeedSet.add(minArea);
                    }
                    finalSeedsEucledian = Math.max(newTotalEucledian, originalTotalEucleidan);
                    mIterations--;
                }

            }

        }
        if (printDebugImages) {
            createImageOfState(seedSet, "selectedSeeds.png", "Final Eucledian: " + finalSeedsEucledian);
        }
        return seedSet;
    }

    //helper method tp retrive a random element from a set
    public static <T> T getRandomElement(Set<T> set) {

        T[] array = (T[]) set.toArray();
        Random rand = new Random();
        int randomIndex = rand.nextInt(array.length);

        return array[randomIndex];
    }

    //MAX min contraint for all the seeds as specified by the query user passes in
    public static boolean satifiesConstraints(Area area, QuerySpecifics queryInfo) {

        for (BoundsSubclause subclause: queryInfo.getBoundsSubclauses()) {

            if (subclause.getAggFunction() == QueryEnums.Aggregate.MIN) {
                //TODO: making assumption here that we did: 100 <= MIN not MIN >= 100!!
                //if we are less than the min we are not valid
                Object spatiallyExtensiveAttribute = area.getSpatiallyExtensiveAttribute();
                Double lowerBound = subclause.getLowerBound();
                if (spatiallyExtensiveAttribute instanceof Number && ((Number) spatiallyExtensiveAttribute).doubleValue() < lowerBound) {
                    return false;
                }
            }

            if (subclause.getAggFunction() == QueryEnums.Aggregate.MAX) {
                //TODO: making assumption here that we did: 100 <= MAX not MAX >= 100!!
                //if we are greater than the MAX we are not valid
                Object spatiallyExtensiveAttribute = area.getSpatiallyExtensiveAttribute();
                Double lowerBound = subclause.getLowerBound();
                if (spatiallyExtensiveAttribute instanceof Number && ((Number) spatiallyExtensiveAttribute).doubleValue() > lowerBound) {
                    return false;
                }
            }
        }

        return true;
    }

}
