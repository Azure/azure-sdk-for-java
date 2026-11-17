// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx.implementation;

import com.azure.core.credential.ClientCertificateBinding;
import com.azure.core.util.logging.ClientLogger;
import io.vertx.core.http.HttpClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Caches a Vert.x HTTP client for each TLS client certificate that requests are bound to, keyed by the certificate's
 * key ID.
 * <p>
 * A client is retired when its certificate expires, or when more certificates are in use than the cache holds, least
 * recently used first. Closing a Vert.x client closes the connections its requests are using, so a request leases its
 * client until its exchange is over, and a retired client is closed once it's no longer leased.
 */
public final class ClientCertificateBoundClients {
    private static final ClientLogger LOGGER = new ClientLogger(ClientCertificateBoundClients.class);

    /**
     * The default number of certificates the cache holds clients for.
     */
    public static final int DEFAULT_MAXIMUM_CLIENTS = 16;

    private final Function<ClientCertificateBinding, HttpClient> clientFactory;
    private final Consumer<HttpClient> closer;
    private final Supplier<Instant> clock;
    private final int maximumClients;

    // Access order, so that the least recently used client comes first.
    private final LinkedHashMap<String, BoundClient> clients = new LinkedHashMap<>(16, 0.75f, true);

    /**
     * Creates a cache of clients bound to TLS client certificates.
     *
     * @param clientFactory Creates the client for a certificate.
     * @param clock Supplies the current time, used to retire clients whose certificate expired.
     * @param maximumClients The number of certificates the cache holds clients for.
     */
    public ClientCertificateBoundClients(Function<ClientCertificateBinding, HttpClient> clientFactory,
        Supplier<Instant> clock, int maximumClients) {
        this(clientFactory, ClientCertificateBoundClients::close, clock, maximumClients);
    }

    ClientCertificateBoundClients(Function<ClientCertificateBinding, HttpClient> clientFactory,
        Consumer<HttpClient> closer, Supplier<Instant> clock, int maximumClients) {
        this.clientFactory = clientFactory;
        this.closer = closer;
        this.clock = clock;
        this.maximumClients = maximumClients;
    }

    /**
     * Leases the client for a TLS client certificate, creating it if needed, and retires clients that are no longer
     * needed.
     * <p>
     * Release the lease once the request's exchange is over.
     *
     * @param binding The client certificate the request is bound to.
     * @return The lease on the client, which presents the certificate.
     */
    public Lease lease(ClientCertificateBinding binding) {
        List<BoundClient> retired = new ArrayList<>();
        BoundClient client;

        synchronized (clients) {
            client = clients.get(binding.getKeyId());
            if (client == null) {
                client = new BoundClient(clientFactory.apply(binding), binding, closer);
                clients.put(binding.getKeyId(), client);
            }

            // Leased while the cache holds it, so that it's only closed after the lease is released.
            client.acquire();

            Instant now = clock.get();
            Iterator<BoundClient> iterator = clients.values().iterator();
            while (iterator.hasNext()) {
                BoundClient candidate = iterator.next();
                if (candidate != client && (candidate.isExpired(now) || clients.size() > maximumClients)) {
                    iterator.remove();
                    retired.add(candidate);
                }
            }
        }

        for (BoundClient retiredClient : retired) {
            retiredClient.retire();
        }

        return new Lease(client);
    }

    /**
     * Gets the key IDs of the certificates the cache holds clients for, least recently used first.
     *
     * @return The key IDs.
     */
    public List<String> getKeyIds() {
        synchronized (clients) {
            return new ArrayList<>(clients.keySet());
        }
    }

    private static void close(HttpClient client) {
        client.close()
            .onFailure(error -> LOGGER.atVerbose()
                .log("Failed to close the HTTP client of a retired client certificate.", error));
    }

    /**
     * A request's lease on the client for a TLS client certificate.
     */
    public static final class Lease {
        private final BoundClient client;
        private final AtomicBoolean released = new AtomicBoolean();

        private Lease(BoundClient client) {
            this.client = client;
        }

        /**
         * Gets the client, which presents the certificate.
         *
         * @return The client.
         */
        public HttpClient getHttpClient() {
            return client.httpClient;
        }

        /**
         * Releases the lease. Releasing it again has no effect.
         */
        public void release() {
            if (released.compareAndSet(false, true)) {
                client.release();
            }
        }
    }

    private static final class BoundClient {
        private final HttpClient httpClient;
        private final Instant notAfter;
        private final Consumer<HttpClient> closer;

        // Guarded by this.
        private int leases;
        private boolean retired;

        private BoundClient(HttpClient httpClient, ClientCertificateBinding binding, Consumer<HttpClient> closer) {
            this.httpClient = httpClient;
            this.notAfter = binding.getCertificate().getNotAfter().toInstant();
            this.closer = closer;
        }

        private synchronized void acquire() {
            leases++;
        }

        private void release() {
            boolean close;
            synchronized (this) {
                leases--;
                close = retired && leases == 0;
            }

            if (close) {
                closer.accept(httpClient);
            }
        }

        private boolean isExpired(Instant now) {
            return now.isAfter(notAfter);
        }

        private void retire() {
            boolean close;
            synchronized (this) {
                retired = true;
                close = leases == 0;
            }

            if (close) {
                closer.accept(httpClient);
            }
        }
    }
}
