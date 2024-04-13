package yorku.indoor_navigation_system.backend.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yorku.indoor_navigation_system.backend.models.Node;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Component
public class FileUtils {
    private static String staticResPath;

    @Value("${application.staticres.path}")
    private void setStaticResPath(String staticResPath) {
        this.staticResPath = staticResPath;
    }

    public static String getStaticResPath() {
        return staticResPath.replace("\\", "/").replaceAll("/$", "") + "/";
    }

    public static String getFileName(String buildingName, int floor) {
        return buildingName + "_" + floor + ".jpg";
    }

    public static String getResultFileName(Node start, Node end) {
        return StringUtils.sha256Hash(String.join("\0",
                start.building,
                String.valueOf(start.floor),
                String.valueOf(start.type),
                start.name,
                start.position,
                end.building,
                String.valueOf(end.floor),
                String.valueOf(end.type),
                end.name,
                end.position
        ));
    }

    public static boolean fileExists(String path) {
        return new File(path).exists();
    }

    public static Date getLastAccessTime(String path) {
        return new Date(new File(path).lastModified());
    }

    public static void updateLastAccessTime(String path) {
        new File(path).setLastModified(System.currentTimeMillis());
    }

    public static List<File> getOldestFiles(String path, int count) {
        File[] files = new File(path).listFiles();
        if (files == null)
            return null;
        List<File> oldestFiles = new ArrayList<>();
        Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        for (int i = 0; i < Math.min(count, files.length); i++)
            oldestFiles.add(files[i]);
        return oldestFiles;
    }

    public static String getMapPath(String fileName) {
        return "map/" + fileName;
    }

    public static String getGraphPath(String fileName) {
        return "graph/" + fileName;
    }

    public static BufferedImage openResImage(String relPath) throws IOException {
        return ImageIO.read(new File(FileUtils.getStaticResPath() + relPath));
    }
}
