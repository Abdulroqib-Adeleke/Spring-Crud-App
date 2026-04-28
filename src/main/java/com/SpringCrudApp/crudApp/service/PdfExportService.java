package com.SpringCrudApp.crudApp.service;

import com.SpringCrudApp.crudApp.exception.ExcelProcessingException;
import com.SpringCrudApp.crudApp.model.Employee;
import com.SpringCrudApp.crudApp.repository.EmployeeRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfExportService {

    private static final String COMPANY_NAME  = "Adeleke Corporation";
    private static final String REPORT_TITLE  = "Employee Report";
    private static final DateTimeFormatter DT  = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    private static final BaseColor HEADER_BG  = new BaseColor(31, 73, 125);   // dark blue
    private static final BaseColor ROW_ALT    = new BaseColor(220, 230, 241); // light blue
    private static final BaseColor ROW_WHITE  = BaseColor.WHITE;
    private static final BaseColor INACTIVE   = new BaseColor(160, 160, 160); // muted gray
    private static final String[] COL_HEADERS = {
            "ID", "Full Name", "Email", "Department", "Salary", "Date of Joining", "Status"
    };
    private static final float[] COL_WIDTHS = {1f, 2.5f, 3f, 2f, 1.5f, 2f, 1.2f};

    private final EmployeeRepository repo;

    @Transactional(readOnly = true)
    public void export(HttpServletResponse response) {
        List<Employee> employees = repo.findAll();

        String filename = "employee_report_" + System.currentTimeMillis() + ".pdf";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        Document document = new Document(PageSize.A4.rotate(), 30, 30, 40, 40);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
            writer.setPageEvent(new PageFooterEvent());

            document.open();
            writePdf(document, employees);
            document.close();

        } catch (Exception ex) {
            throw new ExcelProcessingException("Failed to generate PDF report", ex);
        }
    }

    private void writePdf(Document document, List<Employee> employees) throws DocumentException {

        Font companyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font titleFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
        Font metaFont    = FontFactory.getFont(FontFactory.HELVETICA, 10);

        Paragraph company = new Paragraph(COMPANY_NAME, companyFont);
        company.setAlignment(Element.ALIGN_CENTER);
        document.add(company);

        Paragraph title = new Paragraph(REPORT_TITLE, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(4);
        document.add(title);

        Paragraph meta = new Paragraph(
                "Generated: " + LocalDateTime.now().format(DT)
                        + "    |    Total Records: " + employees.size(), metaFont);
        meta.setAlignment(Element.ALIGN_CENTER);
        meta.setSpacingBefore(4);
        meta.setSpacingAfter(16);
        document.add(meta);

        PdfPTable table = new PdfPTable(COL_HEADERS.length);
        table.setWidthPercentage(100);
        table.setWidths(COL_WIDTHS);

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        for (String header : COL_HEADERS) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(HEADER_BG);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6);
            table.addCell(cell);
        }

        Font dataFont     = FontFactory.getFont(FontFactory.HELVETICA, 8);
        Font inactiveFont = FontFactory.getFont(FontFactory.HELVETICA, 8);

        int rowNum = 0;
        for (Employee emp : employees) {
            boolean isActive = Boolean.TRUE.equals(emp.getActive());
            BaseColor bg = (rowNum % 2 == 0) ? ROW_WHITE : ROW_ALT;
            Font font = isActive ? dataFont : inactiveFont;

            String fullName = emp.getFirstName() + " " + emp.getLastName();
            String salary   = String.format("$ %,.2f", emp.getSalary());
            String doj      = emp.getDateOfJoining() != null
                    ? emp.getDateOfJoining().toString() : "-";
            String status   = isActive ? "Active" : "Inactive";

            addCell(table, String.valueOf(emp.getId()), bg, font, Element.ALIGN_CENTER);
            addCell(table, fullName, bg, font, Element.ALIGN_LEFT);
            addCell(table, emp.getEmail(), bg, font, Element.ALIGN_LEFT);
            addCell(table, emp.getDepartment(), bg, font, Element.ALIGN_LEFT);
            addCell(table, salary, bg, font, Element.ALIGN_RIGHT);
            addCell(table, doj, bg, font, Element.ALIGN_CENTER);
            addCell(table, status, bg, font, Element.ALIGN_CENTER);

            rowNum++;
        }

        document.add(table);
    }


    private void addCell(PdfPTable table, String text, BaseColor bg, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(4);
        table.addCell(cell);
    }


    private static class PageFooterEvent extends PdfPageEventHelper {

        private PdfTemplate totalPagesTemplate;
        private BaseFont baseFont;

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            totalPagesTemplate = writer.getDirectContent().createTemplate(50, 12);
            try {
                baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false);
            } catch (Exception ignored) {
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            cb.saveState();
            String text = "Page " + writer.getPageNumber() + " of ";
            float textWidth = baseFont.getWidthPoint(text, 8);
            float x = (document.right() + document.left()) / 2;
            float y = document.bottom() - 15;

            cb.beginText();
            cb.setFontAndSize(baseFont, 8);
            cb.setTextMatrix(x - textWidth / 2 - 13, y);
            cb.showText(text);
            cb.endText();

            cb.addTemplate(totalPagesTemplate, x - textWidth / 2 + 13, y);
            cb.restoreState();
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            totalPagesTemplate.beginText();
            try {
                totalPagesTemplate.setFontAndSize(baseFont, 8);
            } catch (Exception ignored) {
            }
            totalPagesTemplate.setTextMatrix(0, 0);
            totalPagesTemplate.showText(String.valueOf(writer.getPageNumber() - 1));
            totalPagesTemplate.endText();
        }
    }

    public byte[] generatePdf() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Employee> employees = repo.findAll();

        Document document = new Document(PageSize.A4.rotate(), 30, 30, 40, 40);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new PageFooterEvent());

            document.open();
            writePdf(document, employees);
            document.close();

            return out.toByteArray();

        } catch (Exception ex) {
            throw new ExcelProcessingException("Failed to generate PDF report", ex);
        }
    }
}
