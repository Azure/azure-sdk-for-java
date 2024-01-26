package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.OpenAIFile;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class FilesSyncTests extends AssistantsClientTestBase {
    private AssistantsClient client;


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantTextFileOperations(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        uploadAssistantTextFileRunner(uploadFileRequest -> {
            OpenAIFile file = client.uploadFile(uploadFileRequest);

            client.deleteFile(file.getId());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantImageFileOperations(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        uploadAssistantImageFileRunner(uploadFileRequest -> {
            OpenAIFile file = client.uploadFile(uploadFileRequest);

            client.deleteFile(file.getId());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void fineTuningJsonFileOperations(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        uploadFineTuningJsonFileRunner(uploadFileRequest -> {
            OpenAIFile file = client.uploadFile(uploadFileRequest);

            client.deleteFile(file.getId());
        });
    }

}
