// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.redis.passwordless.data.jedis;

import com.azure.identity.extensions.implementation.template.AzureAuthenticationTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AzureRedisCredentialSupplierTest {

    @Test
    void testGet() {
        AzureAuthenticationTemplate template = mock(AzureAuthenticationTemplate.class);
        when(template.getTokenAsPassword()).thenReturn("fake-password");

        AzureRedisCredentialSupplier supplier = new AzureRedisCredentialSupplier(template);
        Assertions.assertEquals("fake-password", supplier.get());
    }

}
