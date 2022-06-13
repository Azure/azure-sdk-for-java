// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.kafka;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.spring.cloud.core.implementation.properties.AzureThirdPartyServiceProperties;
import com.azure.spring.cloud.service.kafka.KafkaOAuth2AuthenticateCallbackHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.AZURE_TOKEN_CREDENTIAL;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.MANAGED_IDENTITY_ENABLED;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KafkaOAuth2AuthenticateCallbackHandlerTest {

    private static final String KAFKA_BOOTSTRAP_SERVER = "namespace.servicebus.windows.net";
    private static final String TOKEN_CREDENTIAL_FIELD_NAME = "credential";
    private static final String AZURE_THIRD_PARTY_SERVICE_PROPERTIES_FIELD_NAME = "properties";
    private static final String GET_TOKEN_CREDENTIAL_METHOD_NAME = "getTokenCredential";
    private final Map<String, Object> configs = new HashMap<>();

    private KafkaOAuth2AuthenticateCallbackHandler handler;

    private TokenCredential tokenCredential = new TokenCredential() {
        @Override
        public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
            return null;
        }
    };

    @BeforeEach
    public void setup() {
        configs.clear();
        configs.put(BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVER);
        handler = new KafkaOAuth2AuthenticateCallbackHandler();
    }

    @Test
    void tokenCredentialShouldConfig() {
        configs.put(AZURE_TOKEN_CREDENTIAL, tokenCredential);
        handler.configure(configs, null, null);

        TokenCredential tokenCredential = (TokenCredential) ReflectionTestUtils.getField(handler, TOKEN_CREDENTIAL_FIELD_NAME);
        assertEquals(this.tokenCredential, tokenCredential);
        TokenCredential getTokenCredential = ReflectionTestUtils.invokeMethod(handler,
            GET_TOKEN_CREDENTIAL_METHOD_NAME);
        assertEquals(this.tokenCredential, getTokenCredential);
    }

    @Test
    void createDefaultTokenCredential() {
        handler.configure(configs, null, null);

        TokenCredential tokenCredential = (TokenCredential) ReflectionTestUtils.getField(handler, TOKEN_CREDENTIAL_FIELD_NAME);
        assertNull(tokenCredential);
        TokenCredential getTokenCredential = ReflectionTestUtils.invokeMethod(handler, GET_TOKEN_CREDENTIAL_METHOD_NAME);
        assertNotNull(getTokenCredential);
        assertTrue(getTokenCredential instanceof DefaultAzureCredential);
    }

    @Test
    void createTokenCredentialByResolver() {
        configs.put(MANAGED_IDENTITY_ENABLED, true);
        handler.configure(configs, null, null);

        AzureThirdPartyServiceProperties properties = (AzureThirdPartyServiceProperties) ReflectionTestUtils
            .getField(handler, AZURE_THIRD_PARTY_SERVICE_PROPERTIES_FIELD_NAME);
        assertTrue(properties.getCredential().isManagedIdentityEnabled());
        TokenCredential tokenCredential = (TokenCredential) ReflectionTestUtils.getField(handler, TOKEN_CREDENTIAL_FIELD_NAME);
        assertNull(tokenCredential);
        TokenCredential getTokenCredential = ReflectionTestUtils.invokeMethod(handler, GET_TOKEN_CREDENTIAL_METHOD_NAME);
        assertNotNull(getTokenCredential);
        assertTrue(getTokenCredential instanceof ManagedIdentityCredential);
    }
}
