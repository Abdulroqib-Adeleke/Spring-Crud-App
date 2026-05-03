package com.SpringCrudApp.crudApp.dto.responseDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportResultDto {

    private int successCount;

    private int failureCount;

    private List<String> errors;

}
