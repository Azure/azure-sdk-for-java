// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import com.azure.core.credential.ClientCertificateBinding;
import okhttp3.OkHttpClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Caches an OkHttp client for each TLS client certificate that requests are bound to, keyed by the certificate's key
 * ID.
 * <p>
 * A client is retired when its certificate expires, or when more certificates are in use than the cache holds, least
 * recently used first. A retired client is dropped. Its requests in flight complete, and since no other client
 * shares its connections, the connection pool closes them once they've been idle for the pool's keep-alive duration.
 */
public final class ClientCertificateBoundClients {
    /**
     * The default number of certificates the cache holds clients for.
     */
    public static final int DEFAULT_MAXIMUM_CLIENTS = 16;

    private final Function<ClientCertificateBinding, OkHttpClient> clientFactory;
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
    public ClientCertificateBoundClients(Function<ClientCertificateBinding, OkHttpClient> clientFactory,
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
    public OkHttpClient getClient(ClientCertificateBinding binding) {
        synchronized (clients) {
            BoundClient client = clients.get(binding.getKeyId());
            if (client == null) {
                client = new BoundClient(clientFactory.apply(binding), binding);
                clients.put(binding.getKeyId(), client);
            }

            Instant now = clock.get();
            Iterator<BoundClient> iterator = clients.values().iterator();
            while (iterator.hasNext()) {
                BoundClient candidate = iterator.next();
                if (candidate != client && (candidate.isExpired(now) || clients.size() > maximumClients)) {
                    iterator.remove();
                }
            }

            return client.httpClient;
        }
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

    private static final class BoundClient {
        private final OkHttpClient httpClient;
        private final Instant notAfter;

        private BoundClient(OkHttpClient httpClient, ClientCertificateBinding binding) {
            this.httpClient = httpClient;
            this.notAfter = binding.getCertificate().getNotAfter().toInstant();
        }

        private boolean isExpired(Instant now) {
            return now.isAfter(notAfter);
        }
    }
}
