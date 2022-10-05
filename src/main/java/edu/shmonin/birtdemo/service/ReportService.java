package edu.shmonin.birtdemo.service;

import edu.shmonin.birtdemo.model.OutputType;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IExtractionResults;
import org.eclipse.birt.report.engine.api.IParameterDefn;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IResultSetItem;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.io.File.separator;
import static org.apache.poi.ss.usermodel.WorkbookFactory.create;
import static org.eclipse.core.internal.content.ContentTypeManager.shutdown;

@Service
@RequiredArgsConstructor
public class ReportService implements DisposableBean {
    private final IReportEngine iReportEngine;

    public void generateReport(String reportName, Map<String, Object> allQueryParams, HttpServletResponse response) {
        try {
            var report = iReportEngine.openReportDesign("./reports" + separator + reportName + ".rptdesign");
            var reportTask = iReportEngine.createRunAndRenderTask(report);
            var params = extractParameters(allQueryParams, iReportEngine.createGetParameterDefinitionTask(report).getParameterDefns(true));
            reportTask.setParameterValues(params);
            response.setContentType(iReportEngine.getMIMEType(OutputType.HTML.name()));
            var options = new RenderOption();
            options.setOutputFormat(OutputType.HTML.name());
            reportTask.setRenderOption(options);
            options.setOutputStream(response.getOutputStream());
            reportTask.run();
        } catch (EngineException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void fillXLSTemplate(String reportName, Map<String, Object> allQueryParams, HttpServletResponse response) {
        try (var inputStream = new FileInputStream("demo-template.xlsx")) {
            var workbook = create(inputStream);
            var sheet = workbook.getSheetAt(0);
            var resultData = extractDataSets(reportName, allQueryParams);
            var targetCells = new ArrayList<Cell>();
            getTargetCells(targetCells, sheet, resultData);
            for (Cell targetCell : targetCells) {
                writeDataSet(sheet, targetCell, resultData.get((targetCell.getRichStringCellValue().toString())));
            }
            response.setHeader("Content-Disposition", "attachment; filename=test.xlsx");
            workbook.write(response.getOutputStream());
        } catch (IOException | BirtException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> extractParameters(Map<String, Object> allQueryParams, Collection parameterDefns) {
        var parameterTypes = new HashMap<String, Object>();
        for (Object paramDefn : parameterDefns) {
            var param = (IScalarParameterDefn) paramDefn;
            switch (param.getDataType()) {
                case IParameterDefn.TYPE_STRING ->
                        parameterTypes.put(param.getName(), allQueryParams.get(param.getName()).toString());
                case IParameterDefn.TYPE_DATE ->
                        parameterTypes.put(param.getName(), Date.valueOf(LocalDate.parse((CharSequence) allQueryParams.get(param.getName()))));
                case IParameterDefn.TYPE_INTEGER ->
                        parameterTypes.put(param.getName(), Integer.valueOf((String) allQueryParams.get(param.getName())));
                case IParameterDefn.TYPE_DECIMAL ->
                        parameterTypes.put(param.getName(), Double.valueOf((String) allQueryParams.get(param.getName())));
            }
        }
        return parameterTypes;
    }

    private void getTargetCells(List<Cell> targetCells, Sheet sheet, Map<String, IExtractionResults> resultData) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                var cellType = cell.getCellType().toString();
                if (cellType.equals("STRING") && resultData.containsKey(cell.getRichStringCellValue().toString())) {
                    targetCells.add(cell);
                }
            }
        }
    }

    private void writeDataSet(Sheet sheet, Cell target, IExtractionResults data) throws BirtException {
        var columnAddress = target.getAddress().getColumn();
        var rowAddress = target.getAddress().getRow();
        var iData = data.nextResultIterator();
        if (iData != null) {
            var columnCount = iData.getResultMetaData().getColumnCount();
            while (iData.next()) {
                var row = sheet.createRow(rowAddress++);
                for (int i = 0; i < columnCount; i++) {
                    var cell = row.createCell(columnAddress + i);
                    if (iData.getResultMetaData().getColumnTypeName(i).equals("Float")) {
                        cell.setCellValue((Double) iData.getValue(i));
                    } else {
                        cell.setCellValue(iData.getValue(i).toString());
                    }
                }
            }
            iData.close();
        }
    }

    private Map<String, IExtractionResults> extractDataSets(String reportName, Map<String, Object> allQueryParams) throws BirtException {
        var report = iReportEngine.openReportDesign("./reports" + separator + reportName + ".rptdesign");
        var reportTask = iReportEngine.createRunTask(report);
        var params = extractParameters(allQueryParams, iReportEngine.createGetParameterDefinitionTask(report).getParameterDefns(true));
        reportTask.setParameterValues(params);
        reportTask.run("./temp/temp.rptdocument");
        var reportDocument = iReportEngine.openReportDocument("./temp/temp.rptdocument");
        var dataExtractionTask = iReportEngine.createDataExtractionTask(reportDocument);
        Map<String, IExtractionResults> resultSets = new HashMap<>();
        for (var resultSet : dataExtractionTask.getResultSetList()) {
            var dataSetName = ((IResultSetItem) resultSet).getResultSetName();
            dataExtractionTask.selectResultSet(dataSetName);
            resultSets.put("[" + dataSetName + "]", dataExtractionTask.extract());
        }
        dataExtractionTask.close();
        return resultSets;
    }

    @Override
    public void destroy() {
        iReportEngine.destroy();
        shutdown();
    }
}