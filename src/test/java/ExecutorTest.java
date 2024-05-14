import ExecutorFiles.*;
import ParserFiles.InvalidRSqlSyntaxException;
import org.junit.BeforeClass;
import org.junit.Test;
import java.awt.*;
import ParserFiles.*;
import ParserFiles.Parser;

import static ExecutorFiles.SeedSelection.*;
import static org.junit.Assert.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ExecutorFiles.Area;

import java.util.*;
import java.util.List;

public class ExecutorTest {

    private static Parser parser;

    @BeforeClass
    public static void setUp() {
        parser = new Parser();
    }

    @Test
    public void testCreateGridAreas() {
        // Test the createGridAreas method to ensure it generates the correct number of areas
        // and that each area is correctly initialized
    }

    public static List<Area> createSpoofArea() {
        List<Area> areaList = new ArrayList<>();

        int cellSize = 50;
        // Create a 10x10 grid of polygons
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
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
    @Test
    public void testComputeEuclideanDistance() {

//        Set<Area> areaSet = new TreeSet<>(createSpoofArea());
//        double Eucledian = computeEucledianDistance(areaSet, getRandomElement(areaSet));
//        createImageOfState(areaSet, "initalAreas", "Eudlidian");

    }

    @Test
    public void testmaintainsAVGConstraints()  {
        int[] xCoords = {0, 1, 1, 0};
        int[] yCoords = {0, 1, 1, 0};
        Polygon polygon = new Polygon(xCoords, yCoords, 4);

        //NOTE: the coordinates are arbitrary only thing that matters is the spatially extensive attribute
        Set<Area> areaSet = new HashSet<>();
        for (int i = 1; i <= 5; i++) {
            areaSet.add(new Area(i, polygon, 1000, 0.0));
        }
        String query = " SELECT REGIONS, REGIONS.p;"
                + "FROM NYC_census_tracts;"
                + "WHERE p=pmax ,"
                + "1000 <= AVG ON population OR 1000 < AVG < 2000 ON pop, OBJECTIVE COMPACT,"
                + "OPTIMIZATION CONNECTED, HEURISTIC TABU;";
        QuerySpecifics queryInfo = null;
        try {
            parser.validateQuery(query);
            queryInfo = parser.getQueryInfo();
            System.out.println(queryInfo.toString());
        } catch (InvalidRSqlSyntaxException e) {
            System.out.println(e);
        }

        assertTrue(RegionGrowing.maintainsConstraints(areaSet, queryInfo));


    }

    @Test
    public void testSeedSelection() {
        // Test the SeedSelection method to ensure it selects the correct number of seeds
        // and that the selected seeds satisfy the specified criteria

    }

    // Paramaters for test case
    @CsvSource({
            "54, true",
            "5400, false"
    })
    @ParameterizedTest
    public void testSatisfiesConstraints(int population, boolean expectedResult) {
        String query = " SELECT REGIONS, REGIONS.p;"
                + "FROM NYC_census_tracts;"
                + "WHERE p=pmax ,"
                + "5000 <= MAX ON population, OBJECTIVE COMPACT,"
                + "OPTIMIZATION CONNECTED, HEURISTIC TABU;";
        QuerySpecifics queryInfo = null;
        try {
            parser.validateQuery(query);
            queryInfo = parser.getQueryInfo();
        } catch (InvalidRSqlSyntaxException e) {
            System.out.println(e);
        }
        int[] xCoords = {0, 1, 1, 0};
        int[] yCoords = {0, 1, 1, 0};
        Polygon polygon = new Polygon(xCoords, yCoords, 4);
        Area area = new Area(1, polygon, population, 0.0);

        // Perform the test based on the parameters
        if (expectedResult) {
            assertTrue(SeedSelection.satifiesConstraints(area, queryInfo));
        } else {
            assertFalse(SeedSelection.satifiesConstraints(area, queryInfo));
        }
    }

    // Paramaters for test case
    @CsvSource({
            "54, false", // 54 < min of 5000 return false
            "5400, true" // 5400 > min of 500 return true
    })
    @ParameterizedTest
    public void testSatisfiesConstraints2(int population, boolean expectedResult) {
        String query = " SELECT REGIONS, REGIONS.p;"
                + "FROM NYC_census_tracts;"
                + "WHERE p=pmax ,"
                + "5000 <= MIN ON population, OBJECTIVE COMPACT,"
                + "OPTIMIZATION CONNECTED, HEURISTIC TABU;";
        QuerySpecifics queryInfo = null;
        try {
            parser.validateQuery(query);
            queryInfo = parser.getQueryInfo();
        } catch (InvalidRSqlSyntaxException e) {
            System.out.println(e);
        }
        int[] xCoords = {0, 1, 1, 0};
        int[] yCoords = {0, 1, 1, 0};
        Polygon polygon = new Polygon(xCoords, yCoords, 4);
        Area area = new Area(1, polygon, population, 0.0);

        // Perform the test based on the parameters
        if (expectedResult) {
            assertTrue(SeedSelection.satifiesConstraints(area, queryInfo));
        } else {
            assertFalse(SeedSelection.satifiesConstraints(area, queryInfo));
        }
    }




}
