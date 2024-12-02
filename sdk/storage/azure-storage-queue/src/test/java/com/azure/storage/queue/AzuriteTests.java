// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests basic Azure Storage Queue functionality for Azurite.
 */
public class AzuriteTests extends QueueTestBase {
    private static final String[] AZURITE_ENDPOINTS = new String[] {
        "https://127.0.0.1:10000/devstoreaccount1",
        "https://azure-storage-emulator-azurite:10000/devstoreaccount1" };

    /*
     * The credential information for Azurite is static and documented in numerous locations, therefore it is okay to
     * have this "secret" written into public code.
     */
    private static final StorageSharedKeyCredential AZURITE_CREDENTIAL = new StorageSharedKeyCredential(
        "devstoreaccount1", "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==");

    private static String getAzuriteQueueConnectionString(String azuriteEndpoint) {
        return "DefaultEndpointsProtocol=https;AccountName=devstoreaccount1;"
            + "AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;"
            + "QueueEndpoint=" + azuriteEndpoint + ";";
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1 })
    public void useDevelopmentStorageTrue(int index) {
        String originalUseDevelopmentStorage = System.getProperty("UseDevelopmentStorage");

        try {
            System.setProperty("UseDevelopmentStorage", "true");
            QueueServiceClient serviceClient = new QueueServiceClientBuilder()
                .connectionString(getAzuriteQueueConnectionString(AZURITE_ENDPOINTS[index]))
                .buildClient();

            assertEquals(AZURITE_ENDPOINTS[index], serviceClient.getQueueServiceUrl());
            assertEquals("devstoreaccount1", serviceClient.getAccountName());
        } finally {
            if (originalUseDevelopmentStorage != null) {
                System.setProperty("UseDevelopmentStorage", originalUseDevelopmentStorage);
            } else {
                System.clearProperty("UseDevelopmentStorage");
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1 })
    public void azuriteUrlConstructingServiceClientWithConnectionString(int index) {
        QueueServiceClient serviceClient = new QueueServiceClientBuilder()
            .connectionString(getAzuriteQueueConnectionString(AZURITE_ENDPOINTS[index]))
            .buildClient();

        assertEquals("devstoreaccount1", serviceClient.getAccountName());
        assertEquals(AZURITE_ENDPOINTS[index], serviceClient.getQueueServiceUrl());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1 })
    public void azuriteUrlConstructingServiceClientWithDefaultAzureCredential(int index) {
        QueueServiceClient serviceClient = new QueueServiceClientBuilder().endpoint(AZURITE_ENDPOINTS[index])
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        assertEquals("devstoreaccount1", serviceClient.getAccountName());
        assertEquals(AZURITE_ENDPOINTS[index], serviceClient.getQueueServiceUrl());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1 })
    public void azuriteUrlConstructingServiceClientWithCredential(int index) {
        QueueServiceClient serviceClient = new QueueServiceClientBuilder().credential(AZURITE_CREDENTIAL)
            .endpoint(AZURITE_ENDPOINTS[index])
            .buildClient();

        assertEquals("devstoreaccount1", serviceClient.getAccountName());
        assertEquals(AZURITE_ENDPOINTS[index], serviceClient.getQueueServiceUrl());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1 })
    public void azuriteUrlGetQueueClient(int index) {
        QueueClient queueClient = new QueueServiceClientBuilder()
            .connectionString(getAzuriteQueueConnectionString(AZURITE_ENDPOINTS[index]))
            .buildClient()
            .getQueueClient("queue");

        assertEquals("devstoreaccount1", queueClient.getAccountName());
        assertEquals("queue", queueClient.getQueueName());
        assertEquals(AZURITE_ENDPOINTS[index] + "/queue", queueClient.getQueueUrl());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1 })
    public void azuriteUrlConstructingQueueClientWithConnectionString(int index) {
        QueueClient queueClient
            = new QueueClientBuilder().connectionString(getAzuriteQueueConnectionString(AZURITE_ENDPOINTS[index]))
                .queueName("queue")
                .buildClient();

        assertEquals("devstoreaccount1", queueClient.getAccountName());
        assertEquals("queue", queueClient.getQueueName());
        assertEquals(AZURITE_ENDPOINTS[index] + "/queue", queueClient.getQueueUrl());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1 })
    public void azuriteUrlConstructingQueueClientWithDefaultAzureCredential(int index) {
        QueueClient queueClient = new QueueClientBuilder().endpoint(AZURITE_ENDPOINTS[index])
            .credential(new DefaultAzureCredentialBuilder().build())
            .queueName("queue")
            .buildClient();

        assertEquals("devstoreaccount1", queueClient.getAccountName());
        assertEquals("queue", queueClient.getQueueName());
        assertEquals(AZURITE_ENDPOINTS[index] + "/queue", queueClient.getQueueUrl());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1 })
    public void azuriteUrlConstructingQueueClientWithCredential(int index) {
        QueueClient queueClient = new QueueClientBuilder().credential(AZURITE_CREDENTIAL)
            .endpoint(AZURITE_ENDPOINTS[index])
            .queueName("queue")
            .buildClient();

        assertEquals("devstoreaccount1", queueClient.getAccountName());
        assertEquals("queue", queueClient.getQueueName());
        assertEquals(AZURITE_ENDPOINTS[index] + "/queue", queueClient.getQueueUrl());
    }
}
