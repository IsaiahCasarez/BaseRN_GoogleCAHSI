package ExecutorFiles;

import ParserFiles.BoundsSubclause;
import ParserFiles.QuerySpecifics;
import java.util.List;
import java.util.Set;

public class RegionGrowing
{
    public static List<Area> regionGrowing(Set<Area> seeds, List<Area> regions) {


        return null;
    }

    //Takes input of a set containing a bunch of areas and will return boolean true if it meets all the constraints specified
    public static boolean maintainsConstraints(Set<Area> neighborhood, QuerySpecifics queryInfo) {

        //loop over all the AGG functions and check them
        for (BoundsSubclause constraint: queryInfo.getBoundsSubclauses()) {

            double valOverExtensiveAttributes = 0;
            switch (constraint.getAggFunction()) {

                case SUM:
                    for (Area area: neighborhood) {
                        valOverExtensiveAttributes += area.getSpatiallyExtensiveAttribute();
                    }
                    break;
                case MIN:
                //TODO: do i even need to handle these since we test min max constraint for all the areas in seed selection
                    break;
                case MAX:

                    break;
                case COUNT:
                    valOverExtensiveAttributes = neighborhood.size();
                    break;
                case AVG:
                    for (Area area: neighborhood) {
                        valOverExtensiveAttributes += area.getSpatiallyExtensiveAttribute();
                    }
                    valOverExtensiveAttributes = valOverExtensiveAttributes / neighborhood.size();
                    break;
                default:

                    break;
            }
            if (constraint.getComparisonOperator1() != null) {
                if (!compare(constraint.getLowerBound(), valOverExtensiveAttributes, constraint.getComparisonOperator1().trim())) {
                    //current set does not meet the constraints as specified by the query information
                    return false;
                }
            }

            if (constraint.getComparisonOperator2() != null) {
                if (!compare(valOverExtensiveAttributes, constraint.getUpperBound(), constraint.getComparisonOperator2().trim())) {
                    return false;
                }
            }




        }

        return true;
    }

    public static boolean compare(double operand1, double operand2, String operator) {
        System.out.println( + operand1 + " "+ operator + " " + operand2 );
        if (operator == null) {
            // If the comparison operator is null, return true, assuming no constraint is applied
            return true;
        }
        return switch (operator) {
            case "<" -> operand1 < operand2;
            case "<=" -> operand1 <= operand2;
            case ">" -> operand1 > operand2;
            case ">=" -> operand1 >= operand2;
            default -> throw new IllegalArgumentException("Invalid operator: " + operator);
        };
    }

}
