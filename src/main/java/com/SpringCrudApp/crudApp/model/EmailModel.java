package com.SpringCrudApp.crudApp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailModel {

    private String from;
    private String to;
    private String subject;
    private String body;
    private MultipartFile attachment;

}
