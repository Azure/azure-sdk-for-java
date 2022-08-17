package com.azure.identity.providers.jdbc.implementation.credential.descriptor;


import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.providers.jdbc.api.credential.descriptor.CacheKeyDescriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DefaultAzureCredentialCacheKeyDescriptorTest {

    @Test
    void testSupport() {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        DefaultAzureCredentialCacheKeyDescriptor descriptor = new DefaultAzureCredentialCacheKeyDescriptor();
        Assertions.assertTrue(descriptor.support(credential));
    }

    @Test
    void testGetTokenCredentialKeyDescriptors() {
        DefaultAzureCredentialCacheKeyDescriptor descriptor = new DefaultAzureCredentialCacheKeyDescriptor();
        CacheKeyDescriptor.Descriptor[] expected = {
            CacheKeyDescriptor.Descriptor.AUTHORITY_HOST,
            CacheKeyDescriptor.Descriptor.TENANT_ID,
            CacheKeyDescriptor.Descriptor.CLIENT_ID
        };

        CacheKeyDescriptor.Descriptor[] actual = descriptor.getTokenCredentialKeyDescriptors();
        Assertions.assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i], actual[i]);
        }
    }

}
