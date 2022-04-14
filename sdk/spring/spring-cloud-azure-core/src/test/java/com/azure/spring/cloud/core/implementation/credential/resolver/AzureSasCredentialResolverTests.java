// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.credential.resolver;

import com.azure.spring.cloud.core.provider.authentication.SasTokenProvider;
import com.azure.spring.cloud.core.implementation.properties.AzureAmqpSdkProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

class AzureSasCredentialResolverTests {

    private final AzureSasCredentialResolver resolver = new AzureSasCredentialResolver();

    @Test
    void shouldResolveSas() {
        AzurePropertiesWithSasToken properties = new AzurePropertiesWithSasToken();
        properties.setSasToken("test-sas-token");
        Assertions.assertEquals("test-sas-token", Objects.requireNonNull(resolver.resolve(properties)).getSignature());
    }

    @Test
    void sasTokenAwareShouldResolve() {
        AzurePropertiesWithSasToken properties = new AzurePropertiesWithSasToken();
        properties.setSasToken("test-sas-token");
        Assertions.assertTrue(resolver.isResolvable(properties));
    }

    @Test
    void notSasTokenAwareShouldResolve() {
        AzurePropertiesWithoutSasToken properties = new AzurePropertiesWithoutSasToken();
        Assertions.assertFalse(resolver.isResolvable(properties));
    }

    private static class AzurePropertiesWithSasToken extends AzureAmqpSdkProperties implements SasTokenProvider {

        private String sasToken;

        @Override
        public String getSasToken() {
            return sasToken;
        }

        public void setSasToken(String sasToken) {
            this.sasToken = sasToken;
        }
    }

    private static class AzurePropertiesWithoutSasToken extends AzureAmqpSdkProperties {

    }

}
