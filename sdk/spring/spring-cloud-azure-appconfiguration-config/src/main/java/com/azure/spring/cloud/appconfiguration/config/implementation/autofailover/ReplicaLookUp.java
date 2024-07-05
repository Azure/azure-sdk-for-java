// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.autofailover;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static final String SRC_RECORD = "SRV";

    private static final String[] TRUSTED_DOMAIN_LABELS = { "azconfig", "appconfig" };

    private static final Duration FALLBACK_CLIENT_REFRESH_EXPIRED_INTERVAL = Duration.ofHours(1);

    private static final Duration MINIMAL_CLIENT_REFRESH_INTERVAL = Duration.ofSeconds(30);

    InitialDirContext context;

    private Map<String, List<SRVRecord>> records = new HashMap<String, List<SRVRecord>>();

    private Map<String, Instant> wait = new HashMap<>();

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
            for (ConfigStore configStore : properties.getStores()) {
                if (!configStore.isEnabled() || !configStore.isReplicaDiscoveryEnabled()) {
                    continue;
                }
                String mainEndpoint = configStore.getEndpoint();

                List<String> providedEndpoints = new ArrayList<>();
                if (configStore.getConnectionStrings().size() > 0) {
                    providedEndpoints = configStore.getConnectionStrings().stream().map(connectionString -> {
                        return (AppConfigurationReplicaClientsBuilder
                            .getEndpointFromConnectionString(connectionString));
                    }).toList();
                } else if (configStore.getEndpoints().size() > 0) {
                    providedEndpoints = configStore.getEndpoints();
                } else {
                    providedEndpoints = List.of(configStore.getEndpoint());
                }

                try {
                    List<SRVRecord> srvRecords = findAutoFailoverEndpoints(mainEndpoint, providedEndpoints);

                    srvRecords.sort((SRVRecord a, SRVRecord b) -> a.compareTo(b));

                    records.put(mainEndpoint, srvRecords);
                    wait.put(mainEndpoint, Instant.now().plus(FALLBACK_CLIENT_REFRESH_EXPIRED_INTERVAL));
                } catch (AppConfigurationReplicaException e) {
                    LOGGER.warn("Failed to finde replicas due to: " + e.getMessage());
                    wait.put(mainEndpoint, Instant.now().plus(MINIMAL_CLIENT_REFRESH_INTERVAL));
                }

            }
            semaphore.release();
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
        List<SRVRecord> records = new ArrayList<>();
        String host = "";
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
                records.add(origin);
            }
            replicas.stream().forEach(replica -> {
                if (!providedEndpoints.contains(replica.getEndpoint())
                    && validate(knownDomain, replica.getEndpoint())) {
                    records.add(replica);
                }
            });
        }
        return records;
    }

    private SRVRecord getOriginRecord(String url) throws AppConfigurationReplicaException {
        Attribute attribute = requestRecord(ORIGIN_PREFIX + url);
        if (attribute != null) {
            return parseHosts(attribute).get(0);
        }
        return null;
    }

    private List<SRVRecord> getReplicaRecords(SRVRecord origin) throws AppConfigurationReplicaException {
        List<SRVRecord> replicas = new ArrayList<>();
        int i = 0;
        while (true) {
            Attribute attribute = requestRecord(
                REPLICA_PREFIX_ALT + i + REPLICA_PREFIX_TCP + origin.getTarget());

            if (attribute == null) {
                break;
            }

            replicas.addAll(parseHosts(attribute));
            i++;
        }
        return replicas;
    }

    private Attribute requestRecord(String name) throws AppConfigurationReplicaException {
        Instant retryTime = Instant.now().plusSeconds(30);
        while (retryTime.isAfter(Instant.now())) {
            try {
                return context.getAttributes(name, new String[] { SRC_RECORD }).get(SRC_RECORD);
            } catch (NameNotFoundException e) {
                // Found Last Record, should be the case that no SRV Record exists.
                return null;
            } catch (NamingException e) {
                // Will retry for up to 30 seconds
            }
        }
        throw new AppConfigurationReplicaException();
    }

    private List<SRVRecord> parseHosts(Attribute attribute) {
        List<SRVRecord> hosts = new ArrayList<>();
        try {
            NamingEnumeration<?> records = attribute.getAll();
            while (records.hasMore()) {
                hosts.add(new SRVRecord(((String) records.next()).toString().split(" ")));
            }
        } catch (NamingException e) {
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
