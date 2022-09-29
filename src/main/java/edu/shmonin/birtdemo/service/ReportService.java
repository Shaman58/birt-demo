package edu.shmonin.birtdemo.service;

import edu.shmonin.birtdemo.model.OutputType;
import lombok.RequiredArgsConstructor;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.io.File.separator;
import static org.eclipse.birt.core.framework.Platform.shutdown;

@Service
@RequiredArgsConstructor
public class ReportService implements DisposableBean {
    private final IReportEngine iReportEngine;

    public void generateReports(String reportName, OutputType output, HttpServletResponse response) {
        try {
            var report = iReportEngine.openReportDesign("./reports" + separator + reportName + ".rptdesign");
            var reportTask = iReportEngine.createRunAndRenderTask(report);
            response.setContentType(iReportEngine.getMIMEType(output.name()));
            var options = new RenderOption();
            options.setOutputFormat(output.name());
            reportTask.setRenderOption(options);
            options.setOutputStream(response.getOutputStream());
            reportTask.run();
        } catch (EngineException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() {
        iReportEngine.destroy();
        shutdown();
    }
}