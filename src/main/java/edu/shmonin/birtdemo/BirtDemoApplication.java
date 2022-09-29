package edu.shmonin.birtdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
public class BirtDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BirtDemoApplication.class, args);
    }
}