package com.SpringCrudApp.crudApp.controller;

import com.SpringCrudApp.crudApp.dto.requestDto.EmailDto;
import com.SpringCrudApp.crudApp.dto.requestDto.EmployeePartialUpdateDto;
import com.SpringCrudApp.crudApp.dto.requestDto.EmployeeRequestDto;
import com.SpringCrudApp.crudApp.dto.responseDto.EmployeeResponseDto;
import com.SpringCrudApp.crudApp.dto.responseDto.ImportResultDto;
import com.SpringCrudApp.crudApp.service.EmployeeService;
import com.SpringCrudApp.crudApp.service.ExcelExportService;
import com.SpringCrudApp.crudApp.service.PdfExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService service;
    private final ExcelExportService excelExportService;
    private  final PdfExportService pdfExportService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Created new employee")
    public ResponseEntity<EmployeeResponseDto> create(
            @Valid @RequestBody EmployeeRequestDto dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));

    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<EmployeeResponseDto> getAllEmployee(
            @RequestParam(defaultValue = "0")  int     page,
            @RequestParam(defaultValue = "10") int     size,
            @RequestParam(defaultValue = "id") String  sort,
            @RequestParam(required = false)    String  department,
            @RequestParam(required = false)    Boolean active){

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return service.findAll(pageable, department, active);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<EmployeeResponseDto> findById(@PathVariable Long id){

        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<EmployeeResponseDto> update(
            @PathVariable Long id, @Valid @RequestBody EmployeeRequestDto dto){

        return ResponseEntity.ok(service.update(id, dto));
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<EmployeeResponseDto> partialUpdate(
            @PathVariable Long id, @Valid @RequestBody EmployeePartialUpdateDto dto){

        return ResponseEntity.ok(service.partialUpdate(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> softDelete(@PathVariable Long id){

        service.softDelete(id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/hard")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> hardDelete(@PathVariable Long id){

        service.hardDelete(id);

        return ResponseEntity.noContent().build();

    }

    @GetMapping("/salary-range")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<EmployeeResponseDto>> salaryRange(
            @Parameter(description = "Minimum salary") @RequestParam BigDecimal min,
            @Parameter(description = "Maximum salary") @RequestParam BigDecimal max){

        return ResponseEntity.ok(service.findBySalaryRange(min, max));
    }

    @PostMapping(value = "/import", consumes = "multipart/form-data")
    @Operation(summary = "Import employees from .xlsx file")
    public ResponseEntity<ImportResultDto> importFromExcel(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.importFromExcel(file));
    }

    @GetMapping("/export/excel")
    @Operation(summary = "Download employee data as Excel (.xlsx)")
    public void exportToExcel(
            HttpServletResponse response,
            @RequestParam(required = false) String  department) {
        excelExportService.export(response, department);
    }

    @GetMapping("/export/pdf")
    @Operation(summary = "Download employee report as PDF")
    public void exportToPdf(HttpServletResponse response) {

        pdfExportService.export(response);
    }

    @PostMapping("/sendEmail")
    @Operation(summary = "Send Email with attachment")
    public ResponseEntity<String> sendEmail(@RequestBody EmailDto dto){

        service.sendEmail(dto);
        return ResponseEntity.ok("Report emailed successfully");

    }
}
