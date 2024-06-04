// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.VectorStoreFileBatch;
import com.azure.core.http.HttpClient;
import com.azure.core.util.CoreUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.openai.assistants.models.FilePurpose.ASSISTANTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureVectorStoreAsyncTests extends AssistantsClientTestBase {
    private AssistantsAsyncClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    @Disabled("Azure resource won't able to create a vector store with files")
    public void createVectorStore(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        createVectorStoreAsyncRunner(vectorStoreDetails -> {
            AtomicReference<String> vectorStoreId = new AtomicReference<>();
            StepVerifier.create(client.createVectorStore(vectorStoreDetails))
                    .assertNext(vectorStore -> {
                        assertNotNull(vectorStore);
                        vectorStoreId.set(vectorStore.getId());
                        assertNotNull(vectorStore.getId());
                    })
                    .verifyComplete();

            // clean up the created vector store
//            deleteVectorStores(client, vectorStoreId.get());
        }, client);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void updateVectorStoreName(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        modifyVectorStoreAsyncRunner((vectorStoreId, vectorStoreDetails) -> {
            // Modify Vector Store
            StepVerifier.create(client.modifyVectorStore(vectorStoreId, vectorStoreDetails))
                    .assertNext(vectorStore -> {
                        assertNotNull(vectorStore);
                        assertEquals(vectorStoreId, vectorStore.getId());
                        assertEquals(vectorStoreDetails.getName(), vectorStore.getName());
                    })
                    .verifyComplete();

            // clean up the created vector store
            deleteVectorStores(client, vectorStoreId);
        }, client);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void getVectorStore(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        getVectorStoreAsyncRunner((vectorStoreId) -> {
            // Get Vector Store
            StepVerifier.create(client.getVectorStore(vectorStoreId))
                    .assertNext(vectorStore -> {
                        assertNotNull(vectorStore);
                        assertEquals(vectorStoreId, vectorStore.getId());
                    })
                    .verifyComplete();

            // clean up the created vector store
            deleteVectorStores(client, vectorStoreId);
        }, client);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void deleteVectorStore(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        deleteVectorStoreAsyncRunner((vectorStoreId) -> {
            // Delete Vector Store
            StepVerifier.create(client.deleteVectorStore(vectorStoreId))
                    .assertNext(deletionStatus -> {
                        assertTrue(deletionStatus.isDeleted());
                        assertEquals(deletionStatus.getId(), vectorStoreId);
                    })
                    .verifyComplete();
        }, client);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listVectorStore(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        listVectorStoreAsyncRunner((store1, store2) -> {
            // List Vector Stores
            StepVerifier.create(client.listVectorStores())
                    .assertNext(vectorStores -> {
                        assertNotNull(vectorStores);
                        assertFalse(vectorStores.getData().isEmpty());
                        vectorStores.getData().forEach(vectorStore -> {
                            assertNotNull(vectorStore.getId());
                            assertNotNull(vectorStore.getCreatedAt());
                        });
                    })
                    .verifyComplete();
            // clean up the created vector stores
            deleteVectorStores(client, store1.getId(), store2.getId());
        }, client);
    }

    // Vector Store with Files
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    @Disabled("Azure resource won't able to create a vector store with files")
    public void createVectorStoreFile(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        createVectorStoreWithFileAsyncRunner((storeId, fileId) -> {
            StepVerifier.create(client.createVectorStoreFile(storeId, fileId))
                    .assertNext(vectorStoreFile -> {
                        assertNotNull(vectorStoreFile);
                        assertNotNull(vectorStoreFile.getId());
                    })
                    .verifyComplete();
            // clean up the created vector store
            deleteVectorStores(client, storeId);
            client.deleteFile(fileId).block();
        }, client);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    @Disabled("Azure resource won't able to create a vector store with files")
    public void getVectorStoreFile(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        getVectorStoreFileAsyncRunner((vectorStoreFile, fileId) -> {
            String storeId = vectorStoreFile.getVectorStoreId();
            // Get Vector Store File

            StepVerifier.create(client.getVectorStoreFile(storeId, fileId))
                    .assertNext(vectorStoreFileResponse -> {
                        assertNotNull(vectorStoreFileResponse);
                        assertEquals(fileId, vectorStoreFileResponse.getId());
                    })
                    .verifyComplete();

            // clean up the created vector store
            deleteVectorStores(client, storeId);
            client.deleteFile(fileId).block();
        }, client);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    @Disabled("Azure resource won't able to create a vector store with files")
    public void listVectorStoreFiles(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        listVectorStoreFilesAsyncRunner((vectorStoreFile1, vectorStoreFile2) -> {
            String storeId = vectorStoreFile1.getVectorStoreId();
            // List Vector Store Files
            StepVerifier.create(client.listVectorStoreFiles(storeId))
                    .assertNext(vectorStoreFiles -> {
                        assertNotNull(vectorStoreFiles);
                        assertFalse(vectorStoreFiles.getData().isEmpty());
                        vectorStoreFiles.getData().forEach(vectorStoreFile -> {
                            assertNotNull(vectorStoreFile.getId());
                            assertNotNull(vectorStoreFile.getCreatedAt());
                        });
                    })
                    .verifyComplete();

            // clean up the created vector stores
            deleteVectorStores(client, storeId);
            client.deleteFile(vectorStoreFile1.getId()).block();
            client.deleteFile(vectorStoreFile2.getId()).block();
        }, client);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    @Disabled("Azure resource won't able to create a vector store with files")
    public void deleteVectorStoreFile(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        deleteVectorStoreFileAsyncRunner((vectorStoreFile, fileId) -> {
            String storeId = vectorStoreFile.getVectorStoreId();
            // Delete Vector Store File
            StepVerifier.create(client.deleteVectorStoreFile(storeId, fileId))
                    .assertNext(deletionStatus -> {
                        assertTrue(deletionStatus.isDeleted());
                        assertEquals(deletionStatus.getId(), fileId);
                    })
                    .verifyComplete();

            // clean up the created vector store
            deleteVectorStores(client, storeId);
            client.deleteFile(fileId).block();
        }, client);
    }

    // Vector Store File Batch
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    @Disabled("Azure resource won't able to create a vector store with files")
    public void createVectorStoreFileBatch(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        createVectorStoreWithFileBatchAsyncRunner((storeId, batchFiles) -> {
            StepVerifier.create(client.createVectorStoreFileBatch(storeId, batchFiles))
                    .assertNext(vectorStoreFileBatch -> {
                        assertNotNull(vectorStoreFileBatch);
                        assertNotNull(vectorStoreFileBatch.getId());
                        assertEquals(2, vectorStoreFileBatch.getFileCounts().getTotal());
                    })
                    .verifyComplete();
            // clean up the created vector store
            deleteVectorStores(client, storeId);
            for (String fileId : batchFiles) {
                client.deleteFile(fileId).block();
            }
        }, client);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    @Disabled("Azure resource won't able to create a vector store with files")
    public void getVectorStoreFileBatch(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);

        getVectorStoreFileBatchAsyncRunner(vectorStoreFileBatch -> {
            String storeId = vectorStoreFileBatch.getVectorStoreId();
            String batchId = vectorStoreFileBatch.getId();
            int totalFileCounts = vectorStoreFileBatch.getFileCounts().getTotal();

            // Get Vector Store File
            StepVerifier.create(client.getVectorStoreFileBatch(storeId, vectorStoreFileBatch.getId()))
                    .assertNext(vectorStoreFileBatchResponse -> {
                        assertNotNull(vectorStoreFileBatchResponse);
                        assertEquals(storeId, vectorStoreFileBatchResponse.getVectorStoreId());
                        assertEquals(batchId, vectorStoreFileBatchResponse.getId());
                        assertEquals(totalFileCounts, vectorStoreFileBatchResponse.getFileCounts().getTotal());
                    })
                    .verifyComplete();

            // clean up the created vector store
            deleteVectorStores(client, storeId);
            // TODO: delete the files
        }, client);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    @Disabled("Azure resource won't able to create a vector store with files")
    public void listVectorStoreFilesBatch(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        listVectorStoreFilesBatchFilesAsyncRunner((storeId, batchId) -> {
            List<String> files = new ArrayList<>();
            // List Vector Store Files
            StepVerifier.create(client.listVectorStoreFileBatchFiles(storeId, batchId))
                    .assertNext(vectorStoreFiles -> {
                        assertNotNull(vectorStoreFiles);
                        assertFalse(vectorStoreFiles.getData().isEmpty());
                        vectorStoreFiles.getData().forEach(vectorStoreFile -> {
                            String fid = vectorStoreFile.getId();
                            files.add(fid);
                            assertNotNull(fid);
                            assertNotNull(vectorStoreFile.getCreatedAt());
                        });
                    })
                    .verifyComplete();

            // clean up the created vector stores
            deleteVectorStores(client, storeId);
            for (String fid : files) {
                client.deleteFile(fid).block();
            }
        }, client);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    @Disabled("Azure resource won't able to create a vector store with files")
    public void cancelVectorStoreFileBatch(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        cancelVectorStoreFileBatchAsyncRunner(vectorStore -> {
            String storeId = vectorStore.getId();
            String fileId = uploadFileAsync(client, "20210203_alphabet_10K.pdf", ASSISTANTS);
            String fileId2 = uploadFileAsync(client, "20220924_aapl_10k.pdf", ASSISTANTS);
            VectorStoreFileBatch vectorStoreFileBatch = client.createVectorStoreFileBatch(storeId, Arrays.asList(fileId, fileId2)).block();
            // Cancel Vector Store File
            StepVerifier.create(client.cancelVectorStoreFileBatch(storeId, vectorStoreFileBatch.getId()))
                    .assertNext(cancelVectorStoreFileBatch -> {
                        assertNotNull(cancelVectorStoreFileBatch);
                        assertEquals(vectorStoreFileBatch.getId(), cancelVectorStoreFileBatch.getId());
                        assertEquals(vectorStoreFileBatch.getFileCounts().getTotal(), cancelVectorStoreFileBatch.getFileCounts().getTotal());
                    })
                    .verifyComplete();

            // TODO: investigate why the status is not CANCELLED but FAILED instead
//            assertEquals(VectorStoreFileStatus.CANCELLED, cancelVectorStoreFileBatch.getStatus());
            // clean up the created vector store
            deleteVectorStores(client, storeId);
        }, client);
    }

    private void deleteVectorStores(AssistantsAsyncClient client, String... vectorStoreIds) {
        if (!CoreUtils.isNullOrEmpty(vectorStoreIds)) {
            for (String vectorStoreId : vectorStoreIds) {
                client.deleteVectorStore(vectorStoreId).block();
            }
        }
    }
}
