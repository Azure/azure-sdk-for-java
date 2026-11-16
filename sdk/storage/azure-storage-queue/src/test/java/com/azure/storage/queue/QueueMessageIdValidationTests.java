// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that message IDs which would change the target of a message-level request (for example {@code ".."} turning
 * Delete Message into Delete Queue) are rejected before any request is sent.
 */
public class QueueMessageIdValidationTests {
    private static final String QUEUE_NAME = "queue";

    private final List<HttpRequest> sentRequests = new CopyOnWriteArrayList<>();

    private HttpPipeline pipeline() {
        return new HttpPipelineBuilder().httpClient(request -> {
            sentRequests.add(request);
            HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.fromString("x-ms-popreceipt"), "new-receipt")
                .set(HttpHeaderName.fromString("x-ms-time-next-visible"), "Mon, 16 Nov 2026 18:00:00 GMT");
            return Mono.just(new MockHttpResponse(request, 204, headers));
        }).build();
    }

    private QueueClient syncClient() {
        return new QueueClientBuilder().endpoint("https://account.queue.core.windows.net")
            .queueName(QUEUE_NAME)
            .credential(new MockTokenCredential())
            .pipeline(pipeline())
            .buildClient();
    }

    private QueueAsyncClient asyncClient() {
        return new QueueClientBuilder().endpoint("https://account.queue.core.windows.net")
            .queueName(QUEUE_NAME)
            .credential(new MockTokenCredential())
            .pipeline(pipeline())
            .buildAsyncClient();
    }

    @ParameterizedTest
    @ValueSource(strings = { "", ".", ".." })
    public void deleteMessageRejectsInvalidMessageId(String messageId) {
        QueueClient client = syncClient();

        assertThrows(IllegalArgumentException.class, () -> client.deleteMessage(messageId, "receipt"));
        assertThrows(IllegalArgumentException.class,
            () -> client.deleteMessageWithResponse(messageId, "receipt", null, null));
        assertTrue(sentRequests.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", ".", ".." })
    public void updateMessageRejectsInvalidMessageId(String messageId) {
        QueueClient client = syncClient();

        assertThrows(IllegalArgumentException.class,
            () -> client.updateMessage(messageId, "receipt", "text", Duration.ZERO));
        assertThrows(IllegalArgumentException.class,
            () -> client.updateMessageWithResponse(messageId, "receipt", "text", Duration.ZERO, null, null));
        assertTrue(sentRequests.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", ".", ".." })
    public void deleteMessageAsyncRejectsInvalidMessageId(String messageId) {
        QueueAsyncClient client = asyncClient();

        StepVerifier.create(client.deleteMessage(messageId, "receipt")).verifyError(IllegalArgumentException.class);
        StepVerifier.create(client.deleteMessageWithResponse(messageId, "receipt"))
            .verifyError(IllegalArgumentException.class);
        assertTrue(sentRequests.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", ".", ".." })
    public void updateMessageAsyncRejectsInvalidMessageId(String messageId) {
        QueueAsyncClient client = asyncClient();

        StepVerifier.create(client.updateMessage(messageId, "receipt", "text", Duration.ZERO))
            .verifyError(IllegalArgumentException.class);
        StepVerifier.create(client.updateMessageWithResponse(messageId, "receipt", "text", Duration.ZERO))
            .verifyError(IllegalArgumentException.class);
        assertTrue(sentRequests.isEmpty());
    }

    @Test
    public void nullMessageIdIsRejected() {
        QueueClient client = syncClient();
        QueueAsyncClient asyncClient = asyncClient();

        assertThrows(NullPointerException.class, () -> client.deleteMessage(null, "receipt"));
        assertThrows(NullPointerException.class, () -> client.updateMessage(null, "receipt", "text", Duration.ZERO));
        StepVerifier.create(asyncClient.deleteMessage(null, "receipt")).verifyError(NullPointerException.class);
        StepVerifier.create(asyncClient.updateMessage(null, "receipt", "text", Duration.ZERO))
            .verifyError(NullPointerException.class);
        assertTrue(sentRequests.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "6d3e7b5a-1b2c-4d5e-8f90-123456789abc", "...", ".a", "a." })
    public void validMessageIdTargetsSingleMessage(String messageId) {
        QueueClient client = syncClient();
        QueueAsyncClient asyncClient = asyncClient();

        client.deleteMessage(messageId, "receipt");
        client.updateMessage(messageId, "receipt", "text", Duration.ZERO);
        StepVerifier.create(asyncClient.deleteMessage(messageId, "receipt")).verifyComplete();
        StepVerifier.create(asyncClient.updateMessage(messageId, "receipt", "text", Duration.ZERO))
            .expectNextCount(1)
            .verifyComplete();

        assertEquals(4, sentRequests.size());
        for (HttpRequest request : sentRequests) {
            assertEquals("/" + QUEUE_NAME + "/messages/" + messageId, request.getUrl().getPath());
        }
    }
}
