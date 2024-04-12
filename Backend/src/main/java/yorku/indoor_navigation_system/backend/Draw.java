package yorku.indoor_navigation_system.backend;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Draw {
    private ArrayList<Node> nodes;
    private BufferedImage image;
    String start;
    String end;


    public Draw(ArrayList<Node> nodes, BufferedImage image, String start, String end) {
        this.nodes = nodes;
        this.image = image;
        this.start = start;
        this.end = end;
    }

    public void drawRoute() {
        Graphics2D g2d = image.createGraphics();
        g2d.setStroke(new BasicStroke(image.getWidth()/200));
        Color LightPurple = new Color(200, 0, 200);
        g2d.setColor(LightPurple);
        for(int i = 0; i < nodes.size()-1; i++) {
        	g2d.drawLine(nodes.get(i).getC().getX(), nodes.get(i).getC().getY(),
        			nodes.get(i+1).getC().getX(), nodes.get(i+1).getC().getY());
        }

        g2d.setColor(Color.BLUE);
        int circleSize = image.getWidth()/100;
        int firstX = nodes.get(0).getC().getX() - circleSize/2;
        int firstY = nodes.get(0).getC().getY() - circleSize/2;
        int lastX = nodes.get(nodes.size()-1).getC().getX() - circleSize/2;
        int lastY = nodes.get(nodes.size()-1).getC().getY() - circleSize/2;
        g2d.fillOval(firstX, firstY, circleSize, circleSize);
        g2d.fillOval(lastX, lastY, circleSize, circleSize);

        Font font = new Font("Arial", Font.BOLD, image.getWidth()/50);
        g2d.setFont(font);
        Color darkGreen = new Color(0, 150, 0);
        g2d.setColor(darkGreen);
        int shadowOffset = image.getWidth() / 600;
        g2d.drawString(start, firstX + shadowOffset, firstY + circleSize + image.getWidth() / 70 + shadowOffset);
        g2d.drawString(end, lastX + shadowOffset, lastY + circleSize + image.getWidth() / 70 + shadowOffset);

        g2d.setColor(Color.GREEN);
        g2d.drawString(start, firstX, firstY + circleSize + image.getWidth()/70);
        g2d.drawString(end, lastX, lastY + circleSize + image.getWidth()/70);



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

    public BufferedImage getImage() {
    	return image;
    }
}
