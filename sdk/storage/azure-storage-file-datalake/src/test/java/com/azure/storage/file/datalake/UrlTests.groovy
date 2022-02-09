package com.azure.storage.file.datalake

import com.azure.storage.common.StorageSharedKeyCredential
import spock.lang.Specification
import spock.lang.Unroll

class UrlTests extends Specification {

    StorageSharedKeyCredential credential = new StorageSharedKeyCredential("accountname", "accountkey");

    @Unroll
    def "test urls that should not change for datalake"() {
        when:
        DataLakeServiceClient client = new DataLakeServiceClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildClient()
        then:
        client.getAccountUrl() == client.blobServiceClient.getAccountUrl()

        where:
        endpoint                                    | _
        "https://www.customstorageurl.com"          | _
        "https://account.core.windows.net"          | _
        "https://0.0.0.0/account"                   | _
        "https://account.file.core.windows.net"     | _
        "https://www.customdfsstorageurl.com"       | _
        "https://dfsaccount.core.windows.net"       | _
        "https://0.0.0.0/dfsaccount"                | _
        "https://dfsaccount.file.core.windows.net"  | _
    }

    @Unroll
    def "test correct service url set"() {
        when:
        DataLakeServiceClient serviceClient = new DataLakeServiceClientBuilder()
            .endpoint(url)
            .credential(credential)
            .buildClient()
        DataLakeFileSystemClient fileSystemClient = new DataLakeFileSystemClientBuilder()
            .endpoint(url + "/container")
            .credential(credential)
            .buildClient()
        DataLakeFileClient pathClient = new DataLakePathClientBuilder()
            .endpoint(url + "/container/blob")
            .credential(credential)
            .buildFileClient()
        then:
        // In either case the dfs url should be set to the dfs client and blob url set to the blob client
        serviceClient.getAccountUrl() == expectedDfsUrl
        serviceClient.blobServiceClient.getAccountUrl() == expectedBlobUrl
        fileSystemClient.getFileSystemUrl() == expectedDfsUrl + "/container"
        fileSystemClient.blobContainerClient.getBlobContainerUrl() == expectedBlobUrl + "/container"
        pathClient.getPathUrl() == expectedDfsUrl + "/container/blob"
        pathClient.blockBlobClient.getBlobUrl() == expectedBlobUrl + "/container/blob"

        where:
        url                                        || expectedBlobUrl                            | expectedDfsUrl
        "https://account.blob.core.windows.net"    || "https://account.blob.core.windows.net"    | "https://account.dfs.core.windows.net"
        "https://dfsaccount.blob.core.windows.net" || "https://dfsaccount.blob.core.windows.net" | "https://dfsaccount.dfs.core.windows.net"
        "https://account.dfs.core.windows.net"     || "https://account.blob.core.windows.net"    | "https://account.dfs.core.windows.net"
        "https://dfsaccount.dfs.core.windows.net"  || "https://dfsaccount.blob.core.windows.net" | "https://dfsaccount.dfs.core.windows.net"
    }
}
