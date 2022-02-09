// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio


import spock.lang.Unroll

import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.FileStoreAttributeView
import java.nio.file.attribute.PosixFileAttributeView

class AzureFileStoreTest extends APISpec {
    AzureFileSystem fs

    // Just need one fs instance for creating the stores.
    def setup() {
        def config = initializeConfigMap()
        config[AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL] = environment.primaryAccount.credential
        config[AzureFileSystem.AZURE_STORAGE_FILE_STORES] = generateContainerName() + "," + generateContainerName()
        fs = new AzureFileSystem(new AzureFileSystemProvider(), environment.primaryAccount.blobEndpoint, config)
    }

    // The constructor is implicitly tested by creating a file system.

    def "Name"() {
        setup:
        def name = generateContainerName()
        def store = new AzureFileStore(fs, name)

        expect:
        store.name() == name
    }

    def "Type"() {
        expect:
        fs.getFileStores().iterator().next().type() == "AzureBlobContainer"
    }

    def "IsReadOnly"() {
        expect:
        !fs.getFileStores().iterator().next().isReadOnly()
    }

    def "Space"() {
        setup:
        def store = fs.getFileStores().iterator().next()

        expect:
        store.getTotalSpace() == Long.MAX_VALUE
        store.getUsableSpace() == Long.MAX_VALUE
        store.getUnallocatedSpace() == Long.MAX_VALUE
    }

    @Unroll
    def "SupportsFileAttributeView"() {
        setup:
        def store = fs.getFileStores().iterator().next()

        expect:
        store.supportsFileAttributeView(view) == supports
        store.supportsFileAttributeView(viewName) == supports

        where:
        view                                | viewName       || supports
        BasicFileAttributeView.class        | "basic"        || true
        AzureBlobFileAttributeView.class    | "azureBlob"    || true
        AzureBasicFileAttributeView.class   | "azureBasic"   || true
        PosixFileAttributeView.class        | "posix"        || false
    }

    def "GetFileStoreAttributeView"() {
        setup:
        def store = fs.getFileStores().iterator().next()

        expect:
        store.getFileStoreAttributeView(FileStoreAttributeView.class) == null

        when:
        store.getAttribute("basic:size")

        then:
        thrown(UnsupportedOperationException)
    }
}
