package com.SpringCrudApp.crudApp.service.impl;

import com.SpringCrudApp.crudApp.dto.requestDto.EmailDto;
import com.SpringCrudApp.crudApp.dto.requestDto.EmployeePartialUpdateDto;
import com.SpringCrudApp.crudApp.dto.requestDto.EmployeeRequestDto;
import com.SpringCrudApp.crudApp.dto.responseDto.EmployeeResponseDto;
import com.SpringCrudApp.crudApp.dto.responseDto.ImportResultDto;
import com.SpringCrudApp.crudApp.exception.*;
import com.SpringCrudApp.crudApp.mapper.EmployeeMapper;
import com.SpringCrudApp.crudApp.model.Employee;
import com.SpringCrudApp.crudApp.repository.EmployeeRepository;
import com.SpringCrudApp.crudApp.service.EmployeeService;
import com.SpringCrudApp.crudApp.service.ExcelExportService;
import com.SpringCrudApp.crudApp.service.PdfExportService;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private  static final Set<String> INTERN_DEPARTMENTS = Set.of(
            "PROGRAMMING", "HR", "MARKETING", "TESTING", "CUSTOMER SERVICE"
    );
    private static final BigDecimal SALARY_DEFAULT_FLOOR = new BigDecimal("30000.00");
    private static final BigDecimal SALARY_INTERN_FLOOR = new BigDecimal("15000.00");

    private final EmployeeRepository repo;
    private final EmployeeMapper mapper = new EmployeeMapper();

    private final JavaMailSender mailSender;
    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;

    @Value("${app.mail.default-from}")
    private String defaultFromEmail;

    @Override
    public EmployeeResponseDto create(@Valid EmployeeRequestDto dto){
        checkEmail(dto.getEmail(), null);
        validateSalary(dto.getDepartment(), dto.getSalary());
        Employee saved = repo.save(mapper.mapToEmployee(dto));

        return mapper.mapToDto(saved);

    }

    @Override
    @Transactional
    public Page<EmployeeResponseDto> findAll(Pageable pageable, String department, Boolean active) {


        Page<Employee> employeePage = repo.findAll(pageable);
        List<EmployeeResponseDto> dtoResponse = employeePage.stream()
                .map(mapper::mapToDto)
                .toList();

        return new PageImpl<>(dtoResponse, pageable, employeePage.getTotalElements());

    }

    @Override
    public EmployeeResponseDto findById(Long id) {

        return mapper.mapToDto(fetchId(id));
    }

    @Override
    public EmployeeResponseDto update(Long id, EmployeeRequestDto dto) {

        Employee exists = fetchId(id);
        checkEmail(dto.getEmail(), id);
        validateSalary(dto.getDepartment(), dto.getSalary());

        exists.setFirstName(dto.getFirstName());
        exists.setLastName(dto.getLastName());
        exists.setEmail(dto.getEmail());
        exists.setDepartment(dto.getDepartment());
        exists.setSalary(dto.getSalary());
        exists.setActive(dto.getActive());
        exists.setUpdatedAt(LocalDateTime.now());

        return mapper.mapToDto(repo.save(exists));

    }

    @Override
    public EmployeeResponseDto partialUpdate(Long id, @Valid EmployeePartialUpdateDto dto) {

        Employee exists = fetchId(id);

        BigDecimal newSalary = (dto.getSalary() != null) ? dto.getSalary() : exists.getSalary();
        String newDepartment = (dto.getDepartment() != null) ? dto.getDepartment() : exists.getDepartment();

        if (dto.getSalary() != null || dto.getDepartment() != null) {
            validateSalary(newDepartment, newSalary);
        }
        if(dto.getSalary() != null){
            exists.setSalary(dto.getSalary());
        }
        if (dto.getDepartment() != null) {
            exists.setDepartment(dto.getDepartment());
        }
        if (dto.getActive() != null) {
            exists.setActive(dto.getActive());
        }

        exists.setUpdatedAt(LocalDateTime.now());

        return mapper.mapToDto(repo.save(exists));
    }

    @Override
    public void softDelete(Long id) {

        Employee exists = fetchId(id);
        exists.setActive(false);
        repo.save(exists);

    }

    @Override
    public void hardDelete(Long id) {

        Employee exists = fetchId(id);

        if (exists.getActive().equals(Boolean.TRUE)) {
            throw new IllegalArgumentException(
                    "Cannot hard-delete an active employee (id= " + id + "). Soft-delete employee first");
        }
        repo.deleteById(id);

    }

    @Override
    public List<EmployeeResponseDto> findBySalaryRange(BigDecimal min, BigDecimal max) {

        return repo.findBySalaryRange(min, max)
                .stream()
                .map(mapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ImportResultDto importFromExcel(MultipartFile file) {
        validateXlsxFile(file);
        List<String> errors = new ArrayList<>();
        int successCount = 0;

        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int rowIdx = 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) continue;

                List<String> rowErrors = new ArrayList<>();

                EmployeeRequestDto dto = mapper.mapRowToDto(row, rowErrors);

                if (rowErrors.isEmpty()) {

                    try {
                        processRow(dto);
                        successCount++;
                    } catch (Exception e) {
                        rowErrors.add("Row " + (rowIdx + 1) + ": " + e.getMessage());
                    }
                }
                errors.addAll(rowErrors);
            }
        } catch (IOException e) {
            throw new ExcelProcessingException("File error", e);
        }
        return new ImportResultDto(successCount, errors.size(), errors);
    }

    @Override
    public void sendEmail(EmailDto dto) {

        byte[] pdf = pdfExportService.generatePdf();
        byte[] excel = excelExportService.generateExcel();

        sendEmployeeReport(dto, pdf, excel);

    }


    private void checkEmail(String email, Long excludedId) {
        repo.findByEmail(email).ifPresent(existing -> {
            if (!existing.getId().equals(excludedId)) {
                throw new DuplicateEmailException(email);
            }
        });
    }

    private void validateSalary(String department, BigDecimal salary){

        if (department == null || department.isBlank()) {
            throw new IllegalArgumentException("Department is required");
        }
        if (salary == null) {
            throw new IllegalArgumentException("Salary is required");
        }
        boolean acceptsInterns = INTERN_DEPARTMENTS.contains(
                Objects.requireNonNull(department).toUpperCase());

        BigDecimal floorSalary = acceptsInterns
                ? SALARY_INTERN_FLOOR
                : SALARY_DEFAULT_FLOOR;

        if(salary.compareTo(floorSalary) < 0){
            throw new IllegalArgumentException(
                    "Minimum salary for department " + department + " is " + floorSalary);
        }
    }

    private Employee fetchId(Long id){
        return repo.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    private void validateXlsxFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileFormatException("Uploaded file is empty.");
        }
        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase().endsWith(".xlsx")) {
            throw new InvalidFileFormatException(
                    "Only .xlsx files are supported. Received: " + name);
        }
    }

    @Transactional
    public void processRow(EmployeeRequestDto dto) {

        checkEmail(dto.getEmail(), null);
        validateSalary(dto.getDepartment(), dto.getSalary());
        repo.save(mapper.mapToEmployee(dto));
    }

    public void sendEmployeeReport(EmailDto dto, byte[] pdfByte, byte[] excelByte){

        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,true);

            String fromEmail = resolveFromEmail(dto.getFrom());

            helper.setFrom(fromEmail);
            helper.setTo(dto.getTo());
            helper.setSubject(dto.getSubject());
            helper.setText(dto.getBody(), true);

            helper.addAttachment("employees.pdf", new ByteArrayResource(pdfByte));
            helper.addAttachment("employees.xlsx", new ByteArrayResource(excelByte));

            mailSender.send(message);

        }catch(Exception e){
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    private String resolveFromEmail(String fromEmail){
        if(fromEmail == null ||fromEmail.trim().isEmpty()){
            return defaultFromEmail;
        }
        return defaultFromEmail;
    }

}
