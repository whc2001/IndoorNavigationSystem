package yorku.indoor_navigation_system.backend;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Draw {
    private ArrayList<Node> nodes;
    private BufferedImage image;


    public Draw(ArrayList<Node> nodes, BufferedImage image) {
        this.nodes = nodes;
        this.image = image;
    }

    public void drawRoute() {
        Graphics2D g2d = image.createGraphics();
        g2d.setStroke(new BasicStroke(6));
        g2d.setColor(Color.RED);
        for(int i = 0; i < nodes.size()-1; i++) {
        	g2d.drawLine(nodes.get(i).getC().getX(), nodes.get(i).getC().getY(),
        			nodes.get(i+1).getC().getX(), nodes.get(i+1).getC().getY());
        }

        g2d.dispose();
    }

    public void drawAll() {
        Graphics2D g2d = image.createGraphics();
        int pointSize = image.getWidth()/120;
        int fontSize = image.getWidth()/110;
        Font font = new Font("Arial", Font.BOLD, fontSize);
        g2d.setFont(font);

        for (int i = 0; i < nodes.size(); i++) {
            int x = nodes.get(i).getC().getX();
            int y = nodes.get(i).getC().getY();

            g2d.setColor(Color.RED);
            g2d.fillOval(x - pointSize / 2, y - pointSize / 2, pointSize, pointSize);

            g2d.setColor(Color.BLACK);
            int shadowOffset = 2;
            g2d.drawString("" + i, x + 20 + shadowOffset, y + 20 + shadowOffset);

            g2d.setColor(Color.WHITE);
            g2d.drawString("" + i, x + 20, y + 20);

        }


        g2d.setStroke(new BasicStroke(6));
        g2d.setColor(Color.RED);
        for(int i = 0; i < nodes.size(); i++) {
            for(Node n: nodes.get(i).getNodes()) {
                g2d.drawLine(nodes.get(i).getC().getX(), nodes.get(i).getC().getY(),
                        n.getC().getX(), n.getC().getY());
            }
        }

        g2d.dispose();
    }

    public void drawNode() {
        Graphics2D g2d = image.createGraphics();
        int pointSize = image.getWidth()/120;
        int fontSize = image.getWidth()/110;
        Font font = new Font("Arial", Font.BOLD, fontSize);
        g2d.setFont(font);

        for (int i = 0; i < nodes.size(); i++) {
            int x = nodes.get(i).getC().getX();
            int y = nodes.get(i).getC().getY();

            g2d.setColor(Color.RED);
            g2d.fillOval(x - pointSize / 2, y - pointSize / 2, pointSize, pointSize);

            g2d.setColor(Color.BLACK);
            int shadowOffset = 2;
            g2d.drawString("" + i, x + 20 + shadowOffset, y + 20 + shadowOffset);

            g2d.setColor(Color.WHITE);
            g2d.drawString("" + i, x + 20, y + 20);

        }

        g2d.dispose();
    }
}
