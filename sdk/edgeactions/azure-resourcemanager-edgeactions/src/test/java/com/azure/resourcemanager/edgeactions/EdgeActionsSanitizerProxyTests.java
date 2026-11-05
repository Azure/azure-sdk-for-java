// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.edgeactions;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.utils.HttpURLConnectionHttpClient;
import com.azure.core.test.utils.TestProxyManager;
import com.azure.core.test.utils.TestProxyUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.resourcemanager.edgeactions.implementation.models.EdgeActionListResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class EdgeActionsSanitizerProxyTests {
    private static final HttpHeaderName RECORDING_ID = HttpHeaderName.fromString("x-recording-id");
    private static final String QUERY
        = "?api-version=2026-10-01&t=fake-time&c=fake-context&s=fake-signature&h=fake-hash";
    private static final String SANITIZED_QUERY = "?api-version=2026-10-01&t=REDACTED&c=REDACTED&s=REDACTED&h=REDACTED";
    private final HttpURLConnectionHttpClient client = new HttpURLConnectionHttpClient();

    @Test
    public void recordsSanitizedResponsesAndReplaysPollingUrls(@TempDir Path directory) throws IOException {
        TestProxyManager.startProxy();
        AtomicInteger upstreamCalls = new AtomicInteger();
        DisposableServer server = HttpServer.create().host("localhost").port(0).handle((request, response) -> {
            upstreamCalls.incrementAndGet();
            String pollingUrl = "https://example.test/operations/one" + QUERY;
            for (String header : new String[] { "Location", "Azure-AsyncOperation", "Operation-Location" }) {
                response.addHeader(header, pollingUrl);
            }
            for (String header : new String[] {
                "x-ms-correlation-request-id",
                "x-ms-routing-request-id",
                "x-ms-operation-identifier",
                "x-ms-original-request-ids",
                "X-MSEdge-Ref",
                "Set-Cookie" }) {
                response.addHeader(header, "fake-header");
            }
            String body = "{\"id\":\"/subscriptions/11111111-2222-3333-4444-555555555555"
                + "/resourceGroups/test-rg/providers/Microsoft.Cdn/edgeActions/test-action\","
                + "\"name\":\"test-action\",\"sku\":{\"name\":\"Standard\"},\"pollingUrl\":\"" + pollingUrl + "\","
                + "\"access_token\":\"fake-access\",\"refresh_token\":\"fake-refresh\",\"id_token\":\"fake-id\","
                + "\"client_secret\":\"fake-secret\",\"tenantId\":\"fake-tenant\",\"clientId\":\"fake-client\","
                + "\"principalId\":\"fake-principal\","
                + "\"systemData\":{\"createdBy\":\"fake-creator\",\"lastModifiedBy\":\"fake-modifier\"},"
                + "\"value\":[{\"type\":\"Microsoft.Cdn/EdgeActions\",\"name\":\"javaea012345\","
                + "\"id\":\"/subscriptions/11111111-2222-3333-4444-555555555555/resourceGroups/test-rg/"
                + "providers/Microsoft.Cdn/edgeActions/javaea012345\"},"
                + "{\"type\":\"Microsoft.Cdn/edgeActions\",\"name\":\"fake-unrelated-action\","
                + "\"id\":\"/subscriptions/11111111-2222-3333-4444-555555555555/resourceGroups/"
                + "fake-unrelated-group/providers/Microsoft.Cdn/edgeActions/fake-unrelated-action\","
                + "\"properties\":{\"attachments\":[{\"id\":\"fake-attachment\","
                + "\"attachedResourceId\":\"fake-unrelated-profile\"}]}},"
                + "{\"type\":\"Microsoft.Cdn/edgeActions/versions\",\"name\":\"v1\",\"id\":\"version-id\"}]}";
            return response.addHeader("Content-Type", "application/json").sendString(Mono.just(body));
        }).bindNow();
        try {
            String upstream = "http://localhost:" + server.port();
            Path recording = directory.resolve("sanitizers.json");
            String recordingId = start("record", recording);
            try {
                configure(recordingId);
                try (HttpResponse response = send(proxiedRequest(recordingId, "record", upstream, QUERY))) {
                    Assertions.assertTrue(response.getBodyAsBinaryData().toString().contains("fake-access"));
                }
            } finally {
                stop("record", recordingId);
            }
            String recorded = new String(Files.readAllBytes(recording), StandardCharsets.UTF_8);
            Assertions.assertFalse(recorded.contains("fake-"), "Synthetic secrets must not be saved");
            Assertions.assertFalse(recorded.contains("11111111-2222-3333-4444-555555555555"));
            Assertions.assertTrue(recorded.contains(SANITIZED_QUERY));

            for (String query : new String[] { QUERY.replace("fake-", "different-"), SANITIZED_QUERY }) {
                String playbackId = start("playback", recording);
                try {
                    configure(playbackId);
                    try (HttpResponse response = send(proxiedRequest(playbackId, "playback", upstream, query))) {
                        String body = response.getBodyAsBinaryData().toString();
                        Assertions.assertTrue(body.contains("\"test-action\""));
                        Assertions.assertTrue(body.contains("\"Standard\""));
                        Assertions.assertTrue(body.contains("/edgeActions/test-action"));
                        Assertions.assertTrue(body.contains(SANITIZED_QUERY));
                        Assertions.assertFalse(body.contains("fake-"));
                        Assertions.assertTrue(body.contains("\"javaea012345\""));
                        Assertions.assertTrue(body.contains("/edgeActions/javaea012345"));
                        Assertions.assertTrue(body.contains("\"v1\""));
                        Assertions.assertTrue(body.contains("\"version-id\""));
                        EdgeActionListResult list = BinaryData.fromString(body).toObject(EdgeActionListResult.class);
                        Assertions.assertEquals(3, list.value().size(), "Sanitization must retain every list item");
                        Assertions.assertEquals("REDACTED", list.value().get(1).name());
                        Assertions
                            .assertEquals("/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/REDACTED"
                                + "/providers/Microsoft.Cdn/edgeActions/REDACTED", list.value().get(1).id());
                        for (String header : new String[] {
                            "Location",
                            "Azure-AsyncOperation",
                            "Operation-Location" }) {
                            Assertions.assertEquals("https://example.test/operations/one" + SANITIZED_QUERY,
                                response.getHeaderValue(HttpHeaderName.fromString(header)));
                        }
                    }
                } finally {
                    stop("playback", playbackId);
                }
            }
            Assertions.assertEquals(1, upstreamCalls.get(), "Playback must not contact the upstream server");
        } finally {
            server.disposeNow();
        }
    }

    private String start(String mode, Path recording) {
        HttpRequest request = new HttpRequest(HttpMethod.POST, TestProxyUtils.getProxyUrl() + "/" + mode + "/start")
            .setBody(BinaryData.fromObject(Collections.singletonMap("x-recording-file", recording.toString())));
        try (HttpResponse response = send(request)) {
            String id = response.getHeaderValue(RECORDING_ID);
            Assertions.assertNotNull(id);
            return id;
        }
    }

    private void configure(String recordingId) {
        // Match ResourceManagerTestProxyTestBase's exclusions, preserving resource identities and LRO URLs.
        HttpRequest remove = TestProxyUtils.getRemoveSanitizerRequest()
            .setHeader(RECORDING_ID, recordingId)
            .setBody("{\"Sanitizers\":[\"AZSDK2003\",\"AZSDK2030\",\"AZSDK3430\",\"AZSDK3493\"]}");
        try (HttpResponse response = send(remove)) {
            response.getBodyAsBinaryData();
        }
        HttpRequest add = TestProxyUtils
            .createAddSanitizersRequest(EdgeActionsTestBase.edgeActionsSanitizers(), TestProxyUtils.getProxyUrl())
            .setHeader(RECORDING_ID, recordingId);
        try (HttpResponse response = send(add)) {
            response.getBodyAsBinaryData();
        }
    }

    private void stop(String mode, String recordingId) {
        HttpRequest request = new HttpRequest(HttpMethod.POST, TestProxyUtils.getProxyUrl() + "/" + mode + "/stop")
            .setHeader(RECORDING_ID, recordingId)
            .setHeader(HttpHeaderName.fromString("x-recording-save"), "true")
            .setBody("{}");
        try (HttpResponse response = send(request)) {
            response.getBodyAsBinaryData();
        }
    }

    private HttpRequest proxiedRequest(String recordingId, String mode, String upstream, String query) {
        return new HttpRequest(HttpMethod.GET, TestProxyUtils.getProxyUrl() + "/operations/one" + query)
            .setHeader(RECORDING_ID, recordingId)
            .setHeader(HttpHeaderName.fromString("x-recording-mode"), mode)
            .setHeader(HttpHeaderName.fromString("x-recording-upstream-base-uri"), upstream)
            .setHeader(HttpHeaderName.COOKIE, "fake-cookie");
    }

    private HttpResponse send(HttpRequest request) {
        request.setHeader(HttpHeaderName.CONTENT_TYPE, "application/json");
        HttpResponse response = client.sendSync(request, Context.NONE);
        if (response.getStatusCode() < 200 || response.getStatusCode() >= 300) {
            try (HttpResponse error = response) {
                Assertions.fail("Test proxy returned " + error.getStatusCode() + ": " + error.getBodyAsBinaryData());
            }
        }
        return response;
    }
}
