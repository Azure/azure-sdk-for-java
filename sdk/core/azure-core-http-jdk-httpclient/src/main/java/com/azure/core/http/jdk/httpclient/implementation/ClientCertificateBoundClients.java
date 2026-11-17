// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient.implementation;

import com.azure.core.credential.ClientCertificateBinding;
import com.azure.core.util.logging.ClientLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.http.HttpClient;
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
 * Caches a JDK HTTP client for each TLS client certificate that requests are bound to, keyed by the certificate's key
 * ID.
 * <p>
 * A client is retired when its certificate expires, or when more certificates are in use than the cache holds, least
 * recently used first. A request leases its client until it has been submitted, and a retired client is shut down once
 * it's no longer leased, which lets the requests submitted to it complete. Before Java 21, which added
 * {@code HttpClient.shutdown()}, a retired client is released once it's unreachable.
 */
public final class ClientCertificateBoundClients {
    private static final ClientLogger LOGGER = new ClientLogger(ClientCertificateBoundClients.class);

    /**
     * The default number of certificates the cache holds clients for.
     */
    public static final int DEFAULT_MAXIMUM_CLIENTS = 16;

    // HttpClient.shutdown() was added in Java 21, and this module is compiled for Java 11.
    private static final Method SHUTDOWN = getShutdownMethod();

    private final Function<ClientCertificateBinding, HttpClient> clientFactory;
    private final Consumer<HttpClient> shutdown;
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
        this(clientFactory, ClientCertificateBoundClients::shutDown, clock, maximumClients);
    }

    ClientCertificateBoundClients(Function<ClientCertificateBinding, HttpClient> clientFactory,
        Consumer<HttpClient> shutdown, Supplier<Instant> clock, int maximumClients) {
        this.clientFactory = clientFactory;
        this.shutdown = shutdown;
        this.clock = clock;
        this.maximumClients = maximumClients;
    }

    /**
     * Leases the client for a TLS client certificate, creating it if needed, and retires clients that are no longer
     * needed.
     * <p>
     * Release the lease once the request has been submitted to the client.
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
                client = new BoundClient(clientFactory.apply(binding), binding, shutdown);
                clients.put(binding.getKeyId(), client);
            }

            // Leased while the cache holds it, so that it's only shut down after the lease is released.
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

    private static Method getShutdownMethod() {
        try {
            return HttpClient.class.getMethod("shutdown");
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static void shutDown(HttpClient client) {
        if (SHUTDOWN == null) {
            return;
        }

        try {
            SHUTDOWN.invoke(client);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.atVerbose().log("Failed to shut down the HTTP client of a retired client certificate.", e);
        }
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
        private final Consumer<HttpClient> shutdown;

        // Guarded by this.
        private int leases;
        private boolean retired;

        private BoundClient(HttpClient httpClient, ClientCertificateBinding binding, Consumer<HttpClient> shutdown) {
            this.httpClient = httpClient;
            this.notAfter = binding.getCertificate().getNotAfter().toInstant();
            this.shutdown = shutdown;
        }

        private synchronized void acquire() {
            leases++;
        }

        private void release() {
            boolean shutDown;
            synchronized (this) {
                leases--;
                shutDown = retired && leases == 0;
            }

            if (shutDown) {
                shutdown.accept(httpClient);
            }
        }

        private boolean isExpired(Instant now) {
            return now.isAfter(notAfter);
        }

        private void retire() {
            boolean shutDown;
            synchronized (this) {
                retired = true;
                shutDown = leases == 0;
            }

            if (shutDown) {
                shutdown.accept(httpClient);
            }
        }
    }
}
