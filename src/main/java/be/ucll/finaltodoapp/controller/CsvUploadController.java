package be.ucll.finaltodoapp.controller;


import be.ucll.finaltodoapp.service.CsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
@RestController
public class CsvUploadController {

    private final CsvService csvService;

    @Autowired
    public CsvUploadController(CsvService csvService) {
        this.csvService = csvService;
    }

    @PostMapping("/upload-csv")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            csvService.processCsvFile(file, username);
            return ResponseEntity.status(HttpStatus.OK).body("File uploaded and processed successfully");
        } catch (IOException | ParseException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file: " + e.getMessage());
        }
    }
}