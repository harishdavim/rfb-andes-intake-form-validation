package com.abnamro.nl.andes.controller;

import com.abnamro.nl.andes.service.AndesIntakeExcelValidator;
import com.abnamro.nl.andes.service.TempFileStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Map;


import java.io.File;
import java.util.Map;


@Controller
@Slf4j
public class AndesIntakeFormValidationUploadController {

    private final AndesIntakeExcelValidator excelValidator;
    private final TempFileStore tempFileStore;

    public AndesIntakeFormValidationUploadController(AndesIntakeExcelValidator excelValidator,
                                                     TempFileStore tempFileStore) {
        this.excelValidator = excelValidator;
        this.tempFileStore = tempFileStore;
    }

    // ---- Pages ----
    @GetMapping("/upload")
    public String uploadPage() {
        return "upload"; // templates/upload.html
    }

    @GetMapping("/download")
    public String downloadPage(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "download"; // templates/download.html
    }

    // ---- APIs ----
    @PostMapping("/api/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleUpload(@RequestParam("file") MultipartFile file) {
        try {
            boolean valid = excelValidator.validateSheets(file);
            if (!valid) {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Missing one or more required sheets: Dataset, Attributes, Data Model, All Data"
                ));
            }

            // Save the exact uploaded file and return a token
            String token = tempFileStore.save(file.getBytes());
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "token", token
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "valid", false,
                    "message", "A technical error occurred: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/api/download/{token}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String token) {
        Path path = tempFileStore.resolve(token);
        if (path == null) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(path.toFile());
        String filename = path.getFileName().toString();

        // If you want one-time downloads, uncomment the remove line:
        // tempFileStore.remove(token);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/thank-you")
    public String thankYouPage() {
        return "thank-you"; // looks for templates/thank-you.html
    }




}


