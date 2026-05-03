package com.SpringCrudApp.crudApp.mapper;

import com.SpringCrudApp.crudApp.dto.requestDto.EmployeeRequestDto;
import com.SpringCrudApp.crudApp.dto.responseDto.EmployeeResponseDto;
import com.SpringCrudApp.crudApp.model.Employee;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@NoArgsConstructor
public class EmployeeMapper {

    private final DataFormatter dataFormatter = new DataFormatter();

    public Employee mapToEmployee(EmployeeRequestDto dto) {
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

    public EmployeeRequestDto mapRowToDto(Row row, List<String> rowErrors) {
        EmployeeRequestDto dto = new EmployeeRequestDto();
        int rowNum = row.getRowNum() + 1;

        dto.setFirstName(dataFormatter.formatCellValue(row.getCell(0)).trim());
        dto.setLastName(dataFormatter.formatCellValue(row.getCell(1)).trim());
        dto.setEmail(dataFormatter.formatCellValue(row.getCell(2)).trim());
        dto.setDepartment(dataFormatter.formatCellValue(row.getCell(3)).trim());

        try {
            String salaryStr = dataFormatter.formatCellValue(row.getCell(4)).trim();
            dto.setSalary(salaryStr.isBlank() ? BigDecimal.ZERO : new BigDecimal(salaryStr));
        } catch (NumberFormatException e) {
            rowErrors.add("Row " + rowNum + ": Invalid salary format");
        }

        Cell dateCell = row.getCell(5);
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

        String activeStr = dataFormatter.formatCellValue(row.getCell(6)).trim();
        dto.setActive(activeStr.isBlank() || Boolean.parseBoolean(activeStr));

        return dto;
    }

}
