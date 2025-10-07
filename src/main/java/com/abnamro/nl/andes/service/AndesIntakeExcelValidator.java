package com.abnamro.nl.andes.service;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

@Service
public class AndesIntakeExcelValidator {

    private static final Set<String> REQUIRED = Set.of("Dataset", "Attributes","Data Model", "All Data");

    //private static final Set<String> REQUIRED = Set.of("API Details");

   // private static final Set<String> REQUIRED = Set.of("Dataset", "Attributes", "Data Model", "All Data");

    public boolean validateSheets(MultipartFile file) throws Exception {
        Set<String> found = new HashSet<>();

        try (InputStream is = file.getInputStream(); XSSFWorkbook workbook = new XSSFWorkbook(is)) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                String sheetName = workbook.getSheetName(i);
                String normalized = sheetName == null ? "" : sheetName.trim();
                if (REQUIRED.contains(normalized)) {
                    found.add(normalized);
                }
            }
        }
        return found.containsAll(REQUIRED);
    }


}
