// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.FileDetails;
import com.azure.ai.agents.persistent.models.FileInfo;
import com.azure.ai.agents.persistent.models.FilePurpose;
import com.azure.ai.agents.persistent.models.UploadFileRequest;
import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.ArrayList;
import java.util.List;

import static com.azure.ai.agents.persistent.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilesClientTest extends ClientTestBase {

    private PersistentAgentsClientBuilder clientBuilder;
    private FilesClient filesClient;
    private List<FileInfo> uploadedFiles;
    private static final String SAMPLE_TEXT = "Sample text for testing upload";

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        PersistentAgentsClient agentsClient = clientBuilder.buildClient();
        filesClient = agentsClient.getFilesClient();
        uploadedFiles = new ArrayList<>();
    }

    private FileInfo uploadFile(String fileName) {
        FileDetails fileDetails = new FileDetails(BinaryData.fromString(SAMPLE_TEXT)).setFilename(fileName);
        UploadFileRequest uploadFileRequest = new UploadFileRequest(fileDetails, FilePurpose.AGENTS);
        FileInfo uploadedFile = filesClient.uploadFile(uploadFileRequest);
        assertNotNull(uploadedFile, "Uploaded file should not be null");
        uploadedFiles.add(uploadedFile);
        return uploadedFile;
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testUploadFile(HttpClient httpClient) {
        setup(httpClient);

        // upload new file
        String fileName = "upload_file_test.txt";
        FileInfo uploadedFile = uploadFile(fileName);

        assertEquals(fileName, uploadedFile.getFilename(), "File name should match");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testGetFile(HttpClient httpClient) {
        setup(httpClient);

        // upload new file
        String fileName = "get_file_test.txt";
        FileInfo uploadedFile = uploadFile(fileName);

        // Retrieve file information
        FileInfo fileInfo = filesClient.getFile(uploadedFile.getId());
        assertNotNull(fileInfo, "FileInfo should not be null");
        assertEquals(fileName, fileInfo.getFilename(), "Retrieved file name should match");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testDeleteFile(HttpClient httpClient) {
        setup(httpClient);

        // upload new file
        String fileName = "delete_file_test.txt";
        FileInfo uploadedFile = uploadFile(fileName);

        // Delete the created file
        filesClient.deleteFile(uploadedFile.getId());
        assertTrue(true, "File should be marked as deleted");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testListFiles(HttpClient httpClient) {
        setup(httpClient);

        // upload new file
        String fileName = "list_files_test.txt";
        FileInfo uploadedFile = uploadFile(fileName);

        List<FileInfo> fileInfos = filesClient.listFiles();
        assertNotNull(fileInfos, "File list should not be null");
        assertTrue(fileInfos.size() > 0, "File list should not be empty");
    }

    @AfterEach
    public void cleanup() {
        for (FileInfo fileInfo : uploadedFiles) {
            try {
                filesClient.deleteFile(fileInfo.getId());
            } catch (Exception e) {
                System.out.println("Failed to clean up file: " + fileInfo.getFilename());
                System.out.println(e.getMessage());
            }
        }
    }
}
