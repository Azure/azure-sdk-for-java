// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.configuration;

import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class ConnectionString {

    // TODO (trask) should this be bounded?
    private static final Map<String, ConnectionString> cache = new ConcurrentHashMap<>();

    private final String instrumentationKey;
    private final String ingestionEndpoint;
    private final URL liveEndpoint;
    private final URL profilerEndpoint;

    private final String originalString;
    private final String aadAudience;

    ConnectionString(String instrumentationKey, URL ingestionEndpoint, URL liveEndpoint, URL profilerEndpoint,
        String originalString, String aadAudience) {
        this.instrumentationKey = instrumentationKey;
        this.ingestionEndpoint = ingestionEndpoint.toExternalForm();
        this.liveEndpoint = liveEndpoint;
        this.profilerEndpoint = profilerEndpoint;
        this.originalString = originalString;
        this.aadAudience = aadAudience;
    }

    public static ConnectionString parse(String connectionString) {
        Objects.requireNonNull(connectionString, "Connection string cannot be null");
        return cache.computeIfAbsent(connectionString,
            key -> new ConnectionStringBuilder().setConnectionString(key).build());
    }

    public String getInstrumentationKey() {
        return instrumentationKey;
    }

    public String getIngestionEndpoint() {
        return ingestionEndpoint;
    }

    public URL getLiveEndpoint() {
        return liveEndpoint;
    }

    public URL getProfilerEndpoint() {
        return profilerEndpoint;
    }

    public String getOriginalString() {
        return originalString;
    }

    public String getAadAudienceWithScope() {
        return aadAudience + "/.default";
    }
}
