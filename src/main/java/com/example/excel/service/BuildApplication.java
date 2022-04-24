package com.example.excel.service;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;

@Service
public interface BuildApplication {

    Path buildVue(MultipartFile zipFile) throws Exception;

}
