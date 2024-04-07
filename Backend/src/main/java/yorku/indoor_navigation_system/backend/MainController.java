package yorku.indoor_navigation_system.backend;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

@CrossOrigin
@RestController
@RequestMapping("/Main")
public class MainController {

    @Autowired
    private GraphRepository graphRepository;
    @Autowired
    private NodeRepository nodeRepository;
    @Autowired
    private CoordinateRepository coordinateRepository;
    @Autowired
    private Algorithm algorithm;
    @Autowired
    private ResourceLoader resourceLoader;

    @GetMapping("/GetBuildingList")
    public ArrayList<Graph> GetBuildingList() {
        ArrayList<Graph> buildingList = new ArrayList<>();
        for (Graph s : graphRepository.findAll()) {
            boolean flag = true;
            for (Graph t : buildingList) {
                if (t.name.equals(s.name) && t.floor == s.floor) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                Graph temp = new Graph();
                temp.name = s.name;
                temp.floor = s.floor;
                buildingList.add(temp);
            }
        }

        return buildingList;
    }

    @GetMapping("/SetPageOld")
    public void SetPageV1() {

        String name = "CLH";
        int floor = 1;
//        String serializePath = "src/main/resources/graph/clhLevel1.ser";
//
//        ArrayList<Node> Nodes = Algorithm.deserializeObjectFromFile(serializePath);
        String fileName = FileUtils.getFileName(name, floor);
        algorithm.BuildGraph(FileUtils.getGraphPath(fileName), FileUtils.getMapPath(fileName), name, floor);

        floor = 2;
        fileName = FileUtils.getFileName(name, floor);
        algorithm.BuildGraph(FileUtils.getGraphPath(fileName), FileUtils.getMapPath(fileName), name, floor);
    }

    @GetMapping("/SetPage")
    public void SetPageV2() {
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:static/json/*");

            for (Resource resource : resources) {
                File file = resource.getFile();
                System.out.println(file.getName());
                System.out.println(Algorithm.convertFileToString(file));
                algorithm.BuildGraphV2(Algorithm.convertFileToString(file));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load files from classpath.");
        }

    }

    @GetMapping("/Reset")
    public String Reset(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (graphRepository.count() > 0) {
            graphRepository.deleteAll();
        }
        if (nodeRepository.count() > 0) {
            nodeRepository.deleteAll();
        }
        if (coordinateRepository.count() > 0) {
            coordinateRepository.deleteAll();
        }
        return "Reset successfully ";
    }

    @GetMapping("/MainPage")
    public void MainPage(HttpServletResponse response) throws IOException {
        ClassPathResource mainPage = new ClassPathResource("Indoor navigation system.html");
        byte[] contentBytes = FileCopyUtils.copyToByteArray(mainPage.getInputStream());
        String htmlContent = new String(contentBytes, "UTF-8");
        response.setContentType("text/html");
        response.getWriter().write(htmlContent);
    }

    @PostMapping("/GetRooms")
    public ArrayList<String> GetRooms(@RequestBody NavigationInformation NI) {
        String name = NI.name;
        Integer floor = NI.floor;
        ArrayList<Graph> result = new ArrayList<>(graphRepository.findByNameAndFloor(name, floor));
        ArrayList<String> rooms = new ArrayList<>();

        for(Node n : result.get(0).getGraph_node()) {
            if(n.getName()!=null) {
                rooms.add(n.getName());
            }
        }
        quickSort(rooms, 0, rooms.size()-1);
        return rooms;
    }

    static void quickSort(ArrayList<String> arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }

    static int partition(ArrayList<String> arr, int low, int high) {
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
    static int compareRooms(String room1, String room2) {
        int roomNumber1 = Integer.parseInt(room1.replaceAll("[^0-9]", ""));
        int roomNumber2 = Integer.parseInt(room2.replaceAll("[^0-9]", ""));
        if (roomNumber1 == roomNumber2) {
            return room1.replaceAll("[0-9]", "").compareTo(room2.replaceAll("[0-9]", ""));
        }
        return roomNumber1 - roomNumber2;
    }

    @PostMapping("/Load")
    public String Load(@RequestBody NavigationInformation NI) throws IOException {
        String name = NI.name;
        Integer floor = NI.floor;

        String imagePath = "classpath:static/map/" + name + "_" + floor + ".jpg";
        Resource resource = resourceLoader.getResource(imagePath);
        byte[] imageBytes;
        try (InputStream imageInputStream = resource.getInputStream()) {
            imageBytes = IOUtils.toByteArray(imageInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);
        return base64Image;


    }

    @PostMapping("/Navigate")
    public String Navigate(@RequestBody NavigationInformation NI) throws IOException {
        String name = NI.name;
        Integer floor = NI.floor;
        String start = NI.start;
        String end = NI.end;
        ArrayList<Graph> result = new ArrayList<>(graphRepository.findByNameAndFloor(name, floor));
        Node s = null;
        for(Node n : result.get(0).getGraph_node()) {
            if (n.getName()!=null&&n.getName().equals(start)) {
                s = n;
            }
        }
        Node e = null;
        for(Node n : result.get(0).getGraph_node()) {
            if (n.getName()!=null&&n.getName().equals(end)) {
                e = n;
            }
        }
        if(s == null || e == null) {
            return "";
        }
        BufferedImage retImg = algorithm.Navigate(result.get(0), s, e, "src/main/resources/static/result/");

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(retImg, "png", outputStream);
            return java.util.Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException ex) {
            ex.printStackTrace();
            return "";
        }
    }
}


