// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio

import spock.lang.Shared
import spock.lang.Unroll

import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.FileStoreAttributeView
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.UserDefinedFileAttributeView

class AzureFileStoreSpec extends APISpec {
    @Shared
    AzureFileSystem fs

    // Just need one fs instance for creating the stores.
    def setupSpec() {
        def config = initializeConfigMap()
        config[AzureFileSystem.AZURE_STORAGE_ACCOUNT_KEY] = getAccountKey(PRIMARY_STORAGE)
        config[AzureFileSystem.AZURE_STORAGE_FILE_STORES] = "jtcazurepath1,jtcazurepath2"
        fs = new AzureFileSystem(new AzureFileSystemProvider(), getAccountName(PRIMARY_STORAGE), config)
    }

    // The constructor is implicitly tested by creating a file system.

    def "FileStore name"() {
        setup:
        def name = generateContainerName()
        def store = new AzureFileStore(fs, name)

        expect:
        store.name() == name
    }

    def "FileStore type"() {
        expect:
        fs.getFileStores().iterator().next().type() == "AzureBlobContainer"
    }

    def "FileStore isReadOnly"() {
        expect:
        !fs.getFileStores().iterator().next().isReadOnly()
    }

    def "FileStore space"() {
        setup:
        def store = fs.getFileStores().iterator().next()

        expect:
        store.getTotalSpace() == Long.MAX_VALUE
        store.getUsableSpace() == Long.MAX_VALUE
        store.getUnallocatedSpace() == Long.MAX_VALUE
    }

    @Unroll
    def "FileStore supportsFileAttributeView"() {
        setup:
        def store = fs.getFileStores().iterator().next()

        expect:
        store.supportsFileAttributeView(view) == supports
        store.supportsFileAttributeView(viewName) == supports

        where:
        view                                | viewName       || supports
        BasicFileAttributeView.class        | "basic"        || true
        UserDefinedFileAttributeView.class  | "user"         || true
        AzureStorageFileAttributeView.class | "azureStorage" || true
        PosixFileAttributeView.class        | "posix"        || false
    }

    def "FileStore getFileStoreAttributeView"() {
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
