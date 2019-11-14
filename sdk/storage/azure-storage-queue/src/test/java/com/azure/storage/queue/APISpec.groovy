// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue


import com.azure.core.http.ProxyOptions
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder
import com.azure.core.test.InterceptorManager
import com.azure.core.test.TestMode
import com.azure.core.test.TestRunVerifier
import com.azure.core.test.utils.TestResourceNamer
import com.azure.core.util.Configuration
import com.azure.core.util.Context
import com.azure.core.util.logging.ClientLogger
import com.azure.storage.queue.models.QueuesSegmentOptions
import spock.lang.Specification

import java.time.Duration
import java.time.OffsetDateTime

class APISpec extends Specification {
    // Field common used for all APIs.
    static String AZURE_TEST_MODE = "AZURE_TEST_MODE"
    InterceptorManager interceptorManager
    TestResourceNamer testResourceName

    // Clients for API tests
    QueueServiceClient primaryQueueServiceClient
    QueueServiceAsyncClient primaryQueueServiceAsyncClient


    // Test name for test method name.
    String methodName
    static TestMode testMode = getTestMode()
    String connectionString
    TestRunVerifier testRunVerifier

    // If debugging is enabled, recordings cannot run as there can only be one proxy at a time.
    static boolean enableDebugging = false

    /**
     * Setup the QueueServiceClient and QueueClient common used for the API tests.
     */
    def setup() {
        String testName = refactorName(specificationContext.currentIteration.getName())
        String className = specificationContext.getCurrentSpec().getName()
        methodName = className + testName

        testRunVerifier = new TestRunVerifier(specificationContext.getCurrentFeature().getFeatureMethod().getReflection())
        testRunVerifier.verifyTestCanRun(testMode)

        interceptorManager = new InterceptorManager(methodName, testMode, testRunVerifier.doNotRecordTest())
        testResourceName = new TestResourceNamer(methodName, testMode, testRunVerifier.doNotRecordTest(), interceptorManager.getRecordedData())

        connectionString = (testMode == TestMode.PLAYBACK)
            ? "DefaultEndpointsProtocol=https;AccountName=teststorage;AccountKey=atestaccountkey;EndpointSuffix=core.windows.net"
            : Configuration.getGlobalConfiguration().get("AZURE_STORAGE_QUEUE_CONNECTION_STRING")
    }

    /**
     * Clean up the test queues and messages for the account.
     */
    def cleanup() {
        if (testRunVerifier.wasTestRan()) {
            interceptorManager.close()
            if (getTestMode() != TestMode.PLAYBACK) {
                QueueServiceClient cleanupQueueServiceClient = new QueueServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient()
                cleanupQueueServiceClient.listQueues(new QueuesSegmentOptions().setPrefix(methodName.toLowerCase()),
                    Duration.ofSeconds(30), Context.NONE).each {
                    queueItem -> cleanupQueueServiceClient.deleteQueue(queueItem.getName())
                }
            }
        }
    }

    static def getTestMode() {
        def logger = new ClientLogger(APISpec.class)
        def azureTestMode = Configuration.getGlobalConfiguration().get(AZURE_TEST_MODE)

        if (azureTestMode != null) {
            try {
                return TestMode.valueOf(azureTestMode.toUpperCase(Locale.US))
            } catch (IllegalArgumentException ignored) {
                logger.error("Could not parse '{}' into TestEnum. Using 'Playback' mode.", azureTestMode)
                return TestMode.PLAYBACK
            }
        }

        logger.info("Environment variable '{}' has not been set yet. Using 'Playback' mode.", AZURE_TEST_MODE)
        return TestMode.PLAYBACK
    }

    def queueServiceBuilderHelper() {
        def builder = new QueueServiceClientBuilder()
            .connectionString(connectionString)
            .httpClient(getHttpClient())

        if (testMode == TestMode.RECORD && !testRunVerifier.doNotRecordTest()) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        return builder
    }

    def queueBuilderHelper() {
        def builder = new QueueClientBuilder()
            .connectionString(connectionString)
            .queueName(testResourceName.randomName("queue", 16))
            .httpClient(getHttpClient())

        if (testMode == TestMode.RECORD && !testRunVerifier.doNotRecordTest()) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        return builder
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

    def getHttpClient() {
        if (testMode == TestMode.PLAYBACK && !testRunVerifier.doNotRecordTest()) {
            return interceptorManager.getPlaybackClient()
        }

        def httpClientBuilder = new NettyAsyncHttpClientBuilder()
        if (enableDebugging) {
            httpClientBuilder.proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888)))
        }

        return (testMode == TestMode.RECORD && !testRunVerifier.doNotRecordTest())
            ? httpClientBuilder.wiretap(true).build()
            : httpClientBuilder.build()
    }

    def sleepIfLive(long milliseconds) {
        if (testMode == TestMode.PLAYBACK) {
            return;
        }

        sleep(milliseconds)
    }
}
