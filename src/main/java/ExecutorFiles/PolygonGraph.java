package ExecutorFiles;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
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
    }

    public static void main(String[] args) {
        // Create a list of areas using createGridAreas
        List<Area> areaList = createGridAreas();
        printPolygons(areaList, "initalseeds.png");
    }

    public static void printPolygons(List<Area> areaList, String imageName) {
        int panelWidth = 1000;
        int panelHeight = 1000;

        // Create a BufferedImage to hold the image
        BufferedImage image = new BufferedImage(panelWidth, panelHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Fill the background with white
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, panelWidth, panelHeight);

        // Draw each polygon and centroid onto the image
        for (Area area : areaList) {
            // Draw the polygon
            g2d.setColor(Color.BLUE);
            g2d.drawPolygon(area.getPolygon());

            // Plot the centroid
            double[] centroid = area.getCentroid();
            int dotSize = 5; // Diameter of the dot
            int dotX = (int) (centroid[0] - dotSize / 2); // X-coordinate of the top-left corner of the dot
            int dotY = (int) (centroid[1] - dotSize / 2); // Y-coordinate of the top-left corner of the dot
            g2d.setColor(Color.RED);
            g2d.fillOval(dotX, dotY, dotSize, dotSize);
        }

        // Dispose of the graphics object
        g2d.dispose();

        // Create the directory if it doesn't exist
        File outputDirectory = new File("outputImages");
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        // Save the image to a file under the "outputImages" folder
        File outputFile = new File(outputDirectory, imageName); // Adjust filename and extension as needed
        try {
            ImageIO.write(image, "png", outputFile);
            System.out.println("Image saved to: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //dummy data used to represent seeds
    public static java.util.List<Area> createGridAreas() {
        List<Area> areaList = new ArrayList<>();

        int cellSize = 50;

        // Create a 10x10 grid of polygons
        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 15; y++) {
                // coordinates for the polygon
                int[] xCoords = {x * cellSize, (x + 1) * cellSize, (x + 1) * cellSize, x * cellSize};
                int[] yCoords = {y * cellSize, y * cellSize, (y + 1) * cellSize, (y + 1) * cellSize};
                Polygon polygon = new Polygon(xCoords, yCoords, 4);

                int minValue = 1000;
                int maxValue = 1000000;
                Random rand = new Random();

                // right now the spatially extensive attribute is the population
                Area area = new Area(areaList.size() + 1, polygon, rand.nextInt(maxValue - minValue + 1) + minValue, 0.0);

                // Add the area to the list
                areaList.add(area);
            }
        }

        return areaList;
    }
}
