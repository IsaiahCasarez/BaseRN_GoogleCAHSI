import ParserFiles.InvalidRSqlSyntaxException;
import ParserFiles.Parser;
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
    @Test
    public void testSelectValidation() {
        // Valid SELECT statements
        try {
            assertTrue(Parser.validateSelect("SELECT REGIONS"));
            assertTrue(Parser.validateSelect("SELECT REGIONS, REGIONS.p"));
            assertTrue(Parser.validateSelect("SELECT REGIONS, REGIONS.HET"));
        } catch (InvalidRSqlSyntaxException e) {
            fail("Exception should not have been thrown for valid SELECT statements");
        }

        // Invalid SELECT statements
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.validateSelect("SELECT OTHER");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.validateSelect("SELECT OTHER, Regions.p");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.validateSelect("SELECT OTHER, Regions.HET");
        });
    }

    @Test
    public void testOrderByValidCases() {
        // Valid ORDER BY statements
        try {
            assertTrue(Parser.validateOrderByClause("ORDER BY HET ASC"));
            assertTrue(Parser.validateOrderByClause("ORDER BY HET DESC"));
            assertTrue(Parser.validateOrderByClause("ORDER BY CARD ASC"));
            assertTrue(Parser.validateOrderByClause("ORDER BY CARD DESC"));
        } catch (InvalidRSqlSyntaxException e) {
            fail("Exception should not have been thrown for valid ORDER BY statements");
        }
    }

    @Test
    public void testOrderByInvalidCases() {
        // Invalid ORDER BY statements
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.validateOrderByClause("ORDER BY HET"); // Missing ASC/DSC
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.validateOrderByClause("ORDER BY CARD"); // Missing ASC/DSC
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.validateOrderByClause("ORDER BY HET DSC"); // Should be DESC, not DSC
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.validateOrderByClause("ORDER BY INVALID ASC"); // Invalid keyword
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.validateOrderByClause("INVALID ORDER BY HET ASC");
        });
    }

    @Test
    public void testvalidateFromClause() {
        // Valid FROM clauses
        try {
            assertTrue(Parser.validateFromClause("FROM NYC_census_tracts"));
            assertTrue(Parser.validateFromClause("FROM US_counties"));
            assertTrue(Parser.validateFromClause("FROM C:\\Users\\John\\Downloads"));
        } catch (InvalidRSqlSyntaxException e) {
            fail("Exception should not have been thrown for valid FROM clauses");
        }

        // Invalid FROM clauses
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.validateFromClause("FROM multiple words");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.validateFromClause("FROM not multiple words");
        });
    }


    @Test
    public void testSubclauseTypes() {
        assertEquals(QueryEnums.SubclauseType.OBJECTIVE, Parser.determineSubclauseType("OBJECTIVE HETEROGENEOUS ON attribute_name"));
        assertEquals(QueryEnums.SubclauseType.BOUNDS_CLAUSE, Parser.determineSubclauseType("lower_bound (<) SUM (<) upper_bound ON attribute_name"));
        assertEquals(QueryEnums.SubclauseType.OPTIMIZATION, Parser.determineSubclauseType("OPTIMIZATION RANDOM"));
        assertEquals(QueryEnums.SubclauseType.GAPLESS, Parser.determineSubclauseType("GAPLESS"));
        assertEquals(QueryEnums.SubclauseType.WHERE, Parser.determineSubclauseType(" WHERE p=14"));
        assertEquals(QueryEnums.SubclauseType.HEURISTIC, Parser.determineSubclauseType("HEURISTIC MSA"));
        assertEquals(QueryEnums.SubclauseType.WHERE, Parser.determineSubclauseType("WHERE p = (k | 𝑝𝑀𝐴𝑋 )"));
        assertEquals(QueryEnums.SubclauseType.UNKNOWN, Parser.determineSubclauseType("UNKNOWN_SUBCLAUSE"));

        assertEquals(QueryEnums.SubclauseType.BOUNDS_CLAUSE, Parser.determineSubclauseType("5000 <= MAX ON population"));
        assertEquals(QueryEnums.SubclauseType.BOUNDS_CLAUSE, Parser.determineSubclauseType("11,000 < SUM < 20,000 ON population"));

        assertEquals(QueryEnums.SubclauseType.HEURISTIC, Parser.determineSubclauseType("HEURISTIC TABU"));
        assertEquals(QueryEnums.SubclauseType.OPTIMIZATION, Parser.determineSubclauseType("OPTIMIZATION CONNECTED"));
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testHandleWhere() throws InvalidRSqlSyntaxException {
        assertTrue(Parser.handleWhere("WHERE p = k"));
        assertTrue(Parser.handleWhere("WHERE p = pMAX"));
        assertTrue(Parser.handleWhere("  WHERE   p    =   k  "));

        exceptionRule.expect(InvalidRSqlSyntaxException.class);
        exceptionRule.expectMessage("Invalid syntax in WHERE clause: WHERE P = INVALID");

        Parser.handleWhere("WHERE p = invalid");

        assertTrue(Parser.handleWhere("where P = K"));
        assertFalse(Parser.handleWhere("WHERE p = k and some additional text"));
    }

    @Test
    public void testHandleHeuristic() {
        // Valid heuristic cases
        try {
            assertTrue(Parser.handleHeuristic("HEURISTIC MSA"));
            assertTrue(Parser.handleHeuristic("HEURISTIC TABU"));
        } catch (InvalidRSqlSyntaxException e) {
            fail("Unexpected exception thrown: " + e.getMessage());
        }

        // Invalid heuristic cases
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.handleHeuristic("HEURISTIC INVALID");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.handleHeuristic("INVALID HEURISTIC");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.handleHeuristic("HEURISTIC");
        });
    }

    @Test
    public void testHandleGapless() {
        // Valid GAPLESS cases
        try {
            assertTrue(Parser.handleGapless("GAPLESS"));
        } catch (InvalidRSqlSyntaxException e) {
            fail("Unexpected exception thrown: " + e.getMessage());
        }

        // Invalid GAPLESS cases
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.handleGapless("GAPLESS ExtraText");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.handleGapless("ExtraText GAPLESS");
        });
    }

    @Test
    public void testHandleOptimization() {
        // Valid OPTIMIZATION cases
        try {
            assertTrue(Parser.handleOptimization("OPTIMIZATION RANDOM"));
            assertTrue(Parser.handleOptimization("OPTIMIZATION CONNECTED"));
        } catch (InvalidRSqlSyntaxException e) {
            fail("Unexpected exception thrown: " + e.getMessage());
        }

        // Invalid OPTIMIZATION cases
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.handleOptimization("OPTIMIZATION RANDOM ExtraText");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.handleOptimization("optimization CONNECTED");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.handleOptimization("OPTIMIZATION INVALID");
        });
    }

    @Test
    public void testHandleObjective() {
        // Valid OBJECTIVE cases
        try {
            assertTrue(Parser.handleObjective(" OBJECTIVE COMPACT"));
            assertTrue(Parser.handleObjective("OBJECTIVE HETEROGENEOUS ON attribute_name"));
            assertTrue(Parser.handleObjective("OBJECTIVE COMPACT ON another_attribute"));
        } catch (InvalidRSqlSyntaxException e) {
            fail("Unexpected exception thrown: " + e.getMessage());
        }

        // Invalid OBJECTIVE cases
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.handleObjective("OBJECTIVE COMPACT ON another attribute");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.handleObjective("OBJECTIVE INVALID ON attribute_name");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.handleObjective("INVALID HETEROGENEOUS ON attribute_name");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.handleObjective("OBJECTIVE HETEROGENEOUS INVALID");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.handleObjective("INVALID");
        });
    }

    @Test
    public void testHandleBoundsClause() {
        // Valid BOUNDS CLAUSE cases
        try {
            assertTrue(Parser.handleBoundsClause("5 < SUM < 10 ON attribute_name"));
            assertTrue(Parser.handleBoundsClause("15 <= AVG <= 20 ON another_attribute"));
            assertTrue(Parser.handleBoundsClause("11,000 < SUM < 20,000 ON population"));
            assertTrue(Parser.handleBoundsClause("500 <= MIN ON population"));
        } catch (InvalidRSqlSyntaxException e) {
            fail("Unexpected exception thrown: " + e.getMessage());
        }

        // Invalid BOUNDS CLAUSE cases
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.handleBoundsClause("notadigit <= AVG <= 20 ON another_attribute");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.handleBoundsClause("Invalid bounds clause");
        });
        assertThrows(InvalidRSqlSyntaxException.class, () -> {
            Parser.handleBoundsClause("10 < INVALID < 20 ON invalid_attribute");
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


        boolean valid = Parser.validateQuery(validQuery);
        assertTrue("Query should be valid", valid);
        assertTrue(Parser.validateQuery((validQuery2)));
    }


    @Test(expected = InvalidRSqlSyntaxException.class)
    public void testInvalidQuery() throws InvalidRSqlSyntaxException {
        String invalidQuery = "SELECT INVALID_QUERY;";

        Parser.validateQuery(invalidQuery);
        fail("Expected ParserFiles.InvalidRSqlSyntaxException to be thrown");
    }

    @Test(expected = InvalidRSqlSyntaxException.class)
    public void testInvalidQuery2() throws InvalidRSqlSyntaxException {
        String invalid = "SELECT REGIONS;"
                + " ORDER BY HET DESC;"
                + " FROM US_counties;";
        Parser.validateQuery(invalid);
    }

    @Test(expected = InvalidRSqlSyntaxException.class)
    public void testInvalidQuery3() throws InvalidRSqlSyntaxException {
        String invalid = "SELECT OTHER, Regions.p" +
                "FROM NYC_census_tracts;" +
                "WHERE p=pMAX," +
                "5000 <= MAX ON population, OBJECTIVE COMPACT," +
                "OPTIMIZATION CONNECTED, HEURISTIC TABU;";
        Parser.validateQuery(invalid);
    }



}