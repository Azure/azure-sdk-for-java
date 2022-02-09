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
import com.azure.core.util.Context
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.test.shared.StorageSpec
import com.azure.storage.queue.models.QueuesSegmentOptions
import reactor.core.publisher.Mono

import java.time.Duration

class APISpec extends StorageSpec {
    // Clients for API tests
    QueueServiceClient primaryQueueServiceClient
    QueueServiceAsyncClient primaryQueueServiceAsyncClient

    /**
     * Clean up the test queues and messages for the account.
     */
    def cleanup() {
        if (environment.testMode != TestMode.PLAYBACK) {
            def cleanupQueueServiceClient = new QueueServiceClientBuilder()
                .connectionString(environment.primaryAccount.connectionString)
                .buildClient()
            cleanupQueueServiceClient.listQueues(new QueuesSegmentOptions().setPrefix(namer.getResourcePrefix()),
                null, Context.NONE).each {
                queueItem -> cleanupQueueServiceClient.deleteQueue(queueItem.getName())
            }
        }
    }

    def queueServiceBuilderHelper() {
        QueueServiceClientBuilder builder = instrument(new QueueServiceClientBuilder())
        return builder
            .connectionString(environment.primaryAccount.connectionString)
    }

    def queueBuilderHelper() {
        def queueName = namer.getRandomName(60)
        QueueClientBuilder builder = instrument(new QueueClientBuilder())
        return builder
            .connectionString(environment.primaryAccount.connectionString)
            .queueName(queueName)
    }

    QueueServiceClientBuilder getServiceClientBuilder(StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
        QueueServiceClientBuilder builder = new QueueServiceClientBuilder()
            .endpoint(endpoint)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        instrument(builder)

        if (credential != null) {
            builder.credential(credential)
        }

        return builder
    }

    QueueClientBuilder getQueueClientBuilder(String endpoint) {
        QueueClientBuilder builder = instrument(new QueueClientBuilder())
            .endpoint(endpoint)
        return builder
    }

    def sleepIfLive(long milliseconds) {
        if (environment.testMode == TestMode.PLAYBACK) {
            return
        }

        sleep(milliseconds)
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
