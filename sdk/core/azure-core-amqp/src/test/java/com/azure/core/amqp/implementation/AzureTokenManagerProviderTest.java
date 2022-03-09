// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.models.CbsAuthorizationType;
import com.azure.core.credential.AccessToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.stream.Stream;

import static com.azure.core.amqp.implementation.AzureTokenManagerProvider.TOKEN_AUDIENCE_FORMAT;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

class AzureTokenManagerProviderTest {
    private static final String HOST_NAME = "foobar.windows.net";

    @Mock
    private ClaimsBasedSecurityNode cbsNode;
    private AutoCloseable mocksCloseable;

    @BeforeEach
    void setup() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void teardown() throws Exception {
        Mockito.framework().clearInlineMock(this);

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    void constructorNullType() {
        assertThrows(NullPointerException.class, () -> new AzureTokenManagerProvider(null, HOST_NAME, "something."));
    }

    @Test
    void constructorNullHost() {
        assertThrows(NullPointerException.class, () -> new AzureTokenManagerProvider(CbsAuthorizationType.JSON_WEB_TOKEN, null, "some-scope"));
    }

    @Test
    void constructorNullScope() {
        assertThrows(NullPointerException.class, () -> new AzureTokenManagerProvider(CbsAuthorizationType.JSON_WEB_TOKEN, HOST_NAME, null));
    }

    public static Stream<CbsAuthorizationType> getResourceString() {
        return Stream.of(CbsAuthorizationType.JSON_WEB_TOKEN, CbsAuthorizationType.SHARED_ACCESS_SIGNATURE);
    }

    /**
     * Verifies that the correct resource string is returned when we pass in different authorization types.
     */
    @ParameterizedTest
    @MethodSource
    public void getResourceString(CbsAuthorizationType authorizationType) {
        // Arrange
        final String scope = "some-scope";
        final AzureTokenManagerProvider provider = new AzureTokenManagerProvider(authorizationType, HOST_NAME, scope);
        final String entityPath = "event-hub-test-2/partition/2";

        // Act
        final String actual = provider.getScopesFromResource(entityPath);

        // Assert
        if (CbsAuthorizationType.SHARED_ACCESS_SIGNATURE.equals(authorizationType)) {
            final String expected = "amqp://" + HOST_NAME + "/" + entityPath;
            Assertions.assertEquals(expected, actual);
        } else if (CbsAuthorizationType.JSON_WEB_TOKEN.equals(authorizationType)) {
            Assertions.assertEquals(scope, actual);
        } else {
            Assertions.fail("This authorization type is unknown: " + authorizationType);
        }
    }

    /**
     * Verifies that for SAS token credentials, the scope is the exact same as the audience because the token credential
     * is generated from it.
     */
    @Test
    void getCorrectTokenManagerSasToken() {
        // Arrange
        final String aadScope = "some-active-directory-scope";
        final AzureTokenManagerProvider provider = new AzureTokenManagerProvider(CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, HOST_NAME, aadScope);
        final String entityPath = "event-hub-test-2/partition/2";
        final AccessToken token = new AccessToken("a-new-access-token", OffsetDateTime.now().plusMinutes(10));
        final String tokenAudience = String.format(Locale.US, TOKEN_AUDIENCE_FORMAT, HOST_NAME, entityPath);

        when(cbsNode.authorize(argThat(audience -> audience.equals(tokenAudience)), argThat(scope -> scope.equals(tokenAudience))))
            .thenReturn(Mono.just(token.getExpiresAt()));

        // Act
        final TokenManager tokenManager = provider.getTokenManager(Mono.just(cbsNode), entityPath);

        // Assert
        StepVerifier.create(tokenManager.authorize())
            .expectNextCount(1)
            .expectComplete()
            .verify(Duration.ofSeconds(10));
    }

    /**
     * Verifies that for JWT token credentials, the scope is the the one that we expect from Azure AAD scope.
     */
    @Test
    void getCorrectTokenManagerJwt() {
        // Arrange
        final String aadScope = "some-active-directory-scope";
        final AzureTokenManagerProvider provider = new AzureTokenManagerProvider(CbsAuthorizationType.JSON_WEB_TOKEN, HOST_NAME, aadScope);
        final String entityPath = "event-hub-test-2/partition/2";
        final AccessToken token = new AccessToken("a-new-access-token", OffsetDateTime.now().plusMinutes(10));
        final String tokenAudience = String.format(Locale.US, TOKEN_AUDIENCE_FORMAT, HOST_NAME, entityPath);

        when(cbsNode.authorize(argThat(audience -> audience.equals(tokenAudience)), argThat(scope -> scope.equals(aadScope))))
            .thenReturn(Mono.just(token.getExpiresAt()));

        // Act
        final TokenManager tokenManager = provider.getTokenManager(Mono.just(cbsNode), entityPath);

        // Assert
        StepVerifier.create(tokenManager.authorize())
            .expectNextCount(1)
            .expectComplete()
            .verify(Duration.ofSeconds(10));
    }

    /**
     * Verify that if the same tokenAudience and scopes are passed in, the same {@link TokenManager} instance is
     * returned.
     */
    @Test
    void differentInstanceReturned() {
        // Arrange
        final String aadScope = "some-active-directory-scope";
        final AzureTokenManagerProvider provider = new AzureTokenManagerProvider(CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, HOST_NAME, aadScope);
        final String entityPath = "event-hub-test-2/partition/2";
        final String entityPath2 = "event-hub-test-2/partition/2";
        final AccessToken token = new AccessToken("a-new-access-token", OffsetDateTime.now().plusMinutes(10));
        final String tokenAudience = String.format(Locale.US, TOKEN_AUDIENCE_FORMAT, HOST_NAME, entityPath);

        when(cbsNode.authorize(argThat(audience -> audience.equals(tokenAudience)), argThat(scope -> scope.equals(tokenAudience))))
            .thenReturn(Mono.just(token.getExpiresAt()));

        // Act
        final TokenManager tokenManager = provider.getTokenManager(Mono.just(cbsNode), entityPath);
        final TokenManager tokenManager2 = provider.getTokenManager(Mono.just(cbsNode), entityPath2);

        Assertions.assertNotSame(tokenManager, tokenManager2);
    }
}
