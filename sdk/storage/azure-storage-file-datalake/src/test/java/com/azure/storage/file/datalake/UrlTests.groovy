package com.azure.storage.file.datalake

class UrlTests extends APISpec {

    def "test custom url"() {
        when:
        def customUrl = "https://www.customstorageurl.com"
        DataLakeServiceClient client = new DataLakeServiceClientBuilder()
            .endpoint(customUrl)
            .credential(primaryCredential)
            .buildClient()
        then:
        client.getAccountUrl() == client.blobServiceClient.getAccountUrl()
    }

    def "test short host url"() {
        when:
        def shortHostUrl = "https://account.core.windows.net"
        DataLakeServiceClient client = new DataLakeServiceClientBuilder()
            .endpoint(shortHostUrl)
            .credential(primaryCredential)
            .buildClient()
        then:
        client.getAccountUrl() == client.blobServiceClient.getAccountUrl()
    }

    def "test ip style url"() {
        when:
        def ipStyleUrl = "https://0.0.0.0/account"
        DataLakeServiceClient client = new DataLakeServiceClientBuilder()
            .endpoint(ipStyleUrl)
            .credential(primaryCredential)
            .buildClient()
        then:
        client.getAccountUrl() == client.blobServiceClient.getAccountUrl()
    }

    def "test invalid service url"() {
        when:
        def invalidServiceUrl = "https://account.file.core.windows.net"
        DataLakeServiceClient client = new DataLakeServiceClientBuilder()
            .endpoint(invalidServiceUrl)
            .credential(primaryCredential)
            .buildClient()
        then:
        client.getAccountUrl() == client.blobServiceClient.getAccountUrl()
    }

    def "test blob service url"() {
        when:
        def blobUrl = "https://account.blob.core.windows.net"
        def dfsUrl = "https://account.dfs.core.windows.net"
        DataLakeServiceClient serviceClient = new DataLakeServiceClientBuilder()
            .endpoint(blobUrl)
            .credential(primaryCredential)
            .buildClient()
        DataLakeFileSystemClient fileSystemClient = new DataLakeFileSystemClientBuilder()
            .endpoint(blobUrl + "/container")
            .credential(primaryCredential)
            .buildClient()
        DataLakeFileClient pathClient = new DataLakePathClientBuilder()
            .endpoint(blobUrl + "/container/blob")
            .credential(primaryCredential)
            .buildFileClient()
        then:
        serviceClient.getAccountUrl() == dfsUrl
        serviceClient.blobServiceClient.getAccountUrl() == blobUrl
        fileSystemClient.getFileSystemUrl() == dfsUrl + "/container"
        fileSystemClient.blobContainerClient.getBlobContainerUrl() == blobUrl + "/container"
        pathClient.getPathUrl() == dfsUrl + "/container/blob"
        pathClient.blockBlobClient.getBlobUrl() == blobUrl + "/container/blob"
    }

    def "test dfs service url"() {
        when:
        def blobUrl = "https://account.blob.core.windows.net"
        def dfsUrl = "https://account.dfs.core.windows.net"
        DataLakeServiceClient serviceClient = new DataLakeServiceClientBuilder()
            .endpoint(blobUrl)
            .credential(primaryCredential)
            .buildClient()
        DataLakeFileSystemClient fileSystemClient = new DataLakeFileSystemClientBuilder()
            .endpoint(blobUrl + "/container")
            .credential(primaryCredential)
            .buildClient()
        DataLakeFileClient pathClient = new DataLakePathClientBuilder()
            .endpoint(blobUrl + "/container/blob")
            .credential(primaryCredential)
            .buildFileClient()
        then:
        serviceClient.getAccountUrl() == dfsUrl
        serviceClient.blobServiceClient.getAccountUrl() == blobUrl
        fileSystemClient.getFileSystemUrl() == dfsUrl + "/container"
        fileSystemClient.blobContainerClient.getBlobContainerUrl() == blobUrl + "/container"
        pathClient.getPathUrl() == dfsUrl + "/container/blob"
        pathClient.blockBlobClient.getBlobUrl() == blobUrl + "/container/blob"
    }
}
