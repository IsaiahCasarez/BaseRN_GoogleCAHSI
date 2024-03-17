package ParserFiles;

import java.util.regex.*;
import static ParserFiles.QueryEnums.*;
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
    QuerySpecifics queryInformation;

    public Parser() {
        this.queryInformation = new QuerySpecifics();
    }
    public  MainClauseType determineMainClauseType(String clause) {
        String trimmedClause = clause.trim().toUpperCase();

        if (trimmedClause.startsWith("SELECT")) {
            return MainClauseType.SELECT;
        } else if (trimmedClause.startsWith("ORDER BY")) {
            return MainClauseType.ORDER_BY;
        } else if (trimmedClause.startsWith("FROM")) {
            return MainClauseType.FROM;
        } else if (trimmedClause.startsWith("WHERE")) {
            return MainClauseType.WHERE;
        } else {
            return MainClauseType.UNKNOWN;
        }
    }

    public  boolean validateSelect(String selectSubstring) throws InvalidRSqlSyntaxException {
        String regex = "^SELECT REGIONS(?:, REGIONS\\.p|, REGIONS\\.HET)?";

        if (!selectSubstring.matches(regex)) {
            throw new InvalidRSqlSyntaxException("Invalid SELECT syntax: " + selectSubstring);
        }

        return true;
    }

    public  boolean validateOrderByClause(String OrderBySubstring) throws InvalidRSqlSyntaxException {
        String regex = "^ORDER BY (HET|CARD) (ASC|DESC)$";

        if (!OrderBySubstring.trim().matches(regex)) {
            throw new InvalidRSqlSyntaxException("Invalid ORDER BY syntax: " + OrderBySubstring);
        }

        return true;
    }

    public  boolean validateFromClause(String FromClauseSubStr) throws InvalidRSqlSyntaxException {
        String regex = "^FROM\\s.*$";

        if (FromClauseSubStr.trim().split(" ").length != 2 || !FromClauseSubStr.trim().matches(regex)) {
            throw new InvalidRSqlSyntaxException("Invalid FROM syntax: " + FromClauseSubStr);
        }

        return true;
    }


    private  boolean handleSubclause(SubclauseType type, String subclause) throws InvalidRSqlSyntaxException{
        boolean subclauseValidationResult = false;
        subclause = subclause.trim();
        switch (type) {
            case OBJECTIVE:
                subclauseValidationResult = handleObjective(subclause);
                break;
            case BOUNDS_CLAUSE:
                subclauseValidationResult = handleBoundsClause(subclause);
                break;
            case OPTIMIZATION:
                subclauseValidationResult = handleOptimization(subclause);
                break;
            case GAPLESS:
                subclauseValidationResult = handleGapless(subclause);
                break;
            case HEURISTIC:
                subclauseValidationResult = handleHeuristic(subclause);
                break;
            case WHERE:
                subclauseValidationResult = handleWhere(subclause);
                break;
            case UNKNOWN:
                // Handle cases where the type is unknown or unsupported
                System.out.println("unknown classify: \n" + subclause);
                subclauseValidationResult = false;
                break;
        }

        return subclauseValidationResult;
    }

    private  boolean validateWhereClause(String whereSubstring) throws InvalidRSqlSyntaxException {
        whereSubstring = removeCommasFromNums(whereSubstring);
        String[] subclausesArr = whereSubstring.split(",");

        if (subclausesArr.length > 6 || subclausesArr.length < 2) {
            throw new InvalidRSqlSyntaxException("WHERE Clause is specified with the wrong number of args. Perhaps you forgot a comma!");
        }

        boolean validWhere = true; // Assume WHERE clause is valid by default

        for (String item : subclausesArr) {
            SubclauseType type = determineSubclauseType(item);
            boolean subclauseValidationResult = handleSubclause(type, item);

            // Aggregate the validation results for each subclause
            validWhere &= subclauseValidationResult;

            if (!subclauseValidationResult) {
                System.out.println("Validation failed for WHERE subclause: \n" + item);
            }
        }

        return validWhere;
    }

    public  boolean handleObjective(String subclause) throws InvalidRSqlSyntaxException {
        String regex = "^\\s*OBJECTIVE\\s+(HETEROGENEOUS|COMPACT)(\\s+ON\\s+[a-zA-Z_][a-zA-Z0-9_]*)?$";
        if (!subclause.matches(regex)) {
            throw new InvalidRSqlSyntaxException("Invalid OBJECTIVE syntax: " + subclause);
        }
        return true;
    }

    public  boolean handleBoundsClause(String subclause) throws InvalidRSqlSyntaxException{
        subclause = removeCommasFromNums(subclause);

        //this case is really hard and needs some thought
        //TODO: lower_bound (< | <=) (SUM | MIN | MAX | COUNT | AVG) (< | <=) (upper_bound) ON attribute_name]
        String regex2 = "\\d+\\s*(<|<=)\\s*(SUM|MIN|MAX|COUNT|AVG)\\s*ON\\s+[a-zA-Z_]+";
        String regex = "\\d+\\s*(<|<=)\\s*(SUM|MIN|MAX|COUNT|AVG)\\s*(<|<=)\\s*\\d+\\s*ON\\s+[a-zA-Z_]+";


        //500 <= MIN ON population
        if (subclause.matches(regex2) || subclause.matches(regex)) {
            return true;
        }
        throw new InvalidRSqlSyntaxException("Invalid OPTIMIZATION syntax: " + subclause);
    }

    public  boolean handleOptimization(String subclause) throws InvalidRSqlSyntaxException {
        String regex = "^OPTIMIZATION\\s+(RANDOM|CONNECTED)$";
        if (!subclause.matches(regex)) {
            throw new InvalidRSqlSyntaxException("Invalid OPTIMIZATION syntax: " + subclause);
        }
        return true;
    }

    public  boolean handleGapless(String subclause) throws InvalidRSqlSyntaxException {
        String regex = "^GAPLESS$";
        if (!subclause.trim().matches(regex)) {
            throw new InvalidRSqlSyntaxException("Invalid GAPLESS syntax: " + subclause);
        }
        return true;
    }

    public  boolean handleHeuristic(String subclause) throws InvalidRSqlSyntaxException {
        String regex = "^HEURISTIC\\s+(MSA|TABU)$";
        if (!subclause.matches(regex)) {
            throw new InvalidRSqlSyntaxException("Invalid HEURISTIC syntax: " + subclause);
        }
        return true;
    }


    public  boolean handleWhere(String subclause) throws InvalidRSqlSyntaxException {
        String regex = "^\\s*WHERE\\s+P\\s*=(\\s*(K|PMAX|\\d+)\\s*)$";
        boolean isValid = subclause.trim().toUpperCase().matches(regex);

        if (!isValid) {
            throw new InvalidRSqlSyntaxException("Invalid syntax in WHERE clause: " + subclause.trim().toUpperCase());
        }

        return isValid;
    }


    //So that 10,000 -> 10000, way easier to then parse the subclauses by strings
    public  String removeCommasFromNums(String strToClean) {
        String regex = "(?<=[\\d])(,)(?=[\\d])";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(strToClean);
        return m.replaceAll("");
    }

    public  SubclauseType determineSubclauseType(String subclause) {
        subclause = subclause.trim();
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

    public boolean validateQuery(String query) throws InvalidRSqlSyntaxException {
       this.queryInformation = new QuerySpecifics();

        boolean hasSelect = false;
        boolean hasFrom = false;
        boolean hasWhere = false;

        String[] substringsArr = query.split(";");

        // Ensure there are exactly 3-4 statements separated by semicolons
        if (substringsArr.length > 4 || substringsArr.length < 3) {
            throw new InvalidRSqlSyntaxException("Wrong number of SQL statements! You might be missing a semicolon?");
        }

        for (int i = 0; i < substringsArr.length; i++) {
            MainClauseType mainClauseType = determineMainClauseType(substringsArr[i].trim());

            switch (mainClauseType) {
                case SELECT:
                    hasSelect = true;
                    break;
                case FROM:
                    hasFrom = true;
                    break;
                case WHERE:
                    hasWhere = true;
                    break;
                case UNKNOWN:
                    // Handle cases where the main clause type is unknown or unsupported
                    System.out.println("Unknown main clause type: " + substringsArr[i]);
                    break;
            }
        }

        // Check for the presence of required clauses and throw an exception if any is missing
        if (!hasSelect) {
            throw new InvalidRSqlSyntaxException("Missing SELECT clause!");
        }

        if (!hasFrom) {
            throw new InvalidRSqlSyntaxException("Missing FROM clause!");
        }

        if (!hasWhere) {
            throw new InvalidRSqlSyntaxException("Missing WHERE clause!");
        }

        return true;
    }


    public static void main(String[] args) throws InvalidRSqlSyntaxException {

        Parser parser = new Parser();

        String validQuery = "SELECT REGIONS;"
                + " ORDER BY HET DESC;"
                + " FROM US_counties;"
                + " WHERE p=14, GAPLESS,"
                + " 11,000 < SUM < 20,000 ON population, 500 <= MIN"
                + " ON population,"
                + " OBJECTIVE HETEROGENEOUS ON average_house_price;";

        String invalid = "SELECT REGIONS;"
                + " ORDER BY HET DESC;"
                + " FROM US_counties;";

       System.out.println(parser.validateQuery(validQuery));
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