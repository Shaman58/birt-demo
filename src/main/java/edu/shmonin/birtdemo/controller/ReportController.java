package edu.shmonin.birtdemo.controller;

import edu.shmonin.birtdemo.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static edu.shmonin.birtdemo.model.OutputType.getType;

@Controller
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/report/{name}")
    public void getReport(@PathVariable String name, @RequestParam String output) {
        reportService.generateReport(name, getType(output));
    }
}