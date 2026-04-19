package com.SpringCrudApp.crudApp.service.impl;

import com.SpringCrudApp.crudApp.dto.EmployeeRequestDto;
import com.SpringCrudApp.crudApp.dto.EmployeeResponseDto;
import com.SpringCrudApp.crudApp.dto.ImportResultDto;
import com.SpringCrudApp.crudApp.dto.EmployeePartialUpdateDto;
import com.SpringCrudApp.crudApp.exception.DuplicateEmailException;
import com.SpringCrudApp.crudApp.exception.EmployeeNotFoundException;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
    //private final Validator validator;

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

        return mapToDto(repo.save(exists));

    }

    @Override
    public EmployeeResponseDto partialUpdate(Long id, @Valid EmployeePartialUpdateDto dto) {

        Employee exists = fetchId(id);

        if (dto.getSalary() != null) {
            validateSalary(
                    dto.getDepartment() != null ? dto.getDepartment() : exists.getDepartment(),
                    dto.getSalary()
            );
            exists.setSalary(dto.getSalary());
        }
        if (dto.getDepartment() != null) {
            validateSalary(dto.getDepartment(), exists.getSalary());
            exists.setDepartment(dto.getDepartment());
        }
        if (dto.getActive() != null) {
            exists.setActive(dto.getActive());
        }

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
    @Transactional
    public ImportResultDto importFromExcel(MultipartFile file) {

        return null;
    }

    private void checkEmail(String email, Long excludedId) {
        repo.findByEmail(email).ifPresent(existing -> {
            if (!existing.getId().equals(excludedId)) {
                throw new DuplicateEmailException(email);
            }
        });
    }

    private void validateSalary(String department, BigDecimal salary){

        if(department == null || salary == null){

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

}
