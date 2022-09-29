package edu.shmonin.birtdemo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class BirtDemoApplication implements WebMvcConfigurer {
    public static void main(String[] args) {
        SpringApplication.run(BirtDemoApplication.class, args);
    }
}