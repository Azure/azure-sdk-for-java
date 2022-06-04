// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.credential.resolver;

import com.azure.spring.cloud.core.provider.authentication.NamedKeyProvider;
import com.azure.spring.cloud.core.implementation.properties.AzureAmqpSdkProperties;
import com.azure.spring.cloud.core.properties.authentication.NamedKeyProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

class AzureNamedKeyCredentialResolverTests {

    private final AzureNamedKeyCredentialResolver resolver = new AzureNamedKeyCredentialResolver();

    @Test
    void shouldResolveNamedKey() {
        AzurePropertiesWithNamedKey properties = new AzurePropertiesWithNamedKey();
        NamedKeyProperties namedKey = new NamedKeyProperties();
        namedKey.setName("test-key-name");
        namedKey.setKey("test-key");
        properties.setNamedKey(namedKey);
        Assertions.assertEquals("test-key-name", Objects.requireNonNull(resolver.resolve(properties)).getAzureNamedKey().getName());
        Assertions.assertEquals("test-key", Objects.requireNonNull(resolver.resolve(properties)).getAzureNamedKey().getKey());
    }

    @Test
    void namedKeyAwareShouldResolve() {
        AzurePropertiesWithNamedKey properties = new AzurePropertiesWithNamedKey();
        NamedKeyProperties namedKey = new NamedKeyProperties();
        namedKey.setName("test-key-name");
        namedKey.setKey("test-key");
        properties.setNamedKey(namedKey);
        Assertions.assertTrue(resolver.isResolvable(properties));
    }

    @Test
    void notNamedKeyAwareShouldNotResolve() {
        AzurePropertiesWithoutNamedKey properties = new AzurePropertiesWithoutNamedKey();
        Assertions.assertFalse(resolver.isResolvable(properties));
    }

    private static class AzurePropertiesWithNamedKey extends AzureAmqpSdkProperties implements NamedKeyProvider {

        private NamedKeyProperties namedKey;

        @Override
        public NamedKeyProperties getNamedKey() {
            return namedKey;
        }

        public void setNamedKey(NamedKeyProperties namedKey) {
            this.namedKey = namedKey;
        }
    }

    private static class AzurePropertiesWithoutNamedKey extends AzureAmqpSdkProperties {

    }
    
}
