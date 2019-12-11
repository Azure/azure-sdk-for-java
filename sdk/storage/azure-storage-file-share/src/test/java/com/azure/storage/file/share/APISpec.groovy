// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.core.util.Configuration
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.StorageTestBase
import com.azure.storage.file.share.models.ListSharesOptions

import java.time.Duration

class APISpec extends StorageTestBase {
    URL testFolder = getClass().getClassLoader().getResource("testfiles")

    static def PRIMARY_STORAGE = "AZURE_STORAGE_FILE_"
    protected static StorageSharedKeyCredential primaryCredential
    // Primary Clients used for API tests
    ShareServiceClient primaryFileServiceClient
    ShareServiceAsyncClient primaryFileServiceAsyncClient

    String connectionString

    /**
     * Setup the File service clients commonly used for the API tests.
     */
    def setup() {
        connectionString = isPlaybackMode()
            ? "DefaultEndpointsProtocol=https;AccountName=teststorage;AccountKey=atestaccountkey;EndpointSuffix=core.windows.net"
            : Configuration.getGlobalConfiguration().get("AZURE_STORAGE_FILE_CONNECTION_STRING")
    }

    /**
     * Clean up the test shares, directories and files for the account.
     */
    def cleanup() {
        ShareServiceClient cleanupFileServiceClient = new ShareServiceClientBuilder()
            .connectionString(connectionString)
            .buildClient()
        cleanupFileServiceClient.listShares(new ListSharesOptions().setPrefix(testName.toLowerCase()),
            Duration.ofSeconds(30), null).each {
            cleanupFileServiceClient.deleteShare(it.getName())
        }
    }

    def fileServiceBuilderHelper() {
        def builder = new ShareServiceClientBuilder()
            .connectionString(connectionString)
            .httpClient(getHttpClient())

        if (isRecordMode()) {
            builder.addPolicy(getRecordPolicy())
        }

        return builder
    }

    def shareBuilderHelper(final String shareName) {
        def builder = new ShareClientBuilder()
            .connectionString(connectionString)
            .shareName(shareName)
            .httpClient(getHttpClient())

        if (isRecordMode()) {
            builder.addPolicy(getRecordPolicy())
        }

        return builder
    }

    def directoryBuilderHelper(final String shareName, final String directoryPath) {
        return pathBuilderHelper(shareName, directoryPath)
    }

    def fileBuilderHelper(final String shareName, final String filePath) {
        return pathBuilderHelper(shareName, filePath)
    }

    private def pathBuilderHelper(final String shareName, final String resourcePath) {
        def builder = new ShareFileClientBuilder()
            .connectionString(connectionString)
            .shareName(shareName)
            .resourcePath(resourcePath)
            .httpClient(getHttpClient())

        if (isRecordMode()) {
            builder.addPolicy(getRecordPolicy())
        }

        return builder
    }

    String generateRandomName() {
        return generateResourceName(testName, 60)
    }

    InputStream getInputStream(byte[] data) {
        return new ByteArrayInputStream(data)
    }
}
