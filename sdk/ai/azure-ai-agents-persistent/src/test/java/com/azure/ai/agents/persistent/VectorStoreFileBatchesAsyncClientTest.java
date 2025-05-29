// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.implementation.models.FileDetails;
import com.azure.ai.agents.persistent.implementation.models.UploadFileRequest;
import com.azure.ai.agents.persistent.models.FileInfo;
import com.azure.ai.agents.persistent.models.FilePurpose;
import com.azure.ai.agents.persistent.models.VectorStore;
import com.azure.ai.agents.persistent.models.VectorStoreFileBatch;
import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.agents.persistent.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class VectorStoreFileBatchesAsyncClientTest extends ClientTestBase {

    private PersistentAgentsAdministrationClientBuilder clientBuilder;
    private VectorStoresAsyncClient vectorStoresAsyncClient;
    private VectorStoreFileBatchesAsyncClient vectorStoreFileBatchesAsyncClient;
    private FilesAsyncClient filesAsyncClient;
    private List<VectorStore> vectorStores = new ArrayList<>();
    private List<FileInfo> uploadedFiles = new ArrayList<>();

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        vectorStoresAsyncClient = clientBuilder.buildVectorStoresAsyncClient();
        vectorStoreFileBatchesAsyncClient = clientBuilder.buildVectorStoreFileBatchesAsyncClient();
        filesAsyncClient = clientBuilder.buildFilesAsyncClient();
    }

    private Mono<FileInfo> uploadFile(String fileName) {
        FileDetails fileDetails
            = new FileDetails(BinaryData.fromString("Sample text for testing upload")).setFilename(fileName);
        UploadFileRequest uploadFileRequest = new UploadFileRequest(fileDetails, FilePurpose.AGENTS);

        return filesAsyncClient.uploadFile(uploadFileRequest).map(uploadedFile -> {
            uploadedFiles.add(uploadedFile);
            assertNotNull(uploadedFile, "Uploaded file should not be null");
            return uploadedFile;
        });
    }

    // Helper method to create a vector store
    private Mono<VectorStore> createVectorStore(String name) {
        return vectorStoresAsyncClient.createVectorStore(null, name, null, null, null, null).map(vectorStore -> {
            assertNotNull(vectorStore, "Vector store should not be null");
            vectorStores.add(vectorStore);
            return vectorStore;
        });
    }

    private Mono<VectorStoreFileBatch> createVectorStoreFileBatch(String vectorStoreId, List<String> fileIds) {
        return vectorStoreFileBatchesAsyncClient.createVectorStoreFileBatch(vectorStoreId, fileIds, null, null)
            .map(fileBatch -> {
                assertNotNull(fileBatch, "Vector store file batch should not be null");
                return fileBatch;
            });
    }

    // Test creation of a vector store file batch
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testCreateVectorStoreFileBatch(HttpClient httpClient) {
        setup(httpClient);

        String vectorStoreName = "test_create_vector_store_file_batch_async";

        StepVerifier.create(createVectorStore(vectorStoreName)
            .flatMap(vectorStore -> uploadFile("testCreateVectorStoreFileBatchAsync.txt").flatMap(uploadedFile -> {
                List<String> fileIds = Arrays.asList(uploadedFile.getId());
                return vectorStoreFileBatchesAsyncClient.createVectorStoreFileBatch(vectorStore.getId(), fileIds, null,
                    null);
            }))).assertNext(createdBatch -> {
                assertNotNull(createdBatch.getId(), "Vector store file batch ID should not be null");
            }).verifyComplete();
    }

    // Test retrieval of a vector store file batch
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testGetVectorStoreFileBatch(HttpClient httpClient) {
        setup(httpClient);
        String vectorStoreName = "test_get_vector_store_file_batch_async";

        AtomicReference<String> batchIdRef = new AtomicReference<>();

        StepVerifier.create(createVectorStore(vectorStoreName)
            .flatMap(vectorStore -> uploadFile("testGetVectorStoreFileBatchAsync.txt").flatMap(uploadedFile -> {
                List<String> fileIds = Arrays.asList(uploadedFile.getId());
                return vectorStoreFileBatchesAsyncClient.createVectorStoreFileBatch(vectorStore.getId(), fileIds, null,
                    null);
            })
                .doOnNext(batch -> batchIdRef.set(batch.getId()))
                .flatMap(batch -> vectorStoreFileBatchesAsyncClient.getVectorStoreFileBatch(vectorStore.getId(),
                    batch.getId()))))
            .assertNext(retrievedBatch -> {
                assertNotNull(retrievedBatch, "Retrieved file batch should not be null");
                assertEquals(batchIdRef.get(), retrievedBatch.getId(), "File batch IDs should match");
            })
            .verifyComplete();
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testListVectorStoreFileBatchFiles(HttpClient httpClient) {
        setup(httpClient);
        String vectorStoreName = "test_list_vector_store_file_batches_async";

        StepVerifier.create(createVectorStore(vectorStoreName)
            .flatMap(vectorStore -> uploadFile("testListVectorStoreFileBatchesAsync.txt").flatMap(uploadedFile -> {
                List<String> fileIds = Arrays.asList(uploadedFile.getId());
                return vectorStoreFileBatchesAsyncClient.createVectorStoreFileBatch(vectorStore.getId(), fileIds, null,
                    null);
            })
                .flatMap(batch -> vectorStoreFileBatchesAsyncClient
                    .listVectorStoreFileBatchFiles(vectorStore.getId(), batch.getId())
                    .take(10)
                    .collectList())))
            .assertNext(files -> {
                assertNotNull(files, "Vector store batch files list should not be null");
                // Files might not be processed yet, so we don't assert count
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testCancelVectorStoreFileBatch(HttpClient httpClient) {
        setup(httpClient);
        String vectorStoreName = "test_cancel_vector_store_file_batch_async";

        StepVerifier.create(createVectorStore(vectorStoreName)
            .flatMap(vectorStore -> uploadFile("testCancelVectorStoreFileBatchAsync.txt").flatMap(uploadedFile -> {
                List<String> fileIds = Arrays.asList(uploadedFile.getId());
                return vectorStoreFileBatchesAsyncClient.createVectorStoreFileBatch(vectorStore.getId(), fileIds, null,
                    null);
            })
                .flatMap(batch -> vectorStoreFileBatchesAsyncClient.cancelVectorStoreFileBatch(vectorStore.getId(),
                    batch.getId()))))
            .assertNext(cancelledBatch -> {
                assertNotNull(cancelledBatch, "Cancelled batch should not be null");
            })
            .verifyComplete();
    }

    @AfterEach
    public void cleanup() {
        // Clean up uploaded files
        for (FileInfo fileInfo : uploadedFiles) {
            try {
                filesAsyncClient.deleteFile(fileInfo.getId()).block();
            } catch (Exception e) {
                System.out.println("Failed to clean up file: " + fileInfo.getFilename());
                System.out.println(e.getMessage());
            }
        }

        // Clean up vector stores
        for (VectorStore vectorStore : vectorStores) {
            try {
                vectorStoresAsyncClient.deleteVectorStore(vectorStore.getId()).block();
            } catch (Exception e) {
                System.out.println("Failed to clean up vector store: " + vectorStore.getName());
                System.out.println(e.getMessage());
            }
        }
    }
}
