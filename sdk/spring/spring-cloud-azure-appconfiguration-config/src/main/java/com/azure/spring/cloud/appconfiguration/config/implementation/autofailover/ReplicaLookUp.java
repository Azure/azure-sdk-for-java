// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.autofailover;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.InitialDirContext;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationReplicaClientsBuilder;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;

@Component
public class ReplicaLookUp {

    private static final String ORIGIN_PREFIX = "dns:/_origin._tcp.";

    private static final String REPLICA_PREFIX_ALT = "dns:/_alt";

    private static final String REPLICA_PREFIX_TCP = "._tcp.";

    private static final String SRC_RECORD = "SRV";

    private static final List<String> TRUSTED_DOMAIN_LABELS = List.of("azconfig", "appconfig");

    InitialDirContext context;

    private Map<String, List<SRVRecord>> records = new HashMap<String, List<SRVRecord>>();

    private final AppConfigurationProperties properties;

    public ReplicaLookUp(AppConfigurationProperties properties) throws NamingException {
        this.properties = properties;
        this.context = new InitialDirContext();
    }

    @Async
    public void updateAutoFailoverEndpoints() {
        for (ConfigStore configStore : properties.getStores()) {
            if (!configStore.isEnabled()) {
                continue;
            }
            String mainEndpoint = configStore.getEndpoint();

            List<String> providedEndpoints = new ArrayList<>();
            if (configStore.getConnectionStrings().size() > 0) {
                providedEndpoints = configStore.getConnectionStrings().stream().map(connectionString -> {
                    return (AppConfigurationReplicaClientsBuilder.getEndpointFromConnectionString(connectionString));
                }).toList();
            } else if (configStore.getEndpoints().size() > 0) {
                providedEndpoints = configStore.getEndpoints();
            } else {
                providedEndpoints = List.of(configStore.getEndpoint());
            }

            List<SRVRecord> srvRecords = findAutoFailoverEndpoints(mainEndpoint, providedEndpoints);

            srvRecords.sort((SRVRecord a, SRVRecord b) -> a.compareTo(b));

            records.put(mainEndpoint, srvRecords);
        }

    }

    public List<String> getAutoFailoverEndpoints(String mainEndpoint) {
        List<SRVRecord> endpointRecords = records.get(mainEndpoint);
        if (endpointRecords == null) {
            return List.of();
        }
        return endpointRecords.stream().map(record -> record.getEndpoint()).toList();
    }

    private List<SRVRecord> findAutoFailoverEndpoints(String endpoint, List<String> providedEndpoints) {
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
        List<SRVRecord> replicas = getReplicaRecords(origin);
        String knownDomain = getKnownDomain(endpoint);

        if (!providedEndpoints.contains(origin.getEndpoint()) && validate(knownDomain, origin.getEndpoint())) {
            records.add(origin);
        }
        replicas.stream().forEach(replica -> {
            if (!providedEndpoints.contains(replica.getEndpoint()) && validate(knownDomain, replica.getEndpoint())) {
                records.add(replica);
            }
        });
        return records;
    }

    private SRVRecord getOriginRecord(String url) {
        Attribute attribute = requestRecord(ORIGIN_PREFIX + url);
        if (attribute != null) {
            return parseHosts(attribute).get(0);
        }
        return null;
    }

    private List<SRVRecord> getReplicaRecords(SRVRecord origin) {
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

    private Attribute requestRecord(String name) {
        try {
            return context.getAttributes(name, new String[] { SRC_RECORD }).get(SRC_RECORD);
        } catch (NameNotFoundException e) {
            // Found Last Record, should be the case that no SRV Record exists.
            return null;
        } catch (NamingException e) {
            return null;
        }
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
        return TRUSTED_DOMAIN_LABELS.stream().filter(label -> {
            int index = knownHost.toLowerCase().indexOf("." + label + ".");
            System.out.println(index);
            return index > 0;
        }).findAny().orElse("");
    }

}
