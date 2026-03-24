package cn.kmbeast.controller;

import cn.kmbeast.utils.IdFactoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * File upload/download controller.
 */
@RestController
@RequestMapping("/file")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Value("${my-server.api-context-path}")
    private String apiContextPath;

    @Value("${trade.upload-dir:pic}")
    private String uploadDir;

    @PostConstruct
    public void initUploadDirectoryInfo() {
        try {
            File primaryDir = ensurePrimaryUploadDirExists();
            log.info("Primary upload directory: {}", primaryDir.getAbsolutePath());

            List<File> legacyDirs = resolveLegacyReadDirs(primaryDir);
            if (legacyDirs.isEmpty()) {
                log.info("No legacy upload directories found.");
            } else {
                for (File legacyDir : legacyDirs) {
                    log.warn("Legacy upload directory enabled for fallback: {}", legacyDir.getAbsolutePath());
                }
                migrateLegacyFiles(primaryDir, legacyDirs);
            }
        } catch (IOException e) {
            log.error("Failed to initialize upload directories", e);
        }
    }

    @PostMapping("/upload")
    public Map<String, Object> uploadFile(@RequestParam("file") MultipartFile multipartFile) {
        return doUpload(multipartFile, false);
    }

    @PostMapping("/video/upload")
    public Map<String, Object> videoUpload(@RequestParam("file") MultipartFile multipartFile) {
        return doUpload(multipartFile, true);
    }

    private Map<String, Object> doUpload(MultipartFile multipartFile, boolean video) {
        Map<String, Object> response = new HashMap<>();
        String fileName = IdFactoryUtil.getFileId() + safeOriginalFilename(multipartFile.getOriginalFilename());
        try {
            if (uploadFile(multipartFile, fileName)) {
                response.put("code", 200);
                response.put("data", apiContextPath + "/file/getFile?fileName=" + fileName);
                return response;
            }
        } catch (IOException e) {
            log.error("Upload failed. fileName={}, video={}", fileName, video, e);
            response.put("code", 400);
            response.put("msg", video ? "video upload failed" : "file upload failed");
            return response;
        }
        response.put("code", 400);
        response.put("msg", video ? "video upload failed" : "file upload failed");
        return response;
    }

    public boolean uploadFile(MultipartFile multipartFile, String fileName) throws IOException {
        return saveFile(multipartFile, fileName);
    }

    private boolean saveFile(MultipartFile multipartFile, String fileName) throws IOException {
        if (!isSafeFileName(fileName)) {
            log.warn("Rejected unsafe upload fileName: {}", fileName);
            return false;
        }
        File fileDir = ensurePrimaryUploadDirExists();
        File targetFile = new File(fileDir, fileName).getCanonicalFile();
        if (!isInsideDirectory(fileDir, targetFile)) {
            log.warn("Blocked upload path traversal attempt. fileName={}", fileName);
            return false;
        }
        if (targetFile.exists() && !targetFile.delete()) {
            log.error("Failed to delete existing file before overwrite: {}", targetFile.getAbsolutePath());
            return false;
        }
        if (!targetFile.createNewFile()) {
            log.error("Failed to create upload file: {}", targetFile.getAbsolutePath());
            return false;
        }
        multipartFile.transferTo(targetFile);
        log.info("Saved file: {}", targetFile.getAbsolutePath());
        return true;
    }

    private File ensurePrimaryUploadDirExists() throws IOException {
        File primaryDir = resolveUploadDir();
        if (!primaryDir.exists() && !primaryDir.mkdirs()) {
            throw new IOException("Failed to create upload directory: " + primaryDir.getAbsolutePath());
        }
        return primaryDir;
    }

    private File resolveUploadDir() throws IOException {
        File configuredDir = new File(uploadDir);
        if (configuredDir.isAbsolute()) {
            return configuredDir.getCanonicalFile();
        }
        File appBaseDir = resolveApplicationBaseDir();
        return new File(appBaseDir, uploadDir).getCanonicalFile();
    }

    private File resolveApplicationBaseDir() throws IOException {
        File homeDir = new ApplicationHome(FileController.class).getDir();
        if (homeDir == null) {
            return new File(".").getCanonicalFile();
        }
        Path path = homeDir.getCanonicalFile().toPath();
        if (path.getFileName() != null && "classes".equalsIgnoreCase(path.getFileName().toString())) {
            Path targetPath = path.getParent();
            if (targetPath != null
                    && targetPath.getFileName() != null
                    && "target".equalsIgnoreCase(targetPath.getFileName().toString())) {
                Path projectPath = targetPath.getParent();
                if (projectPath != null) {
                    return projectPath.toFile().getCanonicalFile();
                }
            }
        }
        return path.toFile().getCanonicalFile();
    }

    private List<File> resolveLegacyReadDirs(File primaryDir) throws IOException {
        List<File> legacyDirs = new ArrayList<>();
        File appBaseDir = resolveApplicationBaseDir();
        File parentDir = appBaseDir.getParentFile();
        if (parentDir != null) {
            File legacyDir = new File(parentDir, uploadDir).getCanonicalFile();
            if (!legacyDir.equals(primaryDir) && legacyDir.exists() && legacyDir.isDirectory()) {
                legacyDirs.add(legacyDir);
            }
        }
        return legacyDirs;
    }

    private void migrateLegacyFiles(File primaryDir, List<File> legacyDirs) {
        int migratedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;
        for (File legacyDir : legacyDirs) {
            File[] files = legacyDir.listFiles();
            if (files == null) {
                continue;
            }
            for (File legacyFile : files) {
                if (!legacyFile.isFile()) {
                    continue;
                }
                File targetFile = new File(primaryDir, legacyFile.getName());
                if (targetFile.exists()) {
                    skippedCount++;
                    continue;
                }
                try {
                    Files.copy(legacyFile.toPath(), targetFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
                    migratedCount++;
                } catch (IOException e) {
                    failedCount++;
                    log.error("Failed to migrate legacy file. source={}, target={}",
                            legacyFile.getAbsolutePath(), targetFile.getAbsolutePath(), e);
                }
            }
        }
        log.info("Legacy migration finished. migrated={}, skipped={}, failed={}",
                migratedCount, skippedCount, failedCount);
    }

    private File resolveReadableFile(String fileName) throws IOException {
        if (!isSafeFileName(fileName)) {
            return null;
        }
        File primaryDir = ensurePrimaryUploadDirExists();
        File primaryFile = new File(primaryDir, fileName).getCanonicalFile();
        if (isInsideDirectory(primaryDir, primaryFile) && primaryFile.exists() && primaryFile.isFile()) {
            return primaryFile;
        }

        for (File legacyDir : resolveLegacyReadDirs(primaryDir)) {
            File legacyFile = new File(legacyDir, fileName).getCanonicalFile();
            if (!isInsideDirectory(legacyDir, legacyFile)) {
                continue;
            }
            if (legacyFile.exists() && legacyFile.isFile()) {
                File migratedFile = tryMigrateSingleFile(primaryDir, legacyFile);
                if (migratedFile != null && migratedFile.exists() && migratedFile.isFile()) {
                    return migratedFile;
                }
                log.warn("Serving from legacy directory due migration failure. fileName={}, path={}",
                        fileName, legacyFile.getAbsolutePath());
                return legacyFile;
            }
        }
        return null;
    }

    private File tryMigrateSingleFile(File primaryDir, File legacyFile) {
        try {
            File targetFile = new File(primaryDir, legacyFile.getName()).getCanonicalFile();
            if (!isInsideDirectory(primaryDir, targetFile)) {
                return null;
            }
            if (targetFile.exists()) {
                return targetFile;
            }
            Files.copy(legacyFile.toPath(), targetFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
            return targetFile;
        } catch (IOException e) {
            log.error("Failed on-demand migration for legacy file. source={}", legacyFile.getAbsolutePath(), e);
            return null;
        }
    }

    @GetMapping("/getFile")
    public void getImage(@RequestParam("fileName") String imageName, HttpServletResponse response) throws IOException {
        if (!isSafeFileName(imageName)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            log.warn("Rejected unsafe file request. fileName={}", imageName);
            return;
        }
        File image = resolveReadableFile(imageName);
        if (image == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String contentType = Files.probeContentType(image.toPath());
        if (contentType != null && !contentType.isEmpty()) {
            response.setContentType(contentType);
        }
        response.setContentLengthLong(image.length());
        try (FileInputStream inputStream = new FileInputStream(image);
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
        }
    }

    private String safeOriginalFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return "file.bin";
        }
        String normalized = originalFilename.trim().replace("\\", "/");
        int index = normalized.lastIndexOf('/');
        if (index >= 0) {
            normalized = normalized.substring(index + 1);
        }
        if (!isSafeFileName(normalized)) {
            return "file.bin";
        }
        return normalized;
    }

    private boolean isSafeFileName(String fileName) {
        if (fileName == null) {
            return false;
        }
        String trimmed = fileName.trim();
        if (trimmed.isEmpty() || trimmed.length() > 255) {
            return false;
        }
        return !trimmed.contains("..")
                && !trimmed.contains("/")
                && !trimmed.contains("\\")
                && !trimmed.contains(":");
    }

    private boolean isInsideDirectory(File baseDir, File candidateFile) throws IOException {
        String basePath = baseDir.getCanonicalPath() + File.separator;
        String candidatePath = candidateFile.getCanonicalPath();
        return candidatePath.startsWith(basePath);
    }
}
