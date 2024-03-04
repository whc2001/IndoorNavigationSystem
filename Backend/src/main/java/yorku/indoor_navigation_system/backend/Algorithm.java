package yorku.indoor_navigation_system.backend;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class Algorithm {
    @Autowired
    private NodeRepository nodeRepository;
    @Autowired
    private CoordinateRepository coordinateRepository;
    @Autowired
    private GraphRepository graphRepository;
    @Autowired
    private ResourceLoader resourceLoader;

    public ArrayList<Node> extractAndConnectNodes(String graphPath) {
        ArrayList<Node> result = extractNode(graphPath);
//        Resource resource = resourceLoader.getResource(graphPath);
        BufferedImage image = null;
        try {
            image = (BufferedImage) FileUtils.openResImage(graphPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ConnectNode(result, graphPath, image.getWidth() / 5);
        return result;
    }

    public void DrawNode(Graph g) {
        BufferedImage image = null;
//        Resource resource = resourceLoader.getResource(g.getMapPath());
        try {
            image = (BufferedImage) FileUtils.openResImage(g.getMapPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Draw d = new Draw(g.getGraph_node(), image);
        d.drawNode();
        String resultPath = "src/main/resources/static/mapWithNode/";
        try {
            File outputFile = new File(resultPath + g.getName() + "Floor" + g.getFloor() + ".jpg");
            ImageIO.write(image, "jpg", outputFile);
            System.out.println("result is store into: " + resultPath + g.getName() + "Floor" + g.getFloor() + ".jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Test(Graph g) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(g.getMapPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Draw d = new Draw(g.getGraph_node(), image);
        d.drawAll();
        String resultPath = "src/main/resources/static/mapWithNode/";
        File outputFile = new File(resultPath + g.getName() + "Floor" + g.getFloor() + "Test.jpg");
        try {
            ImageIO.write(image, "jpg", outputFile);
            System.out.println("result is store into: " + resultPath + g.getName() + "Floor" + g.getFloor() + "Test.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void BuildGraph(String graphPath, String mapPath, String name, Integer floor) {
        ArrayList<Graph> graphs = (ArrayList<Graph>) graphRepository.findByNameAndFloor(name, floor);
        Graph g = null;
        if (graphs.size() > 0) {
            System.out.println("Graph already exists");
            g = graphs.get(0);
        } else {
            System.out.println("Graph does not exist");
            g = new Graph(extractAndConnectNodes(graphPath), name, floor, mapPath, graphPath);

        }
//		ArrayList<Node> Nodes = g.getGraph_node();
//
//		for(int i = 0;i<Nodes.size();i++) {
//			System.out.println(Nodes.get(i));
//		}
//
//		int a = (int)(Math.random()*Nodes.size());
//		int b = (int)(Math.random()*Nodes.size());
//
//		Navigate(g, Nodes.get(a), Nodes.get(b), "src/main/resources/static/result");
        if (graphs.size() == 0) {
            graphRepository.save(g);
            graphRepository.flush();
        }
        DrawNode(g);


    }

    public BufferedImage Navigate(Graph g, Node start, Node des, String resultPath) {
        System.out.println("Start navigate from " + start.getC() + " to " + des.getC());
        BufferedImage image = null;

        try {
            image = (BufferedImage) FileUtils.openResImage(FileUtils.getMapPath(FileUtils.getFileName(g.getName(), g.getFloor())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Draw d = new Draw(calculateRoute(g, start, des), image);
        d.drawRoute();

        return image;
    }

    public ArrayList<Node> calculateRoute(Graph graph, Node start, Node des) {
        Map<Node, Double> distance = new HashMap<Node, Double>();
        Map<Node, ArrayList<Node>> route = new HashMap<Node, ArrayList<Node>>();
        ArrayList<Node> tmpNodeList = graph.getGraph_node();
        for (Node n : graph.graph_node) {
            distance.put(n, Double.MAX_VALUE);
            ArrayList<Node> tempList = new ArrayList<Node>();
            tempList.add(start);
            route.put(n, tempList);
        }

        distance.replace(start, 0.0);
        ArrayList<Node> pq = new ArrayList<Node>();
        pq.add(start);

        while (pq.size() > 0) {
            Node current = pq.remove(0);
            if (tmpNodeList.contains(current)) {
                tmpNodeList.remove(current);
                for (Node n : current.Nodes) {
                    if (tmpNodeList.contains(n)) {
                        pq.add(n);
                        if (distance.get(current) + NodeDisCalculate(current, n) < distance.get(n)) {
                            distance.replace(n, distance.get(current) + NodeDisCalculate(current, n));
                            ArrayList<Node> tempList2 = (ArrayList<Node>) route.get(current).clone();
                            tempList2.add(n);
                            if (n.c.x == 1154 && n.c.y == 740) {
                                System.out.println(tempList2 + "here");
                            }
                            route.replace(n, tempList2);
                        }
                    }
                }
            }
//			System.out.println("current:"+ current);
//			System.out.println("pq:"+ pq);
//			System.out.println("tmpNodeList:"+ tmpNodeList);
//			System.out.println("distance:"+ distance);
//			System.out.println("route:"+ route);
        }
        return route.get(des);

    }

    public double NodeDisCalculate(Node n1, Node n2) {
        return Math.sqrt((n1.c.x - n2.c.x) * (n1.c.x - n2.c.x) + (n1.c.y - n2.c.y) * (n1.c.y - n2.c.y));
    }

    public ArrayList<Node> createNodes(Node... nodes) {
        ArrayList<Node> r = new ArrayList<Node>();
        for (Node n : nodes) {
            r.add(n);
        }
        return r;
    }

    public void ConnectNode(ArrayList<Node> tmp, String path, int max) {


        for (int i = 0; i < tmp.size(); i++) {
            for (int j = i + 1; j < tmp.size(); j++) {
                Node n1 = tmp.get(i);
                Node n2 = tmp.get(j);

                if (NodeDisCalculate(n1, n2) <= max && (isConnected(n1, n2, path) || isConnected(n2, n1, path))) {
                    if (!n1.Nodes.contains(n2)) {
                        n1.Nodes.add(n2);
                    }
                    if (!n2.Nodes.contains(n1)) {
                        n2.Nodes.add(n1);
                    }
//					System.out.println(n1+"  connect  "+n2+"  is true");
                } else {
//					System.out.println(n1+"  connect  "+n2+"  is false");
                }

            }
        }
    }

    public boolean isConnected(Node n1, Node n2, String path) {
        BufferedImage image = null;
//        Resource resource = resourceLoader.getResource(path);
        try {
            image = (BufferedImage) FileUtils.openResImage(path);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int x1 = n1.c.x;
        int y1 = n1.c.y;
        int x2 = n2.c.x;
        int y2 = n2.c.y;
        Map<Node, ArrayList<ArrayList<Coordinate>>> side = new HashMap<Node, ArrayList<ArrayList<Coordinate>>>();
        side.put(n1, new ArrayList<ArrayList<Coordinate>>());
        side.put(n2, new ArrayList<ArrayList<Coordinate>>());
//		ArrayList<Coordinate> side1 = new ArrayList<Coordinate>();
        if (x2 >= x1 && y2 <= y1) {
            //Northeast
            return isLineOnNE(side, n1, n2, image);

        } else if (x2 <= x1 && y2 <= y1) {
            //Northwest
            return isLineOnNW(side, n1, n2, image);

        } else if (x2 <= x1 && y2 >= y1) {
            //Southwest
            return isLineOnSW(side, n1, n2, image);

        } else if (x2 >= x1 && y2 >= y1) {
            //Southeast
            return isLineOnSE(side, n1, n2, image);

        }


        return false;
    }

    public ArrayList<Node> extractNode(String path) {
        ArrayList<Node> result = new ArrayList<>();
//        Resource resource = resourceLoader.getResource(path);
        try {
            BufferedImage image = (BufferedImage) FileUtils.openResImage(path);
            for (int y = 0; y < image.getHeight() - 22; y++) {
                for (int x = 0; x < image.getWidth() - 24; x++) {
                    boolean flag = false;
                    for (Node n : result) {
                        if (n.c.x - 15 <= x && n.c.x + 15 >= x && n.c.y - 15 <= y && n.c.y + 15 >= y) {
                            flag = true;
                        }
                    }
                    if (flag) {
                        continue;
                    }
                    Coordinate c = new Coordinate(x + 12, y + 11);
                    if (isRedBox(x, y, image)) {
                        Node temp = new Node(c, new ArrayList<Node>(), "none", 0);
                        coordinateRepository.save(c);
                        nodeRepository.save(temp);
                        result.add(temp);
                        x += 24;
                    } else if (isBlueBox(x, y, image)) {
                        Node temp = new Node(c, new ArrayList<Node>(), "stair", 1);
                        coordinateRepository.save(c);
                        nodeRepository.save(temp);
                        result.add(temp);
                        x += 24;
                    } else if (isGreenBox(x, y, image)) {
                        Node temp = new Node(c, new ArrayList<Node>(), "door", 2);
                        coordinateRepository.save(c);
                        nodeRepository.save(temp);
                        result.add(temp);
                        x += 24;
                    } else if (isYellowBox(x, y, image)) {

                        Node temp = new Node(c, new ArrayList<Node>(), "classroom", 3);
                        coordinateRepository.save(c);
                        nodeRepository.save(temp);
                        result.add(temp);
                        x += 24;
                    } else if (isPinkBox(x, y, image)) {
                        Node temp = new Node(c, new ArrayList<Node>(), "elevator", 4);
                        coordinateRepository.save(c);
                        nodeRepository.save(temp);
                        result.add(temp);
                        x += 24;
                    } else if (isCyanBox(x, y, image)) {
                        Node temp = new Node(c, new ArrayList<Node>(), "Connector", -1);
                        coordinateRepository.save(c);
                        nodeRepository.save(temp);
                        result.add(temp);
                        x += 24;
                    }

                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    public String OCR(String path) {
        String result = "";
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("src/tessdata/");
        tesseract.setLanguage("eng");
        try {
            result = tesseract.doOCR(new File(path));
        } catch (TesseractException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }

    public String GetName(int x, int y, BufferedImage image) {
        BufferedImage subimage = image.getSubimage(x, y + 23, 300, 80);
        boolean flag = false;
        for (int a = x; a < x + 300; a++) {
            for (int b = y + 23; b < y + 80; b++) {
                if (isRedBox(a, b, image) || isGreenBox(a, b, image) || isYellowBox(a, b, image) || isBlueBox(a, b, image) || isPinkBox(a, b, image)) {
                    subimage = image.getSubimage(x, y + 23, a, 80);
                    flag = true;
                }
                if (flag) {
                    break;
                }
            }
            if (flag) {
                break;
            }
        }

        File outputFile = new File("src/main/resources/static/graph/temp/temp.jpg");
        try {
            ImageIO.write(subimage, "jpg", outputFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String result = OCR("src/main/resources/static/graph/temp/temp.jpg");
        result = result.substring(0, result.length() - 1);
        return result;
    }


    public boolean isRed(int r, int g, int b) {
        if (r >= 230 && g <= 15 && b <= 15) {
            return true;
        }
        return false;
    }

    public boolean isRedBox(int x, int y, BufferedImage image) {
        int pixel = image.getRGB(x, y);
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = pixel & 0xFF;
        int Xpixel = image.getRGB(x + 20, y);
        int Xred = (Xpixel >> 16) & 0xFF;
        int Xgreen = (Xpixel >> 8) & 0xFF;
        int Xblue = Xpixel & 0xFF;
        int Ypixel = image.getRGB(x, y + 20);
        int Yred = (Ypixel >> 16) & 0xFF;
        int Ygreen = (Ypixel >> 8) & 0xFF;
        int Yblue = Ypixel & 0xFF;
        if (isRed(red, green, blue) && isRed(Xred, Xgreen, Xblue) && isRed(Yred, Ygreen, Yblue)) {
            return true;
        }
        return false;
    }

    public boolean isBlue(int r, int g, int b) {
        if (b >= 230 && r <= 15 && g <= 15) {
            return true;
        }
        return false;
    }

    public boolean isBlueBox(int x, int y, BufferedImage image) {
        int pixel = image.getRGB(x, y);
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = pixel & 0xFF;
        int Xpixel = image.getRGB(x + 20, y);
        int Xred = (Xpixel >> 16) & 0xFF;
        int Xgreen = (Xpixel >> 8) & 0xFF;
        int Xblue = Xpixel & 0xFF;
        int Ypixel = image.getRGB(x, y + 20);
        int Yred = (Ypixel >> 16) & 0xFF;
        int Ygreen = (Ypixel >> 8) & 0xFF;
        int Yblue = Ypixel & 0xFF;
        if (isBlue(red, green, blue) && isBlue(Xred, Xgreen, Xblue) && isBlue(Yred, Ygreen, Yblue)) {
            return true;
        }
        return false;
    }

    public boolean isGreen(int r, int g, int b) {
        if (g >= 230 && r <= 15 && b <= 15) {
            return true;
        }
        return false;
    }

    public boolean isGreenBox(int x, int y, BufferedImage image) {
        int pixel = image.getRGB(x, y);
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = pixel & 0xFF;
        int Xpixel = image.getRGB(x + 20, y);
        int Xred = (Xpixel >> 16) & 0xFF;
        int Xgreen = (Xpixel >> 8) & 0xFF;
        int Xblue = Xpixel & 0xFF;
        int Ypixel = image.getRGB(x, y + 20);
        int Yred = (Ypixel >> 16) & 0xFF;
        int Ygreen = (Ypixel >> 8) & 0xFF;
        int Yblue = Ypixel & 0xFF;

        if (isGreen(red, green, blue) && isGreen(Xred, Xgreen, Xblue) && isGreen(Yred, Ygreen, Yblue)) {
            return true;
        }
        return false;
    }

    public boolean isYellow(int r, int g, int b) {
        if (r >= 230 && g >= 230 && b <= 15) {
            return true;
        }
        return false;
    }

    public boolean isYellowBox(int x, int y, BufferedImage image) {
        int pixel = image.getRGB(x, y);
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = pixel & 0xFF;
        int Xpixel = image.getRGB(x + 20, y);
        int Xred = (Xpixel >> 16) & 0xFF;
        int Xgreen = (Xpixel >> 8) & 0xFF;
        int Xblue = Xpixel & 0xFF;
        int Ypixel = image.getRGB(x, y + 20);
        int Yred = (Ypixel >> 16) & 0xFF;
        int Ygreen = (Ypixel >> 8) & 0xFF;
        int Yblue = Ypixel & 0xFF;

        if (isYellow(red, green, blue) && isYellow(Xred, Xgreen, Xblue) && isYellow(Yred, Ygreen, Yblue)) {
            return true;
        }
        return false;
    }

    public boolean isPink(int r, int g, int b) {
        if (r >= 230 && b >= 230 && g <= 15) {
            return true;
        }
        return false;
    }

    public boolean isPinkBox(int x, int y, BufferedImage image) {
        int pixel = image.getRGB(x, y);
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = pixel & 0xFF;
        int Xpixel = image.getRGB(x + 20, y);
        int Xred = (Xpixel >> 16) & 0xFF;
        int Xgreen = (Xpixel >> 8) & 0xFF;
        int Xblue = Xpixel & 0xFF;
        int Ypixel = image.getRGB(x, y + 20);
        int Yred = (Ypixel >> 16) & 0xFF;
        int Ygreen = (Ypixel >> 8) & 0xFF;
        int Yblue = Ypixel & 0xFF;

        if (isPink(red, green, blue) && isPink(Xred, Xgreen, Xblue) && isPink(Yred, Ygreen, Yblue)) {
            return true;
        }
        return false;
    }

    public boolean isCyan(int r, int g, int b) {
        if (g >= 230 && b >= 230 && r <= 15) {
            return true;
        }
        return false;
    }

    public boolean isCyanBox(int x, int y, BufferedImage image) {
        int pixel = image.getRGB(x, y);
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = pixel & 0xFF;
        int Xpixel = image.getRGB(x + 20, y);
        int Xred = (Xpixel >> 16) & 0xFF;
        int Xgreen = (Xpixel >> 8) & 0xFF;
        int Xblue = Xpixel & 0xFF;
        int Ypixel = image.getRGB(x, y + 20);
        int Yred = (Ypixel >> 16) & 0xFF;
        int Ygreen = (Ypixel >> 8) & 0xFF;
        int Yblue = Ypixel & 0xFF;

        if (isCyan(red, green, blue) && isCyan(Xred, Xgreen, Xblue) && isCyan(Yred, Ygreen, Yblue)) {
            return true;
        }
        return false;
    }

    public boolean isBlack(int r, int g, int b) {
        if (g <= 15 && b <= 15 && r <= 15) {
            return true;
        }
        return false;
    }

    public boolean isBlack(int x, int y, BufferedImage image) {
        int pixel = image.getRGB(x, y);
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = pixel & 0xFF;
        if (isBlack(red, green, blue)) {
            return true;
        }
        return false;
    }

    public boolean isCorrectColor(int type, int x, int y, BufferedImage image) {
        int pixel = image.getRGB(x, y);
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = pixel & 0xFF;
        switch (type) {
            case -1:
                return isCyan(red, green, blue);
            case 0:
                return isRed(red, green, blue);
            case 1:
                return isBlue(red, green, blue);
            case 2:
                return isGreen(red, green, blue);
            case 3:
                return isYellow(red, green, blue);
            case 4:
                return isPink(red, green, blue);

        }
        return false;

    }

    public ArrayList<Coordinate> getLeft(int type, int x, int y, BufferedImage image) {
        int a = 0;
        ArrayList<Coordinate> result = new ArrayList<Coordinate>();
        for (int i = x; i > x - 20; i--) {
            if (isCorrectColor(type, i, y, image)) {
                a = i;
            } else {
                break;
            }
        }
        for (int i = y + 15; i > y - 15; i--) {
            int pixel = image.getRGB(a - 1, i);
            int red = (pixel >> 16) & 0xFF;
            int green = (pixel >> 8) & 0xFF;
            int blue = pixel & 0xFF;
            if (isBlack(red, green, blue)) {
                result.add(new Coordinate(a - 1, i));
            }
        }


        return result;
    }

    public ArrayList<Coordinate> getRight(int type, int x, int y, BufferedImage image) {
        int a = 0;
        ArrayList<Coordinate> result = new ArrayList<Coordinate>();
        for (int i = x; i < x + 20; i++) {
            if (isCorrectColor(type, i, y, image)) {
                a = i;
            } else {
                break;
            }
        }

        for (int i = y + 15; i > y - 15; i--) {
            int pixel = image.getRGB(a + 1, i);
            int red = (pixel >> 16) & 0xFF;
            int green = (pixel >> 8) & 0xFF;
            int blue = pixel & 0xFF;
            if (isBlack(red, green, blue)) {
                result.add(new Coordinate(a + 1, i));
            }
        }


        return result;
    }

    public ArrayList<Coordinate> getTop(int type, int x, int y, BufferedImage image) {
        int a = 0;
        ArrayList<Coordinate> result = new ArrayList<Coordinate>();
        for (int i = y; i > y - 20; i--) {
            if (isCorrectColor(type, x, i, image)) {
                a = i;
            } else {
                break;
            }
        }
        for (int i = x + 15; i > x - 15; i--) {
            int pixel = image.getRGB(i, a - 1);
            int red = (pixel >> 16) & 0xFF;
            int green = (pixel >> 8) & 0xFF;
            int blue = pixel & 0xFF;
            if (isBlack(red, green, blue)) {
                result.add(new Coordinate(i, a - 1));
            }
        }


        return result;
    }

    public ArrayList<Coordinate> getBot(int type, int x, int y, BufferedImage image) {
        int a = 0;
        ArrayList<Coordinate> result = new ArrayList<Coordinate>();
        for (int i = y; i < y + 20; i++) {
            if (isCorrectColor(type, x, i, image)) {
                a = i;
            } else {
                break;
            }
        }
        for (int i = x + 15; i > x - 15; i--) {
            int pixel = image.getRGB(i, a + 1);
            int red = (pixel >> 16) & 0xFF;
            int green = (pixel >> 8) & 0xFF;
            int blue = pixel & 0xFF;
            if (isBlack(red, green, blue)) {
                result.add(new Coordinate(i, a + 1));
            }
        }


        return result;
    }

    public boolean isLineOnNE(Map<Node, ArrayList<ArrayList<Coordinate>>> side, Node n2, Coordinate c, BufferedImage image) {
        boolean flag = true;
        int xt = c.x;
        int yt = c.y;
        while (flag) {
            int check = 0;
            while (isBlack(xt + 1, yt, image)) {
                xt = xt + 1;
                check++;
            }
            while (isBlack(xt, yt - 1, image)) {
                yt = yt - 1;
                check++;
            }
            if (check == 0) {
                Coordinate cc = new Coordinate(xt + 1, yt);
                for (Coordinate cd : side.get(n2).get(0)) {
                    if (cd.equal(cc)) {
                        return true;
                    }
                }
                for (Coordinate cd : side.get(n2).get(1)) {
                    if (cd.equal(cc)) {
                        return true;
                    }
                }
                cc = new Coordinate(xt, yt - 1);
                for (Coordinate cd : side.get(n2).get(0)) {
                    if (cd.equal(cc)) {
                        return true;
                    }
                }
                for (Coordinate cd : side.get(n2).get(1)) {
                    if (cd.equal(cc)) {
                        return true;
                    }
                }
                flag = false;
            }
        }
        return false;
    }

    public boolean isLineOnNW(Map<Node, ArrayList<ArrayList<Coordinate>>> side, Node n2, Coordinate c, BufferedImage image) {
        boolean flag = true;
        int xt = c.x;
        int yt = c.y;
        while (flag) {
            int check = 0;
            while (isBlack(xt - 1, yt, image)) {
                xt = xt - 1;
                check++;
            }
            while (isBlack(xt, yt - 1, image)) {
                yt = yt - 1;
                check++;
            }
            if (check == 0) {
                Coordinate cc = new Coordinate(xt - 1, yt);
                for (Coordinate cd : side.get(n2).get(0)) {
                    if (cd.equal(cc)) {
                        return true;
                    }
                }
                for (Coordinate cd : side.get(n2).get(1)) {
                    if (cd.equal(cc)) {
                        return true;
                    }
                }
                cc = new Coordinate(xt, yt - 1);
                for (Coordinate cd : side.get(n2).get(0)) {
                    if (cd.equal(cc)) {
                        return true;
                    }
                }
                for (Coordinate cd : side.get(n2).get(1)) {
                    if (cd.equal(cc)) {
                        return true;
                    }
                }
                flag = false;
            }
        }
        return false;
    }

    public boolean isLineOnSE(Map<Node, ArrayList<ArrayList<Coordinate>>> side, Node n2, Coordinate c, BufferedImage image) {
        boolean flag = true;
        int xt = c.x;
        int yt = c.y;
        while (flag) {
            int check = 0;
            while (isBlack(xt, yt + 1, image)) {
                yt = yt + 1;
                check++;
            }
            while (isBlack(xt + 1, yt, image)) {
                xt = xt + 1;
                check++;
            }

            if (check == 0) {
                Coordinate cc = new Coordinate(xt + 1, yt);
                for (Coordinate cd : side.get(n2).get(0)) {
                    if (cd.equal(cc)) {
                        return true;
                    }
                }
                for (Coordinate cd : side.get(n2).get(1)) {
                    if (cd.equal(cc)) {
                        return true;
                    }
                }
                cc = new Coordinate(xt, yt + 1);
                for (Coordinate cd : side.get(n2).get(0)) {
                    if (cd.equal(cc)) {
                        return true;
                    }
                }
                for (Coordinate cd : side.get(n2).get(1)) {
                    if (cd.equal(cc)) {
                        return true;
                    }
                }
                flag = false;
            }
        }
        return false;
    }

    public boolean isLineOnSW(Map<Node, ArrayList<ArrayList<Coordinate>>> side, Node n2, Coordinate c, BufferedImage image) {
        boolean flag = true;
        int xt = c.x;
        int yt = c.y;
        while (flag) {
            int check = 0;
            while (isBlack(xt, yt + 1, image)) {
                yt = yt + 1;
                check++;
            }
            while (isBlack(xt - 1, yt, image)) {
                xt = xt - 1;
                check++;
            }

            if (check == 0) {
                Coordinate cc = new Coordinate(xt - 1, yt);
                for (Coordinate cd : side.get(n2).get(0)) {
                    if (cd.equal(cc)) {
                        return true;
                    }
                }
                for (Coordinate cd : side.get(n2).get(1)) {
                    if (cd.equal(cc)) {
                        return true;
                    }
                }
                cc = new Coordinate(xt, yt + 1);
                for (Coordinate cd : side.get(n2).get(0)) {
                    if (cd.equal(cc)) {
                        return true;
                    }
                }
                for (Coordinate cd : side.get(n2).get(1)) {
                    if (cd.equal(cc)) {
                        return true;
                    }
                }
                flag = false;
            }
        }
        return false;
    }

    public boolean isLineOnNE(Map<Node, ArrayList<ArrayList<Coordinate>>> side, Node n1, Node n2, BufferedImage image) {
        int x1 = n1.c.x;
        int y1 = n1.c.y;
        int x2 = n2.c.x;
        int y2 = n2.c.y;
        side.get(n1).add(getRight(n1.type, x1, y1, image));
        side.get(n1).add(getTop(n1.type, x1, y1, image));
        side.get(n2).add(getLeft(n2.type, x2, y2, image));
        side.get(n2).add(getBot(n2.type, x2, y2, image));
        if (side.get(n1).get(0).size() > 2) {
            Coordinate c = side.get(n1).get(0).get(side.get(n1).get(0).size() / 2);
            if (isLineOnNE(side, n2, c, image)) {
                return true;
            }

        }
        if (side.get(n1).get(1).size() > 2) {
            Coordinate c = side.get(n1).get(1).get(side.get(n1).get(1).size() / 2);
            if (isLineOnNE(side, n2, c, image)) {
                return true;
            }

        }
        if (side.get(n2).get(0).size() > 2) {
            Coordinate c = side.get(n2).get(0).get(side.get(n2).get(0).size() / 2);
            if (isLineOnSW(side, n1, c, image)) {
                return true;
            }

        }
        if (side.get(n2).get(1).size() > 2) {
            Coordinate c = side.get(n2).get(1).get(side.get(n2).get(1).size() / 2);
            if (isLineOnSW(side, n1, c, image)) {
                return true;
            }

        }
        return false;
    }

    public boolean isLineOnNW(Map<Node, ArrayList<ArrayList<Coordinate>>> side, Node n1, Node n2, BufferedImage image) {
        int x1 = n1.c.x;
        int y1 = n1.c.y;
        int x2 = n2.c.x;
        int y2 = n2.c.y;
        side.get(n1).add(getLeft(n1.type, x1, y1, image));
        side.get(n1).add(getTop(n1.type, x1, y1, image));
        side.get(n2).add(getRight(n2.type, x2, y2, image));
        side.get(n2).add(getBot(n2.type, x2, y2, image));
        if (side.get(n1).get(0).size() > 2) {
            Coordinate c = side.get(n1).get(0).get(side.get(n1).get(0).size() / 2);
            if (isLineOnNW(side, n2, c, image)) {
                return true;
            }

        }
        if (side.get(n1).get(1).size() > 2) {
            Coordinate c = side.get(n1).get(1).get(side.get(n1).get(1).size() / 2);
            if (isLineOnNW(side, n2, c, image)) {
                return true;
            }

        }
        if (side.get(n2).get(0).size() > 2) {
            Coordinate c = side.get(n2).get(0).get(side.get(n2).get(0).size() / 2);
            if (isLineOnSE(side, n1, c, image)) {
                return true;
            }

        }
        if (side.get(n2).get(1).size() > 2) {
            Coordinate c = side.get(n2).get(1).get(side.get(n2).get(1).size() / 2);
            if (isLineOnSE(side, n1, c, image)) {
                return true;
            }

        }
        return false;
    }

    public boolean isLineOnSW(Map<Node, ArrayList<ArrayList<Coordinate>>> side, Node n1, Node n2, BufferedImage image) {
        int x1 = n1.c.x;
        int y1 = n1.c.y;
        int x2 = n2.c.x;
        int y2 = n2.c.y;
        side.get(n1).add(getLeft(n1.type, x1, y1, image));
        side.get(n1).add(getBot(n1.type, x1, y1, image));
        side.get(n2).add(getRight(n2.type, x2, y2, image));
        side.get(n2).add(getTop(n2.type, x2, y2, image));
        if (side.get(n1).get(0).size() > 2) {
            Coordinate c = side.get(n1).get(0).get(side.get(n1).get(0).size() / 2);
            if (isLineOnSW(side, n2, c, image)) {
                return true;
            }

        }
        if (side.get(n1).get(1).size() > 2) {
            Coordinate c = side.get(n1).get(1).get(side.get(n1).get(1).size() / 2);
            if (isLineOnSW(side, n2, c, image)) {
                return true;
            }

        }
        if (side.get(n2).get(0).size() > 2) {
            Coordinate c = side.get(n2).get(0).get(side.get(n2).get(0).size() / 2);
            if (isLineOnNE(side, n1, c, image)) {
                return true;
            }

        }
        if (side.get(n2).get(1).size() > 2) {
            Coordinate c = side.get(n2).get(1).get(side.get(n2).get(1).size() / 2);
            if (isLineOnNE(side, n1, c, image)) {
                return true;
            }

        }
        return false;
    }

    public boolean isLineOnSE(Map<Node, ArrayList<ArrayList<Coordinate>>> side, Node n1, Node n2, BufferedImage image) {
        int x1 = n1.c.x;
        int y1 = n1.c.y;
        int x2 = n2.c.x;
        int y2 = n2.c.y;
        side.get(n1).add(getRight(n1.type, x1, y1, image));
        side.get(n1).add(getBot(n1.type, x1, y1, image));
        side.get(n2).add(getLeft(n2.type, x2, y2, image));
        side.get(n2).add(getTop(n2.type, x2, y2, image));
        if (side.get(n1).get(0).size() > 2) {
            Coordinate c = side.get(n1).get(0).get(side.get(n1).get(0).size() / 2);
            if (isLineOnSE(side, n2, c, image)) {
                return true;
            }

        }
        if (side.get(n1).get(1).size() > 2) {
            Coordinate c = side.get(n1).get(1).get(side.get(n1).get(1).size() / 2);
            if (isLineOnSE(side, n2, c, image)) {
                return true;
            }

        }
        if (side.get(n2).get(0).size() > 2) {
            Coordinate c = side.get(n2).get(0).get(side.get(n2).get(0).size() / 2);
            if (isLineOnNW(side, n1, c, image)) {
                return true;
            }

        }
        if (side.get(n2).get(1).size() > 2) {
            Coordinate c = side.get(n2).get(1).get(side.get(n2).get(1).size() / 2);
            if (isLineOnNW(side, n1, c, image)) {
                return true;
            }

        }
        return false;
    }

//    public void serializeObjectToFile(Serializable object, String filePath) {
//        try (FileOutputStream fileOut = new FileOutputStream(filePath);
//             ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {
//            objectOut.writeObject(object);
//            System.out.println("Object serialized successfully：" + filePath);
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.err.println("serialization failed：" + e.getMessage());
//        }
//    }
//
//    public static ArrayList<Node> deserializeObjectFromFile(String filePath) {
//        ArrayList<Node> node = null;
//        try (FileInputStream fileIn = new FileInputStream(filePath);
//             ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {
//            node = (ArrayList<Node>) objectIn.readObject();
//            System.out.println("Object in " + filePath + " is successful deserialize");
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        return node;
//    }
}
