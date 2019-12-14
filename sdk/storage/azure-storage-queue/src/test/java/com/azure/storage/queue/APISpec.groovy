// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue

import com.azure.core.http.HttpClient
import com.azure.core.http.ProxyOptions
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder
import com.azure.core.http.policy.HttpLogDetailLevel
import com.azure.core.http.policy.HttpLogOptions
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.test.InterceptorManager
import com.azure.core.test.TestMode
import com.azure.core.test.utils.TestResourceNamer
import com.azure.core.util.Configuration
import com.azure.core.util.Context
import com.azure.core.util.logging.ClientLogger
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.queue.models.QueuesSegmentOptions
import spock.lang.Specification

import java.time.Duration
import java.time.OffsetDateTime

class APISpec extends Specification {
    // Field common used for all APIs.
    def logger = new ClientLogger(APISpec.class)
    def AZURE_TEST_MODE = "AZURE_TEST_MODE"
    InterceptorManager interceptorManager
    TestResourceNamer testResourceName

    // Clients for API tests
    QueueServiceClient primaryQueueServiceClient
    QueueServiceAsyncClient primaryQueueServiceAsyncClient


    static def PRIMARY_STORAGE = "AZURE_STORAGE_QUEUE_"
    protected static StorageSharedKeyCredential primaryCredential

    // Test name for test method name.
    String methodName
    TestMode testMode = getTestMode()
    String connectionString

    // If debugging is enabled, recordings cannot run as there can only be one proxy at a time.
    static boolean enableDebugging = false

    /**
     * Setup the QueueServiceClient and QueueClient common used for the API tests.
     */
    def setup() {
        primaryCredential = getCredential(PRIMARY_STORAGE)
        String testName = refactorName(specificationContext.currentIteration.getName())
        String className = specificationContext.getCurrentSpec().getName()
        methodName = className + testName
        logger.info("Test Mode: {}, Name: {}", testMode, methodName)
        interceptorManager = new InterceptorManager(methodName, testMode)
        testResourceName = new TestResourceNamer(methodName, testMode, interceptorManager.getRecordedData())
        if (getTestMode() == TestMode.RECORD) {
            connectionString = Configuration.getGlobalConfiguration().get("AZURE_STORAGE_QUEUE_CONNECTION_STRING")
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
            cleanupQueueServiceClient.listQueues(new QueuesSegmentOptions().setPrefix(methodName.toLowerCase()),
                Duration.ofSeconds(30), Context.NONE).each {
                queueItem -> cleanupQueueServiceClient.deleteQueue(queueItem.getName())
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

    private StorageSharedKeyCredential getCredential(String accountType) {
        String accountName
        String accountKey

        if (testMode == TestMode.RECORD) {
            accountName = Configuration.getGlobalConfiguration().get(accountType + "ACCOUNT_NAME")
            accountKey = Configuration.getGlobalConfiguration().get(accountType + "ACCOUNT_KEY")
        } else {
            accountName = "azstoragesdkaccount"
            accountKey = "astorageaccountkey"
        }

        if (accountName == null || accountKey == null) {
            logger.warning("Account name or key for the {} account was null. Test's requiring these credentials will fail.", accountType)
            return null
        }

        return new StorageSharedKeyCredential(accountName, accountKey)
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

    QueueServiceClientBuilder getServiceClientBuilder(StorageSharedKeyCredential credential, String endpoint,
                                                      HttpPipelinePolicy... policies) {
        QueueServiceClientBuilder builder = new QueueServiceClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        if (credential != null) {
            builder.credential(credential)
        }

        return builder
    }

    QueueClientBuilder getQueueClientBuilder(String endpoint) {
        QueueClientBuilder builder = new QueueClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))

        if (testMode == TestMode.RECORD) {
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

    HttpClient getHttpClient() {
        NettyAsyncHttpClientBuilder builder = new NettyAsyncHttpClientBuilder()
        if (testMode == TestMode.RECORD) {
            builder.wiretap(true)

            if (Boolean.parseBoolean(Configuration.getGlobalConfiguration().get("AZURE_TEST_DEBUGGING"))) {
                builder.proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888)))
            }

            return builder.build()
        } else {
            return interceptorManager.getPlaybackClient()
        }
    }

    def sleepIfLive(long milliseconds) {
        if (testMode == TestMode.PLAYBACK) {
            return;
        }

        sleep(milliseconds)
    }
}
