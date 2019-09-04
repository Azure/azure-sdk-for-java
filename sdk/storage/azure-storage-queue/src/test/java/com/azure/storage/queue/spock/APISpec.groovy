// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue.spock

import com.azure.core.http.HttpClient
import com.azure.core.http.ProxyOptions
import com.azure.core.test.InterceptorManager
import com.azure.core.test.TestMode
import com.azure.core.test.utils.TestResourceNamer
import com.azure.core.util.configuration.ConfigurationManager
import com.azure.core.util.logging.ClientLogger
import com.azure.storage.queue.QueueClientBuilder
import com.azure.storage.queue.QueueServiceAsyncClient
import com.azure.storage.queue.QueueServiceClient
import com.azure.storage.queue.QueueServiceClientBuilder
import com.azure.storage.queue.models.QueuesSegmentOptions
import spock.lang.Specification

import java.time.OffsetDateTime
import java.util.function.Supplier

class APISpec extends Specification {
    // Field common used for all APIs.
    def logger = new ClientLogger(APISpec.class)
    def AZURE_TEST_MODE = "AZURE_TEST_MODE"
    def interceptorManager
    TestResourceNamer testResourceName

    // Clients for API tests
    QueueServiceClient primaryQueueServiceClient
    QueueServiceAsyncClient primaryQueueServiceAsyncClient


    // Test name for test method name.
    def methodName
    def testMode = getTestMode()
    def connectionString

    // If debugging is enabled, recordings cannot run as there can only be one proxy at a time.
    static boolean enableDebugging = false

    /**
     * Setup the QueueServiceClient and QueueClient common used for the API tests.
     */
    def setup() {
        String testName = refactorName(specificationContext.currentIteration.getName())
        String className = specificationContext.getCurrentSpec().getName()
        methodName = className + testName
        logger.info("Test Mode: {}, Name: {}", testMode, methodName)
        interceptorManager = new InterceptorManager(methodName, testMode)
        testResourceName = new TestResourceNamer(methodName, testMode,
            interceptorManager.getRecordedData())
        if (getTestMode() == TestMode.RECORD) {
            connectionString = ConfigurationManager.getConfiguration().get("AZURE_STORAGE_QUEUE_CONNECTION_STRING")
        } else {
            connectionString = "DefaultEndpointsProtocol=https;AccountName=teststorage;AccountKey=atestaccountkey;" +
                "EndpointSuffix=core.windows.net"
        }
    }

    /**
     * Clean up the test queues and messages for the account.
     */
    def cleanup() {

        interceptorManager.close()
        if (getTestMode() == TestMode.RECORD) {
            QueueServiceClient cleanupQueueServiceClient = new QueueServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient()
            cleanupQueueServiceClient.listQueues(new QueuesSegmentOptions().prefix(methodName.toLowerCase())).each {
                queueItem -> cleanupQueueServiceClient.deleteQueue(queueItem.name())
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
        def azureTestMode = ConfigurationManager.getConfiguration().get(AZURE_TEST_MODE)

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

    def queueServiceBuilderHelper(final InterceptorManager interceptorManager) {
        if (testMode == TestMode.RECORD) {
            return new QueueServiceClientBuilder()
                .connectionString(connectionString)
                .addPolicy(interceptorManager.getRecordPolicy())
                .httpClient(getHttpClient())
        } else {
            return new QueueServiceClientBuilder()
                .connectionString(connectionString)
                .httpClient(interceptorManager.getPlaybackClient())
        }
    }

    def queueBuilderHelper(final InterceptorManager interceptorManager) {
        def queueName = testResourceName.randomName("queue", 16)
        if (testMode == TestMode.RECORD) {
            return new QueueClientBuilder()
                .connectionString(connectionString)
                .queueName(queueName)
                .addPolicy(interceptorManager.getRecordPolicy())
                .httpClient(getHttpClient())
        } else {
            return new QueueClientBuilder()
                .connectionString(connectionString)
                .queueName(queueName)
                .httpClient(interceptorManager.getPlaybackClient())
        }
    }

    private def refactorName(String text) {
        def fullName = text.split(" ").collect { it.capitalize() }.join("")
        def matcher = (fullName =~ /(.*)(\[)(.*)(\])/)

        if (!matcher.find()) {
            return fullName
        }
        return matcher[0][1] + matcher[0][3]
    }

    OffsetDateTime getUTCNow() {
        return testResourceName.now()
    }

    static HttpClient getHttpClient() {
        if (enableDebugging) {
            return HttpClient.createDefault().setProxy(new Supplier<ProxyOptions>() {
                @Override
                ProxyOptions get() {
                    return new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888))
                }
            })
        } else {
            return HttpClient.createDefault()
        }
    }
}
