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

    private static final String CONTAINER_STORES = "container1,container2"; // A comma separated list of container names
    private static final StorageSharedKeyCredential SHARE_KEY_CREDENTIAL
        = new StorageSharedKeyCredential("<account_name>", "<account_key>");
    private static final Map<String, Object> CONFIG = new HashMap<String, Object>() {
        {
            put(AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL, SHARE_KEY_CREDENTIAL);
            put(AzureFileSystem.AZURE_STORAGE_FILE_STORES, CONTAINER_STORES);
        }
    };
    private FileSystem myFs = FileSystems.newFileSystem(new URI("azb://?endpoint=<your_account_name"),
        Collections.emptyMap());
    private Path dirPath = myFs.getPath("dir");
    private Path filePath = myFs.getPath("file");

    public ReadmeSamples() throws URISyntaxException, IOException {
    }

    public void createAFileSystem() throws URISyntaxException, IOException {
        // BEGIN: readme-sample-createAFileSystem
        Map<String, Object> config = new HashMap<>();
        String stores = "<container_name>,<another_container_name>"; // A comma separated list of container names
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential("<account_name", "account_key");
        config.put(AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL, credential);
        config.put(AzureFileSystem.AZURE_STORAGE_FILE_STORES, stores);
        FileSystem myFs = FileSystems.newFileSystem(new URI("azb://?endpoint=<account_endpoint"), config);
        // END: readme-sample-createAFileSystem
    }

    public void createADirectory() throws IOException {
        // BEGIN: readme-sample-createADirectory
        Path dirPath = myFs.getPath("dir");
        Files.createDirectory(dirPath);
        // END: readme-sample-createADirectory
    }

    public void iterateOverDirectoryContents() throws IOException {
        // BEGIN: readme-sample-iterateOverDirectoryContents
        for (Path p : Files.newDirectoryStream(dirPath)) {
            System.out.println(p.toString());
        }
        // END: readme-sample-iterateOverDirectoryContents
    }

    public void readAFile() throws IOException {
        // BEGIN: readme-sample-readAFile
        Path filePath = myFs.getPath("file");
        try (InputStream is = Files.newInputStream(filePath)) {
            is.read();
        }
        // END: readme-sample-readAFile
    }

    public void writeToAFile() throws IOException {
        // BEGIN: readme-sample-writeToAFile
        try (OutputStream os = Files.newOutputStream(filePath)) {
            os.write(0);
        }
        // END: readme-sample-writeToAFile
    }

    public void copyAFile() throws IOException {
        // BEGIN: readme-sample-copyAFile
        Path destinationPath = myFs.getPath("destinationFile");
        Files.copy(filePath, destinationPath, StandardCopyOption.COPY_ATTRIBUTES);
        // END: readme-sample-copyAFile
    }

    public void deleteAFile() throws IOException {
        // BEGIN: readme-sample-deleteAFile
        Files.delete(filePath);
        // END: readme-sample-deleteAFile
    }

    public void readAttributesOnAFile() throws IOException {
        // BEGIN: readme-sample-readAttributesOnAFile
        AzureBlobFileAttributes attr = Files.readAttributes(filePath, AzureBlobFileAttributes.class);
        BlobHttpHeaders headers = attr.blobHttpHeaders();
        // END: readme-sample-readAttributesOnAFile
    }

    public void readAttributesOnAFileString() throws IOException {
        // BEGIN: readme-sample-readAttributesOnAFileString
        Map<String, Object> attributes = Files.readAttributes(filePath, "azureBlob:metadata,headers");
        // END: readme-sample-readAttributesOnAFileString
    }

    public void writeAttributesToAFile() throws IOException {
        // BEGIN: readme-sample-writeAttributesToAFile
        AzureBlobFileAttributeView view = Files.getFileAttributeView(filePath, AzureBlobFileAttributeView.class);
        view.setMetadata(Collections.emptyMap());
        // END: readme-sample-writeAttributesToAFile
    }

    public void writeAttributesToAFileString() throws IOException {
        // BEGIN: readme-sample-writeAttributesToAFileString
        Files.setAttribute(filePath, "azureBlob:blobHttpHeaders", new BlobHttpHeaders());
        // END: readme-sample-writeAttributesToAFileString
    }
}
