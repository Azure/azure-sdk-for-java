// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.credential.ClientCertificateBinding;
import com.azure.core.util.logging.ClientLogger;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Caches a Reactor Netty client for each TLS client certificate that requests are bound to, keyed by the
 * certificate's key ID.
 * <p>
 * A client is retired when its certificate expires, or when more certificates are in use than the cache holds, least
 * recently used first. Retiring a client disposes the connection provider it owns, which closes its connections once
 * their requests complete.
 */
public final class ClientCertificateBoundClients {
    private static final ClientLogger LOGGER = new ClientLogger(ClientCertificateBoundClients.class);

    /**
     * The default number of certificates the cache holds clients for.
     */
    public static final int DEFAULT_MAXIMUM_CLIENTS = 16;

    private final Function<ClientCertificateBinding, BoundClient> clientFactory;
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
    public ClientCertificateBoundClients(Function<ClientCertificateBinding, BoundClient> clientFactory,
        Supplier<Instant> clock, int maximumClients) {
        this.clientFactory = clientFactory;
        this.clock = clock;
        this.maximumClients = maximumClients;
    }

    /**
     * Gets the client for a TLS client certificate, creating it if needed, and retires clients that are no longer
     * needed.
     *
     * @param binding The client certificate the request is bound to.
     * @return The client that presents the certificate.
     */
    public HttpClient getClient(ClientCertificateBinding binding) {
        List<BoundClient> retired = new ArrayList<>();
        BoundClient client;

        synchronized (clients) {
            client = clients.get(binding.getKeyId());
            if (client == null) {
                client = clientFactory.apply(binding);
                clients.put(binding.getKeyId(), client);
            }

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

        return client.httpClient;
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

    /**
     * A Reactor Netty client bound to a TLS client certificate.
     */
    public static final class BoundClient {
        private final HttpClient httpClient;
        private final ConnectionProvider ownedConnectionProvider;
        private final Instant notAfter;

        /**
         * Creates a client bound to a TLS client certificate.
         *
         * @param httpClient The client, which presents the certificate.
         * @param ownedConnectionProvider The connection provider the client owns, disposed when the client retires,
         * or null if it shares a connection provider it doesn't own.
         * @param binding The client certificate the client is bound to.
         */
        public BoundClient(HttpClient httpClient, ConnectionProvider ownedConnectionProvider,
            ClientCertificateBinding binding) {
            this.httpClient = httpClient;
            this.ownedConnectionProvider = ownedConnectionProvider;
            this.notAfter = binding.getCertificate().getNotAfter().toInstant();
        }

        private boolean isExpired(Instant now) {
            return now.isAfter(notAfter);
        }

        private void retire() {
            if (ownedConnectionProvider != null) {
                ownedConnectionProvider.disposeLater()
                    .subscribe(null, error -> LOGGER.atVerbose()
                        .log("Failed to dispose the connection provider of a retired client.", error));
            }
        }
    }
}
