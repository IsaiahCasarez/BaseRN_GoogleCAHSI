import ExecutorFiles.Area;
import ExecutorFiles.SeedSelection;
import ParserFiles.InvalidRSqlSyntaxException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import ParserFiles.*;
import ParserFiles.Parser;
import static org.junit.Assert.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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

    @Test
    public void testComputeEuclideanDistance() {
        // Test the computeEuclideanDistance method to ensure it calculates distances correctly
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
            System.out.println(queryInfo.toString());
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
            System.out.println(queryInfo.toString());
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
