// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue

import com.azure.core.http.HttpClient
import com.azure.core.http.HttpPipelineCallContext
import com.azure.core.http.HttpPipelineNextPolicy
import com.azure.core.http.HttpPipelinePosition
import com.azure.core.http.HttpResponse
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
import com.azure.storage.common.policy.RequestRetryOptions
import com.azure.storage.common.policy.RetryPolicyType
import com.azure.storage.common.test.StorageSpec
import com.azure.storage.queue.models.QueuesSegmentOptions
import org.spockframework.runtime.model.IterationInfo
import reactor.core.publisher.Mono

import java.time.Duration
import java.time.OffsetDateTime

class APISpec extends StorageSpec {
    // Field common used for all APIs.
    def logger = new ClientLogger(APISpec.class)
    InterceptorManager interceptorManager
    TestResourceNamer testResourceName

    // Clients for API tests
    QueueServiceClient primaryQueueServiceClient
    QueueServiceAsyncClient primaryQueueServiceAsyncClient

    static def PRIMARY_STORAGE = "AZURE_STORAGE_QUEUE_"
    protected static StorageSharedKeyCredential primaryCredential

    // Test name for test method name.
    String methodName
    String connectionString

    /**
     * Setup the QueueServiceClient and QueueClient common used for the API tests.
     */
    def setup() {
        primaryCredential = getCredential(PRIMARY_STORAGE)
        String testName = getFullTestName(specificationContext.currentIteration)
        String className = specificationContext.getCurrentSpec().getName()
        methodName = className + testName
        logger.info("Test Mode: {}, Name: {}", environment.testMode, methodName)
        interceptorManager = new InterceptorManager(methodName, environment.testMode)
        testResourceName = new TestResourceNamer(methodName, environment.testMode, interceptorManager.getRecordedData())
        if (environment.testMode != TestMode.PLAYBACK) {
            connectionString = Configuration.getGlobalConfiguration().get("AZURE_STORAGE_QUEUE_CONNECTION_STRING")
        } else {
            connectionString = "DefaultEndpointsProtocol=https;AccountName=teststorage;AccountKey=atestaccountkey;" +
                "EndpointSuffix=core.windows.net"
        }

        // Print out the test name to create breadcrumbs in our test logging in case anything hangs.
        System.out.printf("========================= %s.%s =========================%n", className, testName)
    }

    /**
     * Clean up the test queues and messages for the account.
     */
    def cleanup() {
        interceptorManager.close()
        if (environment.testMode != TestMode.PLAYBACK) {
            def cleanupQueueServiceClient = new QueueServiceClientBuilder()
                .retryOptions(new RequestRetryOptions(RetryPolicyType.FIXED, 3, 60, 1000, 1000, null))
                .connectionString(connectionString)
                .buildClient()
            cleanupQueueServiceClient.listQueues(new QueuesSegmentOptions().setPrefix(methodName.toLowerCase()),
                null, Context.NONE).each {
                queueItem -> cleanupQueueServiceClient.deleteQueue(queueItem.getName())
            }
        }
    }

    private StorageSharedKeyCredential getCredential(String accountType) {
        String accountName
        String accountKey

        if (environment.testMode != TestMode.PLAYBACK) {
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
        QueueServiceClientBuilder builder = new QueueServiceClientBuilder()
        if (environment.testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }
        return builder
            .connectionString(connectionString)
            .httpClient(getHttpClient())
    }

    def queueBuilderHelper(final InterceptorManager interceptorManager) {
        def queueName = testResourceName.randomName("queue", 16)
        QueueClientBuilder builder = new QueueClientBuilder()
        if (environment.testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }
        return builder
            .connectionString(connectionString)
            .queueName(queueName)
            .httpClient(getHttpClient())
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

        if (environment.testMode == TestMode.RECORD) {
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

        if (environment.testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        return builder
    }

    private def getFullTestName(IterationInfo iterationInfo) {
        def fullName = iterationInfo.getParent().getName().split(" ").collect { it.capitalize() }.join("")

        if (iterationInfo.getDataValues().length == 0) {
            return fullName
        }
        def prefix = fullName
        def suffix = iterationInfo.getIterationIndex()

        return prefix + suffix
    }


    OffsetDateTime getUTCNow() {
        return testResourceName.now()
    }

    HttpClient getHttpClient() {
        NettyAsyncHttpClientBuilder builder = new NettyAsyncHttpClientBuilder()
        if (environment.testMode != TestMode.PLAYBACK) {
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
        if (environment.testMode == TestMode.PLAYBACK) {
            return
        }

        sleep(milliseconds)
    }

    boolean liveMode() {
        return environment.testMode == TestMode.RECORD
    }

    def getMessageUpdateDelay(long liveTestDurationInMillis) {
        return (environment.testMode == TestMode.PLAYBACK) ? Duration.ofMillis(10) : Duration.ofMillis(liveTestDurationInMillis)
    }

    def getPerCallVersionPolicy() {
        return new HttpPipelinePolicy() {
            @Override
            Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
                context.getHttpRequest().setHeader("x-ms-version","2017-11-09")
                return next.process()
            }
            @Override
            HttpPipelinePosition getPipelinePosition() {
                return HttpPipelinePosition.PER_CALL
            }
        }
    }
}
