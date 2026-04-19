package com.SpringCrudApp.crudApp.service;

import com.SpringCrudApp.crudApp.dto.ImportResultDto;
import lombok.RequiredArgsConstructor;
import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ExcelImportService {

    @Transactional
    public ImportResultDto importResultDto(MultipartFile file){
        if(file == null || file.isEmpty() || !Objects.requireNonNull(file.getOriginalFilename()).endsWith("xlsx")){
            throw new IllegalArgumentException("Only .xlsx files are supported");
        }

        ImportResultDto resultDto = new ImportResultDto();

        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())){
            Sheet sheet = (Sheet) workbook.getSheetAt(0);

            for(int i = 1; i <= sheet.getLastRowNum(); i++){

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
