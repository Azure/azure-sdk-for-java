// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.customtokenproxy;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CustomTokenProxyHttpResponseTest {
    @Test
    public void asyncBodyReadOffloadsBlockingIoAndCachesBody() throws IOException {
        HttpsURLConnection connection = mockConnection(200);
        when(connection.getInputStream()).thenAnswer(invocation -> {
            assertFalse(Schedulers.isInNonBlockingThread(), "Response I/O must not run on an event-loop thread");
            return new ByteArrayInputStream("response".getBytes(StandardCharsets.UTF_8));
        });
        CustomTokenProxyHttpResponse response = new CustomTokenProxyHttpResponse(
            new HttpRequest(HttpMethod.GET, "https://proxy.example.com/token"), connection);

        StepVerifier.create(response.getBodyAsString().subscribeOn(Schedulers.parallel()))
            .expectNext("response")
            .expectComplete()
            .verify(Duration.ofSeconds(10));
        StepVerifier.create(response.getBodyAsString()).expectNext("response").verifyComplete();
        verify(connection, times(1)).getInputStream();
        response.close();
        verify(connection).disconnect();
    }

    @Test
    public void ioFailureWithoutErrorBodyIsNotAnEmptySuccess() throws IOException {
        HttpsURLConnection connection = mockConnection(200);
        IOException failure = new IOException("Response read failed");
        when(connection.getInputStream()).thenThrow(failure);
        CustomTokenProxyHttpResponse response = new CustomTokenProxyHttpResponse(
            new HttpRequest(HttpMethod.GET, "https://proxy.example.com/token"), connection);

        StepVerifier.create(response.getBodyAsByteArray()).expectErrorMatches(error -> error == failure).verify();
    }

    @Test
    public void errorResponseBodyIsPreserved() throws IOException {
        HttpsURLConnection connection = mockConnection(400);
        when(connection.getErrorStream())
            .thenReturn(new ByteArrayInputStream("{\"error\":\"invalid_grant\"}".getBytes(StandardCharsets.UTF_8)));
        CustomTokenProxyHttpResponse response = new CustomTokenProxyHttpResponse(
            new HttpRequest(HttpMethod.POST, "https://proxy.example.com/token"), connection);

        StepVerifier.create(response.getBodyAsString()).expectNext("{\"error\":\"invalid_grant\"}").verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(ints = { 400, 401, 403, 500 })
    public void errorStatusWithoutBodyIsPreserved(int status) throws IOException {
        HttpsURLConnection connection = mockConnection(status);
        CustomTokenProxyHttpResponse response = new CustomTokenProxyHttpResponse(
            new HttpRequest(HttpMethod.POST, "https://proxy.example.com/token"), connection);

        assertEquals(status, response.getStatusCode());
        StepVerifier.create(response.getBodyAsString()).expectNext("").verifyComplete();
    }

    private HttpsURLConnection mockConnection(int status) throws IOException {
        HttpsURLConnection connection = mock(HttpsURLConnection.class);
        when(connection.getResponseCode()).thenReturn(status);
        when(connection.getHeaderFields()).thenReturn(Collections.emptyMap());
        return connection;
    }
}
