// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common;

import com.azure.communication.common.implementation.JwtTokenMocker;
import com.azure.communication.common.implementation.TokenParser;
import com.azure.core.credential.AccessToken;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import static com.azure.communication.common.EntraCredentialHelper.*;
import static java.lang.System.arraycopy;
import static org.junit.jupiter.api.Assertions.*;

public class EntraTokenCredentialTests {

    private EntraTokenCredential entraTokenCredential;
    private MockTokenCredential mockTokenCredential;
    private MockHttpClient mockHttpClient;

    private void createEntraCredentialWithMocks(String[] scopes, HttpResponse... response) {
        EntraCommunicationTokenCredentialOptions options
            = new EntraCommunicationTokenCredentialOptions(mockTokenCredential, RESOURCE_ENDPOINT);
        if (scopes != null && scopes.length > 0) {
            options = new EntraCommunicationTokenCredentialOptions(mockTokenCredential, RESOURCE_ENDPOINT, scopes);
        }
        mockHttpClient = new MockHttpClient(response);
        entraTokenCredential = new EntraTokenCredential(options, mockHttpClient);
    }

    @BeforeEach
    void setUp() {
        mockTokenCredential = new MockTokenCredential();
    }

    @AfterEach
    void tearDown() {
        if (entraTokenCredential != null) {
            entraTokenCredential.close();
        }
        entraTokenCredential = null;
        mockHttpClient = null;
    }

    @Test
    void entraTokenCredentialOptionsConstructThrowsOnNulls() {
        assertThrows(IllegalArgumentException.class,
            () -> new EntraCommunicationTokenCredentialOptions(new MockTokenCredential(), null));
        assertThrows(IllegalArgumentException.class,
            () -> new EntraCommunicationTokenCredentialOptions(new MockTokenCredential(), ""));
        assertThrows(NullPointerException.class,
            () -> new EntraCommunicationTokenCredentialOptions(null, RESOURCE_ENDPOINT));
    }

    @Test
    void entraTokenCredentialOptionsConstructWithoutScopesDefaultScopeIsSet() {
        EntraCommunicationTokenCredentialOptions options
            = new EntraCommunicationTokenCredentialOptions(mockTokenCredential, RESOURCE_ENDPOINT);
        assertArrayEquals(new String[] { DEFAULT_SCOPE }, options.getScopes());
    }

    @Test
    void entraTokenCredentialOptionsConstructWithExplicitScopesScopesAreSet() {
        String[] scopes = new String[] { TEAMS_EXTENSION_SCOPE };
        EntraCommunicationTokenCredentialOptions options
            = new EntraCommunicationTokenCredentialOptions(mockTokenCredential, RESOURCE_ENDPOINT, scopes);
        assertArrayEquals(scopes, options.getScopes());
    }

    @ParameterizedTest
    @MethodSource("com.azure.communication.common.EntraCredentialHelper#nullOrEmptyScopesProvider")
    void entraTokenCredentialOptionsConstructWithNullOrEmptyScopesThrowsException(String[] scopes) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> new EntraCommunicationTokenCredentialOptions(mockTokenCredential, RESOURCE_ENDPOINT, scopes));
        assertTrue(ex.getMessage().contains("Scopes must not be null or empty. Ensure all scopes start with either"));
    }

    @ParameterizedTest
    @MethodSource("com.azure.communication.common.EntraCredentialHelper#invalidScopesProvider")
    void entraTokenCredentialOptionsConstructWithInvalidScopesThrowsException(String[] scopes) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> new EntraCommunicationTokenCredentialOptions(mockTokenCredential, RESOURCE_ENDPOINT, scopes));
        assertTrue(ex.getMessage().contains("Scopes validation failed. Ensure all scopes start with either"));
    }

    @Test
    void entraTokenCredentialConstructFetchesAccessTokenImmediately() {
        HttpResponse resp = createHttpResponse(200, VALID_TOKEN_RESPONSE);
        createEntraCredentialWithMocks(null, resp);

        assertEquals(1, mockTokenCredential.getCallCount(),
            "MockTokenCredential.getToken should be called during construction");
        assertEquals(1, mockHttpClient.getSendCallCount(), "HttpClient.send should be called during construction");
    }

    @Test
    void entraTokenCredentialConstructWithoutScopesExchangeEntraTokenReturnsCommunicationClientsToken() {
        OffsetDateTime expiryTime = OffsetDateTime.parse(SAMPLE_TOKEN_EXPIRY).truncatedTo(ChronoUnit.SECONDS);
        HttpResponse resp = createHttpResponse(200, VALID_TOKEN_RESPONSE);
        createEntraCredentialWithMocks(null, resp);

        StepVerifier.create(entraTokenCredential.exchangeEntraToken()).assertNext(token -> {
            AccessToken accessToken = (new TokenParser()).parseJWTToken(token);
            assertEquals(SAMPLE_TOKEN, accessToken.getToken());
            assertEquals(expiryTime.toInstant(),
                accessToken.getExpiresAt().truncatedTo(ChronoUnit.SECONDS).toInstant());
            assertEquals(COMMUNICATION_CLIENTS_ENDPOINT, mockHttpClient.getRequest().getUrl().getPath());
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.communication.common.EntraCredentialHelper#validScopesProvider")
    void entraTokenCredentialWithExplicitScopesExchangeEntraTokenReturnsAccessToken(String[] scopes) {
        OffsetDateTime expiryTime = OffsetDateTime.parse(SAMPLE_TOKEN_EXPIRY).truncatedTo(ChronoUnit.SECONDS);
        HttpResponse resp = createHttpResponse(200, VALID_TOKEN_RESPONSE);
        createEntraCredentialWithMocks(scopes, resp);

        StepVerifier.create(entraTokenCredential.exchangeEntraToken()).assertNext(token -> {
            AccessToken accessToken = (new TokenParser()).parseJWTToken(token);
            assertEquals(SAMPLE_TOKEN, accessToken.getToken());
            assertEquals(expiryTime.toInstant(),
                accessToken.getExpiresAt().truncatedTo(ChronoUnit.SECONDS).toInstant());
            assertEquals(Arrays.asList(scopes).contains(TEAMS_EXTENSION_SCOPE)
                ? TEAMS_EXTENSION_ENDPOINT
                : COMMUNICATION_CLIENTS_ENDPOINT, mockHttpClient.getRequest().getUrl().getPath());
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.communication.common.EntraCredentialHelper#validScopesProvider")
    void entraTokenCredentialExchangeEntraTokenMultipleCallsReturnsCachedToken(String[] scopes) {
        HttpResponse resp = createHttpResponse(200, VALID_TOKEN_RESPONSE);
        createEntraCredentialWithMocks(scopes, resp);

        String token = entraTokenCredential.exchangeEntraToken().block();
        for (int i = 0; i < 5; i++) {
            token = entraTokenCredential.exchangeEntraToken().block();
        }

        AccessToken accessToken = (new TokenParser()).parseJWTToken(token);
        assertEquals(SAMPLE_TOKEN, accessToken.getToken());
        assertEquals(1, mockHttpClient.getSendCallCount(), "HttpClient.send should be called only once due to caching");
    }

    @ParameterizedTest
    @MethodSource("com.azure.communication.common.EntraCredentialHelper#validScopesProvider")
    void entraTokenCredentialExchangeEntraTokenInternalEntraTokenChangedInvalidatesCachedToken(String[] scopes) {
        mockTokenCredential
            = new MockTokenCredential(new AccessToken("Entra token for call from constructor", OffsetDateTime.now().minusMinutes(1)),
                new AccessToken("Entra token for the exchangeToken call", OffsetDateTime.parse(SAMPLE_TOKEN_EXPIRY)));

        String tobeExpiredToken
            = new JwtTokenMocker().generateRawToken("resource", "user", OffsetDateTime.now().minusMinutes(1).toInstant());
        String tobeExpiredTokenResponse
            = String.format(TOKEN_RESPONSE_TEMPLATE, tobeExpiredToken, OffsetDateTime.now());
        HttpResponse resp1 = createHttpResponse(200, tobeExpiredTokenResponse);
        HttpResponse resp2 = createHttpResponse(200, VALID_TOKEN_RESPONSE);

        createEntraCredentialWithMocks(scopes, resp1, resp2);

        StepVerifier.create(entraTokenCredential.exchangeEntraToken()).assertNext(token -> {
            AccessToken accessToken = (new TokenParser()).parseJWTToken(token);
            assertEquals(SAMPLE_TOKEN, accessToken.getToken());
            assertEquals(2, mockTokenCredential.getCallCount(), "MockTokenCredential.getToken should be called twice");
            assertEquals(2, mockHttpClient.getSendCallCount(),
                "HttpClient.send should be called twice due to token expiry");
        }).verifyComplete();
    }

    @Test
    void entraTokenCredentialScopesChangedAfterCredentialConstructionNoImpactReturnsToken() {
        String[] scopes = new String[] { COMMUNICATION_CLIENTS_SCOPE, COMMUNICATION_CLIENTS_PREFIX + "Chat" };

        mockTokenCredential
            = new MockTokenCredential(new AccessToken("Entra token for call from constructor", OffsetDateTime.now().minusMinutes(1)),
                new AccessToken("Entra token for the exchangeToken call", OffsetDateTime.parse(SAMPLE_TOKEN_EXPIRY)));

        String tobeExpiredToken
            = new JwtTokenMocker().generateRawToken("resource", "user", OffsetDateTime.now().minusMinutes(1).toInstant());
        String tobeExpiredTokenResponse
            = String.format(TOKEN_RESPONSE_TEMPLATE, tobeExpiredToken, OffsetDateTime.now());
        HttpResponse resp1 = createHttpResponse(200, tobeExpiredTokenResponse);
        HttpResponse resp2 = createHttpResponse(200, VALID_TOKEN_RESPONSE);

        EntraCommunicationTokenCredentialOptions options
            = new EntraCommunicationTokenCredentialOptions(mockTokenCredential, RESOURCE_ENDPOINT, scopes);
        mockHttpClient = new MockHttpClient(resp1, resp2);
        entraTokenCredential = new EntraTokenCredential(options, mockHttpClient);

        // Mutate the array returned by getScopes(), which is a clone
        arraycopy(new String[] { TEAMS_EXTENSION_SCOPE }, 0, options.getScopes(), 0, 1);

        StepVerifier.create(entraTokenCredential.exchangeEntraToken()).assertNext(token -> {
            AccessToken accessToken = (new TokenParser()).parseJWTToken(token);
            assertEquals(SAMPLE_TOKEN, accessToken.getToken());
            assertEquals(2, mockTokenCredential.getCallCount(), "MockTokenCredential.getToken should be called twice");
            assertEquals(2, mockHttpClient.getSendCallCount(),
                "HttpClient.send should be called twice due to token expiry");
        }).verifyComplete();
    }

    @Test
    void entraTokenCredentialScopesChangedAfterOptionsConstructionDoesNotAffectOriginalScopes() {
        String[] validScopes = new String[] { COMMUNICATION_CLIENTS_SCOPE };
        EntraCommunicationTokenCredentialOptions options
            = new EntraCommunicationTokenCredentialOptions(mockTokenCredential, RESOURCE_ENDPOINT, validScopes);

        // Mutate the array returned by getScopes(), which is a clone
        arraycopy(new String[] { "invalid_scope" }, 0, options.getScopes(), 0, 1);

        HttpResponse resp = createHttpResponse(200, VALID_TOKEN_RESPONSE);
        mockHttpClient = new MockHttpClient(resp);
        entraTokenCredential = new EntraTokenCredential(options, mockHttpClient);

        StepVerifier.create(entraTokenCredential.exchangeEntraToken()).assertNext(token -> {
            AccessToken accessToken = (new TokenParser()).parseJWTToken(token);
            assertEquals(SAMPLE_TOKEN, accessToken.getToken());
            assertEquals(1, mockTokenCredential.getCallCount(), "MockTokenCredential.getToken should be called");
            assertEquals(1, mockHttpClient.getSendCallCount(), "HttpClient.send should be called");
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.communication.common.EntraCredentialHelper#validScopesProvider")
    void entraTokenCredentialExchangeEntraTokenFailedResponseThrowsException(String[] scopes) {
        String errorMessage = "{\"error\":{\"code\":\"BadRequest\",\"message\":\"Invalid request.\"}}";
        HttpResponse resp1 = createHttpResponse(400, errorMessage);
        HttpResponse resp2 = createHttpResponse(400, errorMessage);

        createEntraCredentialWithMocks(scopes, resp1, resp2);

        StepVerifier.create(entraTokenCredential.exchangeEntraToken()).verifyError(HttpResponseException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.communication.common.EntraCredentialHelper#validScopesProvider")
    void entraTokenCredentialExchangeEntraTokenInvalidJsonThrowsException(String[] scopes) {
        String invalidJsonResponse = "{\"notAccessToken\":true}";
        HttpResponse resp1 = createHttpResponse(200, invalidJsonResponse);
        HttpResponse resp2 = createHttpResponse(200, invalidJsonResponse);

        createEntraCredentialWithMocks(scopes, resp1, resp2);

        StepVerifier.create(entraTokenCredential.exchangeEntraToken()).verifyError(HttpResponseException.class);
    }

    @Execution(ExecutionMode.SAME_THREAD)
    @ParameterizedTest
    @MethodSource("com.azure.communication.common.EntraCredentialHelper#validScopesProvider")
    void entraTokenCredentialExchangeEntraTokenRetriesThreeTimesOnTransientError(String[] scopes) {
        String lastRetryErrorMessage = "Last Retry Error Message";
        HttpResponse[] mockResponses = new HttpResponse[] {
            createHttpResponse(500, "First Retry Error Message for the pre-warm fetch"),
            createHttpResponse(500, "Second Retry Error Message for the pre-warm fetch"),
            createHttpResponse(500, "Third Retry Error Message for the pre-warm fetch"),
            createHttpResponse(500, "Last retry for the pre-warm fetch"),
            createHttpResponse(500, "First Retry Error Message"),
            createHttpResponse(500, "Second Retry Error Message"),
            createHttpResponse(500, "Third Retry Error Message"),
            createHttpResponse(500, lastRetryErrorMessage) };

        createEntraCredentialWithMocks(scopes, mockResponses);

        StepVerifier.create(entraTokenCredential.exchangeEntraToken()).expectErrorSatisfies(throwable -> {
            assertEquals(8, mockHttpClient.getSendCallCount(),
                "HttpClient.send should be called 8 times (4 for pre-warm, 4 for exchange)");
            assertTrue(throwable instanceof HttpResponseException);
        }).verify();
        entraTokenCredential.close();
    }
}
