import ParserFiles.InvalidRSqlSyntaxException;
import ParserFiles.Parser;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;
import static org.junit.Assert.*;
import org.junit.rules.ExpectedException;
import ParserFiles.*;
public class ParserTest {

    //TODO: clean up this logic into seperate submodules, just get the working implementaiton first.
    /**
     * Testing for the submodules that validate smaller parts of the RSQL signature
     */
    private static Parser parser;

    @BeforeClass
    public static void setUp() {
        parser = new Parser();
    }

    @Test
    public void testSelectValidation() {
        // Valid SELECT statements
        try {
            assertTrue(parser.validateSelect("SELECT REGIONS"));
            assertTrue(parser.validateSelect("SELECT REGIONS, REGIONS.p"));
            assertTrue(parser.validateSelect("SELECT REGIONS, REGIONS.HET"));
        } catch (InvalidRSqlSyntaxException e) {
            fail("Exception should not have been thrown for valid SELECT statements");
        }

        // Invalid SELECT statements
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.validateSelect("SELECT OTHER");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.validateSelect("SELECT OTHER, Regions.p");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.validateSelect("SELECT OTHER, Regions.HET");
        });
    }

    @Test
    public void testOrderByValidCases() {
        // Valid ORDER BY statements
        try {
            assertTrue(parser.validateOrderByClause("ORDER BY HET ASC"));
            assertTrue(parser.validateOrderByClause("ORDER BY HET DESC"));
            assertTrue(parser.validateOrderByClause("ORDER BY CARD ASC"));
            assertTrue(parser.validateOrderByClause("ORDER BY CARD DESC"));
        } catch (InvalidRSqlSyntaxException e) {
            fail("Exception should not have been thrown for valid ORDER BY statements");
        }
    }

    @Test
    public void testOrderByInvalidCases() {
        // Invalid ORDER BY statements
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.validateOrderByClause("ORDER BY HET"); // Missing ASC/DSC
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.validateOrderByClause("ORDER BY CARD"); // Missing ASC/DSC
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.validateOrderByClause("ORDER BY HET DSC"); // Should be DESC, not DSC
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.validateOrderByClause("ORDER BY INVALID ASC"); // Invalid keyword
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.validateOrderByClause("INVALID ORDER BY HET ASC");
        });
    }

    @Test
    public void testvalidateFromClause() {
        // Valid FROM clauses
        try {
            assertTrue(parser.validateFromClause("FROM NYC_census_tracts"));
            assertTrue(parser.validateFromClause("FROM US_counties"));
            assertTrue(parser.validateFromClause("FROM C:\\Users\\John\\Downloads"));
        } catch (InvalidRSqlSyntaxException e) {
            fail("Exception should not have been thrown for valid FROM clauses");
        }

        // Invalid FROM clauses
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.validateFromClause("FROM multiple words");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.validateFromClause("FROM not multiple words");
        });
    }


    @Test
    public void testSubclauseTypes() {
        assertEquals(QueryEnums.SubclauseType.OBJECTIVE, parser.determineSubclauseType("OBJECTIVE HETEROGENEOUS ON attribute_name"));
        assertEquals(QueryEnums.SubclauseType.BOUNDS_CLAUSE, parser.determineSubclauseType("lower_bound (<) SUM (<) upper_bound ON attribute_name"));
        assertEquals(QueryEnums.SubclauseType.OPTIMIZATION, parser.determineSubclauseType("OPTIMIZATION RANDOM"));
        assertEquals(QueryEnums.SubclauseType.GAPLESS, parser.determineSubclauseType("GAPLESS"));
        assertEquals(QueryEnums.SubclauseType.WHERE, parser.determineSubclauseType(" WHERE p=14"));
        assertEquals(QueryEnums.SubclauseType.HEURISTIC, parser.determineSubclauseType("HEURISTIC MSA"));
        assertEquals(QueryEnums.SubclauseType.WHERE, parser.determineSubclauseType("WHERE p = (k | 𝑝𝑀𝐴𝑋 )"));
        assertEquals(QueryEnums.SubclauseType.UNKNOWN, parser.determineSubclauseType("UNKNOWN_SUBCLAUSE"));

        assertEquals(QueryEnums.SubclauseType.BOUNDS_CLAUSE, parser.determineSubclauseType("5000 <= MAX ON population"));
        assertEquals(QueryEnums.SubclauseType.BOUNDS_CLAUSE, parser.determineSubclauseType("11,000 < SUM < 20,000 ON population"));

        assertEquals(QueryEnums.SubclauseType.HEURISTIC, parser.determineSubclauseType("HEURISTIC TABU"));
        assertEquals(QueryEnums.SubclauseType.OPTIMIZATION, parser.determineSubclauseType("OPTIMIZATION CONNECTED"));
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testHandleWhere() throws InvalidRSqlSyntaxException {
        assertTrue(parser.handleWhere("WHERE p = k"));
        assertTrue(parser.handleWhere("WHERE p = pMAX"));
        assertTrue(parser.handleWhere("  WHERE   p    =   k  "));

        exceptionRule.expect(InvalidRSqlSyntaxException.class);
        exceptionRule.expectMessage("Invalid syntax in WHERE clause: WHERE P = INVALID");

        parser.handleWhere("WHERE p = invalid");

        assertTrue(parser.handleWhere("where P = K"));
        assertFalse(parser.handleWhere("WHERE p = k and some additional text"));
    }

    @Test
    public void testHandleHeuristic() {
        // Valid heuristic cases
        try {
            assertTrue(parser.handleHeuristic("HEURISTIC MSA"));
            assertTrue(parser.handleHeuristic("HEURISTIC TABU"));
        } catch (InvalidRSqlSyntaxException e) {
            fail("Unexpected exception thrown: " + e.getMessage());
        }

        // Invalid heuristic cases
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.handleHeuristic("HEURISTIC INVALID");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.handleHeuristic("INVALID HEURISTIC");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.handleHeuristic("HEURISTIC");
        });
    }

    @Test
    public void testHandleGapless() {
        // Valid GAPLESS cases
        try {
            assertTrue(parser.handleGapless("GAPLESS"));
        } catch (InvalidRSqlSyntaxException e) {
            fail("Unexpected exception thrown: " + e.getMessage());
        }

        // Invalid GAPLESS cases
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.handleGapless("GAPLESS ExtraText");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.handleGapless("ExtraText GAPLESS");
        });
    }

    @Test
    public void testHandleOptimization() {
        // Valid OPTIMIZATION cases
        try {
            assertTrue(parser.handleOptimization("OPTIMIZATION RANDOM"));
            assertTrue(parser.handleOptimization("OPTIMIZATION CONNECTED"));
        } catch (InvalidRSqlSyntaxException e) {
            fail("Unexpected exception thrown: " + e.getMessage());
        }

        // Invalid OPTIMIZATION cases
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.handleOptimization("OPTIMIZATION RANDOM ExtraText");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.handleOptimization("optimization CONNECTED");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.handleOptimization("OPTIMIZATION INVALID");
        });
    }

    @Test
    public void testHandleObjective() {
        // Valid OBJECTIVE cases
        try {
            assertTrue(parser.handleObjective(" OBJECTIVE COMPACT"));
            assertTrue(parser.handleObjective("OBJECTIVE HETEROGENEOUS ON attribute_name"));
            assertTrue(parser.handleObjective("OBJECTIVE COMPACT ON another_attribute"));
        } catch (InvalidRSqlSyntaxException e) {
            fail("Unexpected exception thrown: " + e.getMessage());
        }

        // Invalid OBJECTIVE cases
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.handleObjective("OBJECTIVE COMPACT ON another attribute");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.handleObjective("OBJECTIVE INVALID ON attribute_name");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.handleObjective("INVALID HETEROGENEOUS ON attribute_name");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.handleObjective("OBJECTIVE HETEROGENEOUS INVALID");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.handleObjective("INVALID");
        });
    }

    @Test
    public void testHandleBoundsClause() {
        // Valid BOUNDS CLAUSE cases
        try {
            assertTrue(parser.handleBoundsClause("5 < SUM < 10 ON attribute_name"));
            assertTrue(parser.handleBoundsClause("15 <= AVG <= 20 ON another_attribute"));
            assertTrue(parser.handleBoundsClause("11,000 < SUM < 20,000 ON population"));
            assertTrue(parser.handleBoundsClause("500 <= MIN ON population"));
        } catch (InvalidRSqlSyntaxException e) {
            fail("Unexpected exception thrown: " + e.getMessage());
        }

        // Invalid BOUNDS CLAUSE cases
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.handleBoundsClause("notadigit <= AVG <= 20 ON another_attribute");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.handleBoundsClause("Invalid bounds clause");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            parser.handleBoundsClause("10 < INVALID < 20 ON invalid_attribute");
        });
    }


    @Test
    public void testValidQuery() throws InvalidRSqlSyntaxException {
        String validQuery = "SELECT REGIONS;"
                + " ORDER BY HET DESC;"
                + " FROM US_counties;"
                + " WHERE p=14, GAPLESS,"
                + " 11,000 < SUM < 20,000 ON population, 500 <= MIN"
                + " ON population,"
                + " OBJECTIVE HETEROGENEOUS ON average_house_price;";

        String validQuery2 = "SELECT REGIONS, REGIONS.p;" +
                "FROM NYC_census_tracts;" +
                "WHERE p=pMAX," +
                "5000 <= MAX ON population, OBJECTIVE COMPACT," +
                "OPTIMIZATION CONNECTED, HEURISTIC TABU;";


        boolean valid = parser.validateQuery(validQuery);
        assertTrue("Query should be valid", valid);
        assertTrue(parser.validateQuery((validQuery2)));
    }


    @Test(expected = InvalidRSqlSyntaxException.class)
    public void testInvalidQuery() throws InvalidRSqlSyntaxException {
        String invalidQuery = "SELECT INVALID_QUERY;";

        parser.validateQuery(invalidQuery);
        fail("Expected parserFiles.InvalidRSqlSyntaxException to be thrown");
    }

    @Test(expected = InvalidRSqlSyntaxException.class)
    public void testInvalidQuery2() throws InvalidRSqlSyntaxException {
        String invalid = "SELECT REGIONS;"
                + " ORDER BY HET DESC;"
                + " FROM US_counties;";
        parser.validateQuery(invalid);
    }

    @Test(expected = InvalidRSqlSyntaxException.class)
    public void testInvalidQuery3() throws InvalidRSqlSyntaxException {
        String invalid = "SELECT OTHER, Regions.p" +
                "FROM NYC_census_tracts;" +
                "WHERE p=pMAX," +
                "5000 <= MAX ON population, OBJECTIVE COMPACT," +
                "OPTIMIZATION CONNECTED, HEURISTIC TABU;";
        parser.validateQuery(invalid);
    }



}