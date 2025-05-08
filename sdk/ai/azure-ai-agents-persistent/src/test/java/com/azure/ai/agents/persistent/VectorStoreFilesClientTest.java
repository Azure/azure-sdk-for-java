// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.implementation.models.FileDetails;
import com.azure.ai.agents.persistent.implementation.models.UploadFileRequest;
import com.azure.ai.agents.persistent.models.FileDeletionStatus;
import com.azure.ai.agents.persistent.models.FileInfo;
import com.azure.ai.agents.persistent.models.FilePurpose;
import com.azure.ai.agents.persistent.models.VectorStore;
import com.azure.ai.agents.persistent.models.VectorStoreFile;
import com.azure.ai.agents.persistent.models.VectorStoreFileDeletionStatus;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.ArrayList;
import java.util.List;

import static com.azure.ai.agents.persistent.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.agents.persistent.TestUtils.size;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VectorStoreFilesClientTest extends ClientTestBase {

    private PersistentAgentsAdministrationClientBuilder clientBuilder;
    private VectorStoresClient vectorStoresClient;
    private VectorStoreFilesClient vectorStoreFilesClient;
    private FilesClient filesClient;
    private List<VectorStore> vectorStores;
    private List<FileInfo> uploadedFiles;
    private List<VectorStoreFile> vectorStoreFiles;
    private VectorStore vectorStore;

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        vectorStoresClient = clientBuilder.buildVectorStoresClient();
        vectorStoreFilesClient = clientBuilder.buildVectorStoreFilesClient();
        filesClient = clientBuilder.buildFilesClient();
        vectorStores = new ArrayList<>();
        uploadedFiles = new ArrayList<>();
        vectorStoreFiles = new ArrayList<>();
        vectorStore = createVectorStore("VectorStoreFilesClientTest");
    }

    // Helper method to create a vector store.
    private VectorStore createVectorStore(String name) {
        VectorStore vectorStore = vectorStoresClient.createVectorStore(null, name, null, null, null, null);
        assertNotNull(vectorStore, "Vector store should not be null");
        vectorStores.add(vectorStore);
        return vectorStore;
    }

    // Helper method to upload a file using FilesClient.
    private FileInfo uploadFile(String fileName) {
        FileDetails fileDetails
            = new FileDetails(BinaryData.fromString("Sample text for vector store file upload")).setFilename(fileName);
        UploadFileRequest uploadFileRequest = new UploadFileRequest(fileDetails, FilePurpose.AGENTS);
        FileInfo uploadedFile = filesClient.uploadFile(uploadFileRequest);
        assertNotNull(uploadedFile, "Uploaded file should not be null");
        uploadedFiles.add(uploadedFile);
        return uploadedFile;
    }

    private VectorStoreFile createVectorStoreFile(String vectorStoreId, String fileId) {
        VectorStoreFile vectorStoreFile
            = vectorStoreFilesClient.createVectorStoreFile(vectorStoreId, fileId, null, null);
        assertNotNull(vectorStoreFile, "Vector store file should not be null");

        vectorStoreFiles.add(vectorStoreFile);
        return vectorStoreFile;
    }

    // Test uploading a vector store file.
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testCreateVectorStoreFile(HttpClient httpClient) {
        setup(httpClient);

        FileInfo uploadedFile = uploadFile("create_vector_store_file.txt");
        VectorStoreFile vectorStoreFile = createVectorStoreFile(vectorStore.getId(), uploadedFile.getId());

        assertNotNull(vectorStoreFile.getId(), "Vector store file ID should not be null");
    }

    // Test retrieving a vector store file.
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testGetVectorStoreFile(HttpClient httpClient) {
        setup(httpClient);

        FileInfo uploadedFile = uploadFile("get_vector_store_file.txt");
        VectorStoreFile vectorStoreFile = createVectorStoreFile(vectorStore.getId(), uploadedFile.getId());

        VectorStoreFile retrievedFile
            = vectorStoreFilesClient.getVectorStoreFile(vectorStore.getId(), vectorStoreFile.getId());
        assertNotNull(retrievedFile, "Retrieved vector store file should not be null");
        assertEquals(vectorStoreFile.getId(), retrievedFile.getId(), "Vector store file IDs should match");
    }

    // Test listing vector store files.
    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testListVectorStoreFiles(HttpClient httpClient) {
        setup(httpClient);

        FileInfo uploadedFile1 = uploadFile("list_vector_store_file1.txt");
        FileInfo uploadedFile2 = uploadFile("list_vector_store_file2.txt");

        // Upload two files.
        createVectorStoreFile(vectorStore.getId(), uploadedFile1.getId());
        createVectorStoreFile(vectorStore.getId(), uploadedFile2.getId());

        PagedIterable<VectorStoreFile> vectorStoreFiles
            = vectorStoreFilesClient.listVectorStoreFiles(vectorStore.getId());

        assertNotNull(vectorStoreFiles, "Vector store files list should not be null");
        assertTrue(size(vectorStoreFiles) > 0, "There should be at least one vector store file");
    }

    // Test deleting a vector store file.
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testDeleteVectorStoreFile(HttpClient httpClient) {
        setup(httpClient);

        FileInfo uploadedFile = uploadFile("delete_vector_store_file.txt");
        VectorStoreFile vectorStoreFile = createVectorStoreFile(vectorStore.getId(), uploadedFile.getId());

        VectorStoreFileDeletionStatus deletionStatus
            = vectorStoreFilesClient.deleteVectorStoreFile(vectorStore.getId(), vectorStoreFile.getId());
        assertNotNull(deletionStatus, "Deletion status should not be null");
        assertTrue(deletionStatus.isDeleted(), "Vector store file should be marked as deleted");
    }

    @AfterEach
    public void cleanup() {
        // Clean up all created vector stores
        for (VectorStore vectorStore : vectorStores) {
            try {
                vectorStoresClient.deleteVectorStore(vectorStore.getId());
            } catch (Exception e) {
                System.out.println("Failed to clean up vector store: " + vectorStore.getName());
                System.out.println(e.getMessage());
            }
        }
        // Clean up all uploaded files.
        for (FileInfo fileInfo : uploadedFiles) {
            try {
                FileDeletionStatus deletionStatus = filesClient.deleteFile(fileInfo.getId());
            } catch (Exception e) {
                System.out.println("Failed to clean up file: " + fileInfo.getFilename());
                System.out.println(e.getMessage());
            }
        }
    }
}
