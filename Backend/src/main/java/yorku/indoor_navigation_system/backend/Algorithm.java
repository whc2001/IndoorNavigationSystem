package yorku.indoor_navigation_system.backend;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import yorku.indoor_navigation_system.backend.models.Coordinate;
import yorku.indoor_navigation_system.backend.models.Draw;
import yorku.indoor_navigation_system.backend.models.Graph;
import yorku.indoor_navigation_system.backend.models.Node;
import yorku.indoor_navigation_system.backend.repos.CoordinateRepository;
import yorku.indoor_navigation_system.backend.repos.GraphRepository;
import yorku.indoor_navigation_system.backend.repos.NodeRepository;
import yorku.indoor_navigation_system.backend.utils.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public static String convertFileToString(File file) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }

            bufferedReader.close();
            inputStreamReader.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }



    public ArrayList<BufferedImage> Navigate(Node start, Node des, String resultPath) {
        System.out.println("Start navigate from " + start.building + ":" +start.name + " to " + des.building + ":" + des.name);
        ArrayList<BufferedImage> result = new ArrayList<BufferedImage>();
        if(start.building.equals(des.building)&&start.floor!=des.floor) {
            ArrayList<Graph> g = (ArrayList<Graph>) graphRepository.findByNameAndFloor(start.building, start.floor);
            ArrayList<Node> AllNodes = new ArrayList<Node>();
            AllNodes.addAll(g.get(0).getGraph_node());
            g = (ArrayList<Graph>) graphRepository.findByNameAndFloor(start.building, des.floor);
            AllNodes.addAll(g.get(0).getGraph_node());
            ArrayList<Node> Route = calculateRoute(AllNodes, start, des, false, null);

            System.out.println("Route: "+Route);
            ArrayList<Node> Route1 = new ArrayList<>();
            ArrayList<Node> Route2 = new ArrayList<>();
            Node p = Route.get(0);
            Route1.add(p);
            boolean flag = false;
            for(int i = 1;i<Route.size();i++) {
                if((p.type==4&&Route.get(i).type==4)||(p.type==1&&Route.get(i).type==1)) {
                    flag = true;
                }
                if(flag) {
                    Route2.add(Route.get(i));
                }else{
                    Route1.add(Route.get(i));
                }
                p = Route.get(i);
            }

            Draw d1 = null;
            Draw d2 = null;
            System.out.println("Route1: "+Route1);
            System.out.println("Route2: "+Route2);
            try {
                d1 = new Draw(Route1, (BufferedImage) FileUtils.openResImage(FileUtils.getMapPath(FileUtils.getFileName(start.building, start.floor))),"Start","Go to floor "+des.floor);
                d1.drawRoute();
                d2 = new Draw(Route2, (BufferedImage) FileUtils.openResImage(FileUtils.getMapPath(FileUtils.getFileName(start.building, des.floor))),"Start","Destination");
                d2.drawRoute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            File outputFile = new File(resultPath+ FileUtils.getFileName(start.building, start.floor) + "_result.png");
            try {
                ImageIO.write(d1.getImage(), "png", outputFile);
                System.out.println("image save success！");
            } catch (IOException e) {
                System.out.println("image save success fail：" + e.getMessage());
            }
            result.add(d1.getImage());
            outputFile = new File(resultPath+ FileUtils.getFileName(start.building, des.floor) + "_result.png");
            try {
                ImageIO.write(d2.getImage(), "png", outputFile);
                System.out.println("image save success！");
            } catch (IOException e) {
                System.out.println("image save success fail：" + e.getMessage());
            }
            result.add(d2.getImage());
            return result;
        }else if(start.building.equals(des.building)&&start.floor==des.floor) {
            ArrayList<Graph> g = (ArrayList<Graph>) graphRepository.findByNameAndFloor(start.building, start.floor);
            ArrayList<Node> AllNodes = new ArrayList<Node>();
            AllNodes.addAll(g.get(0).getGraph_node());
            ArrayList<Node> Route = calculateRoute(AllNodes, start, des, false, null);
            System.out.println("Route: "+Route);
            Draw d = null;
            try {
                d = new Draw(Route, (BufferedImage) FileUtils.openResImage(FileUtils.getMapPath(FileUtils.getFileName(start.building, start.floor))),"Start","Destination");
                d.drawRoute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            File outputFile = new File(resultPath+ FileUtils.getFileName(start.building, start.floor) + "_result.png");
            try {
                ImageIO.write(d.getImage(), "png", outputFile);
                System.out.println("image save success！");
            } catch (IOException e) {
                System.out.println("image save success fail：" + e.getMessage());
            }
            result.add(d.getImage());
            return result;

        }else{
            ArrayList<Node> AllNodes = new ArrayList<Node>();
            ArrayList<Graph> g = (ArrayList<Graph>) graphRepository.findByNameAndFloor(start.building, start.floor);
            AllNodes.addAll(g.get(0).getGraph_node());
            g = (ArrayList<Graph>) graphRepository.findByNameAndFloor(des.building, des.floor);
            AllNodes.addAll(g.get(0).getGraph_node());
            g = (ArrayList<Graph>) graphRepository.findByNameAndFloor("Campus",0);
            AllNodes.addAll(g.get(0).getGraph_node());
            Map<String,ArrayList<Integer>> m = new HashMap<>();
            m.put(start.building,new ArrayList<Integer>());
            m.put(des.building,new ArrayList<Integer>());
            m.get(start.building).add(start.floor);
            m.get(des.building).add(des.floor);
            m.put("Campus",new ArrayList<Integer>());
            m.get("Campus").add(0);
            if(start.floor!=1){
                g = (ArrayList<Graph>) graphRepository.findByNameAndFloor(start.building, 1);
                AllNodes.addAll(g.get(0).getGraph_node());
                m.get(start.building).add(1);

            }
            if(des.floor!=1){
                g = (ArrayList<Graph>) graphRepository.findByNameAndFloor(des.building, 1);
                AllNodes.addAll(g.get(0).getGraph_node());
                m.get(des.building).add(1);
            }
            ArrayList<Node> Route = calculateRoute(AllNodes, start, des, true, m);
            System.out.println("Route: "+Route);

            ArrayList<Node> RouteT = new ArrayList<>();
            Node p = Route.get(0);
            RouteT.add(p);
            int count = 0;
            for(int i = 1;i<Route.size();i++) {
                if((p.type==4&&Route.get(i).type==4)||(p.type==1&&Route.get(i).type==1)||(p.type==5&&Route.get(i).type==5)){
                    String s = "";
                    if(p.type==4||p.type==1) {
                        s = "Go to floor "+Route.get(i).floor;
                    }else if(p.type==5&&count==0) {
                        s = "Go to Campus";
                        count++;
                    }else if(p.type==5&&count==1) {
                        s = "Go into "+Route.get(i).building;
                    }
                    System.out.println("RouteT: "+RouteT);
                    Draw d = null;
                    try {
                        System.out.println(FileUtils.getMapPath(FileUtils.getFileName(p.building, p.floor)));
                        d = new Draw(RouteT, (BufferedImage) FileUtils.openResImage(FileUtils.getMapPath(FileUtils.getFileName(p.building, p.floor))),"Start",s);
                        d.drawRoute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    File outputFile = new File(resultPath+ FileUtils.getFileName(p.building, p.floor));
                    try {
                        ImageIO.write(d.getImage(), "png", outputFile);
                        System.out.println("image save success！");
                    } catch (IOException e) {
                        System.out.println("image save success fail：" + e.getMessage());
                    }
                    result.add(d.getImage());
                    RouteT = new ArrayList<>();
                }
                RouteT.add(Route.get(i));
                p = Route.get(i);
            }
            System.out.println("RouteT: "+RouteT);
            Draw d = null;
            try {
                d = new Draw(RouteT, (BufferedImage) FileUtils.openResImage(FileUtils.getMapPath(FileUtils.getFileName(des.building, des.floor))),"Start","Destination");
                d.drawRoute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            File outputFile = new File(resultPath+ FileUtils.getFileName(des.building, des.floor));
            try {
                ImageIO.write(d.getImage(), "png", outputFile);
                System.out.println("image save success！");
            } catch (IOException e) {
                System.out.println("image save success fail：" + e.getMessage());
            }
            result.add(d.getImage());
            return result;
        }
    }

    public ArrayList<Node> calculateRoute(ArrayList<Node> graph, Node start, Node des, boolean type,Map<String,ArrayList<Integer>> m) {
        Map<Node, Double> distance = new HashMap<Node, Double>();
        Map<Node, ArrayList<Node>> route = new HashMap<Node, ArrayList<Node>>();
        ArrayList<Node> tmpNodeList = graph;
        for (Node n : graph) {
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
                        if(type){
                            if(checkNode(n,m)){
                                pq.add(n);
                                if (distance.get(current) + NodeDisCalculate(current, n) < distance.get(n)) {
                                    distance.replace(n, distance.get(current) + NodeDisCalculate(current, n));
                                    ArrayList<Node> tempList2 = (ArrayList<Node>) route.get(current).clone();
                                    tempList2.add(n);
                                    route.replace(n, tempList2);
                                }
                            }
                        }else{
                            if(n.floor==start.floor||n.floor==des.floor){
                                pq.add(n);
                                if (distance.get(current) + NodeDisCalculate(current, n) < distance.get(n)) {
                                    distance.replace(n, distance.get(current) + NodeDisCalculate(current, n));
                                    ArrayList<Node> tempList2 = (ArrayList<Node>) route.get(current).clone();
                                    tempList2.add(n);
                                    route.replace(n, tempList2);
                                }
                            }
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

    public boolean checkNode(Node n, Map<String,ArrayList<Integer>> m){
        for(String s: m.keySet()){
            if(n.building.equals(s)&&m.get(s).contains(n.floor)){
                return true;
            }
        }
        return false;
    }

    public double NodeDisCalculate(Node n1, Node n2) {
        if((n1.type==4&&n2.type==4||n1.type==1&&n2.type==1||n1.type==5&&n2.type==5)&&n1.position.equals(n2.position)){
            return 0;
        }
        return Math.sqrt((n1.c.x - n2.c.x) * (n1.c.x - n2.c.x) + (n1.c.y - n2.c.y) * (n1.c.y - n2.c.y));
    }



    public void BuildGraphV2(String json) {
        Graph g = new Graph();
        g.graph_node = new ArrayList<>();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> myMap = new Gson().fromJson(json, type);
        g.name = (String) myMap.get("building");
        if(((String) myMap.get("floor")).equals("B")){
            g.floor = 0;
        } else {
            g.floor = Integer.parseInt((String) myMap.get("floor"));
        }
        if(graphRepository.findByNameAndFloor(g.name, g.floor).size() != 0){
            return;
        }
        ArrayList<?> nodes = (ArrayList<?>) myMap.get("nodes");
        Map<Double,Node> finder = new HashMap<Double,Node>();
        for(Object node : nodes) {
            Map m = (Map<?, ?>) node;
            Node n1 = new Node();
            n1.type = ((Double)m.get("type")).intValue();
            n1.nodeId = (double) m.get("id");
            n1.floor = g.floor;
            n1.building = g.name;
            Map<String, Object> coord = (Map<String, Object>) m.get("coord");
            Coordinate c = new Coordinate(((Double)(Double.parseDouble((String)coord.get("x")))).intValue(), ((Double)(Double.parseDouble((String)coord.get("y")))).intValue());
            coordinateRepository.save(c);
            n1.c = c;
            if(m.get("data") != null) {
                if(n1.type == 1||n1.type == 4||n1.type == 5) {
                    n1.position = (String) m.get("data");
                } else {
                    n1.name = (String) m.get("data");
                    if(n1.name.equals("?")){
                        n1.name = "1";
                    }
                }
            }
            finder.put(n1.nodeId, n1);
            nodeRepository.save(n1);
            g.graph_node.add(n1);
        }
        for(Object node : nodes){
            Map m = (Map<?, ?>) node;
            Node n1 = finder.get((double) m.get("id"));
            for(double d: (ArrayList<Double>)((Map<?, ?>) node).get("adjacents")) {
                n1.Nodes.add(finder.get(d));
            }
        }
        graphRepository.save(g);
    }

    public static void quickSort(ArrayList<String> arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }

    public static int partition(ArrayList<String> arr, int low, int high) {
        String pivot = arr.get(high);

        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (compareRooms(arr.get(j), pivot) < 0) {
                i++;

                String temp = arr.get(i);
                arr.set(i, arr.get(j));
                arr.set(j, temp);
            }
        }
        String temp = arr.get(i + 1);
        arr.set(i + 1, arr.get(high));
        arr.set(high, temp);

        return i + 1;
    }
    public static int compareRooms(String room1, String room2) {

        return room1.compareTo(room2);
    }

    public void connectFloor() {
        List<String> BuildingList =  graphRepository.findAllDistinctNames();
        System.out.println(BuildingList);
        for (String building : BuildingList) {
            List<Integer> FloorList = graphRepository.findAllFloorsByBuilding(building);
            System.out.println(FloorList);
            Map<String,ArrayList<Node>> stairs = new HashMap<>();
            Map<String,ArrayList<Node>> elevator = new HashMap<>();
            for (int floor : FloorList) {
                Graph g = graphRepository.findByNameAndFloor(building, floor).get(0);
                for (Node n : g.graph_node) {
                    if (n.type == 1) {
                        if(stairs.get(n.position) == null){
                            stairs.put(n.position,new ArrayList<Node>());
                        }else{
                            for(Node s: stairs.get(n.position)){
                                s.Nodes.add(n);
                                n.Nodes.add(s);
                                nodeRepository.save(s);
                                nodeRepository.save(n);
                            }
                        }
                        stairs.get(n.position).add(n);
                        System.out.println(building+" "+floor+" "+n);
                    }
                    if (n.type == 4) {
                        if(elevator.get(n.position) == null){
                            elevator.put(n.position,new ArrayList<Node>());
                        }else{
                            for(Node s: elevator.get(n.position)){
                                s.Nodes.add(n);
                                n.Nodes.add(s);
                                nodeRepository.save(s);
                                nodeRepository.save(n);
                            }
                        }
                        elevator.get(n.position).add(n);
                        System.out.println(building+floor+n);
                    }
                }
            }
        }


    }

    public void connectBuilding(){
        List<String> BuildingList =  graphRepository.findAllDistinctNames();
        Map<String,ArrayList<Node>> exit = new HashMap<>();
        for (String building : BuildingList) {
            if(building.equals("Campus")){
                continue;
            }
            Graph g = graphRepository.findByNameAndFloor(building, 1).get(0);
            for (Node n : g.graph_node) {
                if (n.type == 5) {
                    exit.put(n.position,new ArrayList<Node>());
                    exit.get(n.position).add(n);
                }
            }
        }
        Graph g = graphRepository.findByNameAndFloor("Campus", 0).get(0);
        for (Node n : g.graph_node) {
            if (n.type == 5) {
                Node s = exit.get(n.position).get(0);
                s.Nodes.add(n);
                n.Nodes.add(s);
                nodeRepository.save(s);
                nodeRepository.save(n);
            }
        }
    }

}
