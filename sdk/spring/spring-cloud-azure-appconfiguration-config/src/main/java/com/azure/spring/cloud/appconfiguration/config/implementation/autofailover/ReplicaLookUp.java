// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.autofailover;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.InitialDirContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationReplicaClientsBuilder;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;

@Component
public class ReplicaLookUp {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReplicaLookUp.class);

    private static final String ORIGIN_PREFIX = "dns:/_origin._tcp.";

    private static final String REPLICA_PREFIX_ALT = "dns:/_alt";

    private static final String REPLICA_PREFIX_TCP = "._tcp.";

    private static final String SRV_RECORD = "SRV";

    private static final String[] TRUSTED_DOMAIN_LABELS = { "azconfig", "appconfig" };

    private static final Duration FALLBACK_CLIENT_REFRESH_EXPIRED_INTERVAL = Duration.ofHours(1);

    private static final Duration MINIMAL_CLIENT_REFRESH_INTERVAL = Duration.ofSeconds(30);

    InitialDirContext context;

    private final Map<String, List<SRVRecord>> records = new ConcurrentHashMap<>();

    private final Map<String, Instant> wait = new ConcurrentHashMap<>();

    private final AppConfigurationProperties properties;

    private final Semaphore semaphore;

    public ReplicaLookUp(AppConfigurationProperties properties) throws NamingException {
        this.properties = properties;
        this.context = new InitialDirContext();
        this.semaphore = new Semaphore(1);
    }

    @Async
    public void updateAutoFailoverEndpoints() {
        if (semaphore.tryAcquire()) {
            try {
                for (ConfigStore configStore : properties.getStores()) {
                    if (!configStore.isEnabled() || !configStore.isReplicaDiscoveryEnabled()) {
                        continue;
                    }
                    String mainEndpoint = configStore.getEndpoint();

                    Instant nextRefresh = wait.get(mainEndpoint);
                    if (nextRefresh != null && Instant.now().isBefore(nextRefresh)) {
                        continue;
                    }

                    List<String> providedEndpoints;
                    if (!configStore.getConnectionStrings().isEmpty()) {
                        providedEndpoints = configStore.getConnectionStrings().stream()
                            .map(AppConfigurationReplicaClientsBuilder::getEndpointFromConnectionString)
                            .toList();
                    } else if (!configStore.getEndpoints().isEmpty()) {
                        providedEndpoints = configStore.getEndpoints();
                    } else {
                        providedEndpoints = List.of(configStore.getEndpoint());
                    }

                    try {
                        List<SRVRecord> srvRecords = findAutoFailoverEndpoints(mainEndpoint, providedEndpoints);

                        srvRecords.sort(SRVRecord::compareTo);

                        records.put(mainEndpoint, srvRecords);
                        wait.put(mainEndpoint, Instant.now().plus(FALLBACK_CLIENT_REFRESH_EXPIRED_INTERVAL));
                    } catch (AppConfigurationReplicaException e) {
                        LOGGER.warn("Failed to find replicas due to: {}", e.getMessage(), e);
                        wait.put(mainEndpoint, Instant.now().plus(MINIMAL_CLIENT_REFRESH_INTERVAL));
                    }

                }
            } finally {
                semaphore.release();
            }
        }
    }

    public List<String> getAutoFailoverEndpoints(String mainEndpoint) {
        List<SRVRecord> endpointRecords = records.get(mainEndpoint);
        if (endpointRecords == null) {
            return List.of();
        }
        return endpointRecords.stream().map(record -> record.getEndpoint()).toList();
    }

    private List<SRVRecord> findAutoFailoverEndpoints(String endpoint, List<String> providedEndpoints)
        throws AppConfigurationReplicaException {
        List<SRVRecord> srvRecords = new ArrayList<>();
        String host;
        try {
            URI uri = new URI(endpoint);
            host = uri.getHost();
        } catch (URISyntaxException e) {
            // If endpoint uri is invalid then it will fail during startup
            return new ArrayList<>();
        }

        SRVRecord origin = getOriginRecord(host);
        if (origin != null) {
            List<SRVRecord> replicas = getReplicaRecords(origin);
            String knownDomain = getKnownDomain(endpoint);

            if (!providedEndpoints.contains(origin.getEndpoint()) && validate(knownDomain, origin.getEndpoint())) {
                srvRecords.add(origin);
            }
            replicas.forEach(replica -> {
                if (!providedEndpoints.contains(replica.getEndpoint())
                    && validate(knownDomain, replica.getEndpoint())) {
                    srvRecords.add(replica);
                }
            });
        }
        return srvRecords;
    }

    private SRVRecord getOriginRecord(String url) throws AppConfigurationReplicaException {
        Attribute attribute = requestRecord(ORIGIN_PREFIX + url);
        if (attribute != null) {
            List<SRVRecord> hosts = parseHosts(attribute);
            if (!hosts.isEmpty()) {
                return hosts.get(0);
            }
        }
        return null;
    }

    private static final int MAX_REPLICA_COUNT = 20;

    private List<SRVRecord> getReplicaRecords(SRVRecord origin) throws AppConfigurationReplicaException {
        List<SRVRecord> replicas = new ArrayList<>();
        for (int i = 0; i < MAX_REPLICA_COUNT; i++) {
            Attribute attribute = requestRecord(
                REPLICA_PREFIX_ALT + i + REPLICA_PREFIX_TCP + origin.getTarget());

            if (attribute == null) {
                break;
            }

            replicas.addAll(parseHosts(attribute));
        }
        return replicas;
    }

    private Attribute requestRecord(String name) throws AppConfigurationReplicaException {
        Instant retryTime = Instant.now().plusSeconds(30);
        while (retryTime.isAfter(Instant.now())) {
            try {
                return context.getAttributes(name, new String[] { SRV_RECORD }).get(SRV_RECORD);
            } catch (NameNotFoundException e) {
                // Found Last Record, should be the case that no SRV Record exists.
                return null;
            } catch (NamingException e) {
                // Will retry for up to 30 seconds
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new AppConfigurationReplicaException();
                }
            }
        }
        throw new AppConfigurationReplicaException();
    }

    private List<SRVRecord> parseHosts(Attribute attribute) {
        List<SRVRecord> hosts = new ArrayList<>();
        try {
            NamingEnumeration<?> srvRecords = attribute.getAll();
            while (srvRecords.hasMore()) {
                hosts.add(new SRVRecord(((String) srvRecords.next()).split(" ")));
            }
        } catch (NamingException e) {
            LOGGER.warn("Failed to parse SRV record hosts", e);
        }

        return hosts;
    }

    private boolean validate(String knownDomain, String endpoint) {
        if (!StringUtils.hasText(endpoint)) {
            return false;
        }

        if (!StringUtils.hasText(knownDomain)) {
            return false;
        }
        return endpoint.endsWith(knownDomain);
    }

    private String getKnownDomain(String knownHost) {
        for (String label : TRUSTED_DOMAIN_LABELS) {
            int index = knownHost.toLowerCase().indexOf("." + label + ".");
            if (index > 0) {
                return knownHost.substring(index);
            }
        }
        return "";
    }

    private class AppConfigurationReplicaException extends Exception {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

    }

}
