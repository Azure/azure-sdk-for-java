// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.implementation.models.FileDetails;
import com.azure.ai.agents.persistent.implementation.models.UploadFileRequest;
import com.azure.ai.agents.persistent.models.FileInfo;
import com.azure.ai.agents.persistent.models.FilePurpose;
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
import java.util.List;

import static com.azure.ai.agents.persistent.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VectorStoreFilesAsyncClientTest extends ClientTestBase {

    private PersistentAgentsAdministrationClientBuilder clientBuilder;
    private VectorStoresAsyncClient vectorStoresAsyncClient;
    private VectorStoreFilesAsyncClient vectorStoreFilesAsyncClient;
    private FilesAsyncClient filesAsyncClient;
    private List<VectorStore> vectorStores;
    private List<FileInfo> uploadedFiles;
    private List<VectorStoreFile> vectorStoreFiles;
    private VectorStore vectorStore;

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        vectorStoresAsyncClient = clientBuilder.buildVectorStoresAsyncClient();
        vectorStoreFilesAsyncClient = clientBuilder.buildVectorStoreFilesAsyncClient();
        filesAsyncClient = clientBuilder.buildFilesAsyncClient();
        vectorStores = new ArrayList<>();
        uploadedFiles = new ArrayList<>();
        vectorStoreFiles = new ArrayList<>();
    }

    // Helper method to create a vector store.
    private Mono<VectorStore> createVectorStore(String name) {
        return vectorStoresAsyncClient.createVectorStore(null, name, null, null, null, null).map(store -> {
            assertNotNull(store, "Vector store should not be null");
            vectorStores.add(store);
            vectorStore = store;
            return store;
        });
    }

    // Helper method to upload a file using FilesAsyncClient.
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

    private Mono<VectorStoreFile> createVectorStoreFile(String vectorStoreId, String fileId) {
        return vectorStoreFilesAsyncClient.createVectorStoreFile(vectorStoreId, fileId, null, null)
            .map(vectorStoreFile -> {
                assertNotNull(vectorStoreFile, "Vector store file should not be null");
                vectorStoreFiles.add(vectorStoreFile);
                return vectorStoreFile;
            });
    }

    // Test uploading a vector store file.
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testCreateVectorStoreFile(HttpClient httpClient) {
        setup(httpClient);

        StepVerifier
            .create(createVectorStore("VectorStoreFilesAsyncClientTest")
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

        StepVerifier
            .create(
                createVectorStore(
                    "VectorStoreFilesAsyncClientTest")
                        .flatMap(
                            store -> uploadFile("get_vector_store_file_async.txt")
                                .flatMap(uploadedFile -> createVectorStoreFile(store.getId(), uploadedFile.getId()))
                                .flatMap(vectorStoreFile -> vectorStoreFilesAsyncClient
                                    .getVectorStoreFile(store.getId(), vectorStoreFile.getId()))))
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
            .create(createVectorStore("VectorStoreFilesAsyncClientTest")
                .flatMap(store -> uploadFile("list_vector_store_file1_async.txt")
                    .flatMap(uploadedFile1 -> createVectorStoreFile(store.getId(), uploadedFile1.getId()))
                    .then(uploadFile("list_vector_store_file2_async.txt"))
                    .flatMap(uploadedFile2 -> createVectorStoreFile(store.getId(), uploadedFile2.getId()))
                    .then(vectorStoreFilesAsyncClient.listVectorStoreFiles(store.getId()).take(10).collectList())))
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

        StepVerifier
            .create(
                createVectorStore(
                    "VectorStoreFilesAsyncClientTest")
                        .flatMap(
                            store -> uploadFile("delete_vector_store_file_async.txt")
                                .flatMap(uploadedFile -> createVectorStoreFile(store.getId(), uploadedFile.getId()))
                                .flatMap(vectorStoreFile -> vectorStoreFilesAsyncClient
                                    .deleteVectorStoreFile(store.getId(), vectorStoreFile.getId()))))
            .assertNext(deletionStatus -> {
                assertNotNull(deletionStatus, "Deletion status should not be null");
                assertTrue(deletionStatus.isDeleted(), "Vector store file should be marked as deleted");
            })
            .verifyComplete();
    }

    @AfterEach
    public void cleanup() {
        // Clean up all created vector stores
        for (VectorStore vectorStore : vectorStores) {
            try {
                vectorStoresAsyncClient.deleteVectorStore(vectorStore.getId()).block();
            } catch (Exception e) {
                System.out.println("Failed to clean up vector store: " + vectorStore.getName());
                System.out.println(e.getMessage());
            }
        }
        // Clean up all uploaded files.
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
