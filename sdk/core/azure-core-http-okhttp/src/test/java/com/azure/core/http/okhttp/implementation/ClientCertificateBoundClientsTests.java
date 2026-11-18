// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import com.azure.core.credential.ClientCertificateBinding;
import com.azure.core.validation.http.MtlsTestCertificates;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests {@link ClientCertificateBoundClients}.
 */
public class ClientCertificateBoundClientsTests {
    private static final ClientCertificateBinding BINDING_A
        = MtlsTestCertificates.loadBinding(MtlsTestCertificates.CLIENT_A);
    private static final ClientCertificateBinding BINDING_B
        = MtlsTestCertificates.loadBinding(MtlsTestCertificates.CLIENT_B);

    @Test
    public void leastRecentlyUsedClientIsRetiredWhenTooManyCertificatesAreInUse() {
        OkHttpClient base = new OkHttpClient();
        ClientCertificateBoundClients clients
            = new ClientCertificateBoundClients(binding -> base.newBuilder().build(), Instant::now, 1);

        OkHttpClient clientA = clients.getClient(BINDING_A);
        assertSame(clientA, clients.getClient(BINDING_A));
        clients.getClient(BINDING_B);

        assertEquals(Collections.singletonList(BINDING_B.getKeyId()), clients.getKeyIds());
        assertNotSame(clientA, clients.getClient(BINDING_A));
    }
}
