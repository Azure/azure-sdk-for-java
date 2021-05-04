// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue

import com.azure.core.http.HttpPipelineCallContext
import com.azure.core.http.HttpPipelineNextPolicy
import com.azure.core.http.HttpPipelinePosition
import com.azure.core.http.HttpResponse
import com.azure.core.http.policy.HttpLogDetailLevel
import com.azure.core.http.policy.HttpLogOptions
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.test.TestMode
import com.azure.core.util.Configuration
import com.azure.core.util.Context
import com.azure.core.util.logging.ClientLogger
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.policy.RequestRetryOptions
import com.azure.storage.common.policy.RetryPolicyType
import com.azure.storage.common.test.shared.StorageSpec
import com.azure.storage.queue.models.QueuesSegmentOptions
import reactor.core.publisher.Mono

import java.time.Duration
import java.time.OffsetDateTime

class APISpec extends StorageSpec {
    // Field common used for all APIs.
    def logger = new ClientLogger(APISpec.class)

    // Clients for API tests
    QueueServiceClient primaryQueueServiceClient
    QueueServiceAsyncClient primaryQueueServiceAsyncClient

    static def PRIMARY_STORAGE = "AZURE_STORAGE_QUEUE_"
    protected static StorageSharedKeyCredential primaryCredential

    // Test name for test method name.
    String connectionString

    /**
     * Setup the QueueServiceClient and QueueClient common used for the API tests.
     */
    def setup() {
        primaryCredential = getCredential(PRIMARY_STORAGE)
        if (ENVIRONMENT.testMode != TestMode.PLAYBACK) {
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
        if (ENVIRONMENT.testMode != TestMode.PLAYBACK) {
            def cleanupQueueServiceClient = new QueueServiceClientBuilder()
                .retryOptions(new RequestRetryOptions(RetryPolicyType.FIXED, 3, 60, 1000, 1000, null))
                .connectionString(connectionString)
                .buildClient()
            cleanupQueueServiceClient.listQueues(new QueuesSegmentOptions().setPrefix(namer.getResourcePrefix()),
                null, Context.NONE).each {
                queueItem -> cleanupQueueServiceClient.deleteQueue(queueItem.getName())
            }
        }
    }

    // TODO (kasobol-msft) remove this when all modules are migrated
    @Override
    protected shouldUseThisToRecord() {
        return true
    }

    private StorageSharedKeyCredential getCredential(String accountType) {
        String accountName
        String accountKey

        if (ENVIRONMENT.testMode != TestMode.PLAYBACK) {
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

    def queueServiceBuilderHelper() {
        QueueServiceClientBuilder builder = new QueueServiceClientBuilder()
        return builder
            .connectionString(connectionString)
            .addPolicy(getRecordPolicy())
            .httpClient(getHttpClient())
    }

    def queueBuilderHelper() {
        def queueName = namer.getRandomName(60)
        QueueClientBuilder builder = new QueueClientBuilder()
        return builder
            .connectionString(connectionString)
            .queueName(queueName)
            .addPolicy(getRecordPolicy())
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

        builder.addPolicy(getRecordPolicy())

        if (credential != null) {
            builder.credential(credential)
        }

        return builder
    }

    QueueClientBuilder getQueueClientBuilder(String endpoint) {
        QueueClientBuilder builder = new QueueClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())
            .addPolicy(getRecordPolicy())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
        return builder
    }

    def sleepIfLive(long milliseconds) {
        if (ENVIRONMENT.testMode == TestMode.PLAYBACK) {
            return
        }

        sleep(milliseconds)
    }

    def getMessageUpdateDelay(long liveTestDurationInMillis) {
        return (ENVIRONMENT.testMode == TestMode.PLAYBACK) ? Duration.ofMillis(10) : Duration.ofMillis(liveTestDurationInMillis)
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
