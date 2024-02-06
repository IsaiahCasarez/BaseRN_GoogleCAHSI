import org.junit.Test;

import static org.junit.Assert.*;


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
        assertTrue(Parser.validateOrderByClause("ORDER BY HET DSC"));
        assertTrue(Parser.validateOrderByClause("ORDER BY CARD ASC"));
        assertTrue(Parser.validateOrderByClause("ORDER BY CARD DSC"));
    }

    @Test
    public void testOrderByInvalidCases() {
        assertFalse(Parser.validateOrderByClause("ORDER BY HET")); // Missing ASC/DSC
        assertFalse(Parser.validateOrderByClause("ORDER BY CARD")); // Missing ASC/DSC
        assertFalse(Parser.validateOrderByClause("ORDER BY HET DESC")); // Should be DSC, not DESC
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
        assertEquals(Parser.SubclauseType.HEURISTIC, Parser.determineSubclauseType("HEURISTIC MSA"));
        assertEquals(Parser.SubclauseType.WHERE, Parser.determineSubclauseType("WHERE p = (k | 𝑝𝑀𝐴𝑋 )"));
        assertEquals(Parser.SubclauseType.UNKNOWN, Parser.determineSubclauseType("UNKNOWN_SUBCLAUSE"));
    }

    //TODO: once all individual components are implemented test an entire valid RSQL request
}