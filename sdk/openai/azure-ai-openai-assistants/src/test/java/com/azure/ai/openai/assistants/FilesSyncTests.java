// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.implementation.models.FileListResponse;
import com.azure.ai.openai.assistants.models.FileDeletionStatus;
import com.azure.ai.openai.assistants.models.FilePurpose;
import com.azure.ai.openai.assistants.models.OpenAIFile;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilesSyncTests extends AssistantsClientTestBase {

    private AssistantsClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantTextFileOperations(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        uploadAssistantTextFileRunner((fileDetails, filePurpose) -> {
            // Upload file
            OpenAIFile file = client.uploadFile(fileDetails, filePurpose);
            assertNotNull(file);
            assertNotNull(file.getId());

            // Get single file
            OpenAIFile fileFromBackend = client.getFile(file.getId());
            assertFileEquals(file, fileFromBackend);

            // Get file by purpose
            List<OpenAIFile> files = client.listFiles(filePurpose);
            assertTrue(files.stream().anyMatch(f -> f.getId().equals(file.getId())));

            // Delete file
            FileDeletionStatus deletionStatus = client.deleteFile(file.getId());
            assertTrue(deletionStatus.isDeleted());
            assertEquals(deletionStatus.getId(), file.getId());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantImageFileOperations(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        uploadAssistantImageFileRunner((fileDetails, filePurpose) -> {
            // Upload file
            OpenAIFile file = client.uploadFile(fileDetails, filePurpose);
            assertNotNull(file);
            assertNotNull(file.getId());

            // Get single file
            OpenAIFile fileFromBackend = client.getFile(file.getId());
            assertFileEquals(file, fileFromBackend);

            // Get file by purpose
            List<OpenAIFile> files = client.listFiles(filePurpose);
            assertTrue(files.stream().anyMatch(f -> f.getId().equals(file.getId())));

            // Delete file
            FileDeletionStatus deletionStatus = client.deleteFile(file.getId());
            assertTrue(deletionStatus.isDeleted());
            assertEquals(deletionStatus.getId(), file.getId());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void fineTuningJsonFileOperations(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        uploadFineTuningJsonFileRunner((fileDetails, filePurpose) -> {
            // Upload file
            OpenAIFile file = client.uploadFile(fileDetails, filePurpose);
            assertNotNull(file);
            assertNotNull(file.getId());

            // Get single file
            OpenAIFile fileFromBackend = client.getFile(file.getId());
            assertFileEquals(file, fileFromBackend);

            // Get file by purpose
            List<OpenAIFile> files = client.listFiles(filePurpose);
            assertTrue(files.stream().anyMatch(f -> f.getId().equals(file.getId())));

            // Delete file
            FileDeletionStatus deletionStatus = client.deleteFile(file.getId());
            assertTrue(deletionStatus.isDeleted());
            assertEquals(deletionStatus.getId(), file.getId());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantTextFileOperationsWithResponse(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        uploadAssistantTextFileRunner((fileDetails, filePurpose) -> {
            // Upload file
            OpenAIFile file = client.uploadFile(fileDetails, filePurpose);
            assertNotNull(file);
            assertNotNull(file.getId());

            Response<BinaryData> getFileResponse = client.getFileWithResponse(file.getId(), new RequestOptions());
            assertEquals(200, getFileResponse.getStatusCode());
            OpenAIFile fileFromBackend = getFileResponse.getValue().toObject(OpenAIFile.class);
            assertFileEquals(file, fileFromBackend);

            // Get file by purpose
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.addQueryParam("purpose", FilePurpose.ASSISTANTS.toString());
            Response<BinaryData> listFilesResponse = client.listFilesWithResponse(requestOptions);
            assertEquals(200, listFilesResponse.getStatusCode());
            List<OpenAIFile> files = listFilesResponse.getValue().toObject(FileListResponse.class).getData();
            assertTrue(files.stream().anyMatch(f -> f.getId().equals(file.getId())));

            // Delete file
            Response<BinaryData> deleteResponse = client.deleteFileWithResponse(file.getId(), new RequestOptions());
            assertEquals(200, deleteResponse.getStatusCode());
            FileDeletionStatus deletionStatus = deleteResponse.getValue().toObject(FileDeletionStatus.class);
            assertTrue(deletionStatus.isDeleted());
            assertEquals(deletionStatus.getId(), file.getId());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantImageFileOperationsWithResponse(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        uploadAssistantImageFileRunner((fileDetails, filePurpose) -> {
            // Upload file
            OpenAIFile file = client.uploadFile(fileDetails, filePurpose);
            assertNotNull(file);
            assertNotNull(file.getId());

            // Get single file
            Response<BinaryData> getFileResponse = client.getFileWithResponse(file.getId(), new RequestOptions());
            assertEquals(200, getFileResponse.getStatusCode());
            OpenAIFile fileFromBackend = getFileResponse.getValue().toObject(OpenAIFile.class);
            assertFileEquals(file, fileFromBackend);

            // Get file by purpose
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.addQueryParam("purpose", FilePurpose.ASSISTANTS.toString());
            Response<BinaryData> listFilesResponse = client.listFilesWithResponse(requestOptions);
            assertEquals(200, listFilesResponse.getStatusCode());
            List<OpenAIFile> files = listFilesResponse.getValue().toObject(FileListResponse.class).getData();
            assertTrue(files.stream().anyMatch(f -> f.getId().equals(file.getId())));

            // Delete file
            Response<BinaryData> deleteResponse = client.deleteFileWithResponse(file.getId(), new RequestOptions());
            assertEquals(200, deleteResponse.getStatusCode());
            FileDeletionStatus deletionStatus = deleteResponse.getValue().toObject(FileDeletionStatus.class);
            assertTrue(deletionStatus.isDeleted());
            assertEquals(deletionStatus.getId(), file.getId());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void fineTuningJsonFileOperationsWithResponse(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        uploadFineTuningJsonFileRunner((fileDetails, filePurpose) -> {
            // Upload file
            OpenAIFile file = client.uploadFile(fileDetails, filePurpose);
            assertNotNull(file);
            assertNotNull(file.getId());

            Response<BinaryData> getFileResponse = client.getFileWithResponse(file.getId(), new RequestOptions());
            assertEquals(200, getFileResponse.getStatusCode());
            OpenAIFile fileFromBackend = getFileResponse.getValue().toObject(OpenAIFile.class);
            assertFileEquals(file, fileFromBackend);

            // Get file by purpose
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.addQueryParam("purpose", FilePurpose.FINE_TUNE.toString());
            Response<BinaryData> listFilesResponse = client.listFilesWithResponse(requestOptions);
            assertEquals(200, listFilesResponse.getStatusCode());
            List<OpenAIFile> files = listFilesResponse.getValue().toObject(FileListResponse.class).getData();
            assertTrue(files.stream().anyMatch(f -> f.getId().equals(file.getId())));

            // Delete file
            Response<BinaryData> deleteResponse = client.deleteFileWithResponse(file.getId(), new RequestOptions());
            assertEquals(200, deleteResponse.getStatusCode());
            FileDeletionStatus deletionStatus = deleteResponse.getValue().toObject(FileDeletionStatus.class);
            assertTrue(deletionStatus.isDeleted());
            assertEquals(deletionStatus.getId(), file.getId());
        });
    }
}
