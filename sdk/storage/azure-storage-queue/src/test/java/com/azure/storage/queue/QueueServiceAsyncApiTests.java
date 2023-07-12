// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.core.http.rest.PagedResponse;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.queue.models.QueueAnalyticsLogging;
import com.azure.storage.queue.models.QueueErrorCode;
import com.azure.storage.queue.models.QueueItem;
import com.azure.storage.queue.models.QueueMetrics;
import com.azure.storage.queue.models.QueueRetentionPolicy;
import com.azure.storage.queue.models.QueueServiceProperties;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import static com.azure.storage.queue.QueueTestHelper.assertAsyncResponseStatusCode;
import static com.azure.storage.queue.QueueTestHelper.assertExceptionStatusCodeAndMessage;
import static com.azure.storage.queue.QueueTestHelper.assertQueueServicePropertiesAreEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QueueServiceAsyncApiTests extends QueueTestBase {
    @BeforeEach
    public void setup() {
        primaryQueueServiceAsyncClient = queueServiceBuilderHelper().buildAsyncClient();
    }

    @Test
    public void getQueueClient() {
        assertInstanceOf(QueueAsyncClient.class, primaryQueueServiceAsyncClient.getQueueAsyncClient(getRandomName(60)));
    }

    @Test
    public void createQueue() {
        String queueName = getRandomName(60);

        assertAsyncResponseStatusCode(primaryQueueServiceAsyncClient.createQueueWithResponse(queueName, null), 201);
        assertAsyncResponseStatusCode(primaryQueueServiceAsyncClient.getQueueAsyncClient(queueName)
            .sendMessageWithResponse("Testing service client creating a queue", null, null), 201);
    }


    @ParameterizedTest
    @MethodSource("com.azure.storage.queue.QueueServiceApiTests#createQueueWithInvalidNameSupplier")
    public void createQueueWithInvalidName(String queueName, int statusCode, QueueErrorCode errMessage) {
        StepVerifier.create(primaryQueueServiceAsyncClient.createQueue(queueName))
            .verifyErrorSatisfies(ex -> assertExceptionStatusCodeAndMessage(ex, statusCode, errMessage));
    }

    @Test
    public void createNull() {
        assertThrows(NullPointerException.class, () -> primaryQueueServiceAsyncClient.createQueue(null).block());
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.queue.QueueServiceApiTests#createQueueMaxOverloadSupplier")
    public void createQueueMaxOverload(Map<String, String> metadata) {
        String queueName = getRandomName(60);

        assertAsyncResponseStatusCode(primaryQueueServiceAsyncClient.createQueueWithResponse(queueName, metadata), 201);
        assertAsyncResponseStatusCode(primaryQueueServiceAsyncClient.getQueueAsyncClient(queueName)
            .sendMessageWithResponse("Testing service client creating a queue", null, null), 201);
    }

    @Test
    public void createQueueWithInvalidMetadata() {
        StepVerifier.create(primaryQueueServiceAsyncClient.createQueueWithResponse(getRandomName(60),
            Collections.singletonMap("metadata!", "value")))
            .verifyErrorSatisfies(ex -> assertExceptionStatusCodeAndMessage(ex, 400, QueueErrorCode.INVALID_METADATA));
    }

    @Test
    public void deleteQueue() {
        String queueName = getRandomName(60);
        primaryQueueServiceAsyncClient.createQueue(queueName).block();

        assertAsyncResponseStatusCode(primaryQueueServiceAsyncClient.deleteQueueWithResponse(queueName), 204);
        StepVerifier.create(primaryQueueServiceAsyncClient.getQueueAsyncClient(queueName)
            .sendMessageWithResponse("Expecting exception as queue has been deleted.", null, null))
            .verifyErrorSatisfies(ex -> assertExceptionStatusCodeAndMessage(ex, 404, QueueErrorCode.QUEUE_NOT_FOUND));
    }

    @Test
    public void deleteQueueError() {
        StepVerifier.create(primaryQueueServiceAsyncClient.deleteQueueWithResponse(getRandomName(16)))
            .verifyErrorSatisfies(ex -> assertExceptionStatusCodeAndMessage(ex, 404, QueueErrorCode.QUEUE_NOT_FOUND));
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.queue.QueueServiceApiTests#listQueuesSupplier")
    public void listQueues(QueuesSegmentOptions options) {
        String queueName = getRandomName(60);
        LinkedList<QueueItem> testQueues = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            QueueItem queue = new QueueItem().setName(queueName + i)
                .setMetadata(Collections.singletonMap("metadata" + i, "value" + i));
            testQueues.add(queue);
            primaryQueueServiceAsyncClient.createQueueWithResponse(queue.getName(), queue.getMetadata()).block();
        }

        StepVerifier.create(primaryQueueServiceAsyncClient.listQueues(options.setPrefix(prefix)))
            .thenConsumeWhile(queueItem -> {
                QueueTestHelper.assertQueuesAreEqual(testQueues.pop(), queueItem);
                return true;
            }).verifyComplete();
        assertEquals(0, testQueues.size());
    }

    @Test
    public void listQueuesMaxResultsByPage() {
        QueuesSegmentOptions options = new QueuesSegmentOptions().setPrefix(prefix);
        String queueName = getRandomName(60);
        for (int i = 0; i < 3; i++) {
            primaryQueueServiceAsyncClient.createQueueWithResponse(queueName + i, null, null).block();
        }

        Flux<PagedResponse<QueueItem>> listQueuesResult = primaryQueueServiceAsyncClient.listQueues(options).byPage(2);
        StepVerifier.create(listQueuesResult.collectList())
            .assertNext(pages -> {
                for (PagedResponse<QueueItem> page : pages) {
                    assertTrue(page.getValue().size() <= 2, "Expected page size to be less than or equal to 2.");
                }
            }).verifyComplete();
    }

    @Test
    public void listEmptyQueues() {
        // Queue was never made with the prefix, should expect no queues to be listed.
        StepVerifier.create(primaryQueueServiceAsyncClient.listQueues(new QueuesSegmentOptions().setPrefix(prefix)))
            .expectNextCount(0)
            .verifyComplete();
    }

    @ResourceLock("ServiceProperties")
    @Test
    public void getAndSetProperties() {
        QueueServiceProperties originalProperties = primaryQueueServiceAsyncClient.getProperties().block();
        QueueRetentionPolicy retentionPolicy = new QueueRetentionPolicy().setEnabled(true).setDays(3);
        QueueAnalyticsLogging logging = new QueueAnalyticsLogging().setVersion("1.0")
            .setDelete(true)
            .setWrite(true)
            .setRetentionPolicy(retentionPolicy);
        QueueMetrics metrics = new QueueMetrics().setEnabled(true)
            .setIncludeApis(false)
            .setRetentionPolicy(retentionPolicy)
            .setVersion("1.0");
        QueueServiceProperties updatedProperties = new QueueServiceProperties().setAnalyticsLogging(logging)
            .setHourMetrics(metrics)
            .setMinuteMetrics(metrics)
            .setCors(new ArrayList<>());

        StepVerifier.create(primaryQueueServiceAsyncClient.getProperties())
            .assertNext(properties -> assertQueueServicePropertiesAreEqual(originalProperties, properties))
            .verifyComplete();

        assertAsyncResponseStatusCode(primaryQueueServiceAsyncClient.setPropertiesWithResponse(updatedProperties), 202);

        StepVerifier.create(primaryQueueServiceAsyncClient.getProperties())
            .assertNext(properties -> assertQueueServicePropertiesAreEqual(updatedProperties, properties))
            .verifyComplete();

        primaryQueueServiceAsyncClient.setProperties(originalProperties).block();
    }

    @Test
    public void builderBearerTokenValidation() throws MalformedURLException {
        URL url = new URL(primaryQueueServiceAsyncClient.getQueueServiceUrl());
        String endpoint = new URL("http", url.getHost(), url.getPort(), url.getFile()).toString();

        assertThrows(IllegalArgumentException.class, () -> new QueueServiceClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildAsyncClient());
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    @Test
    public void perCallPolicy() {
        QueueServiceAsyncClient queueServiceAsyncClient = queueServiceBuilderHelper()
            .addPolicy(getPerCallVersionPolicy())
            .buildAsyncClient();

        StepVerifier.create(queueServiceAsyncClient.getPropertiesWithResponse()).assertNext(queuePropertiesResponse ->
            assertEquals("2017-11-09", queuePropertiesResponse.getHeaders().getValue("x-ms-version"))).verifyComplete();
    }
}
