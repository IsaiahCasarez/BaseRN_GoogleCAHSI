package ParserFiles;
import java.util.regex.*;
import static ParserFiles.QueryEnums.*;
//TODO: README USER Manual. Author: John Pateros -> jpateros5410@sdsu.edu
/*
Overview: This is an RSQL syntax validator and Parser. The input is a string representing a query and the output is if the syntax is valid and the parsed query information:
TODO: HOW TO USE
In the main method below generate your specified query as a string and then call factory method: validateQueryPrintSpecifics(String Query)
- NOTE: The way this is implemented is that it will ALWAYS throw and exception if there is an issue with the syntax,
    -The Exceptions messages are quite specific so it should be easy to find the source of the code that is wrong, ctrl-f that message to find the code
    -Calling this method with a string that is invalid on purpose is ok and is expected, the exception will be caught error message printed and execution continues
General Implementation Notes:
    -Implementation heavily depends on parsing based on seperating clauses into subclauses and vaidating/parsing them with regex
TODO: Fix/improve the following assumptions
    -NOTE: Not using Semicolons to seperate statements like SELECT, ORDER BY, FROM will break this!
    -NOTE: Not using spaces between contents of a subclause like 11,000<SUM<20,000 instead of 11,000 < SUM < 20,000 will break this (hard coded parsing)
    -NOTE: There is an implicit issue since there can be any number of bounds sub clauses
        Like i can say 11,000 < SUM < 20,000 ON population, 500 <= MIN ON population, in one query
        -right now this is being handled and you can use as many as you want but i would like to dtermine the exact number of these clauses in the future for extra validation
    -NOTE: I am sure there are more bugs/edge cases I am not addressing feel free to give me an input and expected output and i will try to fix
    -NOTE: If you want to validate a specific method or subsection validaiton part see the junit unit tests (ParserTest.java) as this is how i built through testing as i went
More in Depth Implementation Details for those that are interested:
Everything starts from validateQuery which returns a boolean true or will throw an exception if not valid
    -this first ensures there are between 3-4 statements seperated by semicolon since ORDERBY is optional
    -next it will call determineMainClauseType for each clause to using regex classify it as
            -SELECT, ORDERBY, FROM or WHERE with an enum (if it cant will throw an unrecognized error, will throw an error as soon as ther is problem)
            -Based on the mainclause type i use a switch case to call the right handler method
    -Handler Methods: one per each main clause so 4 of them. They will generally use regex to validate the syntax if they can not validate the syntax
        they will throw an error. Otherwise if its valid it will forward the the string to the QuerySpecifics class to parse the fields
            -Parsing the fields right now depends mostly on spaces and knowing indexes could be improved in the future
    -HandleWhereClause: this validation is tricky since it has a lot of potential subcomponents
        -This will break the where clause into smaller subclauses by parsing by comma before it will then validate/parse each subclass in a similar fashion
Note: I know this is quite messy and not the best code practices was planning on getting that part straightened out later
 */
public class Parser {
    QuerySpecifics queryInformation;
    public Parser() {
        this.queryInformation = new QuerySpecifics();
    }
    public static void main(String[] args) throws InvalidRSqlSyntaxException {

        String validQuery = "SELECT REGIONS, REGIONS.p;"
                + " ORDER BY HET DESC;"
                + " FROM US_counties;"
                + " WHERE p=14, GAPLESS,"
                + " 11,000 < SUM < 20,000 ON population, 500 <= MIN"
                + " ON population,"
                + "OPTIMIZATION RANDOM,"
                + "HEURISTIC MSA,"
                + " OBJECTIVE HETEROGENEOUS ON average_house_price;";

        String validQuery2 = " SELECT REGIONS, REGIONS.p;"
                + "FROM NYC_census_tracts;"
                + "WHERE p=pmax ,"
                + "5000 <= MAX ON population, OBJECTIVE COMPACT,"
                + "OPTIMIZATION CONNECTED, HEURISTIC TABU;";

        String validQuery3 = " SELECT REGIONS, REGIONS.p;"
                + " FROM NYC_census_tracts; "
                + " WHERE p=pmax , "
                + " 5000 <= MAX ON population, OBJECTIVE COMPACT, "
                + " OPTIMIZATION CONNECTED, HEURISTIC TABU; ";
        //missing the WHERE
        String invalid = "SELECT REGIONS;"
                + " ORDER BY HET DESC;"
                + " FROM US_counties;";
        //ORDERBY bad syntax
        String invalidQuery2 = " SELECT REGIONS, REGIONS.p;"
                + "ORDER BY HET;"
                + " FROM NYC_census_tracts; "
                + " WHERE p=pmax , "
                + " 5000 <= MAX ON population, OBJECTIVE COMPACT, "
                + " OPTIMIZATION CONNECTED, HEURISTIC TABU; ";
        //no select clause
        String invalidQuery3 =
                "ORDER BY HET DESC;"
                + " FROM NYC_census_tracts; "
                + " WHERE p=pmax , "
                + " 5000 <= MAX ON population, OBJECTIVE COMPACT, "
                + " OPTIMIZATION CONNECTED, HEURISTIC TABU; ";

        validateQueryPrintSpecifics(validQuery);
        validateQueryPrintSpecifics(validQuery2);
        validateQueryPrintSpecifics(validQuery3);

        //These will throw exceptions that are then handled and error messages are printed
        //Note: The unit tests have a lot mor testing for individual clauses this is better to test full query compatiability in main
        validateQueryPrintSpecifics(invalid);
        validateQueryPrintSpecifics(invalidQuery2);
        validateQueryPrintSpecifics(invalidQuery3);
    }

    public static void validateQueryPrintSpecifics(String query) {
        try {
            Parser parseQuery = new Parser();
            System.out.println("Query is valid: " + parseQuery.validateQuery(query) + " with contents of: ");
            System.out.println(parseQuery.getQueryInfo().toString());
        }
        catch (InvalidRSqlSyntaxException e) {
            System.out.println(e);
        }
    }
    public  MainClauseType determineMainClauseType(String clause) throws InvalidRSqlSyntaxException {
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
            throw new InvalidRSqlSyntaxException("Could not determine the Main clause Type of: " + clause);
            //return MainClauseType.UNKNOWN;
        }
    }

    public  boolean validateSelect(String selectSubstring) throws InvalidRSqlSyntaxException {
        String regex = "^SELECT REGIONS(?:, REGIONS\\.P|, REGIONS\\.HET)?";

        if (!selectSubstring.toUpperCase().trim().matches(regex)) {
            throw new InvalidRSqlSyntaxException("Invalid SELECT syntax: " + selectSubstring);
        }
        this.queryInformation.parseREGIONSOptional(selectSubstring.toUpperCase());

        return true;
    }

    public  boolean validateOrderByClause(String OrderBySubstring) throws InvalidRSqlSyntaxException {
        String regex = "^ORDER BY (HET|CARD) (ASC|DESC)$";

        if (!OrderBySubstring.trim().matches(regex)) {
            throw new InvalidRSqlSyntaxException("Invalid ORDER BY syntax: " + OrderBySubstring);
        }
        this.queryInformation.parseORDERBY(OrderBySubstring);

        return true;
    }

    public  boolean validateFromClause(String FromClauseSubStr) throws InvalidRSqlSyntaxException {
        String regex = "^FROM\\s.*$";

        if (FromClauseSubStr.trim().split(" ").length != 2 || !FromClauseSubStr.trim().matches(regex)) {
            throw new InvalidRSqlSyntaxException("Invalid FROM syntax: " + FromClauseSubStr);
        }
        this.queryInformation.parseFromField(FromClauseSubStr);

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

        if (subclausesArr.length > 7 || subclausesArr.length < 2) {
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
        queryInformation.parseObjectiveInfo(subclause);
        return true;
    }

    public  boolean handleBoundsClause(String subclause) throws InvalidRSqlSyntaxException{
        subclause = removeCommasFromNums(subclause);

        //this case is really hard and needs some thought
        //TODO: lower_bound (< | <=) (SUM | MIN | MAX | COUNT | AVG) (< | <=) (upper_bound) ON attribute_name]
        String regex2 = "\\d+\\s*(<|<=)\\s*(SUM|MIN|MAX|COUNT|AVG)\\s*ON\\s+[a-zA-Z_]+";
        String regex = "\\d+\\s*(<|<=)\\s*(SUM|MIN|MAX|COUNT|AVG)\\s*(<|<=)\\s*\\d+\\s*ON\\s+[a-zA-Z_]+";

        if (!( subclause.matches(regex2) || subclause.matches(regex))) {
            throw new InvalidRSqlSyntaxException("Invalid OPTIMIZATION syntax: " + subclause);
        }
        if ( subclause.matches(regex2)) {
            this.queryInformation.parseBoundsNoUpper(subclause);
        }
        else if ( subclause.matches(regex)) {
            this.queryInformation.parseBoundsWithUpper(subclause);
        }
        return true;
    }

    public  boolean handleOptimization(String subclause) throws InvalidRSqlSyntaxException {
        String regex = "^OPTIMIZATION\\s+(RANDOM|CONNECTED)$";
        if (!subclause.matches(regex)) {
            throw new InvalidRSqlSyntaxException("Invalid OPTIMIZATION syntax: " + subclause);
        }
        queryInformation.parseOPTIMIZATION(subclause);
        return true;
    }

    public  boolean handleGapless(String subclause) throws InvalidRSqlSyntaxException {
        String regex = "^GAPLESS$";
        if (!subclause.trim().matches(regex)) {
            throw new InvalidRSqlSyntaxException("Invalid GAPLESS syntax: " + subclause);
        }
        //we have a gapless situation
        queryInformation.setGapless(true);
        return true;
    }

    public  boolean handleHeuristic(String subclause) throws InvalidRSqlSyntaxException {
        String regex = "^HEURISTIC\\s+(MSA|TABU)$";
        if (!subclause.matches(regex)) {
            throw new InvalidRSqlSyntaxException("Invalid HEURISTIC syntax: " + subclause);
        }
        queryInformation.parseHEURISTIC(subclause);
        return true;
    }


    public  boolean handleWhere(String subclause) throws InvalidRSqlSyntaxException {
        String regex = "^\\s*WHERE\\s+P\\s*=(\\s*(K|PMAX|\\d+)\\s*)$";
        boolean isValid = subclause.trim().toUpperCase().matches(regex);

        if (!isValid) {
            throw new InvalidRSqlSyntaxException("Invalid syntax in WHERE clause: " + subclause.trim().toUpperCase());
        }
        this.queryInformation.parseWhereInformation(subclause);

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

        String[] substringsArr = query.trim().toUpperCase().split(";");

        // Ensure there are exactly 3-4 main statements separated by semicolons
        if (substringsArr.length > 4 || substringsArr.length < 3) {
            throw new InvalidRSqlSyntaxException("Wrong number of SQL statements! You might be missing a semicolon?");
        }

        for (String clause : substringsArr) {
            MainClauseType mainClauseType = determineMainClauseType(clause.trim().toUpperCase());

            switch (mainClauseType) {
                case SELECT:
                    hasSelect = true;
                    validateSelect(clause);
                    break;
                case FROM:
                    hasFrom = true;
                    validateFromClause(clause);
                    break;
                case WHERE:
                    hasWhere = true;
                    validateWhereClause(clause);
                    break;
                case ORDER_BY:
                    validateOrderByClause(clause);
                    break;
                case UNKNOWN:
                    // Handle cases where the main clause type is unknown or unsupported
                    System.out.println("Unknown main clause type: " + clause);
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
        this.queryInformation.checkRequiredFields();

        return true;
    }

    public QuerySpecifics getQueryInfo() {
        return this.queryInformation;
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