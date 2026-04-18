package com.SpringCrudApp.crudApp.service.impl;

import com.SpringCrudApp.crudApp.dto.EmployeeRequestDto;
import com.SpringCrudApp.crudApp.dto.EmployeeResponseDto;
import com.SpringCrudApp.crudApp.dto.ImportResultDto;
import com.SpringCrudApp.crudApp.dto.PartialUpdateDto;
import com.SpringCrudApp.crudApp.exception.DuplicateEmailException;
import com.SpringCrudApp.crudApp.model.Employee;
import com.SpringCrudApp.crudApp.repository.EmployeeRepository;
import com.SpringCrudApp.crudApp.service.EmployeeService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private static final BigDecimal SALARY_DEFAULT_FLOOR = new BigDecimal("30000.00");
    private static final BigDecimal SALARY_INTERN_FLOOR = new BigDecimal("15000.00");

    private final EmployeeRepository repo;
    //private final Validator validator;

    @Override
    public EmployeeResponseDto create(@Valid EmployeeRequestDto dto){

        return null;

    }

    @Override
    public EmployeeResponseDto findById(Long id) {
        return null;
    }

    @Override
    public Page<EmployeeResponseDto> findAll(Pageable pageable, String department, Boolean active) {

        log.debug("Fetching all employee");

        List<Employee> employees = repo.findAll();
        List<EmployeeResponseDto> dtoResponse = employees.stream().map(this::mapToDto).toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtoResponse.size());
        List<EmployeeResponseDto> page = start >
                dtoResponse.size() ? Collections.emptyList() :
                dtoResponse.subList(start,end);

        return new PageImpl<>(page, pageable, dtoResponse.size());

    }

    @Override
    public EmployeeResponseDto update(Long id, EmployeeRequestDto dto) {
        return null;
    }

    @Override
    public EmployeeResponseDto partialUpdate(Long id, PartialUpdateDto dto) {
        return null;
    }

    @Override
    public void softDelete(Long id) {

    }

    @Override
    public void hardDelete(Long id) {

    }

    @Override
    public List<EmployeeResponseDto> findBySalaryRange(BigDecimal min, BigDecimal max) {
        return List.of();
    }

    @Override
    public ImportResultDto importFromExcel(MultipartFile file) {
        return null;
    }

    private Employee mapToEmployee(EmployeeRequestDto dto) {
        return Employee.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .department(dto.getDepartment())
                .salary(dto.getSalary())
                .dateOfJoining(dto.getDateOfJoining())
                .active(dto.getActive() != null ? dto.getActive() : true)
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

}
