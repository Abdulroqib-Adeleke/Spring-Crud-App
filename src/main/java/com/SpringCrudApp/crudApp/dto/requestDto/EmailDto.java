package com.SpringCrudApp.crudApp.dto.requestDto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class EmailDto {

    private String from;
    private String to;
    private String subject;
    private String body;

}
