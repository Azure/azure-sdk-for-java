// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.credential.descriptor;

import com.azure.identity.ClientSecretCredential;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class ClientSecretCredentialCacheKeyDescriptorTest {

    @Test
    void testSupport() {
        ClientSecretCredential credential = mock(ClientSecretCredential.class);
        ClientSecretCredentialCacheKeyDescriptor descriptor = new ClientSecretCredentialCacheKeyDescriptor();
        Assertions.assertTrue(descriptor.support(credential));
    }

    @Test
    void testGetTokenCredentialKeyDescriptors() {
        ClientSecretCredentialCacheKeyDescriptor descriptor = new ClientSecretCredentialCacheKeyDescriptor();
        CacheKeyDescriptor.Descriptor[] expected = {
            CacheKeyDescriptor.Descriptor.AUTHORITY_HOST,
            CacheKeyDescriptor.Descriptor.TENANT_ID,
            CacheKeyDescriptor.Descriptor.CLIENT_ID,
            CacheKeyDescriptor.Descriptor.CLIENT_SECRET
        };

        CacheKeyDescriptor.Descriptor[] actual = descriptor.getTokenCredentialKeyDescriptors();
        Assertions.assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i], actual[i]);
        }
    }
}
