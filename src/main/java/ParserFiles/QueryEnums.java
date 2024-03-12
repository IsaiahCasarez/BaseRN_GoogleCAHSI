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
