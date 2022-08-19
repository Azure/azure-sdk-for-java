// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.credential.descriptor;

import com.azure.identity.UsernamePasswordCredential;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class UsernamePasswordCredentialCacheKeyDescriptorTest {
    @Test
    void testSupport() {
        UsernamePasswordCredential credential = mock(UsernamePasswordCredential.class);
        UsernamePasswordCredentialCacheKeyDescriptor descriptor = new UsernamePasswordCredentialCacheKeyDescriptor();
        Assertions.assertTrue(descriptor.support(credential));
    }

    @Test
    void testGetTokenCredentialKeyDescriptors() {
        UsernamePasswordCredentialCacheKeyDescriptor descriptor = new UsernamePasswordCredentialCacheKeyDescriptor();
        CacheKeyDescriptor.Descriptor[] expected = {
            CacheKeyDescriptor.Descriptor.AUTHORITY_HOST,
            CacheKeyDescriptor.Descriptor.TENANT_ID,
            CacheKeyDescriptor.Descriptor.USERNAME,
            CacheKeyDescriptor.Descriptor.PASSWORD
        };

        CacheKeyDescriptor.Descriptor[] actual = descriptor.getTokenCredentialKeyDescriptors();
        Assertions.assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i], actual[i]);
        }
    }
}
