package org.example.apssolution.config;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/apssolution/profile/**").addResourceLocations(
                Path.of(System.getProperty("user.home"), "apssolution", "profile").toUri().toString()
        );

        registry.addResourceHandler("/apssolution/notices/**")
                .addResourceLocations(
                        Path.of(System.getProperty("user.home"), "apssolution", "notices")
                                .toUri().toString() + "/"
                );
    }
}
