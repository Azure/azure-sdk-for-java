// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.spock

import com.azure.core.test.InterceptorManager
import com.azure.core.test.TestMode
import com.azure.core.test.utils.TestResourceNamer
import com.azure.core.util.configuration.ConfigurationManager
import com.azure.core.util.logging.ClientLogger
import com.azure.storage.file.DirectoryClientBuilder
import com.azure.storage.file.FileClientBuilder
import com.azure.storage.file.FileServiceClient
import com.azure.storage.file.FileServiceClientBuilder
import com.azure.storage.file.ShareClientBuilder
import org.junit.Rule
import org.junit.rules.ExpectedException
import org.junit.rules.TestName
import spock.lang.Specification

class APISpec extends Specification {
    // Field common used for all APIs.
    def logger = new ClientLogger(APISpec.class)
    def AZURE_TEST_MODE = "AZURE_TEST_MODE"
    def interceptorManager
    def testResourceName

    // Primary Clients used for API tests
    def primaryFileServiceClient
    def primaryFileServiceAsyncClient
    def primaryShareClient
    def primaryShareAsyncClient
    def primaryDirectoryClient
    def primaryDirectoryAsyncClient
    def primaryFileClient
    def primaryFileAsyncClient


    // Test name for test method name.
    @Rule
    TestName testName = new TestName()
    @Rule
    ExpectedException thrown = ExpectedException.none()
    def testMode = getTestMode()
    def connectionString = ConfigurationManager.getConfiguration().get("AZURE_STORAGE_FILE_CONNECTION_STRING")


    /**
     * Setup the File service clients commonly used for the API tests.
     */
    def setup() {
        def methodName = testName.getMethodName()
        logger.info("Test Mode: {}, Name: {}", testMode, methodName)
        interceptorManager = new InterceptorManager(methodName, testMode)
        testResourceName = new TestResourceNamer(methodName, testMode,
            interceptorManager.getRecordedData())
        
        primaryShareAsyncClient = shareBuilderHelper(interceptorManager).buildAsyncClient()
        primaryDirectoryClient = directoryBuilderHelper(interceptorManager).buildClient()
        primaryDirectoryAsyncClient = directoryBuilderHelper(interceptorManager).buildAsyncClient()
        primaryFileClient = fileBuilderHelper(interceptorManager).buildClient()
        primaryFileAsyncClient = fileBuilderHelper(interceptorManager).buildAsyncClient()
    }

    /**
     * Clean up the test shares, directories and files for the account.
     */
    def cleanup() {
        interceptorManager.close()
        if (getTestMode() == TestMode.RECORD) {
            FileServiceClient cleanupFileServiceClient = new FileServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient()
            cleanupFileServiceClient.listShares().each {
                cleanupFileServiceClient.deleteShare(it.name())
            }
        }
    }

    /**
     * Test mode is initialized whenever test is executed. Helper method which is used to determine what to do under
     * certain test mode.
     * @return The TestMode:
     * <ul>
     *     <li>Record</li>
     *     <li>Playback: (default if no test mode setup)</li>
     * </ul>
     */
    def getTestMode() {
        def azureTestMode = ConfigurationManager.getConfiguration().get("AZURE_TEST_MODE")

        if (azureTestMode != null) {
            try {
                return TestMode.valueOf(azureTestMode.toUpperCase(Locale.US))
            } catch (IllegalArgumentException e) {
                logger.error("Could not parse '{}' into TestEnum. Using 'Playback' mode.", azureTestMode)
                return TestMode.PLAYBACK
            }
        }

        logger.info("Environment variable '{}' has not been set yet. Using 'Playback' mode.", AZURE_TEST_MODE)
        return TestMode.PLAYBACK
    }

    def fileServiceBuilderHelper(final InterceptorManager interceptorManager) {
        if (testMode == TestMode.RECORD) {
            return new FileServiceClientBuilder()
                .connectionString(connectionString)
                .addPolicy(interceptorManager.getRecordPolicy())
        } else {
            return new FileServiceClientBuilder()
                .connectionString(connectionString)
                .httpClient(interceptorManager.getPlaybackClient())
        }
    }

    def shareBuilderHelper(final InterceptorManager interceptorManager) {
        def shareName = testResourceName.randomName("share", 16)
        if (testMode == TestMode.RECORD) {
            return new ShareClientBuilder()
                .connectionString(connectionString)
                .shareName(shareName)
                .addPolicy(interceptorManager.getRecordPolicy())
        } else {
            return new ShareClientBuilder()
                .connectionString(connectionString)
                .shareName(shareName)
                .httpClient(interceptorManager.getPlaybackClient())
        }
    }

    def directoryBuilderHelper(final InterceptorManager interceptorManager) {
        def shareName = testResourceName.randomName("share", 16)
        def directoryPath = testResourceName.randomName("directory", 16)
        if (testMode == TestMode.RECORD) {
            return new DirectoryClientBuilder()
                .connectionString(connectionString)
                .shareName(shareName)
                .directoryPath(directoryPath)
                .addPolicy(interceptorManager.getRecordPolicy())
        } else {
            return new DirectoryClientBuilder()
                .connectionString(connectionString)
                .shareName(shareName)
                .directoryPath(directoryPath)
                .httpClient(interceptorManager.getPlaybackClient())
        }
    }

    def fileBuilderHelper(final InterceptorManager interceptorManager) {
        def shareName = testResourceName.randomName("share", 16)
        def filePath = testResourceName.randomName("file", 16)
        if (testMode == TestMode.RECORD) {
            return new FileClientBuilder()
                .connectionString(connectionString)
                .shareName(shareName)
                .filePath(filePath)
                .addPolicy(interceptorManager.getRecordPolicy())
        } else {
            return new FileClientBuilder()
                .connectionString(connectionString)
                .shareName(shareName)
                .filePath(filePath)
                .httpClient(interceptorManager.getPlaybackClient())
        }
    }
}
