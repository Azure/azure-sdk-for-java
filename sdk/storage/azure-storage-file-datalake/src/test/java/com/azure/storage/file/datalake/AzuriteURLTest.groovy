// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake

import com.azure.core.http.policy.HttpLogDetailLevel
import com.azure.core.http.policy.HttpLogOptions
import com.azure.storage.blob.BlobUrlParts
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.file.datalake.specialized.DataLakeLeaseClientBuilder
import spock.lang.Unroll

class AzuriteURLTest extends APISpec {
    String azuriteEndpoint = "http://127.0.0.1:10000/devstoreaccount1"

    /*
     * The credential information for Azurite is static and documented in numerous locations, therefore it is okay to have this "secret" written into public code.
     */
    StorageSharedKeyCredential azuriteCredential = new StorageSharedKeyCredential("devstoreaccount1", "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==")

    private DataLakeServiceClient getAzuriteServiceClient() {
        def builder = new DataLakeServiceClientBuilder()
            .endpoint(azuriteEndpoint)
            .credential(azuriteCredential)

        instrument(builder)

        return builder.buildClient()
    }

    private static void validatePathClient(DataLakePathClient client, String accountName, String fileSystemName, String pathName, String pathUrl) {
        assert client.getAccountName() == accountName
        assert client.getFileSystemName() == fileSystemName
        assert client.getObjectName() == pathName
        assert client.getPathUrl() == pathUrl
    }

    @Unroll
    def "Azurite URLParser"() {
        when:
        BlobUrlParts parts = BlobUrlParts.parse(new URL(endpoint))

        then:
        parts.getScheme() == scheme
        parts.getHost() == host
        parts.getAccountName() == accountName
        parts.getBlobContainerName() == fileSystem
        parts.getBlobName() == path
        parts.toUrl().toString() == expectedUrl

        where:
        endpoint                                                                   | scheme | host              | accountName        | fileSystem         | path          | expectedUrl
        "http://127.0.0.1:10000/devstoreaccount1"                                  | "http" | "127.0.0.1:10000" | "devstoreaccount1" | null               | null              | "http://127.0.0.1:10000/devstoreaccount1"
        "http://127.0.0.1:10000/devstoreaccount1/fileSystem"                       | "http" | "127.0.0.1:10000" | "devstoreaccount1" | "fileSystem"       | null              | "http://127.0.0.1:10000/devstoreaccount1/fileSystem"
        "http://127.0.0.1:10000/devstoreaccount1/fileSystem/path"                  | "http" | "127.0.0.1:10000" | "devstoreaccount1" | "fileSystem"       | "path"            | "http://127.0.0.1:10000/devstoreaccount1/fileSystem/path"
        "http://localhost:10000/devstoreaccount1"                                  | "http" | "localhost:10000" | "devstoreaccount1" | null               | null              | "http://localhost:10000/devstoreaccount1"
        "http://localhost:10000/devstoreaccount1/fileSystem"                       | "http" | "localhost:10000" | "devstoreaccount1" | "fileSystem"       | null              | "http://localhost:10000/devstoreaccount1/fileSystem"
        "http://localhost:10000/devstoreaccount1/fileSystem/path"                  | "http" | "localhost:10000" | "devstoreaccount1" | "fileSystem"       | "path"            | "http://localhost:10000/devstoreaccount1/fileSystem/path"
        "http://localhost:10000/devstoreaccount1/fileSystem/path/to]a path"        | "http" | "localhost:10000" | "devstoreaccount1" | "fileSystem"       | "path/to]a path"  | "http://localhost:10000/devstoreaccount1/fileSystem/path%2Fto%5Da%20path"
        "http://localhost:10000/devstoreaccount1/fileSystem/path%2Fto%5Da%20path"  | "http" | "localhost:10000" | "devstoreaccount1" | "fileSystem"       | "path/to]a path"  | "http://localhost:10000/devstoreaccount1/fileSystem/path%2Fto%5Da%20path"
        "http://localhost:10000/devstoreaccount1/fileSystem/斑點"                   | "http" | "localhost:10000" | "devstoreaccount1" | "fileSystem"       | "斑點"             | "http://localhost:10000/devstoreaccount1/fileSystem/%E6%96%91%E9%BB%9E"
        "http://localhost:10000/devstoreaccount1/fileSystem/%E6%96%91%E9%BB%9E"    | "http" | "localhost:10000" | "devstoreaccount1" | "fileSystem"       | "斑點"             | "http://localhost:10000/devstoreaccount1/fileSystem/%E6%96%91%E9%BB%9E"
    }

    def "UseDevelopmentStorage true"() {
        setup:
        def originalUseDevelopmentStorage = System.getProperty("UseDevelopmentStorage")
        System.setProperty("UseDevelopmentStorage", "true")

        when:
        def serviceClient = new DataLakeServiceClientBuilder()
            .endpoint(azuriteEndpoint)
            .credential(azuriteCredential)
            .buildClient()

        then:
        serviceClient.getAccountUrl() == "http://127.0.0.1:10000/devstoreaccount1"
        serviceClient.getAccountName() == "devstoreaccount1"

        cleanup:
        if (originalUseDevelopmentStorage != null) {
            System.setProperty("UseDevelopmentStorage", originalUseDevelopmentStorage)
        } else {
            System.clearProperty("UseDevelopmentStorage")
        }
    }

    def "Azurite URL constructing service client"() {
        when:
        def serviceClient = getAzuriteServiceClient()

        then:
        serviceClient.getAccountName() == "devstoreaccount1"
        serviceClient.getAccountUrl() == "http://127.0.0.1:10000/devstoreaccount1"
    }

    def "Azurite URL get file system client"() {
        when:
        def fileSystemClient = getAzuriteServiceClient().getFileSystemClient("fileSystem")

        then:
        fileSystemClient.getAccountName() == "devstoreaccount1"
        fileSystemClient.getFileSystemName() == "fileSystem"
        fileSystemClient.getFileSystemUrl() == "http://127.0.0.1:10000/devstoreaccount1/fileSystem"
    }

    def "Azurite URL construct container client"() {
        when:
        def fileSystemClient = new DataLakeFileSystemClientBuilder()
            .endpoint("http://127.0.0.1:10000/devstoreaccount1/fileSystem")
            .credential(azuriteCredential)
            .buildClient()

        then:
        fileSystemClient.getAccountName() == "devstoreaccount1"
        fileSystemClient.getFileSystemName() == "fileSystem"
        fileSystemClient.getFileSystemUrl() == "http://127.0.0.1:10000/devstoreaccount1/fileSystem"
    }

    def "Azurite URL get file client"() {
        when:
        def pathClient = getAzuriteServiceClient()
            .getFileSystemClient("fileSystem")
            .getFileClient("file")

        then:
        validatePathClient(pathClient, "devstoreaccount1", "fileSystem", "file", "http://127.0.0.1:10000/devstoreaccount1/fileSystem/file")
    }

    def "Azurite URL get directory client"() {
        when:
        def pathClient = getAzuriteServiceClient()
            .getFileSystemClient("fileSystem")
            .getDirectoryClient("directory")

        then:
        validatePathClient(pathClient, "devstoreaccount1", "fileSystem", "directory", "http://127.0.0.1:10000/devstoreaccount1/fileSystem/directory")
    }

    def "Azurite URL construct file client"() {
        when:
        def pathClient = new DataLakePathClientBuilder()
            .endpoint("http://127.0.0.1:10000/devstoreaccount1/fileSystem/file")
            .credential(azuriteCredential)
            .buildFileClient()

        then:
        validatePathClient(pathClient, "devstoreaccount1", "fileSystem", "file", "http://127.0.0.1:10000/devstoreaccount1/fileSystem/file")
    }

    def "Azurite URL construct directory client"() {
        when:
        def pathClient = new DataLakePathClientBuilder()
            .endpoint("http://127.0.0.1:10000/devstoreaccount1/fileSystem/directory")
            .credential(azuriteCredential)
            .buildDirectoryClient()

        then:
        validatePathClient(pathClient, "devstoreaccount1", "fileSystem", "directory", "http://127.0.0.1:10000/devstoreaccount1/fileSystem/directory")
    }

    def "Azurite URL get lease client"() {
        setup:
        def fileSystemClient = getAzuriteServiceClient().getFileSystemClient("fileSystem")
        def pathClient = fileSystemClient.getFileClient("file")

        when:
        def fileSystemLeaseClient = new DataLakeLeaseClientBuilder()
            .fileSystemClient(fileSystemClient)
            .buildClient()

        then:
        fileSystemLeaseClient.getAccountName() == "devstoreaccount1"
        fileSystemLeaseClient.getResourceUrl() == "http://127.0.0.1:10000/devstoreaccount1/fileSystem"

        when:
        def pathLeaseClient = new DataLakeLeaseClientBuilder()
            .fileClient(pathClient)
            .buildClient()

        then:
        pathLeaseClient.getAccountName() == "devstoreaccount1"
        pathLeaseClient.getResourceUrl() == "http://127.0.0.1:10000/devstoreaccount1/fileSystem/file"
    }
}
