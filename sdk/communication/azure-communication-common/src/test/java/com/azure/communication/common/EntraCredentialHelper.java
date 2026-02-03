// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common;

import com.azure.communication.common.implementation.JwtTokenMocker;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import org.junit.jupiter.params.provider.Arguments;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public final class EntraCredentialHelper {

    public static final String AUTH_HEADER = "Bearer Token";
    public static final String ALT_AUTH_HEADER = "Bearer Token2";

    public static final String SAMPLE_TOKEN_EXPIRY = "2034-10-04T12:00:00.4729393+00:00";
    public static final String SAMPLE_TOKEN = new JwtTokenMocker().generateRawToken("resource", "user",
        OffsetDateTime.parse(SAMPLE_TOKEN_EXPIRY).toInstant());
    public static final String TOKEN_RESPONSE_TEMPLATE
        = "{\"identity\":{\"id\":\"id\"},\"accessToken\":{\"token\":\"%s\",\"expiresOn\":\"%s\"}}";
    public static final String VALID_TOKEN_RESPONSE
        = String.format(TOKEN_RESPONSE_TEMPLATE, SAMPLE_TOKEN, SAMPLE_TOKEN_EXPIRY);

    public static final String RESOURCE_ENDPOINT = "https://myResource.communication.azure.com";
    public static final String COMMUNICATION_CLIENTS_ENDPOINT = "/access/entra/:exchangeAccessToken";
    public static final String COMMUNICATION_CLIENTS_PREFIX = "https://communication.azure.com/clients/";
    public static final String COMMUNICATION_CLIENTS_SCOPE = COMMUNICATION_CLIENTS_PREFIX + "VoIP";
    public static final String TEAMS_EXTENSION_ENDPOINT = "/access/teamsExtension/:exchangeAccessToken";
    public static final String TEAMS_EXTENSION_SCOPE
        = "https://auth.msft.communication.azure.com/TeamsExtension.ManageCalls";
    public static final String DEFAULT_SCOPE = "https://communication.azure.com/clients/.default";

    static Stream<Arguments> validScopesProvider() {
        return Stream.of(Arguments.of(asList(COMMUNICATION_CLIENTS_SCOPE)),
            Arguments.of(asList(TEAMS_EXTENSION_SCOPE)));
    }

    static Stream<Arguments> invalidScopesProvider() {
        return Stream.of(Arguments.of(asList(COMMUNICATION_CLIENTS_SCOPE, TEAMS_EXTENSION_SCOPE)),
            Arguments.of(asList(TEAMS_EXTENSION_SCOPE, COMMUNICATION_CLIENTS_SCOPE)),
            Arguments.of(asList("invalidScope")), Arguments.of(asList("")));
    }

    static Stream<Arguments> nullOrEmptyScopesProvider() {
        return Stream.of(Arguments.of(Collections.emptyList()), Arguments.of((Object) null));
    }

    public static class MockTokenCredential implements TokenCredential {
        private int callCount = 0;
        private final AccessToken[] tokens;

        public MockTokenCredential(AccessToken... tokens) {
            this.tokens = tokens;
        }

        @Override
        public Mono<AccessToken> getToken(TokenRequestContext request) {
            if (tokens != null && callCount < tokens.length) {
                return Mono.just(tokens[callCount++]);
            }
            callCount++;
            return Mono.just(new AccessToken("defaultToken" + callCount, OffsetDateTime.now().plusHours(1)));
        }

        public int getCallCount() {
            return callCount;
        }
    }

    public static class MockHttpClient implements HttpClient {
        private final Queue<HttpResponse> responses = new ConcurrentLinkedQueue<>();
        private HttpRequest request;
        private int sendCallCount = 0;

        public MockHttpClient(HttpResponse... responses) {
            this.responses.addAll(Arrays.asList(responses));
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            sendCallCount++;
            this.request = request;
            HttpResponse response = responses.poll();
            return response == null ? Mono.empty() : Mono.just(response);
        }

        public HttpRequest getRequest() {
            return request;
        }

        public int getSendCallCount() {
            return sendCallCount;
        }
    }

    public static MockHttpResponse createHttpResponse(int status, String body) {
        HttpRequest dummyReq = new HttpRequest(HttpMethod.GET, "https://endpoint");
        return new MockHttpResponse(dummyReq, status, body.getBytes(StandardCharsets.UTF_8));
    }
}
