// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx.implementation;

import com.azure.core.credential.ClientCertificateBinding;
import com.azure.core.validation.http.MtlsTestCertificates;
import io.vertx.core.http.HttpClient;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Tests {@link ClientCertificateBoundClients}.
 */
public class ClientCertificateBoundClientsTests {
    private static final ClientCertificateBinding BINDING_A
        = MtlsTestCertificates.loadBinding(MtlsTestCertificates.CLIENT_A);
    private static final ClientCertificateBinding BINDING_B
        = MtlsTestCertificates.loadBinding(MtlsTestCertificates.CLIENT_B);

    @Test
    public void retiredClientIsClosedOnceItIsNotLeased() {
        List<HttpClient> closed = new CopyOnWriteArrayList<>();
        ClientCertificateBoundClients clients
            = new ClientCertificateBoundClients(binding -> mock(HttpClient.class), closed::add, Instant::now, 1);

        // The cache holds one certificate's client, so B's client retires A's, which two requests lease.
        ClientCertificateBoundClients.Lease leaseA = clients.lease(BINDING_A);
        ClientCertificateBoundClients.Lease otherLeaseA = clients.lease(BINDING_A);
        clients.lease(BINDING_B).release();
        assertEquals(Collections.singletonList(BINDING_B.getKeyId()), clients.getKeyIds());

        // Releasing a lease again doesn't release the other lease.
        leaseA.release();
        leaseA.release();
        assertEquals(Collections.emptyList(), closed);

        otherLeaseA.release();
        assertEquals(Collections.singletonList(leaseA.getHttpClient()), closed);
    }
}
