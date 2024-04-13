package yorku.indoor_navigation_system.backend.utils;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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
