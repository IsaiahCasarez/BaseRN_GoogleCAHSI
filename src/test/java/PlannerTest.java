import java.util.regex.*;

public class PlannerTest {
    public static void main(String[] args) {
        String subclause = "WHERE p=14";
        String regex = "^\\s*WHERE\\s+P\\s*=(\\s*(K|PMAX|\\d+)\\s*)$";
        boolean isValid = subclause.trim().toUpperCase().matches(regex);
        System.out.println(isValid);
    }
}