package com.azure.storage.file.datalake

import spock.lang.Unroll

class UrlTests extends APISpec {

    @Unroll
    def "test urls that should not change for datalake"() {
        when:
        DataLakeServiceClient client = new DataLakeServiceClientBuilder()
            .endpoint(endpoint)
            .credential(primaryCredential)
            .buildClient()
        then:
        client.getAccountUrl() == client.blobServiceClient.getAccountUrl()

        where:
        endpoint                                 | _
        "https://www.customstorageurl.com"       | _
        "https://account.core.windows.net"       | _
        "https://0.0.0.0/account"                | _
        "https://account.file.core.windows.net"  | _
    }

    @Unroll
    def "test correct service url set"() {
        when:
        def blobUrl = "https://account.blob.core.windows.net"
        def dfsUrl = "https://account.dfs.core.windows.net"

        def testUrl = blobUrl
        if (useDfsurl) {
            testUrl = dfsUrl
        }

        DataLakeServiceClient serviceClient = new DataLakeServiceClientBuilder()
            .endpoint(testUrl)
            .credential(primaryCredential)
            .buildClient()
        DataLakeFileSystemClient fileSystemClient = new DataLakeFileSystemClientBuilder()
            .endpoint(testUrl + "/container")
            .credential(primaryCredential)
            .buildClient()
        DataLakeFileClient pathClient = new DataLakePathClientBuilder()
            .endpoint(testUrl + "/container/blob")
            .credential(primaryCredential)
            .buildFileClient()
        then:
        // In either case the dfs url should be set to the dfs client and blob url set to the blob client
        serviceClient.getAccountUrl() == dfsUrl
        serviceClient.blobServiceClient.getAccountUrl() == blobUrl
        fileSystemClient.getFileSystemUrl() == dfsUrl + "/container"
        fileSystemClient.blobContainerClient.getBlobContainerUrl() == blobUrl + "/container"
        pathClient.getPathUrl() == dfsUrl + "/container/blob"
        pathClient.blockBlobClient.getBlobUrl() == blobUrl + "/container/blob"

        where:
        useDfsurl | _
        true      | _
        false     | _
    }
}
