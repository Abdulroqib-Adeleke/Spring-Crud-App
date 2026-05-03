package com.SpringCrudApp.crudApp.dto.responseDto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EmployeeResponseDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private BigDecimal salary;
    private LocalDate dateOfJoining;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
