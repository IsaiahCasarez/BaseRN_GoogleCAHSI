package ParserFiles;
import lombok.Data;
import static ParserFiles.QueryEnums.*;

//
@Data
public class QuerySpecifics {


    // Select Clause - Mandatory
    private Regions REGIONS; //[REGIONS.p, REGIONS.HET]
    //OrderBy - Optional
    private OrderByType ORDERTYPE; // (HET | CARD)

    private OrderDirection ORDERDIRECTION; //(ASC | DESC)
    //FROM Mandatory
    private String From; //dataset name or path to the file
    //WHERE
    private pType pValueEnum;
    private double pValueDouble;

    private Objective ObjectiveType; //(HETEROGENEOUS | COMPACT)
    private String ObjectiveAttribute; // OBJECTIVE (HETEROGENEOUS | COMPACT) ON attribute_name

    private double LowerBound; // [lower_bound (< | <=) (SUM | MIN | MAX | COUNT | AVG) (< | <=) upper_bound ON attribute_name]

    private Aggregate AggFunction;

    private double UpperBound;
    private String UpperBoundAttribute;
    private Optimization OptimizationType;
    boolean Gapless;
    private Heuristic HeuristicType;

    // Constructor with required fields and default values
    public QuerySpecifics(Regions REGIONS, String From, Objective ObjectiveType) {
        this(REGIONS, null, null, From, null, 0.0,
                ObjectiveType, null, 0.0, null, 0.0, null,
                null, false, null);
    }
    public QuerySpecifics() {
        this.REGIONS = null;
        this.ORDERTYPE = null;
        this.ORDERDIRECTION = null;
        this.From = null;
        this.pValueEnum = null;
        this.pValueDouble = 0.0; // Assuming a default value for double
        this.ObjectiveType = null;
        this.ObjectiveAttribute = null;
        this.LowerBound = 0.0; // Assuming a default value for double
        this.AggFunction = null;
        this.UpperBound = 0.0; // Assuming a default value for double
        this.UpperBoundAttribute = null;
        this.OptimizationType = null;
        this.Gapless = false; // Assuming a default value for boolean
        this.HeuristicType = null;
    }


    // Full constructor
    public QuerySpecifics(Regions REGIONS, OrderByType ORDERTYPE, OrderDirection ORDERDIRECTION,
                          String From, pType pValueEnum, double pValueDouble,
                          Objective ObjectiveType, String ObjectiveAttribute,
                          double LowerBound, Aggregate AggFunction, double UpperBound, String UpperBoundAttribute,
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
        this.LowerBound = LowerBound;
        this.AggFunction = AggFunction;
        this.UpperBound = UpperBound;
        this.UpperBoundAttribute = UpperBoundAttribute;
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
        return "ParserFiles.QuerySpecifics{" +
                "REGIONS=" + REGIONS +
                ", ORDERTYPE=" + ORDERTYPE +
                ", ORDERDIRECTION=" + ORDERDIRECTION +
                ", From='" + From + '\'' +
                ", pValueEnum=" + pValueEnum +
                ", pValueDouble=" + pValueDouble +
                ", ObjectiveType=" + ObjectiveType +
                ", ObjectiveAttribute='" + ObjectiveAttribute + '\'' +
                ", LowerBound=" + LowerBound +
                ", AggFunction=" + AggFunction +
                ", UpperBound=" + UpperBound +
                ", UpperBoundAttribute='" + UpperBoundAttribute + '\'' +
                ", OptimizationType=" + OptimizationType +
                ", Gapless=" + Gapless +
                ", HeuristicType=" + HeuristicType +
                '}';
    }

    // Method to set the REGIONS field based on the SELECT clause
    public void setRegionsFromSelect(String selectSubstring) {
        // Use regex or other parsing logic to extract relevant information
        // For simplicity, let's assume the SELECT clause is in the format "SELECT REGIONS, [REGIONS.p, REGIONS.HET];"
        if (selectSubstring.contains("REGIONS")) {
            this.REGIONS = Regions.P; // Set the field to a default value, change as needed
        }
    }

    public static void main(String[] args) {
        QuerySpecifics spec = new QuerySpecifics();
        System.out.println(spec.toString());
    }

}

