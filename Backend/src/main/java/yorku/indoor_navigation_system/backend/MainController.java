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
import yorku.indoor_navigation_system.backend.models.Graph;
import yorku.indoor_navigation_system.backend.models.NavigationInformation;
import yorku.indoor_navigation_system.backend.models.Node;
import yorku.indoor_navigation_system.backend.repos.CoordinateRepository;
import yorku.indoor_navigation_system.backend.repos.GraphRepository;
import yorku.indoor_navigation_system.backend.repos.NodeRepository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;

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
            algorithm.connectFloor();
            algorithm.connectBuilding();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load files from classpath.");
        }
        System.out.println("Finished loading files from classpath.");

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
        ClassPathResource mainPage = new ClassPathResource("static/index.html");
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
        Algorithm.quickSort(rooms, 0, rooms.size()-1);
        return rooms;
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
    public ArrayList<String> Navigate(@RequestBody NavigationInformation NI) throws IOException {
        String building1 = NI.name;
        String building2 = NI.name2;
        String start = NI.start.trim();
        String end = NI.end.trim();
        ArrayList<Graph> result = new ArrayList<>(graphRepository.findByName(building1));
        Node s = null;
        System.out.println(start);
        for (Graph g : result) {
            for(Node n : g.getGraph_node()) {
                if (n.getName()!=null&&n.getName().equals(start)) {
                    s = n;
                }
            }
        }
        ArrayList<Graph> result2 = new ArrayList<>(graphRepository.findByName(building2));
        Node e = null;
        for(Graph g : result2) {
            for(Node n : g.getGraph_node()) {
                if (n.getName()!=null&&n.getName().equals(end)) {
                    e = n;
                }
            }
        }
        System.out.println(s);
        System.out.println(e);
        if(s == null || e == null) {
            return null;
        }
        ArrayList<BufferedImage> retImg = algorithm.Navigate( s, e, "src/main/resources/static/result/");
        ArrayList<String> base64Images = new ArrayList<>();
        for (BufferedImage img : retImg) {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                ImageIO.write(img, "png", outputStream);
                base64Images.add(Base64.getEncoder().encodeToString(outputStream.toByteArray()));
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }
        }
        return base64Images;

    }
}


