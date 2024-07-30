// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.queue.models.QueueAnalyticsLogging;
import com.azure.storage.queue.models.QueueAudience;
import com.azure.storage.queue.models.QueueErrorCode;
import com.azure.storage.queue.models.QueueItem;
import com.azure.storage.queue.models.QueueMetrics;
import com.azure.storage.queue.models.QueueRetentionPolicy;
import com.azure.storage.queue.models.QueueServiceProperties;
import com.azure.storage.queue.models.QueueStorageException;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Stream;

import static com.azure.storage.queue.QueueTestHelper.assertQueueServicePropertiesAreEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QueueServiceApiTests extends QueueTestBase {
    @BeforeEach
    public void setup() {
        primaryQueueServiceClient = queueServiceBuilderHelper().buildClient();
    }

    @Test
    public void getQueueClient() {
        assertInstanceOf(QueueClient.class, primaryQueueServiceClient.getQueueClient(getRandomName(60)));
    }

    @Test
    public void createQueue() {
        Response<QueueClient> response =
            primaryQueueServiceClient.createQueueWithResponse(getRandomName(60), null, null, null);
        assertEquals(201, response.getStatusCode());
        assertEquals(201, response.getValue().sendMessageWithResponse("Testing service client creating a queue", null,
            null, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("createQueueWithInvalidNameSupplier")
    public void createQueueWithInvalidName(String queueName, int statusCode, QueueErrorCode errMessage) {
        QueueStorageException exception = assertThrows(QueueStorageException.class,
            () -> primaryQueueServiceClient.createQueue(queueName));
        QueueTestHelper.assertExceptionStatusCodeAndMessage(exception, statusCode, errMessage);
    }

    public static Stream<Arguments> createQueueWithInvalidNameSupplier() {
        String veryLong = "verylong";
        for (int i = 0; i < 3; i++) {
            veryLong += veryLong;
        }
        return Stream.of(
            Arguments.of("a_b", 400, QueueErrorCode.INVALID_RESOURCE_NAME),
            Arguments.of("-ab", 400, QueueErrorCode.INVALID_RESOURCE_NAME),
            Arguments.of("a--b", 400, QueueErrorCode.INVALID_RESOURCE_NAME),
            Arguments.of("Abc", 400, QueueErrorCode.INVALID_RESOURCE_NAME),
            Arguments.of("ab", 400, QueueErrorCode.OUT_OF_RANGE_INPUT),
            Arguments.of(veryLong, 400, QueueErrorCode.OUT_OF_RANGE_INPUT)
        );
    }

    @Test
    public void createNull() {
        assertThrows(NullPointerException.class, () -> primaryQueueServiceClient.createQueue(null));
    }

    @ParameterizedTest
    @MethodSource("createQueueMaxOverloadSupplier")
    public void createQueueMaxOverload(Map<String, String> metadata) {
        Response<QueueClient> response = primaryQueueServiceClient.createQueueWithResponse(getRandomName(60), metadata,
            null, null);
        assertEquals(201, response.getStatusCode());
        assertEquals(201, response.getValue().sendMessageWithResponse("Testing service client creating a queue", null,
            null, null, null).getStatusCode());
    }

    public static Stream<Map<String, String>> createQueueMaxOverloadSupplier() {
        return Stream.of(null, Collections.singletonMap("metadata", "value"),
            Collections.singletonMap("metadata", "va@lue"));
    }

    @Test
    public void createQueueWithInvalidMetadata() {
        QueueStorageException exception = assertThrows(QueueStorageException.class, () -> primaryQueueServiceClient
            .createQueueWithResponse(getRandomName(16), Collections.singletonMap("metadata!", "value"), null, null));
        QueueTestHelper.assertExceptionStatusCodeAndMessage(exception, 400, QueueErrorCode.INVALID_METADATA);
    }

    @Test
    public void deleteQueue() {
        String queueName = getRandomName(60);
        QueueClient queueClient = primaryQueueServiceClient.createQueue(queueName);

        assertEquals(204, primaryQueueServiceClient.deleteQueueWithResponse(queueName, null, null).getStatusCode());

        QueueStorageException exception = assertThrows(QueueStorageException.class,
            () -> queueClient.sendMessage("Expecting exception as queue has been deleted."));
        QueueTestHelper.assertExceptionStatusCodeAndMessage(exception, 404, QueueErrorCode.QUEUE_NOT_FOUND);
    }

    @Test
    public void deleteQueueError() {
        QueueStorageException exception = assertThrows(QueueStorageException.class,
            () -> primaryQueueServiceClient.deleteQueueWithResponse(getRandomName(60), null, null));
        QueueTestHelper.assertExceptionStatusCodeAndMessage(exception, 404, QueueErrorCode.QUEUE_NOT_FOUND);
    }

    @ParameterizedTest
    @MethodSource("listQueuesSupplier")
    public void listQueues(QueuesSegmentOptions options) {
        String prefix = this.prefix + "q";
        String queueName = getRandomName(prefix, 50);
        LinkedList<QueueItem> testQueues = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            String version = Integer.toString(i);
            QueueItem queue = new QueueItem().setName(queueName + version)
                .setMetadata(Collections.singletonMap("metadata" + version, "value" + version));
            testQueues.add(queue);
            primaryQueueServiceClient.createQueueWithResponse(queue.getName(), queue.getMetadata(), null, null);
        }

        PagedIterable<QueueItem> queueListIter = primaryQueueServiceClient.listQueues(options.setPrefix(prefix), null,
            null);

        queueListIter.forEach(it -> {
            QueueTestHelper.assertQueuesAreEqual(testQueues.pop(), it);
            primaryQueueServiceClient.deleteQueue(it.getName());
        });

        assertEquals(0, testQueues.size());
    }

    public static Stream<QueuesSegmentOptions> listQueuesSupplier() {
        return Stream.of(new QueuesSegmentOptions(), new QueuesSegmentOptions().setMaxResultsPerPage(2),
            new QueuesSegmentOptions().setIncludeMetadata(true));
    }

    @Test
    public void listQueuesMaxResultsByPage() {
        QueuesSegmentOptions options = new QueuesSegmentOptions().setPrefix(prefix);
        String queueName = getRandomName(60);
        for (int i = 0; i < 3; i++) {
            primaryQueueServiceClient.createQueueWithResponse(queueName + i, null, null, null);
        }

        Iterable<PagedResponse<QueueItem>> queueListIter = primaryQueueServiceClient.listQueues(options, null, null)
            .iterableByPage(2);

        for (PagedResponse<QueueItem> page : queueListIter) {
            assertTrue(page.getValue().size() <= 2, "Expected page size to be less than or equal to 2.");
        }
    }

    @Test
    public void listEmptyQueues() {
        // Queue was never made with the prefix, should expect no queues to be listed.
        assertFalse(primaryQueueServiceClient.listQueues(new QueuesSegmentOptions().setPrefix(prefix), null, null)
            .iterator().hasNext());
    }

    @ResourceLock("ServiceProperties")
    @Test
    public void getAndSetProperties() {
        QueueServiceProperties originalProperties = primaryQueueServiceClient.getProperties();
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

        assertQueueServicePropertiesAreEqual(originalProperties, primaryQueueServiceClient.getProperties());
        assertEquals(202, primaryQueueServiceClient.setPropertiesWithResponse(updatedProperties, null, null)
            .getStatusCode());
        assertQueueServicePropertiesAreEqual(updatedProperties, primaryQueueServiceClient.getProperties());
        primaryQueueServiceClient.setProperties(originalProperties);
    }


    @Test
    public void builderBearerTokenValidation() throws MalformedURLException {
        URL url = new URL(primaryQueueServiceClient.getQueueServiceUrl());
        String endpoint = new URL("http", url.getHost(), url.getPort(), url.getFile()).toString();
        QueueServiceClientBuilder builder = new QueueServiceClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        assertThrows(IllegalArgumentException.class, builder::buildClient);
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials and auth would fail because we changed a signed header.
    @Test
    public void perCallPolicy() {
        QueueServiceClient serviceClient = queueServiceBuilderHelper()
            .addPolicy(getPerCallVersionPolicy()).buildClient();

        assertEquals("2017-11-09", serviceClient.getPropertiesWithResponse(null, null).getHeaders()
            .getValue("x-ms-version"));
    }

    @Test
    public void defaultAudience() {
        QueueServiceClient aadService = getOAuthServiceClientBuilder()
            .audience(null) // should default to "https://storage.azure.com/"
            .buildClient();

        assertNotNull(aadService.getProperties());
    }

    @Test
    public void storageAccountAudience() {
        QueueServiceClient aadService = getOAuthServiceClientBuilder()
            .audience(QueueAudience.createQueueServiceAccountAudience(primaryQueueServiceClient.getAccountName()))
            .buildClient();

        assertNotNull(aadService.getProperties());
    }

    @RequiredServiceVersion(clazz = QueueServiceVersion.class, min = "2024-08-04")
    @LiveOnly
    @Test
    /* This test tests if the bearer challenge is working properly. A bad audience is passed in, the service returns
    the default audience, and the request gets retried with this default audience, making the call function as expected.
     */
    public void audienceErrorBearerChallengeRetry() {
        QueueServiceClient aadService = getOAuthServiceClientBuilder()
            .audience(QueueAudience.createQueueServiceAccountAudience("badaudience"))
            .buildClient();

        assertNotNull(aadService.getProperties());
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.queue.core.windows.net/", primaryQueueServiceClient.getAccountName());
        QueueAudience audience = QueueAudience.fromString(url);

        QueueServiceClient aadService = getOAuthServiceClientBuilder()
            .audience(audience)
            .buildClient();

        assertNotNull(aadService.getProperties());
    }
}
