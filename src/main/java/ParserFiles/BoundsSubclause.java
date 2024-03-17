package ParserFiles;
import lombok.Data;

import static ParserFiles.QueryEnums.*;

@Data
public class BoundsSubclause {

    private Double LowerBound; // Use Double instead of double to allow null
    private Aggregate AggFunction;
    private Double UpperBound; // Use Double instead of double to allow null
    private String UpperBoundAttribute;
    private String comparisonOperator1;
    private String comparisonOperator2;

    public BoundsSubclause() {
        this.LowerBound = null;
        this.AggFunction = null;
        this.UpperBound = null;
        this.UpperBoundAttribute = null;
        this.comparisonOperator1 = null;
        this.comparisonOperator2 = null;

    }
}
