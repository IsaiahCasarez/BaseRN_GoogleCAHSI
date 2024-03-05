import org.junit.Test;
import org.junit.Rule;
import static org.junit.Assert.*;
import org.junit.rules.ExpectedException;

public class ParserTest {

    //TODO: clean up this logic into seperate submodules, just get the working implementaiton first.
    /**
     * Testing for the submodules that validate smaller parts of the RSQL signature
     */
    @Test
    public void testSelectValidation() {
        assertTrue(Parser.validateSelect("SELECT REGIONS"));
        assertTrue(Parser.validateSelect("SELECT REGIONS, REGIONS.p"));
        assertTrue(Parser.validateSelect("SELECT REGIONS, REGIONS.HET"));
        assertFalse(Parser.validateSelect("SELECT OTHER"));
        assertFalse(Parser.validateSelect("SELECT OTHER, Regions.p"));
        assertFalse(Parser.validateSelect("SELECT OTHER, Regions.HET"));
    }

    @Test
    public void testOrderByValidCases() {
        assertTrue(Parser.validateOrderByClause("ORDER BY HET ASC"));
        assertTrue(Parser.validateOrderByClause("ORDER BY HET DESC"));
        assertTrue(Parser.validateOrderByClause("ORDER BY CARD ASC"));
        assertTrue(Parser.validateOrderByClause("ORDER BY CARD DESC"));
    }

    @Test
    public void testOrderByInvalidCases() {
        assertFalse(Parser.validateOrderByClause("ORDER BY HET")); // Missing ASC/DSC
        assertFalse(Parser.validateOrderByClause("ORDER BY CARD")); // Missing ASC/DSC
        assertFalse(Parser.validateOrderByClause("ORDER BY HET DSC")); // Should be DSC, not DESC
        assertFalse(Parser.validateOrderByClause("ORDER BY INVALID ASC")); // Invalid keyword
        assertFalse(Parser.validateOrderByClause("INVALID ORDER BY HET ASC"));
    }

    @Test
    public void testvalidateFromClause() {
        assertFalse(Parser.validateFromClause("FROM multiple words"));
        assertFalse(Parser.validateFromClause("not multiple words"));
        assertTrue(Parser.validateFromClause("FROM NYC_census_tracts"));
        assertTrue(Parser.validateFromClause("FROM US_counties"));
        //TODO: edge CASE: what if the path has a SPACE
        assertTrue(Parser.validateFromClause("FROM C:\\Users\\John\\Downloads"));
    }

    @Test
    public void testSubclauseTypes() {
        assertEquals(Parser.SubclauseType.OBJECTIVE, Parser.determineSubclauseType("OBJECTIVE HETEROGENEOUS ON attribute_name"));
        assertEquals(Parser.SubclauseType.BOUNDS_CLAUSE, Parser.determineSubclauseType("lower_bound (<) SUM (<) upper_bound ON attribute_name"));
        assertEquals(Parser.SubclauseType.OPTIMIZATION, Parser.determineSubclauseType("OPTIMIZATION RANDOM"));
        assertEquals(Parser.SubclauseType.GAPLESS, Parser.determineSubclauseType("GAPLESS"));
        assertEquals(Parser.SubclauseType.WHERE, Parser.determineSubclauseType(" WHERE p=14"));
        assertEquals(Parser.SubclauseType.HEURISTIC, Parser.determineSubclauseType("HEURISTIC MSA"));
        assertEquals(Parser.SubclauseType.WHERE, Parser.determineSubclauseType("WHERE p = (k | 𝑝𝑀𝐴𝑋 )"));
        assertEquals(Parser.SubclauseType.UNKNOWN, Parser.determineSubclauseType("UNKNOWN_SUBCLAUSE"));

        assertEquals(Parser.SubclauseType.BOUNDS_CLAUSE, Parser.determineSubclauseType("5000 <= MAX ON population"));
        assertEquals(Parser.SubclauseType.BOUNDS_CLAUSE, Parser.determineSubclauseType("11,000 < SUM < 20,000 ON population"));

        assertEquals(Parser.SubclauseType.HEURISTIC, Parser.determineSubclauseType("HEURISTIC TABU"));
        assertEquals(Parser.SubclauseType.OPTIMIZATION, Parser.determineSubclauseType("OPTIMIZATION CONNECTED"));
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
        assertTrue(Parser.handleHeuristic("HEURISTIC MSA"));
        assertTrue(Parser.handleHeuristic("HEURISTIC TABU"));
        assertFalse(Parser.handleHeuristic("HEURISTIC INVALID")); // Should fail with an invalid heuristic
        assertFalse(Parser.handleHeuristic("INVALID HEURISTIC")); // Should fail with an invalid format
        assertFalse(Parser.handleHeuristic("HEURISTIC")); // Should fail without a specified heuristic
    }

    @Test
    public void testHandleGapless() {
        assertTrue(Parser.handleGapless("GAPLESS"));
        assertFalse(Parser.handleGapless("GAPLESS ExtraText"));
        assertFalse(Parser.handleGapless("ExtraText GAPLESS"));
    }

    @Test
    public void testHandleOptimization() {
        assertTrue(Parser.handleOptimization("OPTIMIZATION RANDOM"));
        assertTrue(Parser.handleOptimization("OPTIMIZATION CONNECTED"));

        assertFalse(Parser.handleOptimization("OPTIMIZATION RANDOM ExtraText"));

        assertFalse(Parser.handleOptimization("optimization CONNECTED"));
        assertFalse(Parser.handleOptimization("OPTIMIZATION INVALID"));
    }

    @Test
    public void testHandleObjective() {
        assertTrue(Parser.handleObjective(" OBJECTIVE COMPACT"));
        assertTrue(Parser.handleObjective("OBJECTIVE HETEROGENEOUS ON attribute_name"));
        assertTrue(Parser.handleObjective("OBJECTIVE COMPACT ON another_attribute"));

        assertFalse(Parser.handleObjective("OBJECTIVE COMPACT ON another attribute"));
        assertFalse(Parser.handleObjective("OBJECTIVE INVALID ON attribute_name")); // Should fail due to invalid type
        assertFalse(Parser.handleObjective("INVALID HETEROGENEOUS ON attribute_name")); // Should fail due to invalid keyword
        assertFalse(Parser.handleObjective("OBJECTIVE HETEROGENEOUS INVALID")); // Should fail due to missing attribute
        assertFalse(Parser.handleObjective("INVALID")); // Should fail due to invalid structure
    }

    @Test
    public void testHandleBoundsClause() {
        assertTrue(Parser.handleBoundsClause("5 < SUM < 10 ON attribute_name"));
        assertTrue(Parser.handleBoundsClause("15 <= AVG <= 20 ON another_attribute"));
        assertTrue(Parser.handleBoundsClause("11,000 < SUM < 20,000 ON population"));
        assertTrue(Parser.handleBoundsClause("500 <= MIN ON population"));


        assertFalse(Parser.handleBoundsClause("notadigit <= AVG <= 20 ON another_attribute"));
        assertFalse(Parser.handleBoundsClause("Invalid bounds clause"));
        assertFalse(Parser.handleBoundsClause("10 < INVALID < 20 ON invalid_attribute"));
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
        fail("Expected InvalidRSqlSyntaxException to be thrown");
    }


    //TODO: once all individual components are implemented test an entire valid RSQL request

    //TODO: change the overall code structure and format to good practice :)
}