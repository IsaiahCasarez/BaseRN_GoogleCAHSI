package ExecutorFiles;

import ParserFiles.InvalidRSqlSyntaxException;
import ParserFiles.Parser;
import ParserFiles.QuerySpecifics;
import ParserFiles.QueryEnums;
import java.awt.*;
import java.util.List;
import java.util.*;

import static ParserFiles.QueryEnums.pType;

public class Executor {
    public static void main(String[] args) throws InvalidQueryInformation {
        List<Area> areaList = createGridAreas();

        for (Area area : areaList) {
            System.out.println(area);
        }
        //set up dummy data for this query (could mock this in the future)
//        QuerySpecifics dummyQueryData = new QuerySpecifics();
//        dummyQueryData.setPValueDouble(5.0);
//        dummyQueryData.setPValueEnum(QueryEnums.pType.K);
        Parser parseQuery = new Parser();
        String query = "SELECT REGIONS, REGIONS.p;"
                + " ORDER BY HET DESC;"
                + " FROM US_counties;"
                + " WHERE p=4, GAPLESS,"
                + " 11,000 < SUM < 20,000 ON population, 500 <= MIN"
                + " ON population, 5000 <= MAX ON population,"
                + "OPTIMIZATION RANDOM,"
                + "HEURISTIC MSA,"
                + " OBJECTIVE HETEROGENEOUS ON average_house_price;";
        try {
            parseQuery.validateQuery(query);
            System.out.println(parseQuery.getQueryInfo().toString());
        }
        catch (InvalidRSqlSyntaxException e) {
            System.out.println(e);
        }

//
//        Set<Area> seedSet = SeedSelection(areaList, 5, 4, true, dummyQueryData);
//        for (Area seed: seedSet) {
//            System.out.println(seed);
//        }
    }

    //dummy data to represent a list of areas
    public static List<Area> createGridAreas() {
        List<Area> areaList = new ArrayList<>();

        int cellSize = 1;

        // Create a 10x10 grid of polygons
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                // coordinates for the polygon
                int[] xCoords = {x, x + cellSize, x + cellSize, x};
                int[] yCoords = {y, y, y + cellSize, y + cellSize};
                Polygon polygon = new Polygon(xCoords, yCoords, 4);

                int minValue = 1000;
                int maxValue = 1000000;
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
        //initlaize empty set we will fill with our seeds when done
        Set<Area> seedSet = new HashSet<>();

        //if areas do not satisfy the max and min constraint in C then we do not want to add them to our set
        for (Area area : areaList) {
            //TODO: implement tjis function
            if (satifiesConstraints(area)) {
                seedSet.add(area);
            }
        }

        if (seedSet.size() < pRegions) {
            throw new InvalidQueryInformation("The pRegions you specified is greater than the number of valid seed Areas (impossible to meet)");
        }

        //only want to keep p valid regions initially in our set
        if (querySpecifics.getPValueEnum() != pType.PMAX) {
            Random random = new Random();
            while (seedSet.size() > pRegions) {
                int randomIndex = random.nextInt(seedSet.size());
                Integer[] array = seedSet.toArray(new Integer[0]);
                seedSet.remove(array[randomIndex]);
            }
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

    //TODO: how do i check if they satisfy min or max constraint where would this info be stored / what do i check?
    public static boolean satifiesConstraints(Area area) {

        return true;
    }


}