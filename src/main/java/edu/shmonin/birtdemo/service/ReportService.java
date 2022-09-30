package edu.shmonin.birtdemo.service;

import edu.shmonin.birtdemo.model.*;
import lombok.*;
import org.eclipse.birt.core.exception.*;
import org.eclipse.birt.report.engine.api.*;
import org.springframework.beans.factory.*;
import org.springframework.stereotype.*;

import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import static java.io.File.*;
import static org.eclipse.birt.core.framework.Platform.*;

@Service
@RequiredArgsConstructor
public class ReportService implements DisposableBean {
    private final IReportEngine iReportEngine;

    public void generateReport(String reportName, OutputType output, HttpServletResponse response) {
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

    public void createXLS(String reportName) {
        try {
            extractDataSets(reportName).forEach((k, v) -> printResults(v));
        } catch (EngineException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, IExtractionResults> extractDataSets(String reportName) throws EngineException {
        var report = iReportEngine.openReportDesign("./reports" + separator + reportName + ".rptdesign");
        var reportTask = iReportEngine.createRunTask(report);
        reportTask.run("./temp/temp.rptdocument");
        var reportDocument = iReportEngine.openReportDocument("./temp/temp.rptdocument");
        var dataExtractionTask = iReportEngine.createDataExtractionTask(reportDocument);
        Map<String, IExtractionResults> resultSets = new HashMap<>();
        for (var resultSet : dataExtractionTask.getResultSetList()) {
            var dataSetName = ((IResultSetItem) resultSet).getResultSetName();
            dataExtractionTask.selectResultSet(dataSetName);
            resultSets.put(dataSetName, dataExtractionTask.extract());
        }
        dataExtractionTask.close();
        return resultSets;
    }

    private void printResults(IExtractionResults iExtractionResults) {//заглушка
        IDataIterator iData;
        try {
            if (iExtractionResults != null) {
                iData = iExtractionResults.nextResultIterator();
                if (iData != null) {
                    var resultMetaData = iData.getResultMetaData();
                    for (int i = 0; i < resultMetaData.getColumnCount(); i++) {
                        System.out.println("col name: " + resultMetaData.getColumnName(i));
                        System.out.println("col type: " + resultMetaData.getColumnTypeName(i));
                    }
                    while (iData.next()) {
                        System.out.println(iData.getValue(0) + " " + iData.getValue(1));
                    }
                    iData.close();
                }
            }
        } catch (BirtException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() {
        iReportEngine.destroy();
        shutdown();
    }
}