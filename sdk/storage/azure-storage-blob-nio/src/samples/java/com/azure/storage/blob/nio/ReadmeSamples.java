// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.nio;

import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.common.StorageSharedKeyCredential;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Code samples for the README.md
 */
public class ReadmeSamples {

    private FileSystem myFs = FileSystems.newFileSystem(new URI("azb://?account=<your_account_name"),
        Collections.emptyMap());
    private Path dirPath = myFs.getPath("dir");
    private Path filePath = myFs.getPath("file");

    public ReadmeSamples() throws URISyntaxException, IOException {
    }

    public void createAFileSystem() throws URISyntaxException, IOException {
        Map<String, Object> config = new HashMap<>();
        String stores = "<container_name>,<another_container_name>"; // A comma separated list of container names
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential("<account_name", "account_key");
        config.put(AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL, credential);
        config.put(AzureFileSystem.AZURE_STORAGE_FILE_STORES, stores);
        FileSystem myFs = FileSystems.newFileSystem(new URI("azb://?endpoint=<account_endpoint"), config);
    }

    public void createADirectory() throws IOException {
        Path dirPath = myFs.getPath("dir");
        Files.createDirectory(dirPath);
    }

    public void iterateOverDirectoryContents() throws IOException {
        for (Path p : Files.newDirectoryStream(dirPath)) {
            System.out.println(p.toString());
        }
    }

    public void readAFile() throws IOException {
        Path filePath = myFs.getPath("file");
        InputStream is = Files.newInputStream(filePath);
        is.read();
        is.close();
    }

    public void writeToAFile() throws IOException {
        OutputStream os = Files.newOutputStream(filePath);
        os.write(0);
        os.close();
    }

    public void copyAFile() throws IOException {
        Path destinationPath = myFs.getPath("destinationFile");
        Files.copy(filePath, destinationPath, StandardCopyOption.COPY_ATTRIBUTES);
    }

    public void deleteAFile() throws IOException {
        Files.delete(filePath);
    }

    public void readAttributesOnAFile() throws IOException {
        AzureBlobFileAttributes attr = Files.readAttributes(filePath, AzureBlobFileAttributes.class);
        BlobHttpHeaders headers = attr.blobHttpHeaders();
    }

    public void readAttributesOnAFileString() throws IOException {
        Map<String, Object> attributes = Files.readAttributes(filePath, "azureBlob:metadata,headers");
    }

    public void writeAttributesToAFile() throws IOException {
        AzureBlobFileAttributeView view = Files.getFileAttributeView(filePath, AzureBlobFileAttributeView.class);
        view.setMetadata(Collections.EMPTY_MAP);
    }

    public void writeAttributesToAFileString() throws IOException {
        Files.setAttribute(filePath, "azureBlob:blobHttpHeaders", new BlobHttpHeaders());
    }
}
