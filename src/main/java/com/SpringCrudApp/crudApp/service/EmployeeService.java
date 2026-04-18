package com.SpringCrudApp.crudApp.service;

import com.SpringCrudApp.crudApp.dto.EmployeeRequestDto;
import com.SpringCrudApp.crudApp.dto.EmployeeResponseDto;
import com.SpringCrudApp.crudApp.dto.ImportResultDto;
import com.SpringCrudApp.crudApp.dto.PartialUpdateDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface EmployeeService {

    EmployeeResponseDto create(@Valid EmployeeRequestDto dto);

    Page<EmployeeResponseDto> findAll(Pageable pageable, String department, Boolean active);

    EmployeeResponseDto findById(Long id);

    EmployeeResponseDto update(Long id, EmployeeRequestDto dto);

    EmployeeResponseDto partialUpdate(Long id, @Valid PartialUpdateDto dto);

    void softDelete(Long id);

    void hardDelete(Long id);

    List<EmployeeResponseDto> findBySalaryRange(BigDecimal min, BigDecimal max);

    ImportResultDto importFromExcel(MultipartFile file);
}
