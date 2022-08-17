package com.azure.identity.providers.jdbc.implementation.credential.descriptor;


import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.providers.jdbc.api.credential.descriptor.CacheKeyDescriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class ClientCertificateCredentialCacheKeyDescriptorTest {

    @Test
    void testSupport() {
        ClientCertificateCredential credential = mock(ClientCertificateCredential.class);
        ClientCertificateCredentialCacheKeyDescriptor descriptor = new ClientCertificateCredentialCacheKeyDescriptor();
        Assertions.assertTrue(descriptor.support(credential));
    }

    @Test
    void testGetTokenCredentialKeyDescriptors() {
        ClientCertificateCredentialCacheKeyDescriptor descriptor = new ClientCertificateCredentialCacheKeyDescriptor();
        CacheKeyDescriptor.Descriptor[] expected = {
            CacheKeyDescriptor.Descriptor.AUTHORITY_HOST,
            CacheKeyDescriptor.Descriptor.TENANT_ID,
            CacheKeyDescriptor.Descriptor.CLIENT_CERTIFICATE_PATH,
            CacheKeyDescriptor.Descriptor.CLIENT_CERTIFICATE_PASSWORD
        };

        CacheKeyDescriptor.Descriptor[] actual = descriptor.getTokenCredentialKeyDescriptors();
        Assertions.assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i], actual[i]);
        }
    }
}
