package ExecutorFiles;

import java.awt.Polygon;

public class Area {
    private int identifier;
    private Polygon geometry;
    //what is the purpose of this attribute
    //designated on the fly, query tells us this and disimilalirty
    private Object spatiallyExtensiveAttribute;


    //How dissikilar one area is from another
    private double dissimilarityAttribute;

    private double[] centroid;


    // Constructor with Polygon geometry
    public Area(int identifier, Polygon geometry, Object spatiallyExtensiveAttribute, double dissimilarityAttribute) {
        this.identifier = identifier;
        this.geometry = geometry;
        this.spatiallyExtensiveAttribute = spatiallyExtensiveAttribute;
        this.dissimilarityAttribute = dissimilarityAttribute;
        calculateCentroid();
    }

    // Constructor with xPoints and yPoints
    public Area(int identifier, int[] xPoints, int[] yPoints, Object spatiallyExtensiveAttribute, double dissimilarityAttribute) {
        this.identifier = identifier;
        this.geometry = new Polygon(xPoints, yPoints, xPoints.length);
        this.spatiallyExtensiveAttribute = spatiallyExtensiveAttribute;
        this.dissimilarityAttribute = dissimilarityAttribute;
        calculateCentroid();
    }

    // Method to calculate centroid
    private void calculateCentroid() {
        centroid = new double[2];
        int numPoints = geometry.npoints;
        double signedArea = 0;
        double x0 = 0;
        double y0 = 0;
        double x1 = 0;
        double y1 = 0;
        double a = 0;

        for (int i = 0; i < numPoints - 1; i++) {
            x0 = geometry.xpoints[i];
            y0 = geometry.ypoints[i];
            x1 = geometry.xpoints[i + 1];
            y1 = geometry.ypoints[i + 1];
            a = x0 * y1 - x1 * y0;
            signedArea += a;
            centroid[0] += (x0 + x1) * a;
            centroid[1] += (y0 + y1) * a;
        }

        // Do the last vertex separately to avoid closing the polygon prematurely
        x0 = geometry.xpoints[numPoints - 1];
        y0 = geometry.ypoints[numPoints - 1];
        x1 = geometry.xpoints[0];
        y1 = geometry.ypoints[0];
        a = x0 * y1 - x1 * y0;
        signedArea += a;
        centroid[0] += (x0 + x1) * a;
        centroid[1] += (y0 + y1) * a;

        signedArea *= 0.5;
        centroid[0] /= (6 * signedArea);
        centroid[1] /= (6 * signedArea);
    }

    // Getters and setters
    public Polygon getPolygon() {
        return geometry;
    }

    public double[] getCentroid() {
        return centroid;
    }


    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public Polygon getGeometry() {
        return geometry;
    }

    public void setGeometry(Polygon geometry) {
        this.geometry = geometry;
    }

    public Object getSpatiallyExtensiveAttribute() {
        return spatiallyExtensiveAttribute;
    }

    public void setSpatiallyExtensiveAttribute(Object spatiallyExtensiveAttribute) {
        this.spatiallyExtensiveAttribute = spatiallyExtensiveAttribute;
    }

    public double getDissimilarityAttribute() {
        return dissimilarityAttribute;
    }

    public void setDissimilarityAttribute(double dissimilarityAttribute) {
        this.dissimilarityAttribute = dissimilarityAttribute;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Geometry Points: [");
        for (int i = 0; i < geometry.npoints; i++) {
            sb.append("(").append(geometry.xpoints[i]).append(",").append(geometry.ypoints[i]).append(")");
            if (i < geometry.npoints - 1) {
                sb.append(", ");
            }
        }
        sb.append("], Centroid: (").append(centroid[0]).append(", ").append(centroid[1]).append(")");
        sb.append(" Spatially Extensive Attribute: " + spatiallyExtensiveAttribute);
        return sb.toString();
    }

}
