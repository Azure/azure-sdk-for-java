// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.credential;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.spring.core.aware.authentication.KeyAware;
import com.azure.spring.core.aware.authentication.NamedKeyAware;
import com.azure.spring.core.aware.authentication.SasTokenAware;
import com.azure.spring.core.implementation.credential.resolver.AzureKeyCredentialResolver;
import com.azure.spring.core.implementation.credential.resolver.AzureNamedKeyCredentialResolver;
import com.azure.spring.core.implementation.credential.resolver.AzureSasCredentialResolver;
import com.azure.spring.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.core.properties.AzureHttpSdkProperties;
import com.azure.spring.core.properties.authentication.NamedKeyProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class AzureCredentialResolversTests {

    private AzureCredentialResolvers azureCredentialResolvers;
    private static final TestAzureProperties PROPERTIES;
    private static final AzureTokenCredentialResolver TOKEN_CREDENTIAL_RESOLVER = new AzureTokenCredentialResolver();
    private static final AzureSasCredentialResolver SAS_CREDENTIAL_RESOLVER = new AzureSasCredentialResolver();
    private static final AzureKeyCredentialResolver KEY_CREDENTIAL_RESOLVER = new AzureKeyCredentialResolver();
    private static final AzureNamedKeyCredentialResolver NAMED_KEY_CREDENTIAL_RESOLVER = new AzureNamedKeyCredentialResolver();

    static {
        PROPERTIES = new TestAzureProperties();
        PROPERTIES.setKey("test-key");
        PROPERTIES.getNamedKey().setName("test-named-key-name");
        PROPERTIES.getNamedKey().setKey("test-named-key-key");
        PROPERTIES.setSasToken("test-sas-token");
        PROPERTIES.getCredential().setClientId("test-client-id");
        PROPERTIES.getCredential().setClientSecret("test-client-secret");
        PROPERTIES.getProfile().setTenantId("test-tenant-id");
    }

    @Test
    void shouldResolveTokenCredential() {
        azureCredentialResolvers = new AzureCredentialResolvers(Arrays.asList(TOKEN_CREDENTIAL_RESOLVER));
        Object resolve = azureCredentialResolvers.resolve(PROPERTIES);
        Assertions.assertInstanceOf(TokenCredential.class, resolve);
    }

    @Test
    void shouldResolveKeyCredential() {
        azureCredentialResolvers = new AzureCredentialResolvers(Arrays.asList(KEY_CREDENTIAL_RESOLVER));
        Object resolve = azureCredentialResolvers.resolve(PROPERTIES);
        Assertions.assertInstanceOf(AzureKeyCredential.class, resolve);
    }

    @Test
    void shouldResolveSasCredential() {
        azureCredentialResolvers = new AzureCredentialResolvers(Arrays.asList(SAS_CREDENTIAL_RESOLVER));
        Object resolve = azureCredentialResolvers.resolve(PROPERTIES);
        Assertions.assertInstanceOf(AzureSasCredential.class, resolve);
    }

    @Test
    void shouldResolveNamedKeyCredential() {
        azureCredentialResolvers = new AzureCredentialResolvers(Arrays.asList(NAMED_KEY_CREDENTIAL_RESOLVER));
        Object resolve = azureCredentialResolvers.resolve(PROPERTIES);
        Assertions.assertInstanceOf(AzureNamedKeyCredential.class, resolve);
    }

    @Test
    void shouldResolveTokenCredentialWithTwoResolversProvided() {
        azureCredentialResolvers = new AzureCredentialResolvers(Arrays.asList(TOKEN_CREDENTIAL_RESOLVER, KEY_CREDENTIAL_RESOLVER));
        Object resolve = azureCredentialResolvers.resolve(PROPERTIES);
        Assertions.assertInstanceOf(TokenCredential.class, resolve);
    }

    @Test
    void shouldResolveKeyCredentialWithTwoResolversProvided() {
        azureCredentialResolvers = new AzureCredentialResolvers(Arrays.asList(KEY_CREDENTIAL_RESOLVER, TOKEN_CREDENTIAL_RESOLVER));
        Object resolve = azureCredentialResolvers.resolve(PROPERTIES);
        Assertions.assertInstanceOf(AzureKeyCredential.class, resolve);
    }

    @Test
    void shouldResolveTokenCredentialWithTwoResolversWithComparatorProvided() {
        azureCredentialResolvers = new AzureCredentialResolvers(Arrays.asList(KEY_CREDENTIAL_RESOLVER, TOKEN_CREDENTIAL_RESOLVER), (o1, o2) -> -1);
        Object resolve = azureCredentialResolvers.resolve(PROPERTIES);
        Assertions.assertInstanceOf(TokenCredential.class, resolve);
    }

    private static class TestAzureProperties extends AzureHttpSdkProperties
        implements KeyAware, SasTokenAware, NamedKeyAware {

        private String key;
        private final NamedKeyProperties namedKey = new NamedKeyProperties();
        private String sasToken;

        @Override
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @Override
        public NamedKeyProperties getNamedKey() {
            return namedKey;
        }

        @Override
        public String getSasToken() {
            return sasToken;
        }

        public void setSasToken(String sasToken) {
            this.sasToken = sasToken;
        }
    }

}
