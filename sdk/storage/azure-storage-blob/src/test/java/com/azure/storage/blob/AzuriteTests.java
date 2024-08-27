// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.specialized.BlobClientBase;
import com.azure.storage.blob.specialized.BlobLeaseClient;
import com.azure.storage.blob.specialized.BlobLeaseClientBuilder;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AzuriteTests extends BlobTestBase {
    private static final String[] AZURITE_ENDPOINTS = new String[]{
        "https://127.0.0.1:10000/devstoreaccount1",
        "https://azure-storage-emulator-azurite:10000/devstoreaccount1"};

    /*
     The credential information for Azurite is static and documented in numerous locations, therefore it is okay to
     have this "secret" written into public code.
     */
    private static final StorageSharedKeyCredential AZURITE_CREDENTIAL = new StorageSharedKeyCredential(
        "devstoreaccount1",
        "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==");

    private static String getAzuriteBlobConnectionString(String azuriteEndpoint) {
        return "DefaultEndpointsProtocol=https;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint="
            + azuriteEndpoint + ";";
    }

    private BlobServiceClient getAzuriteServiceClient(String azuriteEndpoint) {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .endpoint(azuriteEndpoint)
            .credential(AZURITE_CREDENTIAL);

        instrument(builder);

        return builder.buildClient();
    }

    private static void validateBlobClient(BlobClientBase client, String blobUrl) {
        assertEquals(client.getAccountName(), "devstoreaccount1");
        assertEquals(client.getContainerName(), "container");
        assertEquals(client.getBlobName(), "blob");
        assertEquals(client.getBlobUrl(), blobUrl);
    }

    @ParameterizedTest
    @MethodSource("azuriteURLParserSupplier")
    public void azuriteURLParser(String endpoint, String scheme, String host, String accountName,
        String blobContainerName, String blobName, String expectedUrl) throws MalformedURLException {
        BlobUrlParts parts = BlobUrlParts.parse(new URL(endpoint));

        assertEquals(parts.getScheme(), scheme);
        assertEquals(parts.getHost(), host);
        assertEquals(parts.getAccountName(), accountName);
        assertEquals(parts.getBlobContainerName(), blobContainerName);
        assertEquals(parts.getBlobName(), blobName);
        assertEquals(parts.toUrl().toString(), expectedUrl);
    }

    private static Stream<Arguments> azuriteURLParserSupplier() {
        return Stream.of(
            Arguments.of("http://127.0.0.1:10000/devstoreaccount1", "http", "127.0.0.1:10000",
                "devstoreaccount1", null, null, "http://127.0.0.1:10000/devstoreaccount1"),
            Arguments.of("http://127.0.0.1:10000/devstoreaccount1/container", "http", "127.0.0.1:10000",
                "devstoreaccount1", "container", null, "http://127.0.0.1:10000/devstoreaccount1/container"),
            Arguments.of("http://127.0.0.1:10000/devstoreaccount1/container/blob", "http", "127.0.0.1:10000",
                "devstoreaccount1", "container", "blob", "http://127.0.0.1:10000/devstoreaccount1/container/blob"),
            Arguments.of("http://localhost:10000/devstoreaccount1", "http", "localhost:10000",
                "devstoreaccount1", null, null, "http://localhost:10000/devstoreaccount1"),
            Arguments.of("http://localhost:10000/devstoreaccount1/container", "http", "localhost:10000",
                "devstoreaccount1", "container", null, "http://localhost:10000/devstoreaccount1/container"),
            Arguments.of("http://localhost:10000/devstoreaccount1/container/blob", "http", "localhost:10000",
                "devstoreaccount1", "container", "blob", "http://localhost:10000/devstoreaccount1/container/blob"),
            Arguments.of("http://localhost:10000/devstoreaccount1/container/path/to]a blob", "http",
                "localhost:10000", "devstoreaccount1", "container", "path/to]a blob",
                "http://localhost:10000/devstoreaccount1/container/path%2Fto%5Da%20blob"),
            Arguments.of("http://localhost:10000/devstoreaccount1/container/path%2Fto%5Da%20blob", "http",
                "localhost:10000", "devstoreaccount1", "container", "path/to]a blob",
                "http://localhost:10000/devstoreaccount1/container/path%2Fto%5Da%20blob"),
            Arguments.of("http://localhost:10000/devstoreaccount1/container/斑點", "http", "localhost:10000",
                "devstoreaccount1", "container", "斑點",
                "http://localhost:10000/devstoreaccount1/container/%E6%96%91%E9%BB%9E"),
            Arguments.of("http://localhost:10000/devstoreaccount1/container/%E6%96%91%E9%BB%9E", "http",
                "localhost:10000", "devstoreaccount1", "container", "斑點",
                "http://localhost:10000/devstoreaccount1/container/%E6%96%91%E9%BB%9E"),
            Arguments.of("http://azure-storage-emulator-azurite:10000/devstoreaccount1", "http",
                "azure-storage-emulator-azurite:10000", "devstoreaccount1", null, null,
                "http://azure-storage-emulator-azurite:10000/devstoreaccount1"),
            Arguments.of("http://azure-storage-emulator-azurite:10000/devstoreaccount1/container", "http",
                "azure-storage-emulator-azurite:10000", "devstoreaccount1", "container", null,
                "http://azure-storage-emulator-azurite:10000/devstoreaccount1/container"),
            Arguments.of("http://azure-storage-emulator-azurite:10000/devstoreaccount1/container/blob",
                "http", "azure-storage-emulator-azurite:10000", "devstoreaccount1", "container", "blob",
                "http://azure-storage-emulator-azurite:10000/devstoreaccount1/container/blob")
        );

    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    public void useDevelopmentStorageTrue(int index) {
        String originalUseDevelopmentStorage = System.getProperty("UseDevelopmentStorage");
        System.setProperty("UseDevelopmentStorage", "true");

        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
            .connectionString(getAzuriteBlobConnectionString(AZURITE_ENDPOINTS[index]))
            .buildClient();

        assertEquals(serviceClient.getAccountUrl(), AZURITE_ENDPOINTS[index]);
        assertEquals(serviceClient.getAccountName(), "devstoreaccount1");

        // cleanup:
        if (originalUseDevelopmentStorage != null) {
            System.setProperty("UseDevelopmentStorage", originalUseDevelopmentStorage);
        } else {
            System.clearProperty("UseDevelopmentStorage");
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    public void azuriteURLConstructingServiceClient(int index) {
        BlobServiceClient serviceClient = getAzuriteServiceClient(AZURITE_ENDPOINTS[index]);

        assertEquals(serviceClient.getAccountUrl(), AZURITE_ENDPOINTS[index]);
        assertEquals(serviceClient.getAccountName(), "devstoreaccount1");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    public void azuriteURLGetContainerClient(int index) {
        BlobContainerClient containerClient = getAzuriteServiceClient(AZURITE_ENDPOINTS[index])
            .getBlobContainerClient("container");

        assertEquals(containerClient.getAccountName(), "devstoreaccount1");
        assertEquals(containerClient.getBlobContainerName(), "container");
        assertEquals(containerClient.getBlobContainerUrl(), AZURITE_ENDPOINTS[index] + "/container");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    public void azuriteURLConstructContainerClient(int index) {
        BlobContainerClient containerClient = new BlobContainerClientBuilder()
            .endpoint(AZURITE_ENDPOINTS[index] + "/container")
            .credential(AZURITE_CREDENTIAL)
            .buildClient();

        assertEquals(containerClient.getAccountName(), "devstoreaccount1");
        assertEquals(containerClient.getBlobContainerName(), "container");
        assertEquals(containerClient.getBlobContainerUrl(), AZURITE_ENDPOINTS[index] + "/container");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    public void azuriteURLConstructContainerClientWithDefaultAzureCredential(int index) {
        BlobContainerClient containerClient = new BlobContainerClientBuilder()
            .endpoint(AZURITE_ENDPOINTS[index] + "/container")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        assertEquals(containerClient.getAccountName(), "devstoreaccount1");
        assertEquals(containerClient.getBlobContainerName(), "container");
        assertEquals(containerClient.getBlobContainerUrl(), AZURITE_ENDPOINTS[index] + "/container");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    public void azuriteURLGetBlobClient(int index) {
        BlobClient blobClient = getAzuriteServiceClient(AZURITE_ENDPOINTS[index])
            .getBlobContainerClient("container")
            .getBlobClient("blob");

        validateBlobClient(blobClient,
            AZURITE_ENDPOINTS[index] + "/container/blob");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    public void azuriteURLConstructBlobClient(int index) {
        BlobClient blobClient = new BlobClientBuilder()
            .endpoint(AZURITE_ENDPOINTS[index] + "/container/blob")
            .credential(AZURITE_CREDENTIAL)
            .buildClient();

        validateBlobClient(blobClient,
            AZURITE_ENDPOINTS[index] + "/container/blob");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    public void azuriteURLConstructBlobClientDefaultAzureCredential(int index) {
        BlobClient blobClient = new BlobClientBuilder()
            .endpoint(AZURITE_ENDPOINTS[index] + "/container/blob")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        validateBlobClient(blobClient,
            AZURITE_ENDPOINTS[index] + "/container/blob");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    public void azuriteURLGetSpecializedClients(int index) {
        BlobClient blobClient = getAzuriteServiceClient(AZURITE_ENDPOINTS[index])
            .getBlobContainerClient("container")
            .getBlobClient("blob");

        validateBlobClient(blobClient.getAppendBlobClient(),
            AZURITE_ENDPOINTS[index] + "/container/blob");
        validateBlobClient(blobClient.getBlockBlobClient(),
            AZURITE_ENDPOINTS[index] + "/container/blob");
        validateBlobClient(blobClient.getPageBlobClient(),
            AZURITE_ENDPOINTS[index] + "/container/blob");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    public void azuriteURLConstructSpecializedClient(int index) {
        SpecializedBlobClientBuilder specializedClientBuilder = new SpecializedBlobClientBuilder()
            .endpoint(AZURITE_ENDPOINTS[index] + "/container/blob")
            .credential(AZURITE_CREDENTIAL);

        validateBlobClient(specializedClientBuilder.buildAppendBlobClient(),
            AZURITE_ENDPOINTS[index] + "/container/blob");
        validateBlobClient(specializedClientBuilder.buildBlockBlobClient(),
            AZURITE_ENDPOINTS[index] + "/container/blob");
        validateBlobClient(specializedClientBuilder.buildPageBlobClient(),
            AZURITE_ENDPOINTS[index] + "/container/blob");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    public void azuriteURLGetLeaseClient(int index) {
        BlobContainerClient containerClient = getAzuriteServiceClient(AZURITE_ENDPOINTS[index])
            .getBlobContainerClient("container");
        BlobClient blobClient = containerClient.getBlobClient("blob");

        BlobLeaseClient containerLeaseClient = new BlobLeaseClientBuilder()
            .containerClient(containerClient)
            .buildClient();

        assertEquals(containerLeaseClient.getAccountName(), "devstoreaccount1");
        assertEquals(containerLeaseClient.getResourceUrl(), AZURITE_ENDPOINTS[index] + "/container");

        BlobLeaseClient blobLeaseClient = new BlobLeaseClientBuilder()
            .blobClient(blobClient)
            .buildClient();

        assertEquals(blobLeaseClient.getAccountName(), "devstoreaccount1");
        assertEquals(blobLeaseClient.getResourceUrl(), AZURITE_ENDPOINTS[index] + "/container/blob");
    }

    @Disabled("Enable once the April 2023 release of azure-core-http-netty happens")
    @Test
    public void uploadEmptyFile() throws IOException {
        String containerName = generateContainerName();
        BlobServiceClient serviceClient = getServiceClient(AZURITE_CREDENTIAL,
            "http://127.0.0.1:10000/devstoreaccount1");
        BlobClient blobClient = serviceClient.createBlobContainer(containerName).getBlobClient(generateBlobName());
        File file = getRandomFile(0);
        file.deleteOnExit();

        assertDoesNotThrow(() -> blobClient.uploadFromFile(file.toPath().toString(), true));

        // cleanup:
        serviceClient.deleteBlobContainer(containerName);
    }
}
