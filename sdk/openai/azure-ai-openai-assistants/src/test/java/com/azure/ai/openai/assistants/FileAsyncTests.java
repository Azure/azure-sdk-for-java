// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.FileDeletionStatus;
import com.azure.ai.openai.assistants.models.FileListResponse;
import com.azure.ai.openai.assistants.models.FilePurpose;
import com.azure.ai.openai.assistants.models.OpenAIFile;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileAsyncTests extends AssistantsClientTestBase {

    private AssistantsAsyncClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantTextFileOperations(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        uploadAssistantImageFileRunner(uploadFileRequest -> {
            StepVerifier.create(
                client.uploadFile(uploadFileRequest)
                    // Upload file
                    .flatMap(uploadedFile -> {
                        assertNotNull(uploadedFile);
                        assertNotNull(uploadedFile.getId());
                        return client.getFile(uploadedFile.getId()).zipWith(Mono.just(uploadedFile));
                    })
                    // Compare uploaded file with file from backend
                    .flatMap(tuple -> {
                        OpenAIFile fileFromBackend = tuple.getT1();
                        OpenAIFile uploadedFile = tuple.getT2();

                        assertNotNull(uploadedFile);
                        assertNotNull(fileFromBackend);
                        assertFileEquals(uploadedFile, fileFromBackend);
                        return client.listFiles(FilePurpose.ASSISTANTS).zipWith(Mono.just(uploadedFile));
                    })
                    // Check for existence of file when fetched by purpose
                    .flatMap(tuple -> {
                        FileListResponse files = tuple.getT1();
                        OpenAIFile uploadedFile = tuple.getT2();

                        assertTrue(files.getData().stream().anyMatch(f -> f.getId().equals(uploadedFile.getId())));
                        return client.deleteFile(uploadedFile.getId()).zipWith(Mono.just(uploadedFile));
                    }))
                // File deletion
                .assertNext(tuple -> {
                    FileDeletionStatus deletionStatus = tuple.getT1();
                    OpenAIFile file = tuple.getT2();

                    assertNotNull(deletionStatus);
                    assertNotNull(deletionStatus.getId());
                    assertEquals(file.getId(), deletionStatus.getId());
                })
                .verifyComplete();
        });
    }

}
