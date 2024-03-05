public class QuerySpecifics {
    private String regions = "None";
    private String orderBy = "None";
    private String from;
    private String where;
    private String objective;
    private String aggregate = "None";
    private String optimization = "None";
    private boolean gapless = false;
    private String heuristic = "None";

    // Constructors, getters, and setters...

    // Default constructor
    public QuerySpecifics() {
    }

    // Parameterized constructor
    public QuerySpecifics(String regions, String orderBy, String from, String where, String objective,
                          String aggregate, String optimization, boolean gapless, String heuristic) {
        this.regions = regions;
        this.orderBy = orderBy;
        this.from = from;
        this.where = where;
        this.objective = objective;
        this.aggregate = aggregate;
        this.optimization = optimization;
        this.gapless = gapless;
        this.heuristic = heuristic;
    }

    // Getters and setters...

    public String getRegions() {
        return regions;
    }

    public void setRegions(String regions) {
        this.regions = regions;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getAggregate() {
        return aggregate;
    }

    public void setAggregate(String aggregate) {
        this.aggregate = aggregate;
    }

    public String getOptimization() {
        return optimization;
    }

    public void setOptimization(String optimization) {
        this.optimization = optimization;
    }

    public boolean isGapless() {
        return gapless;
    }

    public void setGapless(boolean gapless) {
        this.gapless = gapless;
    }

    public String getHeuristic() {
        return heuristic;
    }

    public void setHeuristic(String heuristic) {
        this.heuristic = heuristic;
    }
}
