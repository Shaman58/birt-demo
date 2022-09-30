package edu.shmonin.birtdemo.controller;

import edu.shmonin.birtdemo.service.*;
import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.*;

import static edu.shmonin.birtdemo.model.OutputType.*;

@Controller
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/report/{name}")
    public void getReport(HttpServletResponse response, @PathVariable String name, @RequestParam String output) {
        reportService.generateReport(name, getType(output), response);
    }

    @GetMapping("/data/{name}")
    public void getData(@PathVariable String name) {
        reportService.createXLS(name);
    }
}