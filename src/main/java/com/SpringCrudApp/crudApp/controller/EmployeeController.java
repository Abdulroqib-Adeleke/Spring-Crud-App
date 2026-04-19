package com.SpringCrudApp.crudApp.controller;

import com.SpringCrudApp.crudApp.dto.EmployeeRequestDto;
import com.SpringCrudApp.crudApp.dto.EmployeeResponseDto;
import com.SpringCrudApp.crudApp.service.EmployeeService;
import com.SpringCrudApp.crudApp.service.impl.EmployeeServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Created new employee")
    public ResponseEntity<EmployeeResponseDto> create(
            @Valid @RequestBody EmployeeRequestDto dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));

    }

    @GetMapping
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<EmployeeResponseDto> getAllEmployee(
            @RequestParam(defaultValue = "0")  int     page,
            @RequestParam(defaultValue = "10") int     size,
            @RequestParam(defaultValue = "id") String  sort,
            @RequestParam(required = false)    String  department,
            @RequestParam(required = false)    Boolean active){

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return ResponseEntity.ok((EmployeeResponseDto) service.findAll(pageable, department, active));
    }
}
