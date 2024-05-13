package ExecutorFiles;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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

//        // Draw X and Y axis labels
//        g2d.setColor(Color.black);
//        g2d.drawString("X Axis", getWidth() - 30, getHeight() / 2 + 15); // Adjust the position as needed
//        g2d.drawString("Y Axis", getWidth() / 2 - 15, 20); // Adjust the position as needed
//
//        // Draw labels for each point
//        int[] xPoints = area.getPolygon().xpoints;
//        int[] yPoints = area.getPolygon().ypoints;
//        for (int i = 0; i < area.getPolygon().npoints; i++) {
//            String label = "(" + xPoints[i] + "," + yPoints[i] + ")";
//            g2d.drawString(label, xPoints[i], yPoints[i] - 5); // Adjust the position as needed
//        }
    }

    public static void main(String[] args) {
        // Create a list of areas using createGridAreas
        List<Area> areaList = createGridAreas();
        printPolygons(areaList, "initalseeds.png");

    }
    public static void printPolygons(List<Area> areaList, String imageName) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Polygon Graph");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 1000);

            // Create a JPanel with GridLayout to hold all the PolygonGraph instances
            GridLayout gridLayout = new GridLayout(10, 10);
            gridLayout.setHgap(0); // Set horizontal gap between columns
            gridLayout.setVgap(0); // Set vertical gap between rows
            JPanel panel = new JPanel(gridLayout);

            // Add each PolygonGraph instance to the panel
            for (Area curArea : areaList) {
                panel.add(new PolygonGraph(curArea));
            }

            // Add the panel to the frame
            frame.add(panel);
            frame.setVisible(true);

            // Create a BufferedImage from the panel
            BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            panel.print(g2d);
            g2d.dispose();

            // Save the image to a file
            File outputFile = new File(imageName); // Adjust filename and extension as needed
            try {
                ImageIO.write(image, "png", outputFile);
                System.out.println("Image saved to: " + outputFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }



    public static java.util.List<Area> createGridAreas() {
        List<Area> areaList = new ArrayList<>();

        int cellSize = 50;

        // Create a 10x10 grid of polygons
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                // coordinates for the polygon
                int[] xCoords = {x, x + cellSize, x + cellSize, x};
                int[] yCoords = {y, y, y + cellSize, y + cellSize};
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
