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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.ArrayList;
import java.util.List;

import static com.azure.ai.agents.persistent.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilesAsyncClientTest extends ClientTestBase {

    private PersistentAgentsClientBuilder clientBuilder;
    private FilesAsyncClient filesAsyncClient;
    private List<FileInfo> uploadedFiles;
    private static final String SAMPLE_TEXT = "Sample text for testing upload";

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        PersistentAgentsAsyncClient agentsAsyncClient = clientBuilder.buildAsyncClient();
        filesAsyncClient = agentsAsyncClient.getFilesAsyncClient();
        uploadedFiles = new ArrayList<>();
    }

    private Mono<FileInfo> uploadFile(String fileName, FilePurpose filePurpose) {
        FileDetails fileDetails = new FileDetails(BinaryData.fromString(SAMPLE_TEXT)).setFilename(fileName);
        UploadFileRequest uploadFileRequest = new UploadFileRequest(fileDetails, filePurpose);

        return filesAsyncClient.uploadFile(uploadFileRequest).map(uploadedFile -> {
            uploadedFiles.add(uploadedFile);
            assertNotNull(uploadedFile, "Uploaded file should not be null");
            return uploadedFile;
        });
    }

    private Mono<FileInfo> uploadFile(String fileName) {
        return uploadFile(fileName, FilePurpose.AGENTS);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testUploadFile(HttpClient httpClient) {
        setup(httpClient);

        // upload new file
        String fileName = "upload_file_test.txt";
        StepVerifier.create(uploadFile(fileName)).assertNext(uploadedFile -> {
            assertNotNull(uploadedFile, "Uploaded file should not be null");
            assertEquals(fileName, uploadedFile.getFilename(), "File name should match");
        }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testGetFile(HttpClient httpClient) {
        setup(httpClient);

        // upload new file
        String fileName = "get_file_test.txt";
        StepVerifier.create(uploadFile(fileName).flatMap(uploadedFile -> {
            String fileId = uploadedFile.getId();
            return filesAsyncClient.getFile(fileId);
        })).assertNext(fileInfo -> {
            assertNotNull(fileInfo, "FileInfo should not be null");
            assertEquals(fileName, fileInfo.getFilename(), "Retrieved file name should match");
        }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testDeleteFile(HttpClient httpClient) {
        setup(httpClient);

        // upload new file
        String fileName = "delete_file_test.txt";
        StepVerifier.create(uploadFile(fileName).flatMap(uploadedFile -> {
            String fileId = uploadedFile.getId();
            return filesAsyncClient.deleteFile(fileId);
        })).verifyComplete();

        // Remove the file from our tracking list since it's been deleted
        uploadedFiles.clear();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testListFiles(HttpClient httpClient) {
        setup(httpClient);

        // upload new file
        String fileName = "list_files_test.txt";
        StepVerifier.create(uploadFile(fileName).then(filesAsyncClient.listFiles().collectList()))
            .assertNext(fileInfos -> {
                assertNotNull(fileInfos, "File list should not be null");
                assertTrue(!fileInfos.isEmpty(), "File list should not be empty");
            })
            .verifyComplete();
    }

    @AfterEach
    public void cleanup() {
        for (FileInfo fileInfo : uploadedFiles) {
            try {
                filesAsyncClient.deleteFile(fileInfo.getId()).block();
            } catch (Exception e) {
                System.out.println("Failed to clean up file: " + fileInfo.getFilename());
                System.out.println(e.getMessage());
            }
        }
    }
}
