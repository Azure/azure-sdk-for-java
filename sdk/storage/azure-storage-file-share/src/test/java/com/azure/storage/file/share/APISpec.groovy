// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.core.http.policy.HttpLogDetailLevel
import com.azure.core.http.policy.HttpLogOptions
import com.azure.core.test.InterceptorManager
import com.azure.core.test.TestMode
import com.azure.core.util.Configuration
import com.azure.storage.common.StorageTestBase
import com.azure.storage.file.share.models.ListSharesOptions

import java.time.Duration

class APISpec extends StorageTestBase {
    URL testFolder = getClass().getClassLoader().getResource("testfiles")

    // Primary Clients used for API tests
    ShareServiceClient primaryFileServiceClient
    ShareServiceAsyncClient primaryFileServiceAsyncClient

    String connectionString

    /**
     * Setup the File service clients commonly used for the API tests.
     */
    def setup() {
        connectionString = (testMode == TestMode.PLAYBACK)
            ? "DefaultEndpointsProtocol=https;AccountName=teststorage;AccountKey=atestaccountkey;EndpointSuffix=core.windows.net"
            : Configuration.getGlobalConfiguration().get("AZURE_STORAGE_FILE_CONNECTION_STRING")
    }

    /**
     * Clean up the test shares, directories and files for the account.
     */
    def cleanup() {
        interceptorManager.close()

        if (testMode == TestMode.PLAYBACK) {
            return
        }

        ShareServiceClient cleanupFileServiceClient = new ShareServiceClientBuilder()
            .connectionString(connectionString)
            .buildClient()
        cleanupFileServiceClient.listShares(new ListSharesOptions().setPrefix(testName.toLowerCase()),
            Duration.ofSeconds(30), null).each {
            cleanupFileServiceClient.deleteShare(it.getName())
        }
    }

    def fileServiceBuilderHelper(final InterceptorManager interceptorManager) {
        if (testMode == TestMode.RECORD) {
            return new ShareServiceClientBuilder()
                .connectionString(connectionString)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .addPolicy(interceptorManager.getRecordPolicy())
                .httpClient(getHttpClient())
        } else {
            return new ShareServiceClientBuilder()
                .connectionString(connectionString)
                .httpClient(interceptorManager.getPlaybackClient())
        }
    }

    def shareBuilderHelper(final InterceptorManager interceptorManager, final String shareName) {
        if (testMode == TestMode.RECORD) {
            return new ShareClientBuilder()
                .connectionString(connectionString)
                .shareName(shareName)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .addPolicy(interceptorManager.getRecordPolicy())
                .httpClient(getHttpClient())
        } else {
            return new ShareClientBuilder()
                .connectionString(connectionString)
                .shareName(shareName)
                .httpClient(interceptorManager.getPlaybackClient())
        }
    }

    def directoryBuilderHelper(final InterceptorManager interceptorManager, final String shareName, final String directoryPath) {
        if (testMode == TestMode.RECORD) {
            return new ShareFileClientBuilder()
                .connectionString(connectionString)
                .shareName(shareName)
                .resourcePath(directoryPath)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .addPolicy(interceptorManager.getRecordPolicy())
                .httpClient(getHttpClient())
        } else {
            return new ShareFileClientBuilder()
                .connectionString(connectionString)
                .shareName(shareName)
                .resourcePath(directoryPath)
                .httpClient(interceptorManager.getPlaybackClient())
        }
    }

    def fileBuilderHelper(final InterceptorManager interceptorManager, final String shareName, final String filePath) {
        if (testMode == TestMode.RECORD) {
            return new ShareFileClientBuilder()
                .connectionString(connectionString)
                .shareName(shareName)
                .resourcePath(filePath)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .addPolicy(interceptorManager.getRecordPolicy())
                .httpClient(getHttpClient())
        } else {
            return new ShareFileClientBuilder()
                .connectionString(connectionString)
                .shareName(shareName)
                .resourcePath(filePath)
                .httpClient(interceptorManager.getPlaybackClient())
        }
    }

    String generateRandomName() {
        return generateResourceName(testName, 60)
    }

    InputStream getInputStream(byte[] data) {
        return new ByteArrayInputStream(data)
    }
}
