// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.security.attestation.models.TpmAttestationResult;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Offline tests for the TPM attestation APIs on {@link AttestationClient} and {@link AttestationAsyncClient}.
 * <p>
 * The end-to-end TPM tests in {@link AttestationTest} are live-only, so they don't run in playback. These tests use a
 * mock {@link HttpClient} to verify the client-side contract of both the {@code BinaryData} overloads and the
 * deprecated {@code String} overloads (wire encoding, response decoding, null handling and error mapping) without
 * requiring recordings or live resources.
 */
public class AttestationTpmOfflineTests {
    private static final String ENDPOINT = "https://tpmunittest.wus.attest.azure.net";

    // Non-ASCII characters verify that UTF-8 is used on both the request and the response path.
    private static final String REQUEST_PAYLOAD
        = "{\"payload\": { \"type\": \"aikcert\", \"note\": \"h\u00e9llo \u20ac\" } }";
    private static final String RESPONSE_PAYLOAD
        = "{\"payload\": { \"challenge\": \"ch\u00e0llenge \u2713\", \"service_context\": \"ctx\" } }";

    // Bytes that aren't valid UTF-8 verify that BinaryData payloads are sent and returned unchanged.
    private static final byte[] BINARY_REQUEST = new byte[] { 0x00, (byte) 0xFF, (byte) 0xFE, (byte) 0x80, 0x7F, 0x01 };
    private static final byte[] BINARY_RESPONSE
        = new byte[] { (byte) 0xC3, 0x28, 0x00, (byte) 0xA0, (byte) 0xA1, 0x10 };

    // BinaryData overloads

    @Test
    void attestTpmBinaryDataSendsBytesUnchangedAndReturnsResult() {
        MockTpmHttpClient httpClient = MockTpmHttpClient.success(BINARY_RESPONSE);
        AttestationClient client = buildClient(httpClient);

        TpmAttestationResult result = client.attestTpm(BinaryData.fromBytes(BINARY_REQUEST));

        assertArrayEquals(BINARY_RESPONSE, result.getTpmResult().toBytes());
        httpClient.assertSingleTpmRequest(BINARY_REQUEST);
    }

    @Test
    void attestTpmWithResponseBinaryDataReturnsStatusAndResult() {
        MockTpmHttpClient httpClient = MockTpmHttpClient.success(BINARY_RESPONSE);
        AttestationClient client = buildClient(httpClient);

        Response<TpmAttestationResult> response
            = client.attestTpmWithResponse(BinaryData.fromBytes(BINARY_REQUEST), Context.NONE);

        assertEquals(200, response.getStatusCode());
        assertArrayEquals(BINARY_RESPONSE, response.getValue().getTpmResult().toBytes());
        httpClient.assertSingleTpmRequest(BINARY_REQUEST);
    }

    @Test
    void attestTpmBinaryDataAsyncSendsBytesUnchangedAndReturnsResult() {
        MockTpmHttpClient httpClient = MockTpmHttpClient.success(BINARY_RESPONSE);
        AttestationAsyncClient client = buildAsyncClient(httpClient);

        StepVerifier.create(client.attestTpm(BinaryData.fromBytes(BINARY_REQUEST)))
            .assertNext(result -> assertArrayEquals(BINARY_RESPONSE, result.getTpmResult().toBytes()))
            .verifyComplete();

        httpClient.assertSingleTpmRequest(BINARY_REQUEST);
    }

    @Test
    void attestTpmWithResponseBinaryDataAsyncReturnsStatusAndResult() {
        MockTpmHttpClient httpClient = MockTpmHttpClient.success(BINARY_RESPONSE);
        AttestationAsyncClient client = buildAsyncClient(httpClient);

        StepVerifier.create(client.attestTpmWithResponse(BinaryData.fromBytes(BINARY_REQUEST))).assertNext(response -> {
            assertEquals(200, response.getStatusCode());
            assertArrayEquals(BINARY_RESPONSE, response.getValue().getTpmResult().toBytes());
        }).verifyComplete();

        httpClient.assertSingleTpmRequest(BINARY_REQUEST);
    }

    @Test
    void attestTpmBinaryDataNullRequestThrowsWithoutSendingRequest() {
        MockTpmHttpClient httpClient = MockTpmHttpClient.success(BINARY_RESPONSE);
        AttestationClient client = buildClient(httpClient);

        assertThrows(NullPointerException.class, () -> client.attestTpm((BinaryData) null));
        assertThrows(NullPointerException.class, () -> client.attestTpmWithResponse((BinaryData) null, Context.NONE));
        assertEquals(0, httpClient.requests.size());
    }

    @Test
    void attestTpmBinaryDataAsyncNullRequestEmitsErrorWithoutSendingRequest() {
        MockTpmHttpClient httpClient = MockTpmHttpClient.success(BINARY_RESPONSE);
        AttestationAsyncClient client = buildAsyncClient(httpClient);

        // Null requests are reported through the returned Mono, not thrown when the Mono is created.
        Mono<TpmAttestationResult> result = client.attestTpm((BinaryData) null);
        Mono<Response<TpmAttestationResult>> responseResult = client.attestTpmWithResponse((BinaryData) null);

        StepVerifier.create(result).verifyError(NullPointerException.class);
        StepVerifier.create(responseResult).verifyError(NullPointerException.class);
        assertEquals(0, httpClient.requests.size());
    }

    @Test
    void attestTpmBinaryDataMapsServiceErrors() {
        AttestationClient client = buildClient(MockTpmHttpClient.error(401, "Unauthorized"));
        ClientAuthenticationException exception = assertThrows(ClientAuthenticationException.class,
            () -> client.attestTpm(BinaryData.fromBytes(BINARY_REQUEST)));
        assertEquals(401, exception.getResponse().getStatusCode());

        AttestationAsyncClient asyncClient = buildAsyncClient(MockTpmHttpClient.error(401, "Unauthorized"));
        StepVerifier.create(asyncClient.attestTpm(BinaryData.fromBytes(BINARY_REQUEST)))
            .verifyError(ClientAuthenticationException.class);
    }

    // Deprecated String overloads

    @Test
    @SuppressWarnings("deprecation")
    void attestTpmStringSendsUtf8RequestAndDecodesResponse() {
        MockTpmHttpClient httpClient = MockTpmHttpClient.success(RESPONSE_PAYLOAD);
        AttestationClient client = buildClient(httpClient);

        String result = client.attestTpm(REQUEST_PAYLOAD);

        assertEquals(RESPONSE_PAYLOAD, result);
        httpClient.assertSingleTpmRequest(REQUEST_PAYLOAD.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @SuppressWarnings("deprecation")
    void attestTpmWithResponseStringReturnsStatusAndDecodedValue() {
        MockTpmHttpClient httpClient = MockTpmHttpClient.success(RESPONSE_PAYLOAD);
        AttestationClient client = buildClient(httpClient);

        Response<String> response = client.attestTpmWithResponse(REQUEST_PAYLOAD, Context.NONE);

        assertEquals(200, response.getStatusCode());
        assertEquals(RESPONSE_PAYLOAD, response.getValue());
        httpClient.assertSingleTpmRequest(REQUEST_PAYLOAD.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @SuppressWarnings("deprecation")
    void attestTpmStringAsyncSendsUtf8RequestAndDecodesResponse() {
        MockTpmHttpClient httpClient = MockTpmHttpClient.success(RESPONSE_PAYLOAD);
        AttestationAsyncClient client = buildAsyncClient(httpClient);

        StepVerifier.create(client.attestTpm(REQUEST_PAYLOAD)).expectNext(RESPONSE_PAYLOAD).verifyComplete();

        httpClient.assertSingleTpmRequest(REQUEST_PAYLOAD.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @SuppressWarnings("deprecation")
    void attestTpmWithResponseStringAsyncReturnsStatusAndDecodedValue() {
        MockTpmHttpClient httpClient = MockTpmHttpClient.success(RESPONSE_PAYLOAD);
        AttestationAsyncClient client = buildAsyncClient(httpClient);

        StepVerifier.create(client.attestTpmWithResponse(REQUEST_PAYLOAD)).assertNext(response -> {
            assertEquals(200, response.getStatusCode());
            assertEquals(RESPONSE_PAYLOAD, response.getValue());
        }).verifyComplete();

        httpClient.assertSingleTpmRequest(REQUEST_PAYLOAD.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @SuppressWarnings("deprecation")
    void stringOverloadsMatchBinaryDataOverloads() {
        MockTpmHttpClient stringHttpClient = MockTpmHttpClient.success(RESPONSE_PAYLOAD);
        MockTpmHttpClient binaryHttpClient = MockTpmHttpClient.success(RESPONSE_PAYLOAD);

        String stringResult = buildClient(stringHttpClient).attestTpm(REQUEST_PAYLOAD);
        TpmAttestationResult binaryResult
            = buildClient(binaryHttpClient).attestTpm(BinaryData.fromString(REQUEST_PAYLOAD));

        assertArrayEquals(binaryHttpClient.getSingleRequestData(), stringHttpClient.getSingleRequestData());
        assertArrayEquals(binaryResult.getTpmResult().toBytes(), stringResult.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @SuppressWarnings("deprecation")
    void attestTpmStringNullRequestThrowsWithoutSendingRequest() {
        MockTpmHttpClient httpClient = MockTpmHttpClient.success(RESPONSE_PAYLOAD);
        AttestationClient client = buildClient(httpClient);

        assertThrows(NullPointerException.class, () -> client.attestTpm((String) null));
        assertThrows(NullPointerException.class, () -> client.attestTpmWithResponse((String) null, Context.NONE));
        assertEquals(0, httpClient.requests.size());
    }

    @Test
    @SuppressWarnings("deprecation")
    void attestTpmStringAsyncNullRequestEmitsErrorWithoutSendingRequest() {
        MockTpmHttpClient httpClient = MockTpmHttpClient.success(RESPONSE_PAYLOAD);
        AttestationAsyncClient client = buildAsyncClient(httpClient);

        // Null requests are reported through the returned Mono, not thrown when the Mono is created.
        Mono<String> result = client.attestTpm((String) null);
        Mono<Response<String>> responseResult = client.attestTpmWithResponse((String) null);

        StepVerifier.create(result).verifyError(NullPointerException.class);
        StepVerifier.create(responseResult).verifyError(NullPointerException.class);
        assertEquals(0, httpClient.requests.size());
    }

    @Test
    @SuppressWarnings("deprecation")
    void attestTpmStringMapsServiceErrors() {
        AttestationClient unauthorizedClient = buildClient(MockTpmHttpClient.error(401, "Unauthorized"));
        ClientAuthenticationException authException
            = assertThrows(ClientAuthenticationException.class, () -> unauthorizedClient.attestTpm(REQUEST_PAYLOAD));
        assertEquals(401, authException.getResponse().getStatusCode());

        AttestationClient badRequestClient = buildClient(MockTpmHttpClient.error(400, "Bad request"));
        HttpResponseException badRequestException
            = assertThrows(HttpResponseException.class, () -> badRequestClient.attestTpm(REQUEST_PAYLOAD));
        assertEquals(400, badRequestException.getResponse().getStatusCode());
        assertTrue(badRequestException.getMessage().contains("A VBS attestation policy has not been set"),
            badRequestException::getMessage);
    }

    @Test
    @SuppressWarnings("deprecation")
    void attestTpmStringAsyncMapsServiceErrors() {
        AttestationAsyncClient client = buildAsyncClient(MockTpmHttpClient.error(401, "Unauthorized"));

        StepVerifier.create(client.attestTpm(REQUEST_PAYLOAD)).expectErrorSatisfies(error -> {
            ClientAuthenticationException exception = assertInstanceOf(ClientAuthenticationException.class, error);
            assertEquals(401, exception.getResponse().getStatusCode());
        }).verify();
    }

    // Payloads containing every byte value (0x00-0xFF). TPM attestation payloads are opaque binary data.

    @Test
    void attestTpmBinaryDataAllByteValuesRoundTrip() {
        byte[] payload = allByteValues();
        MockTpmHttpClient httpClient = MockTpmHttpClient.success(payload);

        TpmAttestationResult result = buildClient(httpClient).attestTpm(BinaryData.fromBytes(payload));

        httpClient.assertSingleTpmRequest(payload);
        assertArrayEquals(payload, result.getTpmResult().toBytes());
    }

    @Test
    void attestTpmBinaryDataAsyncAllByteValuesRoundTrip() {
        byte[] payload = allByteValues();
        MockTpmHttpClient httpClient = MockTpmHttpClient.success(payload);

        StepVerifier.create(buildAsyncClient(httpClient).attestTpm(BinaryData.fromBytes(payload)))
            .assertNext(result -> assertArrayEquals(payload, result.getTpmResult().toBytes()))
            .verifyComplete();

        httpClient.assertSingleTpmRequest(payload);
    }

    @Test
    void attestTpmBinaryDataRandomPayloadsRoundTrip() {
        Random random = new Random(42);
        for (int i = 0; i < 100; i++) {
            byte[] payload = new byte[1 + random.nextInt(4096)];
            random.nextBytes(payload);
            MockTpmHttpClient httpClient = MockTpmHttpClient.success(payload);

            TpmAttestationResult result = buildClient(httpClient).attestTpm(BinaryData.fromBytes(payload));

            httpClient.assertSingleTpmRequest(payload);
            assertArrayEquals(payload, result.getTpmResult().toBytes(), "payload " + i);
        }
    }

    /**
     * Documents the limitation of the deprecated {@code String} overloads, which is unchanged from version 1.1.41: a
     * binary payload converted to a {@code String} with UTF-8 loses every byte that isn't valid UTF-8, so the service
     * receives different data than the original payload. Use the {@code BinaryData} overloads for binary payloads.
     */
    @Test
    @SuppressWarnings("deprecation")
    void attestTpmStringCannotCarryNonUtf8Bytes() {
        byte[] payload = allByteValues();
        MockTpmHttpClient httpClient = MockTpmHttpClient.success(payload);

        String result = buildClient(httpClient).attestTpm(new String(payload, StandardCharsets.UTF_8));

        // Request: bytes 0x00-0x7F are sent unchanged; each of 0x80-0xFF was replaced with U+FFFD (EF BF BD) when the
        // caller converted the payload to a String, so 256 bytes become 512 on the wire.
        byte[] sent = httpClient.getSingleRequestData();
        assertEquals(512, sent.length);
        assertArrayEquals(Arrays.copyOfRange(payload, 0, 128), Arrays.copyOfRange(sent, 0, 128));
        for (int i = 128; i < sent.length; i += 3) {
            assertArrayEquals(new byte[] { (byte) 0xEF, (byte) 0xBF, (byte) 0xBD }, Arrays.copyOfRange(sent, i, i + 3),
                "replacement character at offset " + i);
        }

        // Response: a binary reply decoded as UTF-8 doesn't round-trip either.
        assertEquals(512, result.getBytes(StandardCharsets.UTF_8).length);
    }

    private static byte[] allByteValues() {
        byte[] bytes = new byte[256];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }
        return bytes;
    }

    private static AttestationClient buildClient(HttpClient httpClient) {
        return new AttestationClientBuilder().endpoint(ENDPOINT).httpClient(httpClient).buildClient();
    }

    private static AttestationAsyncClient buildAsyncClient(HttpClient httpClient) {
        return new AttestationClientBuilder().endpoint(ENDPOINT).httpClient(httpClient).buildAsyncClient();
    }

    /**
     * An {@link HttpClient} that records every request and returns a fixed TPM attestation response.
     */
    private static final class MockTpmHttpClient implements HttpClient {
        private static final HttpHeaders JSON_HEADERS
            = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");

        private final List<HttpRequest> requests = new CopyOnWriteArrayList<>();
        private final List<byte[]> requestBodies = new CopyOnWriteArrayList<>();
        private final int statusCode;
        private final byte[] responseBody;

        private MockTpmHttpClient(int statusCode, String responseBody) {
            this.statusCode = statusCode;
            this.responseBody = responseBody.getBytes(StandardCharsets.UTF_8);
        }

        static MockTpmHttpClient success(String responsePayload) {
            return success(responsePayload.getBytes(StandardCharsets.UTF_8));
        }

        static MockTpmHttpClient success(byte[] responseData) {
            String data = Base64.getUrlEncoder().withoutPadding().encodeToString(responseData);
            return new MockTpmHttpClient(200, "{\"data\":\"" + data + "\"}");
        }

        static MockTpmHttpClient error(int statusCode, String code) {
            return new MockTpmHttpClient(statusCode, "{\"error\":{\"code\":\"" + code
                + "\",\"message\":\"A VBS attestation policy has not been set on the attestation provider.\"}}");
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            requests.add(request);
            BinaryData body = request.getBodyAsBinaryData();
            requestBodies.add(body == null ? new byte[0] : body.toBytes());
            return Mono.just(new MockHttpResponse(request, statusCode, JSON_HEADERS, responseBody));
        }

        /**
         * Returns the decoded {@code data} field of the only request sent.
         */
        byte[] getSingleRequestData() {
            assertEquals(1, requests.size());
            try (JsonReader reader = JsonProviders.createReader(requestBodies.get(0))) {
                Map<String, Object> json = reader.readMap(JsonReader::readUntyped);
                Object data = json.get("data");
                assertNotNull(data, "TPM request body is missing the 'data' property.");
                return Base64.getUrlDecoder().decode((String) data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        void assertSingleTpmRequest(byte[] expectedData) {
            byte[] data = getSingleRequestData();
            HttpRequest request = requests.get(0);

            assertEquals(HttpMethod.POST, request.getHttpMethod());
            assertEquals("/attest/Tpm", request.getUrl().getPath());
            assertTrue(request.getUrl().getQuery().contains("api-version="), request.getUrl()::getQuery);
            assertArrayEquals(expectedData, data);
        }
    }
}
