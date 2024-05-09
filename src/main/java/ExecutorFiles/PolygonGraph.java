package ExecutorFiles;

import java.awt.*;
import javax.swing.*;

public class PolygonGraph extends JPanel {
    private Area area;

    public PolygonGraph(Area area) {
        this.area = area;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Set rendering hints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw the polygon
        g2d.setColor(Color.blue);
        g2d.drawPolygon(area.getPolygon());

        // Plot the centroid
        double[] centroid = area.getCentroid();
        int dotSize = 5; // Diameter of the dot
        int dotX = (int) (centroid[0] - dotSize / 2); // X-coordinate of the top-left corner of the dot
        int dotY = (int) (centroid[1] - dotSize / 2); // Y-coordinate of the top-left corner of the dot
        g2d.setColor(Color.red);
        g2d.fillOval(dotX, dotY, dotSize, dotSize);

        // Draw X and Y axis labels
        g2d.setColor(Color.black);
        g2d.drawString("X Axis", getWidth() - 30, getHeight() / 2 + 15); // Adjust the position as needed
        g2d.drawString("Y Axis", getWidth() / 2 - 15, 20); // Adjust the position as needed

        // Draw labels for each point
        int[] xPoints = area.getPolygon().xpoints;
        int[] yPoints = area.getPolygon().ypoints;
        for (int i = 0; i < area.getPolygon().npoints; i++) {
            String label = "(" + xPoints[i] + "," + yPoints[i] + ")";
            g2d.drawString(label, xPoints[i], yPoints[i] - 5); // Adjust the position as needed
        }
    }

    public static void main(String[] args) {
        // Create a PolygonGraph with an Area object
        Area area = createArea();
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Polygon Graph");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 400); // Increased size to accommodate labels
            frame.add(new PolygonGraph(area));
            frame.setVisible(true);
        });
    }

    // This method creates and returns an Area object with the polygon and centroid calculated
    private static Area createArea() {
        // Define polygon dimensions and other attributes here
        // For demonstration purposes, let's create a sample polygon
        int[] xPoints = {100, 200, 150, 25};
        int[] yPoints = {50, 100, 200, 200};

        // Create the Polygon
        Polygon polygon = new Polygon(xPoints, yPoints, xPoints.length);

        // Create the Area object with the polygon
        Area area = new Area(1, polygon, "Sample Attribute", 0.0);



        return area;
    }
}
