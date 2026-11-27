// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.credential.ClientCertificateBinding;
import com.azure.core.http.netty.implementation.ClientCertificateBoundClients.BoundClient;
import com.azure.core.validation.http.MtlsTestCertificates;
import io.netty.resolver.AddressResolverGroup;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.transport.TransportConfig;

import java.net.SocketAddress;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
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
    public void clientIsReusedForTheSameCertificate() {
        ClientCertificateBoundClients clients = new ClientCertificateBoundClients(
            binding -> createClient(binding, new ConcurrentHashMap<>()), Instant::now, 16);

        assertSame(clients.getClient(BINDING_A), clients.getClient(BINDING_A));
    }

    @Test
    public void leastRecentlyUsedClientIsRetiredWhenTooManyCertificatesAreInUse() {
        Map<String, AtomicBoolean> disposed = new ConcurrentHashMap<>();
        ClientCertificateBoundClients clients
            = new ClientCertificateBoundClients(binding -> createClient(binding, disposed), Instant::now, 1);

        clients.getClient(BINDING_A);
        clients.getClient(BINDING_B);

        assertEquals(Collections.singletonList(BINDING_B.getKeyId()), clients.getKeyIds());
        assertTrue(disposed.get(BINDING_A.getKeyId()).get());
        assertFalse(disposed.get(BINDING_B.getKeyId()).get());
    }

    @Test
    public void clientIsRetiredWhenItsCertificateExpires() {
        Map<String, AtomicBoolean> disposed = new ConcurrentHashMap<>();
        AtomicReference<Instant> now = new AtomicReference<>(Instant.now());
        ClientCertificateBoundClients clients
            = new ClientCertificateBoundClients(binding -> createClient(binding, disposed), now::get, 16);

        clients.getClient(BINDING_A);
        now.set(BINDING_A.getCertificate().getNotAfter().toInstant().plusSeconds(1));
        clients.getClient(BINDING_B);

        assertEquals(Collections.singletonList(BINDING_B.getKeyId()), clients.getKeyIds());
        assertTrue(disposed.get(BINDING_A.getKeyId()).get());
    }

    // Creates a client whose connection provider records when it's disposed.
    private static BoundClient createClient(ClientCertificateBinding binding, Map<String, AtomicBoolean> disposed) {
        AtomicBoolean providerDisposed = new AtomicBoolean();
        disposed.put(binding.getKeyId(), providerDisposed);
        ConnectionProvider connectionProvider = new ConnectionProvider() {
            @Override
            public Mono<? extends Connection> acquire(TransportConfig config, ConnectionObserver connectionObserver,
                Supplier<? extends SocketAddress> remoteAddress, AddressResolverGroup<?> resolverGroup) {
                return Mono.never();
            }

            @Override
            public Mono<Void> disposeLater() {
                return Mono.fromRunnable(() -> providerDisposed.set(true));
            }
        };

        return new BoundClient(HttpClient.create(connectionProvider), connectionProvider, binding);
    }
}
