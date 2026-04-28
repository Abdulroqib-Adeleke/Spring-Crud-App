package com.SpringCrudApp.crudApp.service;

import com.SpringCrudApp.crudApp.exception.ExcelProcessingException;
import com.SpringCrudApp.crudApp.model.Employee;
import com.SpringCrudApp.crudApp.repository.EmployeeRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExcelExportService {

    private static final String[] HEADERS = {
            "id", "firstName", "lastName", "email", "department",
            "salary", "dateOfJoining", "active", "createdAt", "updatedAt"
    };
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final EmployeeRepository repo;

    @Transactional(readOnly = true)
    public void export(HttpServletResponse response, String department) {
        List<Employee> employees = repo.findByDepartment(department);

        String filename = "employees_" + System.currentTimeMillis() + ".xlsx";
        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Employees");

            writeSheet(workbook, sheet, employees);

            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();

        } catch (IOException ex) {
            throw new ExcelProcessingException("Failed to write Excel export", ex);
        }
    }

    private XSSFCellStyle buildHeaderStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        return style;
    }

    private XSSFCellStyle buildFillStyle(XSSFWorkbook wb, IndexedColors color) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private XSSFCellStyle buildSalaryStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        DataFormat fmt = wb.createDataFormat();
        style.setDataFormat(fmt.getFormat("#,##0.00"));
        return style;
    }

    private void createCell(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value instanceof String s)        cell.setCellValue(s);
        else if (value instanceof Long l)     cell.setCellValue(l);
        else if (value instanceof Boolean b)  cell.setCellValue(b);
        else if (value != null)               cell.setCellValue(value.toString());
        cell.setCellStyle(style);
    }

    public byte[] generateExcel() {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Employee> employees = repo.findAll();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Employees");

            writeSheet(workbook, sheet, employees);

            workbook.write(out);
        }catch(Exception e){
            throw new ExcelProcessingException("Failed to generate excel report" + e.getMessage());
        }

        return out.toByteArray();
    }

    private void writeSheet(XSSFWorkbook workbook, XSSFSheet sheet, List<Employee> employees) {

        XSSFCellStyle headerStyle  = buildHeaderStyle(workbook);
        XSSFCellStyle whiteStyle   = buildFillStyle(workbook, IndexedColors.WHITE);
        XSSFCellStyle blueStyle    = buildFillStyle(workbook, IndexedColors.LIGHT_TURQUOISE);
        XSSFCellStyle salaryFormat = buildSalaryStyle(workbook);


        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }


        int rowNum = 1;
        for (Employee emp : employees) {

            Row row = sheet.createRow(rowNum);
            XSSFCellStyle rowStyle = (rowNum % 2 == 0) ? blueStyle : whiteStyle;

            createCell(row, 0, emp.getId(),         rowStyle);
            createCell(row, 1, emp.getFirstName(),  rowStyle);
            createCell(row, 2, emp.getLastName(),   rowStyle);
            createCell(row, 3, emp.getEmail(),      rowStyle);
            createCell(row, 4, emp.getDepartment(), rowStyle);


            Cell salaryCell = row.createCell(5);
            salaryCell.setCellValue(emp.getSalary().doubleValue());

            XSSFCellStyle combined = workbook.createCellStyle();
            combined.cloneStyleFrom(salaryFormat);
            combined.setFillForegroundColor(rowStyle.getFillForegroundColor());
            combined.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            salaryCell.setCellStyle(combined);


            createCell(row, 6,
                    emp.getDateOfJoining() != null
                            ? emp.getDateOfJoining().toString() : "", rowStyle);

            createCell(row, 7, emp.getActive(), rowStyle);

            createCell(row, 8,
                    emp.getCreatedAt() != null
                            ? emp.getCreatedAt().format(DT_FMT) : "", rowStyle);

            createCell(row, 9,
                    emp.getUpdatedAt() != null
                            ? emp.getUpdatedAt().format(DT_FMT) : "", rowStyle);

            rowNum++;
        }


        for (int i = 0; i < HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

}
