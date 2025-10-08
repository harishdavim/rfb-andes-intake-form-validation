package com.abnamro.nl.andes.controller;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class FileUploadController {

    private final Path uploadDir = Paths.get(System.getProperty("java.io.tmpdir"), "intake-uploads");

    public FileUploadController() throws IOException {
        Files.createDirectories(uploadDir);
    }

    @GetMapping({"/", "/upload"})
    public String page() { return "upload"; }

    /** ---------- Upload: Save file then show thank-you ---------- */
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Please choose a file first.");
            return "upload";
        }
        try {
            String saved = save(file);
            model.addAttribute("info", "Uploaded: " + saved);
            return "thank-you";
        } catch (Exception e) {
            model.addAttribute("error", "Technical error while uploading: " + e.getMessage());
            return "upload";
        }
    }

    /** ---------- Validate: Check required worksheets ---------- */
    @PostMapping("/validate")
    public String validate(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Please choose a file first.");
            return "upload";
        }

// Required sheets (normalized names)
        List<String> required = List.of("dataset", "attributes", "data model", "all data");

        try (InputStream is = file.getInputStream(); Workbook wb = WorkbookFactory.create(is)) {

// Collect all sheet names normalized
            Set<String> present = new HashSet<>();
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                present.add(normalize(wb.getSheetName(i)));
            }

// Find what's missing
            List<String> missing = required.stream()
                    .filter(req -> !present.contains(normalize(req)))
                    .collect(Collectors.toList());

            if (missing.isEmpty()) {
                String saved = save(file); // optional: keep a copy
                model.addAttribute("validatedFile", saved);
                return "download"; // show download page
            } else {
                model.addAttribute("error",
                        "Validation failed. Missing worksheet(s): " + String.join(", ", missing)
                               );
                return "upload";
            }

        } catch (Exception e) {
            model.addAttribute("error", "Technical error during validation: " + e.getMessage());
            return "upload";
        }
    }

    /** Optional: download endpoint used by download.html */
    @GetMapping("/download/file")
    public ResponseEntity<Resource> download(@RequestParam(required = false) String file) {
        Path path = (file != null) ? uploadDir.resolve(file) : uploadDir.resolve("validated.xlsx");
        Resource resource = new FileSystemResource(path.toFile());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName() + "\"")
                .body(resource);
    }

    /* ---------- Helpers ---------- */

    private static String normalize(String s) {
        if (s == null) return "";
// trim, lower, unify spaces/underscores/dashes
        String norm = s.trim().toLowerCase(Locale.ROOT)
                .replace('_', ' ')
                .replace('-', ' ')
                .replaceAll("\\s+", " ");
        return norm;
    }

    private String save(MultipartFile file) throws IOException {
        String clean = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String unique = System.currentTimeMillis() + "_" + clean;
        Path target = uploadDir.resolve(unique);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return unique;
    }
}
