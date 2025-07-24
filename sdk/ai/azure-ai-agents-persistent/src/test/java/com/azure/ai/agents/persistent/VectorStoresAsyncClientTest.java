// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.FileDetails;
import com.azure.ai.agents.persistent.models.FileInfo;
import com.azure.ai.agents.persistent.models.FilePurpose;
import com.azure.ai.agents.persistent.models.UploadFileRequest;
import com.azure.ai.agents.persistent.models.VectorStore;
import com.azure.ai.agents.persistent.models.VectorStoreFile;
import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.agents.persistent.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VectorStoresAsyncClientTest extends ClientTestBase {

    private PersistentAgentsClientBuilder clientBuilder;
    private VectorStoresAsyncClient vectorStoresAsyncClient;
    private FilesAsyncClient filesAsyncClient;
    private List<VectorStore> vectorStores = new ArrayList<>();
    private List<FileInfo> uploadedFiles = new ArrayList<>();
    private List<VectorStoreFile> vectorStoreFiles = new ArrayList<>();

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        PersistentAgentsAsyncClient agentsAsyncClient = clientBuilder.buildAsyncClient();
        vectorStoresAsyncClient = agentsAsyncClient.getVectorStoresAsyncClient();
        filesAsyncClient = agentsAsyncClient.getFilesAsyncClient();
    }

    private Mono<FileInfo> uploadFile(String fileName) {
        FileDetails fileDetails
            = new FileDetails(BinaryData.fromString("Sample text for vector store file upload")).setFilename(fileName);
        UploadFileRequest uploadFileRequest = new UploadFileRequest(fileDetails, FilePurpose.AGENTS);

        return filesAsyncClient.uploadFile(uploadFileRequest).map(uploadedFile -> {
            assertNotNull(uploadedFile, "Uploaded file should not be null");
            uploadedFiles.add(uploadedFile);
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

    private Mono<VectorStoreFile> createVectorStoreFile(String vectorStoreId, String fileId) {
        return vectorStoresAsyncClient.createVectorStoreFile(vectorStoreId, fileId, null, null).map(vectorStoreFile -> {
            assertNotNull(vectorStoreFile, "Vector store file should not be null");
            vectorStoreFiles.add(vectorStoreFile);
            return vectorStoreFile;
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testCreateVectorStore(HttpClient httpClient) {
        setup(httpClient);
        String vectorStoreName = "test_create_vector_store_async";

        StepVerifier.create(createVectorStore(vectorStoreName)).assertNext(vectorStore -> {
            assertNotNull(vectorStore, "Vector store should not be null");
            assertEquals(vectorStoreName, vectorStore.getName(), "Vector store name should match");
        }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testGetVectorStore(HttpClient httpClient) {
        setup(httpClient);
        String vectorStoreName = "test_get_vector_store_async";

        StepVerifier
            .create(createVectorStore(vectorStoreName)
                .flatMap(createdStore -> vectorStoresAsyncClient.getVectorStore(createdStore.getId())))
            .assertNext(retrievedVectorStore -> {
                assertNotNull(retrievedVectorStore, "Retrieved vector store should not be null");
                assertEquals(vectorStoreName, retrievedVectorStore.getName(), "Vector store names should match");
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testModifyVectorStore(HttpClient httpClient) {
        setup(httpClient);
        String vectorStoreName = "test_modify_vector_store_async";
        String updatedName = vectorStoreName + "_updated";
        Map<String, String> metadata = new HashMap<>();
        metadata.put("environment", "test");

        StepVerifier
            .create(createVectorStore(vectorStoreName).flatMap(createdStore -> vectorStoresAsyncClient
                .modifyVectorStore(createdStore.getId(), updatedName, null, metadata)))
            .assertNext(modifiedVectorStore -> {
                assertNotNull(modifiedVectorStore, "Modified vector store should not be null");
                assertEquals(updatedName, modifiedVectorStore.getName(), "Vector store name should be updated");
                assertNotNull(modifiedVectorStore.getMetadata(), "Vector store metadata should not be null");
                assertEquals("test", modifiedVectorStore.getMetadata().get("environment"),
                    "Metadata environment value should match");
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testListVectorStores(HttpClient httpClient) {
        setup(httpClient);
        String vectorStoreName = "test_list_vector_store_async";

        StepVerifier
            .create(createVectorStore(vectorStoreName)
                .then(Mono.defer(() -> vectorStoresAsyncClient.listVectorStores().take(10).collectList())))
            .assertNext(vectorStoreList -> {
                assertNotNull(vectorStoreList, "Vector store list should not be null");
                assertTrue(vectorStoreList.size() > 0, "Vector store list should not be empty");
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testDeleteVectorStore(HttpClient httpClient) {
        setup(httpClient);
        String vectorStoreName = "test_delete_vector_store_async";

        StepVerifier.create(createVectorStore(vectorStoreName).flatMap(createdStore -> {
            vectorStores.remove(createdStore);
            return vectorStoresAsyncClient.deleteVectorStore(createdStore.getId());
        })).verifyComplete();
    }

    // Test uploading a vector store file.
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testCreateVectorStoreFile(HttpClient httpClient) {
        setup(httpClient);

        StepVerifier
            .create(createVectorStore("vectorStoresAsyncClientTest")
                .flatMap(store -> uploadFile("create_vector_store_file_async.txt")
                    .flatMap(uploadedFile -> createVectorStoreFile(store.getId(), uploadedFile.getId()))))
            .assertNext(vectorStoreFile -> {
                assertNotNull(vectorStoreFile.getId(), "Vector store file ID should not be null");
            })
            .verifyComplete();
    }

    // Test retrieving a vector store file.
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testGetVectorStoreFile(HttpClient httpClient) {
        setup(httpClient);

        StepVerifier.create(createVectorStore("vectorStoresAsyncClientTest")
            .flatMap(store -> uploadFile("get_vector_store_file_async.txt")
                .flatMap(uploadedFile -> createVectorStoreFile(store.getId(), uploadedFile.getId()))
                .flatMap(vectorStoreFile -> vectorStoresAsyncClient.getVectorStoreFile(store.getId(),
                    vectorStoreFile.getId()))))
            .assertNext(retrievedFile -> {
                assertNotNull(retrievedFile, "Retrieved vector store file should not be null");
            })
            .verifyComplete();
    }

    // Test listing vector store files.
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testListVectorStoreFiles(HttpClient httpClient) {
        setup(httpClient);

        StepVerifier
            .create(createVectorStore("vectorStoresAsyncClientTest")
                .flatMap(store -> uploadFile("list_vector_store_file1_async.txt")
                    .flatMap(uploadedFile1 -> createVectorStoreFile(store.getId(), uploadedFile1.getId()))
                    .then(uploadFile("list_vector_store_file2_async.txt"))
                    .flatMap(uploadedFile2 -> createVectorStoreFile(store.getId(), uploadedFile2.getId()))
                    .then(vectorStoresAsyncClient.listVectorStoreFiles(store.getId()).take(10).collectList())))
            .assertNext(files -> {
                assertNotNull(files, "Vector store files list should not be null");
                assertTrue(!files.isEmpty(), "There should be at least one vector store file");
            })
            .verifyComplete();
    }

    // Test deleting a vector store file.
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testDeleteVectorStoreFile(HttpClient httpClient) {
        setup(httpClient);

        StepVerifier.create(createVectorStore("vectorStoresAsyncClientTest")
            .flatMap(store -> uploadFile("delete_vector_store_file_async.txt")
                .flatMap(uploadedFile -> createVectorStoreFile(store.getId(), uploadedFile.getId()))
                .flatMap(vectorStoreFile -> vectorStoresAsyncClient.deleteVectorStoreFile(store.getId(),
                    vectorStoreFile.getId()))))
            .verifyComplete();
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
                return vectorStoresAsyncClient.createVectorStoreFileBatch(vectorStore.getId(), fileIds, null, null);
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
                return vectorStoresAsyncClient.createVectorStoreFileBatch(vectorStore.getId(), fileIds, null, null);
            })
                .doOnNext(batch -> batchIdRef.set(batch.getId()))
                .flatMap(batch -> vectorStoresAsyncClient.getVectorStoreFileBatch(vectorStore.getId(), batch.getId()))))
            .assertNext(retrievedBatch -> {
                assertNotNull(retrievedBatch, "Retrieved file batch should not be null");
                assertEquals(batchIdRef.get(), retrievedBatch.getId(), "File batch IDs should match");
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testListVectorStoreFileBatchFiles(HttpClient httpClient) {
        setup(httpClient);
        String vectorStoreName = "test_list_vector_store_file_batches_async";

        StepVerifier.create(createVectorStore(vectorStoreName)
            .flatMap(vectorStore -> uploadFile("testListVectorStoreFileBatchesAsync.txt").flatMap(uploadedFile -> {
                List<String> fileIds = Arrays.asList(uploadedFile.getId());
                return vectorStoresAsyncClient.createVectorStoreFileBatch(vectorStore.getId(), fileIds, null, null);
            })
                .flatMap(
                    batch -> vectorStoresAsyncClient.listVectorStoreFileBatchFiles(vectorStore.getId(), batch.getId())
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
                return vectorStoresAsyncClient.createVectorStoreFileBatch(vectorStore.getId(), fileIds, null, null);
            })
                .flatMap(
                    batch -> vectorStoresAsyncClient.cancelVectorStoreFileBatch(vectorStore.getId(), batch.getId()))))
            .assertNext(cancelledBatch -> {
                assertNotNull(cancelledBatch, "Cancelled batch should not be null");
            })
            .verifyComplete();
    }

    @AfterEach
    public void cleanup() {
        for (VectorStore vectorStore : new ArrayList<>(vectorStores)) {
            try {
                vectorStoresAsyncClient.deleteVectorStore(vectorStore.getId()).block();
                vectorStores.remove(vectorStore);
            } catch (Exception e) {
                System.out.println("Failed to clean up vector store: " + vectorStore.getName());
                System.out.println(e.getMessage());
            }
        }
    }
}
