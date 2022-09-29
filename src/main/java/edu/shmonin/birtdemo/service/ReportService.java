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
        File folder = new File("");
        for (String file : requireNonNull(folder.list())) {
            if (file.endsWith(".rptdesign")) {
                reports.put(file.replace(".rptdesign", ""),
                        iReportEngine.openReportDesign(folder.getAbsolutePath() + File.separator + file));
            }
        }
    }

    public void generateReport(String reportName, OutputType output) {
        switch (output) {
            case HTML -> generateHTMLReport(reports.get(reportName));
            case XLS -> generateXLSReport(reports.get(reportName));
            default -> throw new IllegalArgumentException("Output type not recognized:" + output);
        }
    }

    private void generateHTMLReport(IReportRunnable iReportRunnable) {
        var reportTask = iReportEngine.createRunAndRenderTask(iReportRunnable);
        try {
            reportTask.run();
        } catch (EngineException e) {
            throw new RuntimeException(e);
        } finally {
            reportTask.close();
        }
    }

    private void generateXLSReport(IReportRunnable iReportRunnable) {

    }

    @Override
    public void destroy() {
        iReportEngine.destroy();
        shutdown();
    }
}