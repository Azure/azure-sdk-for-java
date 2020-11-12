package com.azure.storage.queue

import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.storage.common.StorageSharedKeyCredential
import spock.lang.Unroll

class AzuriteTests extends APISpec {
    String[] azuriteEndpoints = ["https://127.0.0.1:10000/devstoreaccount1",
                                 "https://azure-storage-emulator-azurite:10000/devstoreaccount1"]

    /*
     * The credential information for Azurite is static and documented in numerous locations, therefore it is okay to have this "secret" written into public code.
     */
    StorageSharedKeyCredential azuriteCredential = new StorageSharedKeyCredential("devstoreaccount1", "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==")
    private getAzuriteQueueConnectionString(String azuriteEndpoint) {
        return "DefaultEndpointsProtocol=https;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;QueueEndpoint=" + azuriteEndpoint + ";"
    }

    @Unroll
    def "UseDevelopmentStorage true"() {
        setup:
        def originalUseDevelopmentStorage = System.getProperty("UseDevelopmentStorage")
        System.setProperty("UseDevelopmentStorage", "true")

        when:
        def serviceClient = new QueueServiceClientBuilder()
            .connectionString(getAzuriteQueueConnectionString(azuriteEndpoints[index]))
            .buildClient()

        then:
        serviceClient.getQueueServiceUrl() == azuriteEndpoints[index]
        serviceClient.getAccountName() == "devstoreaccount1"

        cleanup:
        if (originalUseDevelopmentStorage != null) {
            System.setProperty("UseDevelopmentStorage", originalUseDevelopmentStorage)
        } else {
            System.clearProperty("UseDevelopmentStorage")
        }

        where:
        index | _
        0     | _
        1     | _
    }

    @Unroll
    def "Azurite URL constructing service client with connection string"() {
        when:
        def serviceClient = new QueueServiceClientBuilder()
            .connectionString(getAzuriteQueueConnectionString(azuriteEndpoints[index]))
            .buildClient()

        then:
        serviceClient.getAccountName() == "devstoreaccount1"
        serviceClient.getQueueServiceUrl() == azuriteEndpoints[index]

        where:
        index | _
        0     | _
        1     | _
    }

    @Unroll
    def "Azurite URL constructing service client with default azure credential"() {
        when:
        def serviceClient = new QueueServiceClientBuilder()
            .endpoint(azuriteEndpoints[index])
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient()

        then:
        serviceClient.getAccountName() == "devstoreaccount1"
        serviceClient.getQueueServiceUrl() == azuriteEndpoints[index]

        where:
        index | _
        0     | _
        1     | _
    }

    @Unroll
    def "Azurite URL constructing service client with credential"() {
        when:
        def serviceClient = new QueueServiceClientBuilder()
            .credential(azuriteCredential)
            .endpoint(azuriteEndpoints[index])
            .buildClient()

        then:
        serviceClient.getAccountName() == "devstoreaccount1"
        serviceClient.getQueueServiceUrl() == azuriteEndpoints[index]

        where:
        index | _
        0     | _
        1     | _
    }

    @Unroll
    def "Azurite URL get queue client"() {
        when:
        def queueClient = new QueueServiceClientBuilder()
        .connectionString(getAzuriteQueueConnectionString(azuriteEndpoints[index]))
            .buildClient()
            .getQueueClient("queue")

        then:
        queueClient.getAccountName() == "devstoreaccount1"
        queueClient.getQueueName() == "queue"
        queueClient.getQueueUrl() == azuriteEndpoints[index] + "/queue"

        where:
        index | _
        0     | _
        1     | _
    }

    @Unroll
    def "Azurite URL constructing queue client with connection string"() {
        when:
        def queueClient = new QueueClientBuilder()
            .connectionString(getAzuriteQueueConnectionString(azuriteEndpoints[index]))
            .queueName("queue")
            .buildClient()

        then:
        queueClient.getAccountName() == "devstoreaccount1"
        queueClient.getQueueName() == "queue"
        queueClient.getQueueUrl() == azuriteEndpoints[index] + "/queue"

        where:
        index | _
        0     | _
        1     | _
    }

    @Unroll
    def "Azurite URL constructing queue client with default azure credential"() {
        when:
        def queueClient = new QueueClientBuilder()
            .endpoint(azuriteEndpoints[index])
            .credential(new DefaultAzureCredentialBuilder().build())
            .queueName("queue")
            .buildClient()

        then:
        queueClient.getAccountName() == "devstoreaccount1"
        queueClient.getQueueName() == "queue"
        queueClient.getQueueUrl() == azuriteEndpoints[index] + "/queue"

        where:
        index | _
        0     | _
        1     | _
    }

    @Unroll
    def "Azurite URL constructing queue client with credential"() {
        when:
        def queueClient = new QueueClientBuilder()
            .credential(azuriteCredential)
            .endpoint(azuriteEndpoints[index])
            .queueName("queue")
            .buildClient()

        then:
        queueClient.getAccountName() == "devstoreaccount1"
        queueClient.getQueueName() == "queue"
        queueClient.getQueueUrl() == azuriteEndpoints[index] + "/queue"

        where:
        index | _
        0     | _
        1     | _
    }
}
