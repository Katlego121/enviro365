package com.enviro.assessment.grad001.katlegomononyane.enviro;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class FileUploadController {

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    // File size validation - Required size in bytes.
    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB

    // File type validation method - Required file type
    private boolean isValidFileType(MultipartFile file) {
        // Get the file name
        String fileName = file.getOriginalFilename();

        // Get the file extension
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        // Define the list of allowed file extensions
        List<String> allowedExtensions = Arrays.asList("pdf", "doc", "docx", "txt"); // Add more file types as needed

        // Check if the file extension is in the allowed list
        return allowedExtensions.contains(fileExtension);
    }

    // Enpoint for posting a text file into the H2 in-memory database
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {

            // Log API request
            logger.info("Received file upload request: {}", file.getOriginalFilename());

            // Validate file size
            if (file.getSize() > MAX_FILE_SIZE_BYTES) {
                return ResponseEntity.badRequest().body("File size exceeds the allowed limit.");
            }

            // Validate file type (Implement isValidFileType method)
            if (!isValidFileType(file)) {
                return ResponseEntity.badRequest().body("Unsupported file type.");
            }

            // Check if file is empty
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("No file found in the request.");
            }

            // Perform file processing
            UploadedFile uploadedFile = new UploadedFile();
            uploadedFile.setFileName(file.getOriginalFilename());
            uploadedFile.setData(file.getBytes());
            uploadedFileRepository.save(uploadedFile);


            // Return success response
            return ResponseEntity.ok("File uploaded successfully.");
        } catch (Exception ex) {
            // Log the exception
            logger.error("Error processing file upload:", ex);

            // Return error response
            return ResponseEntity.badRequest().body("An unexpected error occurred.");
        }
    }

    // Endpoint mapping for retrieving the file from the H2 in-memory database
    @GetMapping("/files/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        @SuppressWarnings("null")
        Optional<UploadedFile> fileOptional = uploadedFileRepository.findById(id);
        if (fileOptional.isPresent()) {
            UploadedFile file = fileOptional.get();
            return ResponseEntity
                    .ok()
                    .header("Content-Disposition", "attachment; filename=\"" + file.getFileName() + "\"")
                    .body(file.getData());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
