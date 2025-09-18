package com.example.snsapp;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5MB

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "ファイルが指定されていません"));
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body(Map.of("error", "ファイルサイズが大きすぎます (最大5MB)"));
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Content-Type が不明です"));
        }

        // 許可する MIME タイプ
        if (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/gif")) {
            return ResponseEntity.badRequest().body(Map.of("error", "許可されていないファイルタイプです"));
        }

        // マジックバイト検証（先頭数バイト）
        try (InputStream is = file.getInputStream()) {
            byte[] header = is.readNBytes(8);
            // JPEG: FF D8, PNG: 89 50 4E 47, GIF: 47 49 46
            if (!(header[0] == (byte)0xFF && header[1] == (byte)0xD8) &&
                !(header[0] == (byte)0x89 && header[1] == (byte)0x50 && header[2] == (byte)0x4E && header[3] == (byte)0x47) &&
                !(header[0] == (byte)0x47 && header[1] == (byte)0x49 && header[2] == (byte)0x46)) {
                return ResponseEntity.badRequest().body(Map.of("error", "ファイルの内容が画像形式ではありません"));
            }
        }

        // 保存先ディレクトリ
        Path uploadDir = Paths.get("uploads");
        Files.createDirectories(uploadDir);

        // 拡張子の推定
        String ext = switch (contentType) {
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            default -> ".jpg"; // jpeg
        };

        String filename = UUID.randomUUID().toString() + ext;
        Path target = uploadDir.resolve(filename);

        // ファイル保存
        try (InputStream is = file.getInputStream()) {
            Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
        }

        // アクセス用 URL（WebMvcConfig で /uploads/** を file:uploads/ にマップします）
        String url = "/uploads/" + filename;

        return ResponseEntity.ok(Map.of("url", url));
    }
}
