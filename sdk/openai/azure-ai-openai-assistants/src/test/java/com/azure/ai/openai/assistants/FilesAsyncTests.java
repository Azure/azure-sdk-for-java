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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilesAsyncTests extends AssistantsClientTestBase {

    private AssistantsAsyncClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantTextFileOperations(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        uploadAssistantTextFileRunner((fileDetails, filePurpose) -> {
            StepVerifier.create(
                client.uploadFile(fileDetails, filePurpose)
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
                        List<OpenAIFile> files = tuple.getT1();
                        OpenAIFile uploadedFile = tuple.getT2();

                        assertTrue(files.stream().anyMatch(f -> f.getId().equals(uploadedFile.getId())));
                        return client.deleteFile(uploadedFile.getId()).zipWith(Mono.just(uploadedFile));
                    }))
                // File deletion
                .assertNext(tuple -> {
                    FileDeletionStatus deletionStatus = tuple.getT1();
                    OpenAIFile file = tuple.getT2();

                    assertNotNull(deletionStatus);
                    assertNotNull(deletionStatus.getId());
                    assertTrue(deletionStatus.isDeleted());
                    assertEquals(file.getId(), deletionStatus.getId());
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantImageFileOperations(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        uploadAssistantImageFileRunner((fileDetails, filePurpose) -> {
            StepVerifier.create(
                    client.uploadFile(fileDetails, filePurpose)
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
                            List<OpenAIFile> files = tuple.getT1();
                            OpenAIFile uploadedFile = tuple.getT2();

                            assertTrue(files.stream().anyMatch(f -> f.getId().equals(uploadedFile.getId())));
                            return client.deleteFile(uploadedFile.getId()).zipWith(Mono.just(uploadedFile));
                        }))
                // File deletion
                .assertNext(tuple -> {
                    FileDeletionStatus deletionStatus = tuple.getT1();
                    OpenAIFile file = tuple.getT2();

                    assertNotNull(deletionStatus);
                    assertNotNull(deletionStatus.getId());
                    assertTrue(deletionStatus.isDeleted());
                    assertEquals(file.getId(), deletionStatus.getId());
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void fineTuningJsonFileOperations(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        uploadFineTuningJsonFileRunner((fileDetails, filePurpose) -> {
            StepVerifier.create(
                    client.uploadFile(fileDetails, filePurpose)
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
                            return client.listFiles(FilePurpose.FINE_TUNE).zipWith(Mono.just(uploadedFile));
                        })
                        // Check for existence of file when fetched by purpose
                        .flatMap(tuple -> {
                            List<OpenAIFile> files = tuple.getT1();
                            OpenAIFile uploadedFile = tuple.getT2();

                            assertTrue(files.stream().anyMatch(f -> f.getId().equals(uploadedFile.getId())));
                            return client.deleteFile(uploadedFile.getId()).zipWith(Mono.just(uploadedFile));
                        }))
                // File deletion
                .assertNext(tuple -> {
                    FileDeletionStatus deletionStatus = tuple.getT1();
                    OpenAIFile file = tuple.getT2();

                    assertNotNull(deletionStatus);
                    assertNotNull(deletionStatus.getId());
                    assertTrue(deletionStatus.isDeleted());
                    assertEquals(file.getId(), deletionStatus.getId());
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantTextFileOperationsWithResponse(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        uploadAssistantTextFileRunner((fileDetails, filePurpose) -> {
            StepVerifier.create(
                    client.uploadFile(fileDetails, filePurpose)
                        // Upload file
                        .flatMap(uploadedFile -> {
                            assertNotNull(uploadedFile);
                            assertNotNull(uploadedFile.getId());
                            return client.getFileWithResponse(uploadedFile.getId(), new RequestOptions()).zipWith(Mono.just(uploadedFile));
                        })
                        // Compare uploaded file with file from backend
                        .flatMap(tuple -> {
                            Response<BinaryData> response = tuple.getT1();
                            OpenAIFile uploadedFile = tuple.getT2();

                            assertNotNull(uploadedFile);
                            assertNotNull(response);
                            assertEquals(200, response.getStatusCode());
                            OpenAIFile fileFromBackend = response.getValue().toObject(OpenAIFile.class);
                            assertFileEquals(uploadedFile, fileFromBackend);

                            RequestOptions requestOptions = new RequestOptions();
                            requestOptions.addQueryParam("purpose", FilePurpose.ASSISTANTS.toString());
                            return client.listFilesWithResponse(requestOptions).zipWith(Mono.just(uploadedFile));
                        })
                        // Check for existence of file when fetched by purpose
                        .flatMap(tuple -> {
                            Response<BinaryData> response = tuple.getT1();
                            OpenAIFile uploadedFile = tuple.getT2();

                            assertEquals(200, response.getStatusCode());
                            List<OpenAIFile> files = response.getValue().toObject(FileListResponse.class).getData();
                            assertTrue(files.stream().anyMatch(f -> f.getId().equals(uploadedFile.getId())));
                            return client.deleteFileWithResponse(uploadedFile.getId(), new RequestOptions()).zipWith(Mono.just(uploadedFile));
                        }))
                // File deletion
                .assertNext(tuple -> {
                    Response<BinaryData> response = tuple.getT1();
                    OpenAIFile file = tuple.getT2();

                    assertEquals(200, response.getStatusCode());
                    FileDeletionStatus deletionStatus = response.getValue().toObject(FileDeletionStatus.class);

                    assertNotNull(deletionStatus);
                    assertNotNull(deletionStatus.getId());
                    assertTrue(deletionStatus.isDeleted());
                    assertEquals(file.getId(), deletionStatus.getId());
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantImageFileOperationsWithResponse(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        uploadAssistantImageFileRunner((fileDetails, filePurpose) -> {
            StepVerifier.create(
                    client.uploadFile(fileDetails, filePurpose)
                        // Upload file
                        .flatMap(uploadedFile -> {
                            assertNotNull(uploadedFile);
                            assertNotNull(uploadedFile.getId());
                            return client.getFileWithResponse(uploadedFile.getId(), new RequestOptions()).zipWith(Mono.just(uploadedFile));
                        })
                        // Compare uploaded file with file from backend
                        .flatMap(tuple -> {
                            Response<BinaryData> response = tuple.getT1();
                            OpenAIFile uploadedFile = tuple.getT2();

                            assertNotNull(uploadedFile);
                            assertNotNull(response);
                            assertEquals(200, response.getStatusCode());
                            OpenAIFile fileFromBackend = response.getValue().toObject(OpenAIFile.class);
                            assertFileEquals(uploadedFile, fileFromBackend);

                            RequestOptions requestOptions = new RequestOptions();
                            requestOptions.addQueryParam("purpose", FilePurpose.ASSISTANTS.toString());
                            return client.listFilesWithResponse(requestOptions).zipWith(Mono.just(uploadedFile));
                        })
                        // Check for existence of file when fetched by purpose
                        .flatMap(tuple -> {
                            Response<BinaryData> response = tuple.getT1();
                            OpenAIFile uploadedFile = tuple.getT2();

                            assertEquals(200, response.getStatusCode());
                            List<OpenAIFile> files = response.getValue().toObject(FileListResponse.class).getData();
                            assertTrue(files.stream().anyMatch(f -> f.getId().equals(uploadedFile.getId())));
                            return client.deleteFileWithResponse(uploadedFile.getId(), new RequestOptions()).zipWith(Mono.just(uploadedFile));
                        }))
                // File deletion
                .assertNext(tuple -> {
                    Response<BinaryData> response = tuple.getT1();
                    OpenAIFile file = tuple.getT2();

                    assertEquals(200, response.getStatusCode());
                    FileDeletionStatus deletionStatus = response.getValue().toObject(FileDeletionStatus.class);

                    assertNotNull(deletionStatus);
                    assertNotNull(deletionStatus.getId());
                    assertTrue(deletionStatus.isDeleted());
                    assertEquals(file.getId(), deletionStatus.getId());
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void fineTuningJsonFileOperationsWithResponse(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        uploadFineTuningJsonFileRunner((fileDetails, filePurpose) -> {
            StepVerifier.create(
                    client.uploadFile(fileDetails, filePurpose)
                        .flatMap(uploadedFile -> {
                            assertNotNull(uploadedFile);
                            assertNotNull(uploadedFile.getId());
                            return client.getFileWithResponse(uploadedFile.getId(), new RequestOptions()).zipWith(Mono.just(uploadedFile));
                        })
                        // Compare uploaded file with file from backend
                        .flatMap(tuple -> {
                            Response<BinaryData> response = tuple.getT1();
                            OpenAIFile uploadedFile = tuple.getT2();

                            assertNotNull(uploadedFile);
                            assertNotNull(response);
                            assertEquals(200, response.getStatusCode());
                            OpenAIFile fileFromBackend = response.getValue().toObject(OpenAIFile.class);
                            assertFileEquals(uploadedFile, fileFromBackend);

                            RequestOptions requestOptions = new RequestOptions();
                            requestOptions.addQueryParam("purpose", FilePurpose.FINE_TUNE.toString());
                            return client.listFilesWithResponse(requestOptions).zipWith(Mono.just(uploadedFile));
                        })
                        // Check for existence of file when fetched by purpose
                        .flatMap(tuple -> {
                            Response<BinaryData> response = tuple.getT1();
                            OpenAIFile uploadedFile = tuple.getT2();

                            assertEquals(200, response.getStatusCode());
                            List<OpenAIFile> files = response.getValue().toObject(FileListResponse.class).getData();
                            assertTrue(files.stream().anyMatch(f -> f.getId().equals(uploadedFile.getId())));
                            return client.deleteFileWithResponse(uploadedFile.getId(), new RequestOptions()).zipWith(Mono.just(uploadedFile));
                        }))
                // File deletion
                .assertNext(tuple -> {
                    Response<BinaryData> response = tuple.getT1();
                    OpenAIFile file = tuple.getT2();

                    assertEquals(200, response.getStatusCode());
                    FileDeletionStatus deletionStatus = response.getValue().toObject(FileDeletionStatus.class);

                    assertNotNull(deletionStatus);
                    assertNotNull(deletionStatus.getId());
                    assertTrue(deletionStatus.isDeleted());
                    assertEquals(file.getId(), deletionStatus.getId());
                })
                .verifyComplete();
        });
    }
}
