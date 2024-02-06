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

    private static boolean validateWhereClause(String selectSubstring) {
        return selectSubstring.equalsIgnoreCase("SELECT REGIONS");
    }


    private static boolean noOrderByValidation(String[] substringArr) {
        return (validateSelect(substringArr[0]) && validateFromClause(substringArr[1]) && validateWhereClause(substringArr[2]));
    }

    private static boolean standardValidation(String[] substringArr) {
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
            System.out.println("statements " + substringsArr.length);
            //there will be exactly 3- 4 statements seperated by semicolons
            if (substringsArr.length > 4 | substringsArr.length < 3) {
                throw new InvalidRSqlSyntaxException("Wrong number of SQL statements! You might be missing a semicolon?");
            }
            System.out.println("Test intial thing: " + validateSelect(substringsArr[0]));

            if (substringsArr.length == 3) {
                validSyntax = noOrderByValidation(substringsArr);
            }
            validSyntax = standardValidation(substringsArr);

            for (int i = 0; i < substringsArr.length; i++) {
                String curSubStr = substringsArr[i];
                System.out.println(substringsArr[i]);

            }

            System.out.println("Query Valid " + validSyntax);


        }
}

// Overall query structure
/*
1. Select statement
2. Order by or From
3. From if order by is done
4. Where with a bunch of stuff that can be valid or not
 */

//Where Clause Specifics: This is the trciky one out of them al
/*
 will range from 2 - 6. Must be in that exact range
 1. WHERE
 2. Objective ...
 ~3. range
 ~4. optimization
 ~5. gapless
 ~6. heiristic
 */