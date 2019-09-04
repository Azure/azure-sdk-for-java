// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file


import com.azure.core.test.TestMode
import com.azure.core.util.configuration.ConfigurationManager
import com.azure.storage.common.TestBase
import com.azure.storage.file.models.ListSharesOptions

class APISpec extends TestBase {
    // Field common used for all APIs.
    def tmpFolder = getClass().getClassLoader().getResource("tmptestfiles")
    def testFolder = getClass().getClassLoader().getResource("testfiles")

    // Primary Clients used for API tests
    FileServiceClient primaryFileServiceClient
    FileServiceAsyncClient primaryFileServiceAsyncClient

    String connectionString

    /**
     * Setup the File service clients commonly used for the API tests.
     */
    def setup() {
        if (getTestMode() == TestMode.RECORD) {
            connectionString = ConfigurationManager.getConfiguration().get("AZURE_STORAGE_FILE_CONNECTION_STRING")
        } else {
            connectionString = "DefaultEndpointsProtocol=https;AccountName=teststorage;" +
                "AccountKey=atestaccountkey;EndpointSuffix=core.windows.net"
        }
    }

    /**
     * Clean up the test shares, directories and files for the account.
     */
    def cleanup() {
        if (getTestMode() == TestMode.RECORD) {
            FileServiceClient cleanupFileServiceClient = new FileServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient()

            cleanupFileServiceClient.listShares(new ListSharesOptions().prefix(getTestName())).each {
                cleanupFileServiceClient.deleteShare(it.name())
            }
        }
    }

    def setupFileServiceClientBuilder() {
        return setupBuilder(new FileServiceClientBuilder(), connectionString)
    }

    def setupShareClientBuilder(String shareName) {
        return setupBuilder(new ShareClientBuilder(), connectionString).shareName(shareName)
    }

    def setupFileClientBuilder(String shareName, String resourcePath) {
        return setupBuilder(new FileClientBuilder(), connectionString)
            .shareName(shareName)
            .resourcePath(resourcePath)
    }

    def generateResourceName() {
        return generateResourceName(getTestName(), 60)
    }
}
