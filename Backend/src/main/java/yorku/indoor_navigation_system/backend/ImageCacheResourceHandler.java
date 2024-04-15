package yorku.indoor_navigation_system.backend;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import yorku.indoor_navigation_system.backend.utils.FileUtils;

@Configuration
public class ImageCacheResourceHandler implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/result/**")
                .addResourceLocations("file:" + FileUtils.getStaticResPath() + "result/");
    }
}
