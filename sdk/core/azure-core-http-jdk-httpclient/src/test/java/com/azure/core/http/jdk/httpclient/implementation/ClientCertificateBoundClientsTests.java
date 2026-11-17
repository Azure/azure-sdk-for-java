// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient.implementation;

import com.azure.core.credential.ClientCertificateBinding;
import com.azure.core.validation.http.MtlsTestCertificates;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import java.lang.reflect.Method;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link ClientCertificateBoundClients}.
 */
public class ClientCertificateBoundClientsTests {
    private static final ClientCertificateBinding BINDING_A
        = MtlsTestCertificates.loadBinding(MtlsTestCertificates.CLIENT_A);
    private static final ClientCertificateBinding BINDING_B
        = MtlsTestCertificates.loadBinding(MtlsTestCertificates.CLIENT_B);

    @Test
    public void retiredClientIsShutDownOnceItIsNotLeased() {
        List<HttpClient> shutDown = new CopyOnWriteArrayList<>();
        ClientCertificateBoundClients clients
            = new ClientCertificateBoundClients(binding -> HttpClient.newHttpClient(), shutDown::add, Instant::now, 1);

        // The cache holds one certificate's client, so B's client retires A's, which is still leased.
        ClientCertificateBoundClients.Lease leaseA = clients.lease(BINDING_A);
        ClientCertificateBoundClients.Lease leaseB = clients.lease(BINDING_B);
        leaseB.release();
        assertEquals(Collections.singletonList(BINDING_B.getKeyId()), clients.getKeyIds());
        assertEquals(Collections.emptyList(), shutDown);

        leaseA.release();
        assertEquals(Collections.singletonList(leaseA.getHttpClient()), shutDown);

        // A new client for A retires B's, which isn't leased.
        ClientCertificateBoundClients.Lease newLeaseA = clients.lease(BINDING_A);
        newLeaseA.release();
        assertNotSame(leaseA.getHttpClient(), newLeaseA.getHttpClient());
        assertEquals(Arrays.asList(leaseA.getHttpClient(), leaseB.getHttpClient()), shutDown);
    }

    @Test
    @EnabledForJreRange(min = JRE.JAVA_21)
    public void retiredClientIsShutDown() throws ReflectiveOperationException {
        ClientCertificateBoundClients clients
            = new ClientCertificateBoundClients(binding -> HttpClient.newHttpClient(), Instant::now, 1);
        ClientCertificateBoundClients.Lease leaseA = clients.lease(BINDING_A);
        leaseA.release();

        clients.lease(BINDING_B).release();

        // HttpClient.awaitTermination(Duration) was added in Java 21, and the tests are compiled for Java 11.
        Method awaitTermination = HttpClient.class.getMethod("awaitTermination", Duration.class);
        assertTrue((Boolean) awaitTermination.invoke(leaseA.getHttpClient(), Duration.ofSeconds(30)));
    }
}
