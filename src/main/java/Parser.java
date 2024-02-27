import java.util.Arrays;
import java.util.regex.*;

//Template for the Query Syntax
/**
SELECT REGIONS, [REGIONS.p, REGIONS.HET];
[ORDER BY (HET | CARD) [(ASC | DESC)];]
FROM (dataset_name | path);
WHERE p = (k | 𝑝𝑀𝐴𝑋 ),
OBJECTIVE (HETEROGENEOUS | COMPACT) ON attribute_name,
[lower_bound (< | <=) (SUM | MIN | MAX | COUNT | AVG)
(< | <=) upper_bound ON attribute_name],
[OPTIMIZATION (RANDOM | CONNECTED)],
[GAPLESS],
[HEURISTIC (MSA | TABU)];
**/

public class Parser {
    public enum SubclauseType {
        OBJECTIVE,
        BOUNDS_CLAUSE,
        OPTIMIZATION,
        GAPLESS,
        HEURISTIC,
        WHERE,
        UNKNOWN
    }
    //space and CASE sensitive right now, could be improved in the future
    public static boolean validateSelect(String selectSubstring) {
        String regex = "^SELECT REGIONS(?:, REGIONS\\.p|, REGIONS\\.HET)?";
        return selectSubstring.matches(regex);
    }

    public static boolean validateOrderByClause(String OrderBySubstring) {
        String regex = "^ORDER BY (HET|CARD) (ASC|DSC)$";
        return OrderBySubstring.matches(regex);
    }

    public static boolean validateFromClause(String FromClauseSubStr) {
        String regex = "^FROM\\s.*$";
        if (FromClauseSubStr.trim().split(" ").length == 2) {
            return FromClauseSubStr.matches(regex);
        }
        return false;

    }

    private static void handleSubclause(SubclauseType type, String subclause) {
        switch (type) {
            case OBJECTIVE:
                handleObjective(subclause);
                break;
            case BOUNDS_CLAUSE:
                handleBoundsClause(subclause);
                break;
            case OPTIMIZATION:
                handleOptimization(subclause);
                break;
            case GAPLESS:
                handleGapless(subclause);
                break;
            case HEURISTIC:
                handleHeuristic(subclause);
                break;
            case WHERE:
                handleWhere(subclause);
                break;
            case UNKNOWN:
                // Handle cases where the type is unknown or unsupported
                break;
        }
    }

    // Define empty methods for each case
    public static boolean handleObjective(String subclause) {
        String regex = "OBJECTIVE (HETEROGENEOUS|COMPACT) ON [a-zA-Z_][a-zA-Z0-9_]*$";
        return subclause.matches(regex);
    }

    public static boolean handleBoundsClause(String subclause) {
        subclause = removeCommasFromNums(subclause);

        //this case is really hard and needs some thought
       //TODO: lower_bound (< | <=) (SUM | MIN | MAX | COUNT | AVG) (< | <=) (upper_bound) ON attribute_name]
        //"5 < SUM < 10 ON attribute_name"
        String regex = "\\d+\\s*(<|<=)\\s*(SUM|MIN|MAX|COUNT|AVG)\\s*(<|<=)\\s*\\d+\\s*ON\\s+[a-zA-Z_]+";

        return subclause.matches(regex);
    }




    public static boolean handleOptimization(String subclause) {
        String regex = "^OPTIMIZATION\\s+(RANDOM|CONNECTED)$";
        return subclause.matches(regex);
    }

    public static boolean handleGapless(String subclause) {
        String regex = "^GAPLESS$";
        return subclause.matches(regex);
    }

    public static boolean handleHeuristic(String subclause) {
        String regex = "^HEURISTIC\\s+(MSA|TABU)$";
        return subclause.matches(regex);
    }

    public static boolean handleWhere(String subclause) {
        String regex = "^\\s*WHERE\\s+P\\s*=\\s+(K|PMAX)\\s*$";
        return subclause.toUpperCase().matches(regex);
    }
    private static boolean validateWhereClause(String whereSubstring) throws InvalidRSqlSyntaxException {
        whereSubstring = removeCommasFromNums(whereSubstring);

        String[] subclausesArr = whereSubstring.split(",");
        for (String item : subclausesArr) {
            SubclauseType type = determineSubclauseType(item);
            handleSubclause(type, item);
        }

        if (subclausesArr.length > 6 | subclausesArr.length < 2) {
            throw new InvalidRSqlSyntaxException("WHERE Clause is specified with wrong number of args. Perhaps you forgot a comma!");
        }
        return false;
    }
//So that 10,000 -> 10000, way easier to then parse the subclauses by strings
    public static String removeCommasFromNums(String strToClean) {
        String regex = "(?<=[\\d])(,)(?=[\\d])";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(strToClean);
        return m.replaceAll("");
    }

    public static SubclauseType determineSubclauseType(String subclause) {
        if (subclause.matches("^OBJECTIVE.*")) {
            return SubclauseType.OBJECTIVE;
        } else if (subclause.matches(".*(<|<=|>|>=).*")) {
            return SubclauseType.BOUNDS_CLAUSE;
        } else if (subclause.matches("^OPTIMIZATION.*")) {
            return SubclauseType.OPTIMIZATION;
        } else if (subclause.matches("^GAPLESS.*")) {
            return SubclauseType.GAPLESS;
        } else if (subclause.matches("^HEURISTIC.*")) {
            return SubclauseType.HEURISTIC;
        } else if (subclause.matches("^WHERE.*")) {
            return SubclauseType.WHERE;
        } else {
            // Handle cases where the type is unknown or unsupported
            return SubclauseType.UNKNOWN;
        }
    }


    private static boolean noOrderByValidation(String[] substringArr) throws InvalidRSqlSyntaxException {
        return (validateSelect(substringArr[0]) && validateFromClause(substringArr[1]) && validateWhereClause(substringArr[2]));
    }

    private static boolean standardValidation(String[] substringArr) throws InvalidRSqlSyntaxException {
        validateWhereClause(substringArr[3]);
        return (validateSelect(substringArr[0]) && validateOrderByClause(substringArr[1]) && validateFromClause(substringArr[2]) && validateWhereClause(substringArr[3]));
    }


    public static void main (String[]args) throws InvalidRSqlSyntaxException {

        boolean validSyntax = false;

            String validQuery = "SELECT REGIONS;"
                    + " ORDER BY HET DESC;"
                    + " FROM US_counties;"
                    + " WHERE p=14, GAPLESS,"
                    + " 11,000 < SUM < 20,000 ON population, 500 <= MIN"
                    + " ON population,"
                    + " OBJECTIVE HETEROGENEOUS ON average_house_price;";

            String invalidQuery = "INVALID QUERY";

            String[] substringsArr = validQuery.split(";");

            //there will be exactly 3- 4 statements seperated by semicolons
            if (substringsArr.length > 4 | substringsArr.length < 3) {
                throw new InvalidRSqlSyntaxException("Wrong number of SQL statements! You might be missing a semicolon?");
            }

            if (substringsArr.length == 3) {
                validSyntax = noOrderByValidation(substringsArr);
            }
            validSyntax = standardValidation(substringsArr);

            System.out.println("Query Valid " + validSyntax);


        }
}

/*

Overall logic:
Dvide into the Clauses:
SELECT
ORDER BY

 */

// Overall query structure
/* 3 - 4 total sub statements
1. Select statement
~2. Order by
3. From if order by is done
4. Where with a bunch of stuff that can be valid or not
 */

//Where Clause Specifics: This is the tricky one out of them al
/*
 will range from 2 - 6. Must be in that exact range
 1. WHERE
 2. Objective ...
 ~3. range
 ~4. optimization
 ~5. gapless
 ~6. heiristic
 */