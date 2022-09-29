package edu.shmonin.birtdemo.service;

import edu.shmonin.birtdemo.model.OutputType;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.engine.api.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.eclipse.birt.core.framework.Platform.*;
import static org.eclipse.birt.report.engine.api.IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY;

@Service
public class ReportService implements ApplicationContextAware, DisposableBean {

    private ApplicationContext context;
    private IReportEngine iReportEngine;
    private final Map<String, IReportRunnable> reports = new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @SuppressWarnings("unchecked")
    @PostConstruct
    protected void initialize() throws BirtException {
        var config = new EngineConfig();
        config.getAppContext().put("spring", this.context);
        startup(config);
        var factory = (IReportEngineFactory) createFactoryObject(EXTENSION_REPORT_ENGINE_FACTORY);
        iReportEngine = factory.createReportEngine(config);
        loadReports();
    }

    public void loadReports() throws EngineException {
        var folder = new File("./reports");
        for (String file : requireNonNull(folder.list())) {
            if (file.endsWith(".rptdesign")) {
                reports.put(file.replace(".rptdesign", ""), iReportEngine.openReportDesign(folder.getAbsolutePath() + File.separator + file));
            }
        }
    }

    public void generateReport(String reportName, OutputType output, HttpServletResponse response, HttpServletRequest request) {
        switch (output) {
            case HTML -> generateHTMLReport(reports.get(reportName), response, request);
            case XLS -> generateXLSReport(reports.get(reportName), response);
            default -> throw new IllegalArgumentException("Output type not recognized:" + output);
        }
    }

    @SuppressWarnings("unchecked")
    private void generateHTMLReport(IReportRunnable report, HttpServletResponse response, HttpServletRequest request) {
        var reportTask = iReportEngine.createRunAndRenderTask(report);
        var options = new RenderOption();
        var htmlOptions = new HTMLRenderOption(options);
        htmlOptions.setOutputFormat("html");
        reportTask.setRenderOption(htmlOptions);
        reportTask.getAppContext().put(EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST, request);
        try {
            htmlOptions.setOutputStream(response.getOutputStream());
            reportTask.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            reportTask.close();
        }
    }

    @SuppressWarnings("unchecked")
    private void generateXLSReport(IReportRunnable report, HttpServletResponse response) {
        IRunAndRenderTask reportTask = iReportEngine.createRunAndRenderTask(report);
        response.setContentType(iReportEngine.getMIMEType("xls"));
        IRenderOption options = new RenderOption();
        options.setOutputFileName("report.xlsx");
        options.setOutputFormat("xlsx");
        reportTask.setRenderOption(options);
        try {
            options.setOutputStream(response.getOutputStream());
            reportTask.run();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            reportTask.close();
        }
    }

    @Override
    public void destroy() {
        iReportEngine.destroy();
        shutdown();
    }
}