// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.credential.descriptor;

import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.identity.providers.jdbc.api.credential.descriptor.CacheKeyDescriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ManagedIdentityCredentialCacheKeyDescriptorTest {

    @Test
    void testSupport() {
        ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().build();
        ManagedIdentityCredentialCacheKeyDescriptor descriptor = new ManagedIdentityCredentialCacheKeyDescriptor();
        Assertions.assertTrue(descriptor.support(credential));
    }

    @Test
    void testGetTokenCredentialKeyDescriptors() {
        ManagedIdentityCredentialCacheKeyDescriptor descriptor = new ManagedIdentityCredentialCacheKeyDescriptor();
        CacheKeyDescriptor.Descriptor[] expected = {
            CacheKeyDescriptor.Descriptor.AUTHORITY_HOST,
            CacheKeyDescriptor.Descriptor.CLIENT_ID
        };

        CacheKeyDescriptor.Descriptor[] actual = descriptor.getTokenCredentialKeyDescriptors();
        Assertions.assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i], actual[i]);
        }
    }

}
