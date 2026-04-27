package com.SpringCrudApp.crudApp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailModel {

    private String from;
    private String to;
    private String subject;
    private String body;
    private MultipartFile attachment;

}
