package edu.shmonin.birtdemo.controller;

import edu.shmonin.birtdemo.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;

import java.util.Map;

import static edu.shmonin.birtdemo.model.OutputType.getType;

@Controller
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/report/{name}")
    public void getReport(HttpServletResponse response, @PathVariable String name, @RequestParam Map<String, Object> allQueryParams) {
        reportService.generateReport(name, allQueryParams, response);
    }

    @GetMapping("/data/{name}")
    public void getXlsx(HttpServletResponse response, @PathVariable String name, @RequestParam Map<String, Object> allQueryParams) {
        reportService.fillXLSTemplate(name, allQueryParams, response);
    }
}