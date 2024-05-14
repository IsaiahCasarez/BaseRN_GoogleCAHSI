package ParserFiles;
import lombok.Data;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static ParserFiles.QueryEnums.*;


@Data
public class QuerySpecifics {


    // Select Clause - Mandatory
    private Regions REGIONS; //[REGIONS.p, REGIONS.HET]
    //OrderBy - Optional
    private OrderByType ORDERTYPE; // (HET | CARD)
    //ORDERDirection - optional
    private OrderDirection ORDERDIRECTION; //(ASC | DESC)
    //FROM Mandatory
    private String From; //dataset name or path to the file
    //WHERE
    private pType pValueEnum; //mandatory
    private double pValueDouble; //mandatory
    private Objective ObjectiveType; //optional (HETEROGENEOUS | COMPACT)
    private String ObjectiveAttribute; // mandatory: OBJECTIVE (HETEROGENEOUS | COMPACT) ON attribute_name

    private ArrayList<BoundsSubclause> boundsSubclauses; //optional, but can be multiple
    private LogicalOperator boundsClauselogicalOperator; //OPTIONAL will show AND or OR if they are speciifed on the clauses

    private Optimization OptimizationType; //optional
    boolean Gapless; //optional 1 or 0, 0 default
    private Heuristic HeuristicType; //optional

    // Method to check if all the required fields are not null
    public boolean checkRequiredFields() throws InvalidRSqlSyntaxException {
        if (From == null) {
            throw new InvalidRSqlSyntaxException("Missing required information of From");
        }
        if (pValueEnum == null) {
            throw new InvalidRSqlSyntaxException("Missing required information of pValueEnum");
        }
        if (pValueDouble == 0.0) {
            throw new InvalidRSqlSyntaxException("Missing required information of pValueDouble");
        }
        if (ObjectiveType == null) {
            throw new InvalidRSqlSyntaxException("Missing required information of ObjectiveAttribute");
        }
        return true;
    }
    // Constructor with required fields and default values
//    public QuerySpecifics(Regions REGIONS, String From, Objective ObjectiveType) {
//        this(REGIONS, null, null, From, null, 0.0,
//                ObjectiveType, null, 0.0, null, 0.0, null,
//                null, false, null);
//    }

    public QuerySpecifics() {
        this.REGIONS = null;
        this.ORDERTYPE = null;
        this.ORDERDIRECTION = null;
        this.From = null;
        this.pValueEnum = null;
        this.pValueDouble = 0.0; // Assuming a default value for double
        this.ObjectiveType = null;
        this.ObjectiveAttribute = null;
        this.boundsSubclauses = null;
        this.OptimizationType = null;
        this.Gapless = false; // Assuming a default value for boolean
        this.HeuristicType = null;
        this.boundsClauselogicalOperator = null;
    }


    // Full constructor
    public QuerySpecifics(Regions REGIONS, OrderByType ORDERTYPE, OrderDirection ORDERDIRECTION,
                          String From, pType pValueEnum, double pValueDouble,
                          Objective ObjectiveType, String ObjectiveAttribute,
                          ArrayList<BoundsSubclause> boundsSubclauses,
                          Optimization OptimizationType, boolean Gapless, Heuristic HeuristicType) {
        // Assign values as before
        this.REGIONS = REGIONS;
        this.ORDERTYPE = ORDERTYPE;
        this.ORDERDIRECTION = ORDERDIRECTION;
        this.From = From;
        this.pValueEnum = pValueEnum;
        this.pValueDouble = pValueDouble;
        this.ObjectiveType = ObjectiveType;
        this.ObjectiveAttribute = ObjectiveAttribute;
        this.boundsSubclauses = boundsSubclauses;
        this.OptimizationType = OptimizationType;
        this.Gapless = Gapless;
        this.HeuristicType = HeuristicType;

        // Validate required fields
        validateRequiredFields();
    }

    // Helper method to ensure all required fields are here
    private void validateRequiredFields() {
        if (REGIONS == null || From == null || ObjectiveType == null) {
            throw new IllegalArgumentException("Required fields are not provided.");
        }
    }

    // toString method
    @Override
    public String toString() {
        return "ParserFiles.QuerySpecifics{" + "\n" +
                "REGIONS=" + REGIONS + "\n" +
                "ORDERTYPE=" + ORDERTYPE + "\n" +
                "ORDERDIRECTION=" + ORDERDIRECTION + "\n" +
                "From='" + From + '\'' + "\n" +
                "pValueEnum=" + pValueEnum + "\n" +
                "pValueDouble=" + pValueDouble + "\n" +
                printingBounds(boundsSubclauses) + "\n" +
                "ObjectiveType=" + ObjectiveType + "\n" +
                "ObjectiveAttribute='" + ObjectiveAttribute + '\'' + "\n" +
                "OptimizationType=" + OptimizationType + "\n" +
                "Gapless=" + Gapless + "\n" +
                "HeuristicType=" + HeuristicType + "\n" +
                '}';
    }

    public String printingBounds(ArrayList<BoundsSubclause> boundsClauses) {
        StringBuilder s = new StringBuilder();
        if (boundsClauses != null) {
            int i = 1;
            s.append("Bounds Clause Logical Operator: ").append(boundsClauselogicalOperator).append("\n");
            for (BoundsSubclause clause : boundsClauses) {
                s.append("For the bound clause number: ").append(i).append(" attributes: ").append("\n");

                s.append("Agg Function: ").append(clause.getAggFunction()).append("\n");
                s.append("UpperBound: ").append(clause.getUpperBound()).append("\n");
                s.append("UpperBound Attribute: ").append(clause.getUpperBoundAttribute()).append("\n");
                s.append("LowerBound: ").append(clause.getLowerBound()).append("\n");
                s.append("comparisonOperator1: ").append(clause.getComparisonOperator1()).append("\n");
                s.append("comparisonOperator2: ").append(clause.getComparisonOperator2()).append("\n");
                i++;
            }
        }
        else {
            s.append("There are no bounds clauses");
        }
        return s.toString();
    }


    public static String parseSecondWord(String input) {
        String[] parts = input.trim().split("\\s+");

        if (parts.length == 2) {
            return parts[1];
        } else {
            return "";
        }
    }

    public void parseFromField(String s) throws InvalidRSqlSyntaxException {

        String fromField = parseSecondWord(s);
        if (!fromField.isEmpty()) {
            this.setFrom(fromField);
        } else {
            throw new InvalidRSqlSyntaxException("Issue PArsing the from clause even after structure was validated");
        }

    }

    public void determineREGIONSType(String s) throws InvalidRSqlSyntaxException {
        String pPattern = "REGIONS\\.P";
        String hetPattern = "REGIONS\\.HET";
        if (s.matches(pPattern)) {
            this.setREGIONS(Regions.P);
        } else if (s.matches(hetPattern)) {
            this.setREGIONS(Regions.HET);
        } else {
            // If the parsed string doesn't match any of the patterns, throw an exception
            throw new InvalidRSqlSyntaxException("Invalid REGIONS syntax REGIONScan only be REGIONS.P or REGIONS.HET you put: " + s);
        }
    }

    public void parseREGIONSOptional(String s) throws InvalidRSqlSyntaxException {
        String[] parts = s.trim().split("\\s+");
        //optional argument not given
        if (parts.length == 2) {
            this.setREGIONS(null);
        } else if (parts.length == 3) {
            determineREGIONSType(parts[2]);
        } else {
            throw new InvalidRSqlSyntaxException("Invalid SELECT clause too many words (>2): " + s);
        }
    }

    public void parseORDERBYMore(String orderTypeString) throws InvalidRSqlSyntaxException {
        switch (orderTypeString) {
            case "HET" -> this.setORDERTYPE(ORDERTYPE.HET);
            case "CARD" -> this.setORDERTYPE(ORDERTYPE.CARD);
            case "ASC" -> this.setORDERDIRECTION(OrderDirection.ASC);
            case "DESC" -> this.setORDERDIRECTION(OrderDirection.DESC);
            default ->
                    throw new InvalidRSqlSyntaxException("Select ORDER Type or Driection did not match one of four:  (HET | CARD) [(ASC | DESC): " + orderTypeString);
        }
        ;
    }

    public void parseORDERBY(String s) throws InvalidRSqlSyntaxException {
        String[] words = s.trim().split("\\s+");
        int numWords = words.length;

        switch (numWords) {
            //no optional arguments given
            case 2 -> {
                this.setORDERDIRECTION(null);
                this.setORDERTYPE(null);
            }
            //last word we parse
            case 3 -> parseORDERBYMore(words[2]);

            //last two words we parse
            case 4 -> {
                parseORDERBYMore(words[2]);
                parseORDERBYMore(words[3]);
            }
            default -> throw new InvalidRSqlSyntaxException("Invalid RSQL syntax: " + s);
        }
    }

    public void parseHEURISTIC(String s) throws InvalidRSqlSyntaxException {
        String[] words = s.trim().split("\\s+");

        if (words.length != 2) {
            throw new InvalidRSqlSyntaxException("Invalid HEURISTIC syntax: " + s);
        }

        String heuristic = words[1];

        switch (heuristic) {
            case "MSA" -> this.setHeuristicType(Heuristic.MSA);
            case "TABU" -> this.setHeuristicType(Heuristic.TABU);
            default -> throw new InvalidRSqlSyntaxException("Invalid HEURISTIC: " + heuristic);
        }
    }

    public void parseOPTIMIZATION(String s) throws InvalidRSqlSyntaxException {
        String[] words = s.trim().split("\\s+");

        if (words.length < 1 || words.length > 2) {
            throw new InvalidRSqlSyntaxException("Invalid OPTIMIZATION syntax, length of words not 1-2: " + s);
        }

        if (words.length == 2) {
            String heuristic = words[1];
            switch (heuristic) {
                case "RANDOM" -> this.setOptimizationType(Optimization.RANDOM);
                case "CONNECTED" -> this.setOptimizationType(Optimization.CONNECTED);
                default -> throw new InvalidRSqlSyntaxException("Invalid HEURISTIC: " + heuristic);
            }
        }
    }
    public void parseObjectiveInfo(String s) throws InvalidRSqlSyntaxException {
        String[] words = s.trim().split("\\s+");
        // Check if the number of words is within the valid range
        if (words.length < 2 || words.length > 4) {
            throw new InvalidRSqlSyntaxException("Invalid OBJECTIVE syntax, number of words must be 2-4: " + s);
        }

        // Set objective type and attribute based on the number of words
        switch (words.length) {
            case 2:
                switch (words[1]) {
                    case "HETEROGENEOUS" -> setObjectiveType(Objective.HETEROGENEOUS);
                    case "COMPACT" -> setObjectiveType(Objective.COMPACT);
                    default ->
                            throw new InvalidRSqlSyntaxException("Invalid Objective TYPE, must be either HETEROGENEOUS or COMPACT: " + words[1]);
                }
                setObjectiveAttribute(null);
                break;
            case 3:
                setObjectiveType(null);
                setObjectiveAttribute(words[2]);
                break;
            case 4:
                setObjectiveAttribute(words[3]);
                switch (words[1]) {
                    case "HETEROGENEOUS" -> setObjectiveType(Objective.HETEROGENEOUS);
                    case "COMPACT" -> setObjectiveType(Objective.COMPACT);
                    default ->
                            throw new InvalidRSqlSyntaxException("Invalid Objective TYPE, must be either HETEROGENEOUS or COMPACT: " + words[1]);
                }
                break;
        }
    }

    public void parseWhereInformation(String s) throws InvalidRSqlSyntaxException {
        String[] words = s.trim().split("=");
        String digitRegex = "\\d+";

        if (words[1].matches(digitRegex)) {
            double number = Integer.parseInt(words[1]);
            this.setPValueDouble(number);
            this.setPValueEnum(pType.K);
        } else {
            this.setPValueEnum(pType.PMAX);
            this.setPValueDouble(Double.POSITIVE_INFINITY);
        }

    }

    //500 <= MIN ON population
    public void parseBoundsNoUpper(String s) throws InvalidRSqlSyntaxException {

        String[] words = s.trim().split("\\s+");
        if (words.length != 5) {
            throw new InvalidRSqlSyntaxException("Invalid bounds clause based on number of args" + s);
        }
        BoundsSubclause boundsclause = new BoundsSubclause();
        boundsclause.setLowerBound(Double.parseDouble(words[0]));
        boundsclause.setComparisonOperator1(words[1]);

        AssignAggFn(words, boundsclause);
        boundsclause.setUpperBoundAttribute(words[4]);
        if (this.boundsSubclauses == null) {
            boundsSubclauses = new ArrayList<BoundsSubclause>();
        }
        this.boundsSubclauses.add(boundsclause);
    }

    //11000 < SUM < 20000 ON population
    public void parseBoundsWithUpper(String s) throws InvalidRSqlSyntaxException {

        String[] words = s.trim().split("\\s+");
        if (words.length != 7) {
            throw new InvalidRSqlSyntaxException("Invalid bounds clause must have 7 words seprated by space" + s);
        }
        BoundsSubclause boundsclause = new BoundsSubclause();

        boundsclause.setLowerBound(Double.parseDouble(words[0]));
        boundsclause.setUpperBound(Double.parseDouble(words[4]));
        boundsclause.setUpperBoundAttribute(words[6]);

        boundsclause.setComparisonOperator1(words[1]);
        boundsclause.setComparisonOperator2(words[3]);

        AssignAggFn(words, boundsclause);
        if (this.boundsSubclauses == null) {
            boundsSubclauses = new ArrayList<BoundsSubclause>();
        }
        this.boundsSubclauses.add(boundsclause);
    }

    private void AssignAggFn(String[] words, BoundsSubclause boundsclause) throws InvalidRSqlSyntaxException {
        switch (words[2]) {
            case "MIN" -> boundsclause.setAggFunction(Aggregate.MIN);
            case "SUM" -> boundsclause.setAggFunction(Aggregate.SUM);
            case "MAX" -> boundsclause.setAggFunction(Aggregate.MAX);
            case "COUNT" -> boundsclause.setAggFunction(Aggregate.COUNT);
            case "AVG" -> boundsclause.setAggFunction(Aggregate.AVG);
            default ->
                    throw new InvalidRSqlSyntaxException("Invalid AGG fuinction only allowing (SUM | MIN | MAX | COUNT | AVG): " + words[2]);
        }
    }


    public static void main(String[] args){
            QuerySpecifics spec = new QuerySpecifics();
            System.out.println(spec.toString());
        }

    }


