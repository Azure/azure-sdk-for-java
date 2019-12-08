package com.azure.storage.queue

import com.azure.storage.common.StorageSharedKeyCredential

class AzuriteTests extends APISpec {
    String azuriteEndpoint = "http://127.0.0.1:10001/devstoreaccount1"

    /*
     * The credential information for Azurite is static and documented in numerous locations, therefore it is okay to have this "secret" written into public code.
     */
    StorageSharedKeyCredential azuriteCredential = new StorageSharedKeyCredential("devstoreaccount1", "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==")
    String azuriteBlobConnectionString = "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;QueueEndpoint=http://127.0.0.1:10001/devstoreaccount1;"

    def "UseDevelopmentStorage true"() {
        setup:
        def originalUseDevelopmentStorage = System.getProperty("UseDevelopmentStorage")
        System.setProperty("UseDevelopmentStorage", "true")

        when:
        def serviceClient = new QueueServiceClientBuilder()
            .connectionString(azuriteBlobConnectionString)
            .buildClient()

        then:
        serviceClient.getQueueServiceUrl() == "http://127.0.0.1:10001/devstoreaccount1"
        serviceClient.getAccountName() == "devstoreaccount1"

        cleanup:
        if (originalUseDevelopmentStorage != null) {
            System.setProperty("UseDevelopmentStorage", originalUseDevelopmentStorage)
        } else {
            System.clearProperty("UseDevelopmentStorage")
        }
    }

    def "Azurite URL constructing service client with connection string"() {
        when:
        def serviceClient = new QueueServiceClientBuilder()
            .connectionString(azuriteBlobConnectionString)
            .buildClient()

        then:
        serviceClient.getAccountName() == "devstoreaccount1"
        serviceClient.getQueueServiceUrl() == "http://127.0.0.1:10001/devstoreaccount1"
    }

    def "Azurite URL constructing service client with credential"() {
        when:
        def serviceClient = new QueueServiceClientBuilder()
            .credential(azuriteCredential)
            .endpoint(azuriteEndpoint)
            .buildClient()

        then:
        serviceClient.getAccountName() == "devstoreaccount1"
        serviceClient.getQueueServiceUrl() == "http://127.0.0.1:10001/devstoreaccount1"
    }

    def "Azurite URL get queue client"() {
        when:
        def queueClient = new QueueServiceClientBuilder()
            .connectionString(azuriteBlobConnectionString)
            .buildClient()
            .getQueueClient("queue")

        then:
        queueClient.getAccountName() == "devstoreaccount1"
        queueClient.getQueueName() == "queue"
        queueClient.getQueueUrl() == "http://127.0.0.1:10001/devstoreaccount1/queue"
    }

    def "Azurite URL constructing queue client with connection string"() {
        when:
        def queueClient = new QueueClientBuilder()
            .connectionString(azuriteBlobConnectionString)
            .queueName("queue")
            .buildClient()

        then:
        queueClient.getAccountName() == "devstoreaccount1"
        queueClient.getQueueName() == "queue"
        queueClient.getQueueUrl() == "http://127.0.0.1:10001/devstoreaccount1/queue"
    }

    def "Azurite URL constructing queue client with credential"() {
        when:
        def queueClient = new QueueClientBuilder()
            .credential(azuriteCredential)
            .endpoint(azuriteEndpoint)
            .queueName("queue")
            .buildClient()

        then:
        queueClient.getAccountName() == "devstoreaccount1"
        queueClient.getQueueName() == "queue"
        queueClient.getQueueUrl() == "http://127.0.0.1:10001/devstoreaccount1/queue"
    }
}
