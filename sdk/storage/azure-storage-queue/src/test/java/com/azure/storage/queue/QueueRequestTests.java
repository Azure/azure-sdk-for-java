// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the request URLs and bodies the queue clients send, independent of recorded tests.
 */
public class QueueRequestTests {
    private static final String UDK_RESPONSE = "<?xml version=\"1.0\" encoding=\"utf-8\"?><UserDelegationKey>"
        + "<SignedOid>oid</SignedOid><SignedTid>tid</SignedTid><SignedStart>2026-01-01T00:00:00Z</SignedStart>"
        + "<SignedExpiry>2026-01-02T00:00:00Z</SignedExpiry><SignedService>q</SignedService>"
        + "<SignedVersion>2026-04-06</SignedVersion><Value>a2V5</Value></UserDelegationKey>";

    private static Stream<Arguments> endpoints() {
        return Stream.of(Arguments.of("https://account.queue.core.windows.net", "/myqueue"),
            // Path-style endpoints, such as Azurite's, carry the account name in the path.
            Arguments.of("http://127.0.0.1:10001/devstoreaccount1", "/devstoreaccount1/myqueue"));
    }

    @ParameterizedTest
    @MethodSource("endpoints")
    public void queueOperationsTargetTheQueuePath(String endpoint, String queuePath) {
        List<HttpRequest> requests = new ArrayList<>();
        HttpPipeline pipeline = capturingPipeline(requests, 204, null);
        QueueClientBuilder builder = new QueueClientBuilder().endpoint(endpoint)
            .queueName("myqueue")
            .credential(new MockTokenCredential())
            .pipeline(pipeline);
        QueueServiceClient serviceClient = new QueueServiceClientBuilder().endpoint(endpoint)
            .credential(new MockTokenCredential())
            .pipeline(pipeline)
            .buildClient();

        QueueClient client = builder.buildClient();
        client.create();
        client.clearMessages();
        client.deleteMessage("message id", "popReceipt");
        StepVerifier.create(builder.buildAsyncClient().deleteMessage("message id", "popReceipt")).verifyComplete();
        serviceClient.getQueueClient("myqueue").clearMessages();
        StepVerifier.create(new QueueServiceClientBuilder().endpoint(endpoint)
            .credential(new MockTokenCredential())
            .pipeline(pipeline)
            .buildAsyncClient()
            .getQueueAsyncClient("myqueue")
            .clearMessages()).verifyComplete();

        assertEquals(queuePath, requests.get(0).getUrl().getPath());
        assertEquals(queuePath + "/messages", requests.get(1).getUrl().getPath());
        // The message id is a path parameter, so it's percent-encoded.
        assertEquals(queuePath + "/messages/message%20id", requests.get(2).getUrl().getPath());
        assertEquals(queuePath + "/messages/message%20id", requests.get(3).getUrl().getPath());
        assertEquals(queuePath + "/messages", requests.get(4).getUrl().getPath());
        assertEquals(queuePath + "/messages", requests.get(5).getUrl().getPath());
        assertEquals(endpoint + "/myqueue", client.getQueueUrl());

        // Query-only queue routes must not gain a trailing slash.
        requests.clear();
        client.setMetadata(null);
        client.getProperties();
        StepVerifier.create(builder.buildAsyncClient().getProperties()).expectNextCount(1).verifyComplete();
        for (HttpRequest request : requests) {
            assertEquals(queuePath, request.getUrl().getPath());
            assertEquals("comp=metadata", request.getUrl().getQuery());
        }
    }

    @Test
    public void userDelegationKeyRequestUsesUtcSecondsAndEmptyStart() {
        List<HttpRequest> requests = new ArrayList<>();
        QueueServiceClientBuilder builder
            = new QueueServiceClientBuilder().endpoint("https://account.queue.core.windows.net")
                .credential(new MockTokenCredential())
                .pipeline(capturingPipeline(requests, 200, UDK_RESPONSE));
        OffsetDateTime start = OffsetDateTime.of(2026, 1, 1, 16, 0, 0, 123_456_789, ZoneOffset.ofHours(-8));
        OffsetDateTime expiry = start.plusDays(1);

        builder.buildClient().getUserDelegationKey(null, expiry);
        StepVerifier.create(builder.buildAsyncClient().getUserDelegationKey(start, expiry))
            .expectNextCount(1)
            .verifyComplete();

        String noStartBody = requests.get(0).getBodyAsBinaryData().toString();
        assertTrue(noStartBody.matches(".*<Start(/>|></Start>).*"), noStartBody);
        assertTrue(noStartBody.contains("<Expiry>2026-01-03T00:00:00Z</Expiry>"), noStartBody);

        String body = requests.get(1).getBodyAsBinaryData().toString();
        assertTrue(body.contains("<Start>2026-01-02T00:00:00Z</Start>"), body);
        assertTrue(body.contains("<Expiry>2026-01-03T00:00:00Z</Expiry>"), body);
    }

    private static HttpPipeline capturingPipeline(List<HttpRequest> requests, int statusCode, String body) {
        return new HttpPipelineBuilder().httpClient(request -> {
            requests.add(request);
            HttpHeaders headers = new HttpHeaders();
            if (body == null) {
                return Mono.just(new MockHttpResponse(request,
                    request.getHttpMethod() == HttpMethod.GET ? 200 : statusCode, headers));
            }
            headers.set(HttpHeaderName.CONTENT_TYPE, "application/xml");
            return Mono.just(new MockHttpResponse(request, statusCode, headers, body.getBytes(StandardCharsets.UTF_8)));
        }).build();
    }
}
