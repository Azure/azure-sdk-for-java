// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue.spock

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

    // Clients for API tests
    def primaryQueueServiceClient
    def primaryQueueServiceAsyncClient
    def primaryQueueClient
    def primaryQueueAsyncClient


    // Test name for test method name.
    @Rule
    TestName testName = new TestName()
    @Rule
    ExpectedException thrown = ExpectedException.none()
    def testMode = getTestMode()
    def connectionString = ConfigurationManager.getConfiguration().get("AZURE_STORAGE_CONNECTION_STRING")


    /**
     * Setup the QueueServiceClient and QueueClient common used for the API tests.
     */
    def setup() {
        def methodName = testName.getMethodName()
        logger.info("Test Mode: {}, Name: {}", testMode, methodName)
        interceptorManager = new InterceptorManager(methodName, testMode)
        testResourceName = new TestResourceNamer(methodName, testMode,
            interceptorManager.getRecordedData())
        primaryQueueServiceClient = queueServiceBuilderHelper(interceptorManager).buildClient()
        primaryQueueServiceAsyncClient = queueServiceBuilderHelper(interceptorManager).buildAsyncClient()
        primaryQueueClient = queueBuilderHelper(interceptorManager).buildClient()
        primaryQueueAsyncClient = queueBuilderHelper(interceptorManager).buildAsyncClient()
    }

    /**
     * Clean up the test queues and messages for the account.
     */
    def cleanup() {
        interceptorManager.close()
        if (getTestMode() == TestMode.RECORD) {
            FileServiceClient cleanupQueueServiceClient = new FileServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient()
            cleanupQueueServiceClient.listQueues().each {
                queueItem -> primaryQueueServiceClient.deleteQueue(queueItem.name())
            }
        }
    }

    /**
     * Test mode is initialized whenever test is executed. Helper method which is used to determine what to do under
     * certain test mode.
     * @return The TestMode:
     * <ul>
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
        if (testMode == TestMode.RECORD) {
            return new ShareClientBuilder()
                .connectionString(connectionString)
            .shareName()
                .addPolicy(interceptorManager.getRecordPolicy())
        } else {
            return new ShareClientBuilder()
                .connectionString(connectionString)
                .httpClient(interceptorManager.getPlaybackClient())
        }
    }

    def directoryBuilderHelper(final InterceptorManager interceptorManager) {
        if (testMode == TestMode.RECORD) {
            return new DirectoryClientBuilder()
                .connectionString(connectionString)
                .addPolicy(interceptorManager.getRecordPolicy())
        } else {
            return new DirectoryClientBuilder()
                .connectionString(connectionString)
                .httpClient(interceptorManager.getPlaybackClient())
        }
    }

    def fileBuilderHelper(final InterceptorManager interceptorManager) {
        def queueName = testResourceName.randomName("queue", 16)
        if (testMode == TestMode.RECORD) {
            return new FileClientBuilder()
                .connectionString(connectionString)
                .queueName(queueName)
                .addPolicy(interceptorManager.getRecordPolicy())
        } else {
            return new FileClientBuilder()
                .connectionString(connectionString)
                .queueName(queueName)
                .httpClient(interceptorManager.getPlaybackClient())
        }
    }
}
