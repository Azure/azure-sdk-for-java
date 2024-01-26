package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.FileListResponse;
import com.azure.ai.openai.assistants.models.OpenAIFile;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilesSyncTests extends AssistantsClientTestBase {

    private AssistantsClient client;


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantTextFileOperations(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        uploadAssistantTextFileRunner(uploadFileRequest -> {
            // Upload file
            OpenAIFile file = client.uploadFile(uploadFileRequest);
            assertNotNull(file);
            assertNotNull(file.getId());

            // Get single file
            OpenAIFile fileFromBackend = client.getFile(file.getId());
            assertFileEquals(file, fileFromBackend);

            // Get file by purpose
            FileListResponse files = client.listFiles(uploadFileRequest.getPurpose());
            assertTrue(files.getData().stream().anyMatch(f -> f.getId().equals(file.getId())));

            // Delete file
            client.deleteFile(file.getId());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantImageFileOperations(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        uploadAssistantImageFileRunner(uploadFileRequest -> {
            // Upload file
            OpenAIFile file = client.uploadFile(uploadFileRequest);
            assertNotNull(file);
            assertNotNull(file.getId());

            // Get single file
            OpenAIFile fileFromBackend = client.getFile(file.getId());
            assertFileEquals(file, fileFromBackend);

            // Get file by purpose
            FileListResponse files = client.listFiles(uploadFileRequest.getPurpose());
            assertTrue(files.getData().stream().anyMatch(f -> f.getId().equals(file.getId())));

            // Delete file
            client.deleteFile(file.getId());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void fineTuningJsonFileOperations(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        uploadFineTuningJsonFileRunner(uploadFileRequest -> {
            // Upload file
            OpenAIFile file = client.uploadFile(uploadFileRequest);
            assertNotNull(file);
            assertNotNull(file.getId());

            // Get single file
            OpenAIFile fileFromBackend = client.getFile(file.getId());
            assertFileEquals(file, fileFromBackend);

            // Get file by purpose
            FileListResponse files = client.listFiles(uploadFileRequest.getPurpose());
            assertTrue(files.getData().stream().anyMatch(f -> f.getId().equals(file.getId())));

            // Delete file
            client.deleteFile(file.getId());
        });
    }

    private static void assertFileEquals(OpenAIFile expected, OpenAIFile actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getFilename(), actual.getFilename());
        assertEquals(expected.getBytes(), actual.getBytes());
        assertEquals(expected.getPurpose(), actual.getPurpose());
        assertEquals(expected.getCreatedAt(), actual.getCreatedAt());
    }
}
