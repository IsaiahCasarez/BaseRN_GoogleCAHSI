package ExecutorFiles;

import ParserFiles.*;

import java.awt.*;
import java.util.List;
import java.util.*;

import static ExecutorFiles.PolygonGraph.printPolygons;
import static ParserFiles.QueryEnums.pType;

public class Executor {
    public static void main(String[] args) throws InvalidQueryInformation {
        List<Area> areaList = createGridAreas();
        printPolygons(areaList, "initalSeeds2.png");

        //set up dummy data for this query (could mock this in the future)
//        QuerySpecifics dummyQueryData = new QuerySpecifics();
//        dummyQueryData.setPValueDouble(5.0);
//        dummyQueryData.setPValueEnum(QueryEnums.pType.K);
        Parser parseQuery = new Parser();

//                String query = "SELECT REGIONS, REGIONS.p;"
//                + " ORDER BY HET DESC;"
//                + " FROM US_counties;"
//                + " WHERE p=14, GAPLESS,"
//                + " 11,000 < SUM < 20,000 ON population, 500 <= MIN"
//                + " ON population,"
//                + "OPTIMIZATION RANDOM,"
//                + "HEURISTIC MSA,"
//                + " OBJECTIVE HETEROGENEOUS ON average_house_price;";
                String query = " SELECT REGIONS, REGIONS.p;"
                + "FROM NYC_census_tracts;"
                + "WHERE p=5 ,"
                + "5000 <= MIN ON population, OBJECTIVE COMPACT,"
                + "OPTIMIZATION CONNECTED, HEURISTIC TABU;";

        try {
            parseQuery.validateQuery(query);
            System.out.println(parseQuery.getQueryInfo().toString());
        }
        catch (InvalidRSqlSyntaxException e) {
            System.out.println(e);
        }

//
        Set<Area> seedSet = SeedSelection(areaList, 5, 4, true, parseQuery.getQueryInfo());

    }

    //dummy data used to represent seeds
    public static java.util.List<Area> createGridAreas() {
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

    public static Set<Area> SeedSelection(List<Area> areaList, int pRegions, int mIterations, boolean Scattered, QuerySpecifics querySpecifics) throws InvalidQueryInformation {
        //initialize empty set we will fill with our seeds when done
        Set<Area> seedSet = new HashSet<>();

        //if areas do not satisfy the max and min constraint in C then we do not want to add them to our set (value is above max or below min)
        for (Area area : areaList) {
            //TODO: implement tjis function
            if (satifiesConstraints(area, querySpecifics)) {
                seedSet.add(area);
            }
        }

        if (seedSet.size() < pRegions) {
            throw new InvalidQueryInformation("The pRegions you specified: " + pRegions + " is greater than the number of valid seed areas: " + seedSet.size());
        }

        //Srandom = p seed areas selected randomly from S
        if (querySpecifics.getPValueEnum() != pType.PMAX) {
            Random random = new Random();
            while (seedSet.size() > pRegions) {
                int randomIndex = random.nextInt(seedSet.size());
                Integer[] array = seedSet.toArray(new Integer[0]);
                seedSet.remove(array[randomIndex]);
            }
        }
        System.out.println(querySpecifics.getPValueEnum());
        for (Area cur: seedSet) {
            System.out.println(seedSet);
        }


        //areas are replaced with the areas that are not in S to ensure that the seeds in S are as far away from possible from each other
        if (Scattered) {
            //S notseeds = S- S random
            Set<Area> notSeedSet = new HashSet<>(areaList);
            notSeedSet.removeAll(seedSet);

            //the area is initialized to a
            Area minArea = getRandomElement(seedSet);
            double minEucledianDistance = Double.NEGATIVE_INFINITY;


            while (mIterations > 0) {

                double originalTotalEucleidan = 0;
                //find smallest euclidan distance among all seeds
                for (Area seed: seedSet) {
                    double currentEucledian = computeEucledianDistance(seed, seedSet);
                    originalTotalEucleidan += currentEucledian;
                    if (currentEucledian < minEucledianDistance) {
                        minArea = seed;
                        minEucledianDistance = currentEucledian;
                    }
                }

                Set<Area> modifiedSeedSet = new HashSet<>(seedSet);
                Area randomArea = getRandomElement(notSeedSet);

                //replace one of the areas and see if it improves
                modifiedSeedSet.remove(minArea);
                modifiedSeedSet.add(randomArea);

                Area copiedMinArea = getRandomElement(modifiedSeedSet);
                double newMinEucledianDistance = Double.NEGATIVE_INFINITY;
                double newTotalEucledian = 0;

                for (Area seed: modifiedSeedSet) {
                    double currentEucledian = computeEucledianDistance(seed, modifiedSeedSet);
                    newTotalEucledian += currentEucledian;

                    if (currentEucledian < minEucledianDistance) {
                        copiedMinArea = seed;
                        newMinEucledianDistance = currentEucledian;
                    }
                }

                if (newTotalEucledian < minEucledianDistance) {
                    seedSet = modifiedSeedSet;
                }


                mIterations++;
            }

        }

        return seedSet;
    }
    
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