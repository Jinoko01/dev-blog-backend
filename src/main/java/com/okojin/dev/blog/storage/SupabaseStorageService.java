package com.okojin.dev.blog.storage;

import com.okojin.dev.blog.admin.dto.SignedUploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    private final String supabaseUrl;
    private final String serviceRoleKey;
    private final String storageBucket;
    private final RestClient restClient;

    public SupabaseStorageService(
            @Value("${app.supabase.url}") String supabaseUrl,
            @Value("${app.supabase.service-role-key}") String serviceRoleKey,
            @Value("${app.supabase.storage-bucket}") String storageBucket
    ) {
        this.supabaseUrl = supabaseUrl;
        this.serviceRoleKey = serviceRoleKey;
        this.storageBucket = storageBucket;
        this.restClient = RestClient.create(supabaseUrl);
    }

    public SignedUploadResponse createSignedUploadUrl(String filename) {
        String filePath = buildFilePath(filename);
        String uri = "/storage/v1/object/upload/sign/" + storageBucket + "/" + filePath;

        Map<String, Object> response;
        try {
            response = restClient.post()
                    .uri(uri)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Supabase Storage 요청 실패");
        }

        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Supabase Storage 응답 없음");
        }

        String urlPath = (String) response.get("url");
        String token = (String) response.get("token");
        String signedUrl = supabaseUrl + urlPath;

        return new SignedUploadResponse(signedUrl, token, filePath);
    }

    private String buildFilePath(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        String ext = dotIndex >= 0 ? filename.substring(dotIndex + 1) : "png";
        String baseName = dotIndex >= 0 ? filename.substring(0, dotIndex) : filename;
        String safeName = baseName.replaceAll("[^a-zA-Z0-9_-]", "");
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return "posts/" + System.currentTimeMillis() + "-" + safeName + "-" + suffix + "." + ext;
    }
}
