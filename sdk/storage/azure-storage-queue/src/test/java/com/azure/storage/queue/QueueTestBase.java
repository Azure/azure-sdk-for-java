// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.core.client.traits.HttpTrait;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.Context;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.test.shared.StorageCommonTestUtils;
import com.azure.storage.common.test.shared.TestEnvironment;
import com.azure.storage.common.test.shared.policy.PerCallVersionPolicy;
import com.azure.storage.queue.models.QueuesSegmentOptions;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

/**
 * Base class for Azure Storage Queue tests.
 */
public class QueueTestBase extends TestProxyTestBase {
    protected static final TestEnvironment ENVIRONMENT = TestEnvironment.getInstance();

    protected String prefix;

    // Clients for API tests
    protected QueueServiceClient primaryQueueServiceClient;
    protected QueueServiceAsyncClient primaryQueueServiceAsyncClient;

    @Override
    public void beforeTest() {
        super.beforeTest();
        prefix = StorageCommonTestUtils.getCrc32(testContextManager.getTestPlaybackRecordingName());

        if (getTestMode() != TestMode.LIVE) {
            interceptorManager.addSanitizers(
                Collections.singletonList(new TestProxySanitizer("sig=(.*)", "REDACTED", TestProxySanitizerType.URL)));
        }

        // Ignore changes to the order of query parameters and wholly ignore the 'sv' (service version) query parameter
        // in SAS tokens.
        // TODO (alzimmer): Once all Storage libraries are migrated to test proxy move this into the common parent.
        interceptorManager.addMatchers(Arrays.asList(new CustomMatcher()
            .setQueryOrderingIgnored(true)
            .setIgnoredQueryParameters(Arrays.asList("sv"))));
    }

    /**
     * Clean up the test queues and messages for the account.
     */
    @Override
    protected void afterTest() {
        super.afterTest();
        if (getTestMode() == TestMode.PLAYBACK) {
            return;
        }

        QueueServiceClient cleanupQueueServiceClient = new QueueServiceClientBuilder()
            .connectionString(getPrimaryConnectionString())
            .buildClient();

        cleanupQueueServiceClient.listQueues(new QueuesSegmentOptions().setPrefix(prefix), null, Context.NONE)
            .forEach(queueItem -> cleanupQueueServiceClient.deleteQueue(queueItem.getName()));
    }

    protected String getRandomName(int length) {
        return getRandomName(prefix, length);
    }

    protected String getRandomName(String prefix, int length) {
        return testResourceNamer.randomName(prefix, length);
    }

    protected QueueServiceClientBuilder queueServiceBuilderHelper() {
        return instrument(new QueueServiceClientBuilder()).connectionString(getPrimaryConnectionString());
    }

    protected QueueClientBuilder queueBuilderHelper() {
        return instrument(new QueueClientBuilder())
            .connectionString(getPrimaryConnectionString())
            .queueName(getRandomName(60));
    }

    protected QueueServiceClientBuilder getOAuthServiceClientBuilder() {
        QueueServiceClientBuilder builder = new QueueServiceClientBuilder()
            .endpoint(ENVIRONMENT.getPrimaryAccount().getQueueEndpoint());

        instrument(builder);
        return builder.credential(StorageCommonTestUtils.getTokenCredential(interceptorManager));
    }

    protected QueueServiceClientBuilder getServiceClientBuilder(StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
        QueueServiceClientBuilder builder = new QueueServiceClientBuilder()
            .endpoint(endpoint);

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy);
        }

        instrument(builder);

        if (credential != null) {
            builder.credential(credential);
        }

        return builder;
    }

    protected QueueClientBuilder getQueueClientBuilder(String endpoint) {
        return instrument(new QueueClientBuilder()).endpoint(endpoint);
    }

    protected QueueClientBuilder getOAuthQueueClientBuilder() {
        QueueClientBuilder builder = new QueueClientBuilder()
            .endpoint(ENVIRONMENT.getPrimaryAccount().getQueueEndpoint());

        instrument(builder);
        return builder.credential(StorageCommonTestUtils.getTokenCredential(interceptorManager));
    }

    protected Duration getMessageUpdateDelay(long liveMillis) {
        return (getTestMode() == TestMode.PLAYBACK) ? Duration.ofMillis(10) : Duration.ofMillis(liveMillis);
    }

    protected HttpPipelinePolicy getPerCallVersionPolicy() {
        return new PerCallVersionPolicy("2017-11-09");
    }

    protected <T extends HttpTrait<T>, E extends Enum<E>> T instrument(T builder) {
        return StorageCommonTestUtils.instrument(builder, QueueServiceClientBuilder.getDefaultHttpLogOptions(),
            interceptorManager);
    }

    protected String getPrimaryConnectionString() {
        return ENVIRONMENT.getPrimaryAccount().getConnectionString();
    }
}
