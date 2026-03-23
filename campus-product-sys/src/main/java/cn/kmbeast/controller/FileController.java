package cn.kmbeast.controller;

import cn.kmbeast.utils.IdFactoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.web.bind.annotation.*;
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
 * 閺傚洣娆㈤崜宥囶伂閹貉冨煑閸? *
 * @since 2024-03-22
 */
@RestController
@RequestMapping("/file")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Value("${my-server.api-context-path}")
    private String API;

    @Value("${trade.upload-dir:pic}")
    private String uploadDir;

    @PostConstruct
    public void initUploadDirectoryInfo() {
        try {
            File primaryDir = ensurePrimaryUploadDirExists();
            log.info("Primary upload directory resolved to: {}", primaryDir.getAbsolutePath());

            List<File> legacyDirs = resolveLegacyReadDirs(primaryDir);
            if (legacyDirs.isEmpty()) {
                log.info("No legacy upload directories detected for fallback reads.");
            } else {
                for (File legacyDir : legacyDirs) {
                    log.warn("Legacy upload directory enabled for migration/fallback: {}", legacyDir.getAbsolutePath());
                }
                migrateLegacyFiles(primaryDir, legacyDirs);
            }
        } catch (IOException e) {
            log.error("Failed to initialize upload directory during startup", e);
        }
    }

    /**
     * 閺傚洣娆㈡稉濠佺炊
     *
     * @param multipartFile 閺傚洣娆㈠ù?     * @return 閸濆秴绨?     */
    @PostMapping("/upload")
    public Map<String, Object> uploadFile(@RequestParam("file") MultipartFile multipartFile) {
        String uuid = IdFactoryUtil.getFileId();
        String fileName = uuid + multipartFile.getOriginalFilename();
        Map<String, Object> rep = new HashMap<>();
        try {
            if (uploadFile(multipartFile, fileName)) {
                rep.put("code", 200);
                rep.put("data", API + "/file/getFile?fileName=" + fileName);
                return rep;
            }
        } catch (IOException e) {
            log.error("File upload failed, fileName={}", fileName, e);
            rep.put("code", 400);
            rep.put("msg", "鏂囦欢涓婁紶寮傚父");
            return rep;
        }
        rep.put("code", 400);
        rep.put("msg", "鏂囦欢涓婁紶寮傚父");
        return rep;
    }

    /**
     * 瑙嗛涓婁紶
     *
     * @param multipartFile 閺傚洣娆㈠ù?     * @return 閸濆秴绨?     */
    @PostMapping("/video/upload")
    public Map<String, Object> videoUpload(@RequestParam("file") MultipartFile multipartFile) {
        String uuid = IdFactoryUtil.getFileId();
        String fileName = uuid + multipartFile.getOriginalFilename();
        Map<String, Object> rep = new HashMap<>();

        try {
            if (uploadFile(multipartFile, fileName)) {
                rep.put("code", 200);
                rep.put("data", API + "/file/getFile?fileName=" + fileName);
                return rep;
            }
        } catch (IOException e) {
            log.error("Video upload failed, fileName={}", fileName, e);
            rep.put("code", 400);
            rep.put("msg", "鏂囦欢涓婁紶寮傚父");
            return rep;
        }
        rep.put("code", 400);
        rep.put("msg", "鏂囦欢涓婁紶寮傚父");
        return rep;
    }

    /**
     * 涓婁紶鏂囦欢
     *
     * @param multipartFile 閺傚洣娆㈠ù?     * @param fileName      閺傚洣娆㈠Λ鍕敩     * @return boolean
     * @throws IOException 閸掓稑缂撻敓锟?    */
    public boolean uploadFile(MultipartFile multipartFile, String fileName) throws IOException {
        return saveFile(multipartFile, fileName);
    }

    private boolean saveFile(MultipartFile multipartFile, String fileName) throws IOException {
        File fileDir = ensurePrimaryUploadDirExists();
        File file = new File(fileDir.getAbsolutePath() + "/" + fileName);
        if (file.exists()) {
            if (!file.delete()) {
                log.error("Failed to delete existing file before overwrite: {}", file.getAbsolutePath());
                return false;
            }
        }
        if (file.createNewFile()) {
            multipartFile.transferTo(file);
            log.info("Saved uploaded file to: {}", file.getAbsolutePath());
            return true;
        }
        log.error("Failed to create upload file: {}", file.getAbsolutePath());
        return false;
    }

    private File ensurePrimaryUploadDirExists() throws IOException {
        File primaryDir = resolveUploadDir();
        if (!primaryDir.exists()) {
            if (!primaryDir.mkdirs()) {
                throw new IOException("Failed to create upload directory: " + primaryDir.getAbsolutePath());
            }
            log.info("Created upload directory: {}", primaryDir.getAbsolutePath());
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
            if (targetPath != null && targetPath.getFileName() != null
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
                log.warn("Unable to list files in legacy upload directory: {}", legacyDir.getAbsolutePath());
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
                    log.info("Migrated legacy upload file to primary directory. source={}, target={}",
                            legacyFile.getAbsolutePath(), targetFile.getAbsolutePath());
                } catch (IOException e) {
                    failedCount++;
                    log.error("Failed to migrate legacy upload file. source={}, target={}",
                            legacyFile.getAbsolutePath(), targetFile.getAbsolutePath(), e);
                }
            }
        }
        log.info("Legacy upload migration finished. migrated={}, skippedExisting={}, failed={}",
                migratedCount, skippedCount, failedCount);
    }

    private File resolveReadableFile(String fileName) throws IOException {
        File primaryDir = ensurePrimaryUploadDirExists();
        File primaryFile = new File(primaryDir, fileName);
        if (primaryFile.exists() && primaryFile.isFile()) {
            return primaryFile;
        }

        for (File legacyDir : resolveLegacyReadDirs(primaryDir)) {
            File legacyFile = new File(legacyDir, fileName);
            if (legacyFile.exists() && legacyFile.isFile()) {
                File migratedFile = tryMigrateSingleFile(primaryDir, legacyFile);
                if (migratedFile != null && migratedFile.exists() && migratedFile.isFile()) {
                    return migratedFile;
                }
                log.warn("Serving file from legacy upload directory because on-demand migration failed. fileName={}, path={}",
                        fileName, legacyFile.getAbsolutePath());
                return legacyFile;
            }
        }
        return null;
    }

    private File tryMigrateSingleFile(File primaryDir, File legacyFile) {
        File targetFile = new File(primaryDir, legacyFile.getName());
        if (targetFile.exists()) {
            return targetFile;
        }
        try {
            Files.copy(legacyFile.toPath(), targetFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
            log.info("On-demand migrated legacy upload file to primary directory. source={}, target={}",
                    legacyFile.getAbsolutePath(), targetFile.getAbsolutePath());
            return targetFile;
        } catch (IOException e) {
            log.error("Failed on-demand migration for legacy upload file. source={}, target={}",
                    legacyFile.getAbsolutePath(), targetFile.getAbsolutePath(), e);
            return null;
        }
    }

    /**
     * 鏌ョ湅鍥剧墖璧勬簮
     *
     * @param imageName 閺傚洣娆㈠Λ鍕敩     * @param response  閸濆秴绨?    * @throws IOException 閸掓稑缂撻敓锟?    */
    @GetMapping("/getFile")
    public void getImage(@RequestParam("fileName") String imageName,
                         HttpServletResponse response) throws IOException {
        File image = resolveReadableFile(imageName);
        if (image == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            log.warn("Requested file not found in primary or legacy upload directories. fileName={}", imageName);
            return;
        }
        String contentType = Files.probeContentType(image.toPath());
        if (contentType != null && !contentType.isEmpty()) {
            response.setContentType(contentType);
        }
        response.setContentLengthLong(image.length());
        try (FileInputStream fileInputStream = new FileInputStream(image);
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
        }
    }

}
