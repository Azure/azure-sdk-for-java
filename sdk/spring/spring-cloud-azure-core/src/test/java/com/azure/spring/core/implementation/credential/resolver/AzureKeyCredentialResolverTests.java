// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.implementation.credential.resolver;

import com.azure.spring.core.aware.authentication.KeyAware;
import com.azure.spring.core.properties.AzureHttpSdkProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

class AzureKeyCredentialResolverTests {

    private final AzureKeyCredentialResolver resolver = new AzureKeyCredentialResolver();

    @Test
    void shouldResolveKey() {
        AzurePropertiesWithKey properties = new AzurePropertiesWithKey();
        properties.setKey("test-key");
        Assertions.assertEquals("test-key", Objects.requireNonNull(resolver.resolve(properties)).getKey());
    }

    @Test
    void keyAwareShouldResolve() {
        AzurePropertiesWithKey properties = new AzurePropertiesWithKey();
        properties.setKey("test-key");
        Assertions.assertTrue(resolver.isResolvable(properties));
    }

    @Test
    void notKeyAwareShouldNotResolve() {
        AzurePropertiesWithoutKey properties = new AzurePropertiesWithoutKey();
        Assertions.assertFalse(resolver.isResolvable(properties));
    }

    private static class AzurePropertiesWithKey extends AzureHttpSdkProperties implements KeyAware {

        private String key;

        @Override
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

    private static class AzurePropertiesWithoutKey extends AzureHttpSdkProperties {

    }
}
