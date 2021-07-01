package com.azure.iot.deviceupdate.sample;

import com.azure.iot.deviceupdate.models.FileImportMetadata;
import com.azure.iot.deviceupdate.models.ImportManifestMetadata;
import com.azure.iot.deviceupdate.models.ImportUpdateInput;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ContentFactory {
    private static final String FILE_NAME = "setup.exe";

    private final String connectionString;
    private final String blobContainer;

    public ContentFactory(String storageName, String storageKey, String blobContainer) {
        this.connectionString = String.format(
            "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net",
            storageName,
            storageKey);
        this.blobContainer = blobContainer;
    }

    public ImportUpdateInput CreateImportUpdate(String provider, String name, String version)  throws IOException {
        // Create actual update payload (fake "setup.exe")
        String payloadLocalFile = CreateAduPayloadFile(FILE_NAME);
        long payloadFileSize = GetFileSize(payloadLocalFile);
        String payloadFileHash = GetFileHash(payloadLocalFile);
        // Upload the payload file to Azure Blob storage
        String payloadUrl = UploadFile(payloadLocalFile, GenerateStorageId(payloadFileHash));

        // Create import manifest (describing the update and all it's payload files)
        String importManifestFile = CreateImportManifestContent(
            provider,
            name,
            version,
            FILE_NAME,
            payloadFileSize,
            payloadFileHash,
            String.format("[{\"DeviceManufacturer\": \"%s\", \"DeviceModel\": \"%s\"}]", provider.toLowerCase(), name.toLowerCase()));
        long importManifestFileSize = GetFileSize(importManifestFile);
        String importManifestFileHash = GetFileHash(importManifestFile);
        // Upload the import manifest file to Azure Blob storage
        String importManifestUrl = UploadFile(importManifestFile, GenerateStorageId(importManifestFileHash));

        // Create import update request body (containing Urls to import manifest and update payload files)
        return CreateImportBody(importManifestUrl, importManifestFileSize, importManifestFileHash, payloadUrl);
    }

    private String CreateAduPayloadFile(String fileName) throws IOException {
        String content = String.format("{\"Scenario\": \"DeviceUpdateClientSample\", \"Timestamp\": \"%s\"}",
            new SimpleDateFormat("yyyy.MMdd.HHmm.ss").format(Calendar.getInstance().getTime()));

        try {
            File file = File.createTempFile("payload", ".json");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(content);
            bw.close();

            return file.getAbsolutePath();
        }
        catch (IOException e) {
            System.out.println("Failed to create local payload file:");
            throw e;
        }
    }

    private String CreateImportManifestContent(String provider, String name, String version, String fileName, long fileSize, String fileHash, String compatibilityIds) throws IOException {
        String content = String.format("{\"UpdateId\": {\"Provider\": \"%s\", \"Name\": \"%s\", \"Version\": \"%s\" }," +
            "\"CreatedDateTime\": \"%s\", " +
            "\"Files\": [{\"FileName\": \"%s\", \"SizeInBytes\": %s, \"Hashes\": {\"SHA256\": \"%s\"}}]," +
            "\"Compatibility\": %s, \"ManifestVersion\": \"2.0\", \"InstalledCriteria\": \"1.2.3.4\","+
            "\"UpdateType\": \"microsoft/swupdate:1\"}",
            provider, name, version,
            DateTimeFormatter.ISO_INSTANT.format(OffsetDateTime.now()),
            fileName, fileSize, fileHash,
            compatibilityIds);

        try {
            File file = File.createTempFile("manifest", ".json");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(content);
            bw.close();

            return file.getAbsolutePath();
        }
        catch (IOException e) {
            System.out.println("Failed to create local payload file:");
            throw e;
        }
    }

    private ImportUpdateInput CreateImportBody(String importManifestUrl, long importManifestFileSize, String importManifestFileHash, String payloadUrl) {
        Map<String, String> hashes = new HashMap<String, String>();
        hashes.put("SHA256", importManifestFileHash);
        ImportManifestMetadata importManifest = new ImportManifestMetadata()
            .setUrl(importManifestUrl)
            .setSizeInBytes(importManifestFileSize)
            .setHashes(hashes);
        List<FileImportMetadata> files = new ArrayList<FileImportMetadata>();
        files.add(
            new FileImportMetadata()
                .setFilename(FILE_NAME)
                .setUrl(payloadUrl));

        return new ImportUpdateInput()
            .setImportManifest(importManifest)
            .setFiles(files);
    }

    private String UploadFile(String localFile, String storageId) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .connectionString(this.connectionString)
            .buildClient();

        BlobContainerClient cloudBlobContainer = blobServiceClient.getBlobContainerClient(this.blobContainer);
        BlobClient blobClient = cloudBlobContainer.getBlobClient(storageId);
        blobClient.uploadFromFile(localFile, true);

        BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(
            OffsetDateTime.now().plusDays(1),
            new BlobSasPermission().setReadPermission(true));
        String token = blobClient.generateSas(values);

        return String.format("%s?%s", blobClient.getBlobUrl(), token);
    }

    private long GetFileSize(String payloadLocalFile) {
        File file = new File(payloadLocalFile);
        return file.length();
    }

    private String GetFileHash(String payloadLocalFile) throws IOException {
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA-256");
            String payload = readAllTextFromFile(payloadLocalFile);
            byte[] result = mDigest.digest(payload.getBytes());

            return Base64.getEncoder().encodeToString(result);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private static String readAllTextFromFile(String filePath) throws IOException {
        String content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
        return content;
    }

    private String GenerateStorageId(String fileHash) {
        return fileHash.substring(0, 22);
    }
}
