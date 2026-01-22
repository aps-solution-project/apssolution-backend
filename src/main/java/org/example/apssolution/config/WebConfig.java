package org.example.apssolution.config;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path.of(System.getProperty("user.home"), "apssolution", "profile");
        String uploadDirPath = Paths.get(System.getProperty("user.home"), "apssolution", "profile")
                .toAbsolutePath()
                .toString();

        registry.addResourceHandler("/car-images/**")
                .addResourceLocations("file:" + uploadDirPath + "/");
    }
}
