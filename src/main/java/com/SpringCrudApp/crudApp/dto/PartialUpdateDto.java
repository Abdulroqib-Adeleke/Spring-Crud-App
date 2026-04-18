package com.SpringCrudApp.crudApp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartialUpdateDto {

    @NotBlank
    @Size(max = 100)
    private String department;

    @NotNull
    @DecimalMin("o.oo")
    private BigDecimal salary;

    @NotNull
    private Boolean active;

}
