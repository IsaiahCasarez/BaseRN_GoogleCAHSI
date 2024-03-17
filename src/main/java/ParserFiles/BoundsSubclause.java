package ParserFiles;
import lombok.Data;

import static ParserFiles.QueryEnums.*;

@Data
public class BoundsSubclause {

    private double LowerBound; // [lower_bound (< | <=) (SUM | MIN | MAX | COUNT | AVG) (< | <=) upper_bound ON attribute_name]
    private Aggregate AggFunction;
    private double UpperBound;
    private String UpperBoundAttribute;
    private String comparisonOperator1;
    private String comparisonOperator2;

}
