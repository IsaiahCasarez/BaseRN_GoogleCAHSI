import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Planner {
    public static void main(String[] args) {


        String regex = ".*\\bAND\\b.*";

        String clause = " 29 < MAX < 200 On income AND 20 < SUM on population;";


        System.out.println(clause.matches(regex));


    }

}