package yorku.indoor_navigation_system.backend;

import yorku.indoor_navigation_system.backend.models.Graph;
import yorku.indoor_navigation_system.backend.models.Node;
import yorku.indoor_navigation_system.backend.utils.FileUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Visualization extends JPanel {
    private Graph graph;
    private ArrayList<Node> route;

    public Visualization(Graph graph, ArrayList<Node> route) {
        this.graph = graph;
        this.route = route;
    }


//        saveGraphImage(v, "graph_image.png");

    public static void visualizeGraphAndRoute(Graph graph, ArrayList<Node> route) {
        Visualization v = new Visualization(graph, route);
        JFrame frame = new JFrame("Graph Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(FileUtils.getStaticResPath() + graph.mapPath));
        } catch (IOException e) {

            e.printStackTrace();
        }
        frame.setSize(image.getWidth(), image.getHeight());
        frame.add(v);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void saveGraphImage(JComponent component, String fileName) {
        BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        component.paint(g2d);
        g2d.dispose();

        try {
            File outputFile = new File(fileName);
            ImageIO.write(image, "png", outputFile);
            System.out.println("save to: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.route == null) {
            paintComponentMain(g);
        } else {
            paintComponentRoute(g);
        }

    }

    private void paintComponentMain(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;


        g2d.setStroke(new BasicStroke(3));
        for (Node node : graph.graph_node) {
            int x = node.c.x;
            int y = node.c.y;

            g.setColor(Color.BLUE);
            g.fillOval(x, y, 30, 30);


            g.setColor(Color.BLACK);
            g.drawString(node.name, x + 20, y + 20);


            for (Node neighbor : node.Nodes) {
                int x1 = x + 15;
                int y1 = y + 15;
                int x2 = neighbor.c.x + 15;
                int y2 = neighbor.c.y + 15;

                g.setColor(Color.BLACK);
                g.drawLine(x1, y1, x2, y2);
            }
        }
    }

    private void paintComponentRoute(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;


        ArrayList<Node> route = this.route;
        g2d.setStroke(new BasicStroke(3));
        for (Node node : graph.graph_node) {
            int x = node.c.x;
            int y = node.c.y;

            if (!route.contains(node)) {
                g.setColor(Color.BLUE);
            } else {
                g.setColor(Color.RED);
            }
            g.fillOval(x, y, 30, 30);


            g.setColor(Color.BLACK);
            g.drawString(node.name, x + 20, y + 20);


            for (Node neighbor : node.Nodes) {
                int x1 = x + 15;
                int y1 = y + 15;
                int x2 = neighbor.c.x + 15;
                int y2 = neighbor.c.y + 15;
                if (route.contains(node) && (route.indexOf(node) - route.indexOf(neighbor) == 1 || route.indexOf(node) - route.indexOf(neighbor) == -1)) {
                    g.setColor(Color.RED);
                } else {
                    g.setColor(Color.BLACK);
                }
                g.drawLine(x1, y1, x2, y2);
            }
        }
    }

}
