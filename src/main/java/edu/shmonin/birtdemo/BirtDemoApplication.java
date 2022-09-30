package edu.shmonin.birtdemo;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.servlet.config.annotation.*;

@SpringBootApplication
public class BirtDemoApplication implements WebMvcConfigurer {
    public static void main(String[] args) {
        SpringApplication.run(BirtDemoApplication.class, args);
    }
}