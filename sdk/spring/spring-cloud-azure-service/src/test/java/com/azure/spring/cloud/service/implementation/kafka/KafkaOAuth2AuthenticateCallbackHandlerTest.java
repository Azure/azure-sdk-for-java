// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.kafka;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.ManagedIdentityCredential;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerTokenCallback;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import org.springframework.test.util.ReflectionTestUtils;

import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.AZURE_TOKEN_CREDENTIAL;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.Mapping.managedIdentityEnabled;
import static com.azure.spring.cloud.service.implementation.kafka.AzureOAuthBearerTokenTest.FAKE_TOKEN;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class KafkaOAuth2AuthenticateCallbackHandlerTest {

    private static final List<String> KAFKA_BOOTSTRAP_SERVER = Arrays.asList("namespace.servicebus.windows.net:9093");
    private static final String TOKEN_CREDENTIAL_FIELD_NAME = "credential";
    private static final String TOKEN_AUDIENCE_FIELD_NAME = "tokenAudience";
    private static final String AZURE_THIRD_PARTY_SERVICE_PROPERTIES_FIELD_NAME = "properties";
    private static final String GET_TOKEN_CREDENTIAL_METHOD_NAME = "getTokenCredential";

    @Test
    void testTokenCredentialShouldConfig() {
        TokenCredential tokenCredential = new TokenCredential() {
            @Override
            public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
                return null;
            }
        };
        Map<String, Object> configs = new HashMap<>();
        configs.put(BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVER);
        configs.put(AZURE_TOKEN_CREDENTIAL, tokenCredential);

        KafkaOAuth2AuthenticateCallbackHandler handler = new KafkaOAuth2AuthenticateCallbackHandler();
        handler.configure(configs, null, null);

        assertEquals(tokenCredential, ReflectionTestUtils.getField(handler, TOKEN_CREDENTIAL_FIELD_NAME));
        TokenCredential getTokenCredential = ReflectionTestUtils.invokeMethod(handler,
            GET_TOKEN_CREDENTIAL_METHOD_NAME);
        assertEquals(tokenCredential, getTokenCredential);
    }

    @Test
    void testCreateDefaultTokenCredential() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVER);
        KafkaOAuth2AuthenticateCallbackHandler handler = new KafkaOAuth2AuthenticateCallbackHandler();
        handler.configure(configs, null, null);

        TokenCredential tokenCredential = (TokenCredential) ReflectionTestUtils.getField(handler, TOKEN_CREDENTIAL_FIELD_NAME);
        assertNull(tokenCredential);
        TokenCredential getTokenCredential = ReflectionTestUtils.invokeMethod(handler, GET_TOKEN_CREDENTIAL_METHOD_NAME);
        assertNotNull(getTokenCredential);
        assertTrue(getTokenCredential instanceof DefaultAzureCredential);
    }

    @Test
    void testCreateTokenCredentialByResolver() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVER);
        configs.put(managedIdentityEnabled.propertyKey(), "true");

        KafkaOAuth2AuthenticateCallbackHandler handler = new KafkaOAuth2AuthenticateCallbackHandler();
        handler.configure(configs, null, null);

        AzureKafkaProperties properties = (AzureKafkaProperties) ReflectionTestUtils
            .getField(handler, AZURE_THIRD_PARTY_SERVICE_PROPERTIES_FIELD_NAME);
        assertTrue(properties.getCredential().isManagedIdentityEnabled());
        TokenCredential tokenCredential = (TokenCredential) ReflectionTestUtils.getField(handler, TOKEN_CREDENTIAL_FIELD_NAME);
        assertNull(tokenCredential);
        TokenCredential getTokenCredential = ReflectionTestUtils.invokeMethod(handler, GET_TOKEN_CREDENTIAL_METHOD_NAME);
        assertNotNull(getTokenCredential);
        assertTrue(getTokenCredential instanceof ManagedIdentityCredential);
    }

    @Test
    void testMultipleBootstrapServersShouldThrowException() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVER);
        configs.put(BOOTSTRAP_SERVERS_CONFIG, Arrays.asList(KAFKA_BOOTSTRAP_SERVER, "localhost:9092"));

        KafkaOAuth2AuthenticateCallbackHandler handler = new KafkaOAuth2AuthenticateCallbackHandler();

        assertThrows(IllegalArgumentException.class, () -> handler.configure(configs, null, null));
    }

    @Test
    void testNoneBootstrapServersShouldThrowException() {
        Map<String, Object> configs = new HashMap<>();
        KafkaOAuth2AuthenticateCallbackHandler handler = new KafkaOAuth2AuthenticateCallbackHandler();

        assertThrows(IllegalArgumentException.class, () -> handler.configure(configs, null, null));
    }

    @Test
    void testInvalidBootstrapServerValueShouldThrowException() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVER);
        configs.put(BOOTSTRAP_SERVERS_CONFIG, Arrays.asList("localhost:9092"));
        KafkaOAuth2AuthenticateCallbackHandler handler = new KafkaOAuth2AuthenticateCallbackHandler();
        assertThrows(IllegalArgumentException.class, () -> handler.configure(configs, null, null));
    }

    @Test
    void testGetDifferentOAuthBearerTokens() throws UnsupportedCallbackException {
        AccessToken accessToken = new AccessToken(FAKE_TOKEN, OffsetDateTime.now().plusMinutes(30));
        TokenCredential tokenCredential = Mockito.mock(TokenCredential.class);
        when(tokenCredential.getToken(any(TokenRequestContext.class)))
                .thenAnswer(invocationOnMock -> Mono.fromCallable(() -> accessToken));
        Map<String, Object> configs = new HashMap<>();
        configs.put(BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVER);
        configs.put(AZURE_TOKEN_CREDENTIAL, tokenCredential);

        KafkaOAuth2AuthenticateCallbackHandler handler = new KafkaOAuth2AuthenticateCallbackHandler();
        handler.configure(configs, null, null);
        OAuthBearerTokenCallback firstOAuthBearerTokenCallback = new OAuthBearerTokenCallback();
        handler.handle(new Callback[] {firstOAuthBearerTokenCallback});
        OAuthBearerTokenCallback secondOAuthBearerTokenCallback = new OAuthBearerTokenCallback();
        handler.handle(new Callback[] {secondOAuthBearerTokenCallback});

        assertNotEquals(firstOAuthBearerTokenCallback.token(), secondOAuthBearerTokenCallback.token());
    }
}
