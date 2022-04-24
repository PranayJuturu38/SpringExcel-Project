package com.example.excel.controller;

import com.example.excel.service.BuildApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

@RestController
public class BuildController {

    @Autowired
    private BuildApplication buildApplication;



    @PostMapping("/buildApp")
    public ResponseEntity<String> buildVue(@RequestParam("file") MultipartFile zipFile) throws Exception {
        try {

            Path path = buildApplication.buildVue(zipFile);

            return ResponseEntity.status(HttpStatus.OK).body("Path to the build directory: " + path.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(e.getMessage());
        }
    }
}
