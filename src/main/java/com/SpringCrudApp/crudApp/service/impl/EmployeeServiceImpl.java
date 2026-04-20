package com.SpringCrudApp.crudApp.service.impl;

import com.SpringCrudApp.crudApp.dto.EmployeeRequestDto;
import com.SpringCrudApp.crudApp.dto.EmployeeResponseDto;
import com.SpringCrudApp.crudApp.dto.ImportResultDto;
import com.SpringCrudApp.crudApp.dto.EmployeePartialUpdateDto;
import com.SpringCrudApp.crudApp.exception.DuplicateEmailException;
import com.SpringCrudApp.crudApp.exception.EmployeeNotFoundException;
import com.SpringCrudApp.crudApp.exception.ExcelProcessingException;
import com.SpringCrudApp.crudApp.exception.InvalidFileFormatException;
import com.SpringCrudApp.crudApp.model.Employee;
import com.SpringCrudApp.crudApp.repository.EmployeeRepository;
import com.SpringCrudApp.crudApp.service.EmployeeService;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    private final Validator validator;

    private final DataFormatter dataFormatter = new DataFormatter();

    @Override
    public EmployeeResponseDto create(@Valid EmployeeRequestDto dto){
        checkEmail(dto.getEmail(), null);
        validateSalary(dto.getDepartment(), dto.getSalary());
        Employee saved = repo.save(mapToEmployee(dto));

        return mapToDto(saved);

    }

    @Override
    @Transactional
    public Page<EmployeeResponseDto> findAll(Pageable pageable, String department, Boolean active) {


        Page<Employee> employeePage = repo.findAll(pageable);
        List<EmployeeResponseDto> dtoResponse = employeePage.stream()
                .map(this::mapToDto)
                .toList();

        return new PageImpl<>(dtoResponse, pageable, employeePage.getTotalElements());

    }

    @Override
    public EmployeeResponseDto findById(Long id) {

        return mapToDto(fetchId(id));
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

        return mapToDto(repo.save(exists));

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

        return mapToDto(repo.save(exists));
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
                .map(this::mapToDto)
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

                EmployeeRequestDto dto = mapRowToDto(row, rowErrors);

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

        assert salary != null;
        if(salary.compareTo(floorSalary) < 0){
            throw new IllegalArgumentException(
                    "Minimum salary for department " + department + " is " + floorSalary);
        }
    }

    private Employee mapToEmployee(EmployeeRequestDto dto) {
        return Employee.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .department(dto.getDepartment())
                .salary(dto.getSalary())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .dateOfJoining(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public EmployeeResponseDto mapToDto(Employee e) {
        return EmployeeResponseDto.builder()
                .id(e.getId())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .email(e.getEmail())
                .department(e.getDepartment())
                .salary(e.getSalary())
                .dateOfJoining(e.getDateOfJoining())
                .active(e.getActive())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
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

    private EmployeeRequestDto mapRowToDto(Row row, List<String> rowErrors) {
        EmployeeRequestDto dto = new EmployeeRequestDto();
        int rowNum = row.getRowNum() + 1;




        dto.setFirstName(dataFormatter.formatCellValue(row.getCell(1)).trim());
        dto.setLastName(dataFormatter.formatCellValue(row.getCell(2)).trim());
        dto.setEmail(dataFormatter.formatCellValue(row.getCell(3)).trim());
        dto.setDepartment(dataFormatter.formatCellValue(row.getCell(4)).trim());
        String deptRaw = dataFormatter.formatCellValue(row.getCell(4));
        log.info("Processing Row {}: Detected Department as '{}'", rowNum, deptRaw);


        try {
            String salaryStr = dataFormatter.formatCellValue(row.getCell(5));
            dto.setSalary(salaryStr.isBlank() ? BigDecimal.ZERO : new BigDecimal(salaryStr));
        } catch (NumberFormatException e) {
            rowErrors.add("Row " + rowNum + ": Invalid salary format");
        }


        Cell dateCell = row.getCell(6);
        if (dateCell != null) {
            try {
                if (DateUtil.isCellDateFormatted(dateCell)) {
                    dto.setDateOfJoining(dateCell.getLocalDateTimeCellValue().toLocalDate());
                } else {
                    String dateStr = dataFormatter.formatCellValue(dateCell).trim();

                    try {
                        dto.setDateOfJoining(LocalDate.parse(dateStr));
                    } catch (DateTimeParseException e) {

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
                        dto.setDateOfJoining(LocalDate.parse(dateStr, formatter));
                    }
                }
            } catch (Exception e) {
                rowErrors.add("Row " + rowNum + ": Invalid date format '" +
                        dataFormatter.formatCellValue(dateCell) + "'. Expected YYYY-MM-DD");
            }
        }


        String activeStr = dataFormatter.formatCellValue(row.getCell(7));
        dto.setActive(activeStr.isBlank() ? true : Boolean.parseBoolean(activeStr));

        return dto;
    }

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default      -> "";
        };
    }

    @Transactional
    public void processRow(EmployeeRequestDto dto) {

        checkEmail(dto.getEmail(), null);
        validateSalary(dto.getDepartment(), dto.getSalary());
        repo.save(mapToEmployee(dto));
    }

}
