package ParserFiles;

public class QueryEnums {
    public enum SubclauseType {
        OBJECTIVE,
        BOUNDS_CLAUSE,
        OPTIMIZATION,
        GAPLESS,
        HEURISTIC,
        WHERE,
        UNKNOWN
    }
    public enum MainClauseType {
        SELECT,
        ORDER_BY,
        FROM,
        WHERE,
        UNKNOWN
    }
    public enum Regions {
        P,
        HET
    }

    public enum OrderByType {
        HET,
        CARD
    }

    public enum OrderDirection {
        ASC,
        DESC
    }

    public enum Objective {
        HETEROGENEOUS,
        COMPACT
    }

    public enum LogicalOperator {
        AND,
        OR
    }

    public enum Aggregate {
        SUM,
        MIN,
        MAX,
        COUNT,
        AVG
    }

    public enum Optimization {
        RANDOM,
        CONNECTED
    }

    public enum pType {
        K,
        PMAX
    }

    public enum Heuristic {
        MSA,
        TABU
    }
}
//
//// Select Clause - Mandatory
//    private Regions REGIONS; //[REGIONS.p, REGIONS.HET]
//    //OrderBy - Optional
//    private OrderByType ORDERTYPE; // (HET | CARD)
//
//    private OrderDirection ORDERDIRECTION; //(ASC | DESC)
//    //FROM Mandatory
//    private String From; //dataset name or path to the file
//    //WHERE
//    private pType pValueEnum;
//    private double pValueDouble;
//
//    private Objective ObjectiveType; //(HETEROGENEOUS | COMPACT)
//    private String ObjectiveAttribute; // OBJECTIVE (HETEROGENEOUS | COMPACT) ON attribute_name
//
//    private double LowerBound; // [lower_bound (< | <=) (SUM | MIN | MAX | COUNT | AVG) (< | <=) upper_bound ON attribute_name]
//
//    private Aggregate AggFunction;
//
//    private double UpperBound;
//    private String UpperBoundAttribute;
//    private Optimization OptimizationType;
//    boolean Gapless;
//    private Heuristic HeuristicType;
//
