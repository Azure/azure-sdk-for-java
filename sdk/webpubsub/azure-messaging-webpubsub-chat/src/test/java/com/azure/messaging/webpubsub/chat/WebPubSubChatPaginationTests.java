// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.chat;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebPubSubChatPaginationTests {
    @ParameterizedTest
    @ValueSource(strings = { "roles", "members", "messages", "rooms", "topics", "users", "userRooms" })
    public void syncPageSizeReachesFirstAndNextRequests(String operation) {
        List<String> requests = new ArrayList<>();
        WebPubSubChatServiceClient client = builder(requests).buildClient();

        syncPages(client, operation).iterableByPage(1).forEach(page -> {
        });

        assertPageSize(requests);
    }

    @ParameterizedTest
    @ValueSource(strings = { "roles", "members", "messages", "rooms", "topics", "users", "userRooms" })
    public void asyncPageSizeReachesFirstAndNextRequests(String operation) {
        List<String> requests = new ArrayList<>();
        WebPubSubChatServiceAsyncClient client = builder(requests).buildAsyncClient();

        assertEquals(2, asyncPages(client, operation, false).byPage(1).collectList().block().size());

        assertPageSize(requests);
    }

    @ParameterizedTest
    @ValueSource(strings = { "roles", "members", "messages", "rooms", "topics", "users", "userRooms" })
    public void asyncProtocolPageSizeReachesFirstAndNextRequests(String operation) {
        List<String> requests = new ArrayList<>();
        WebPubSubChatServiceAsyncClient client = builder(requests).buildAsyncClient();

        assertEquals(2, asyncPages(client, operation, true).byPage(1).collectList().block().size());

        assertPageSize(requests);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void roomsForUserContinuationIsAQueryParameter(boolean async) {
        List<String> requests = new ArrayList<>();
        WebPubSubChatServiceClientBuilder builder = builder(requests);
        if (async) {
            builder.buildAsyncClient().listRoomsForUser("alice", "initial-token").byPage().collectList().block();
        } else {
            builder.buildClient().listRoomsForUser("alice", "initial-token").iterableByPage().forEach(page -> {
            });
        }
        assertTrue(requests.get(0).contains("continuationToken=initial-token"));
    }

    private static void assertPageSize(List<String> requests) {
        assertEquals(2, requests.size());
        for (String request : requests) {
            assertTrue(request.contains("maxpagesize=1"), request);
        }
        assertTrue(requests.get(1).contains("continuationToken=next"));
    }

    private static WebPubSubChatServiceClientBuilder builder(List<String> requests) {
        HttpClient transport = request -> Mono.defer(() -> {
            String url = request.getUrl().toString();
            requests.add(url);
            String body = "{\"value\":[]}";
            if (requests.size() == 1) {
                String nextLink = "https://example.webpubsub.azure.com" + request.getUrl().getPath()
                    + "?api-version=2026-08-01-preview&continuationToken=next";
                body = "{\"value\":[],\"nextLink\":\"" + nextLink + "\"}";
            }
            return Mono.just(new MockHttpResponse(request, 200,
                new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
                body.getBytes(StandardCharsets.UTF_8)));
        });
        return new WebPubSubChatServiceClientBuilder().endpoint("https://example.webpubsub.azure.com")
            .hub("chat")
            .credential(new MockTokenCredential())
            .httpClient(transport);
    }

    private static PagedIterable<?> syncPages(WebPubSubChatServiceClient client, String operation) {
        switch (operation) {
            case "roles":
                return client.listRoles();

            case "members":
                return client.listRoomMembers("room");

            case "messages":
                return client.listMessages("c.room.default");

            case "rooms":
                return client.listRooms();

            case "topics":
                return client.listTopics("room");

            case "users":
                return client.listUsers();

            case "userRooms":
                return client.listRoomsForUser("alice");

            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    private static PagedFlux<?> asyncPages(WebPubSubChatServiceAsyncClient client, String operation, boolean protocol) {
        switch (operation) {
            case "roles":
                return protocol ? client.listRoles(new RequestOptions()) : client.listRoles();

            case "members":
                return protocol ? client.listRoomMembers("room", new RequestOptions()) : client.listRoomMembers("room");

            case "messages":
                return protocol
                    ? client.listMessages("c.room.default", new RequestOptions())
                    : client.listMessages("c.room.default");

            case "rooms":
                return protocol ? client.listRooms(new RequestOptions()) : client.listRooms();

            case "topics":
                return protocol ? client.listTopics("room", new RequestOptions()) : client.listTopics("room");

            case "users":
                return protocol ? client.listUsers(new RequestOptions()) : client.listUsers();

            case "userRooms":
                return protocol
                    ? client.listRoomsForUser("alice", new RequestOptions())
                    : client.listRoomsForUser("alice");

            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }
}
