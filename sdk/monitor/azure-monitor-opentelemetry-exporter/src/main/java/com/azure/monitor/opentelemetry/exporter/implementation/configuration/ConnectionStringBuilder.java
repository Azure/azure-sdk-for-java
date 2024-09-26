// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.configuration;

import com.azure.core.util.CoreUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

class ConnectionStringBuilder {

    // visible for testing
    static final int CONNECTION_STRING_MAX_LENGTH = 4096;

    private String originalString;

    private String instrumentationKey;

    private URL ingestionEndpoint;
    private URL liveEndpoint;
    private URL profilerEndpoint;

    ConnectionStringBuilder() {
        try {
            ingestionEndpoint = new URL(DefaultEndpoints.INGESTION_ENDPOINT);
            liveEndpoint = new URL(DefaultEndpoints.LIVE_ENDPOINT);
            profilerEndpoint = new URL(DefaultEndpoints.PROFILER_ENDPOINT);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("ConnectionString.Defaults are invalid", e);
        }
    }

    ConnectionStringBuilder setConnectionString(String connectionString) {
        originalString = connectionString;
        mapToConnectionConfiguration(getKeyValuePairs(connectionString));
        return this;
    }

    ConnectionString build() {
        return new ConnectionString(instrumentationKey, ingestionEndpoint, liveEndpoint, profilerEndpoint,
            originalString);
    }

    private static Map<String, String> getKeyValuePairs(String connectionString) {
        if (connectionString.length() > CONNECTION_STRING_MAX_LENGTH) { // guard against malicious input
            throw new IllegalArgumentException("ConnectionString values with more than " + CONNECTION_STRING_MAX_LENGTH
                + " characters are not allowed.");
        }
        // parse key value pairs
        Map<String, String> kvps;
        try {
            kvps = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            kvps.putAll(extractKeyValuesFromConnectionString(connectionString));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Could not parse connection string.", e);
        }

        return kvps;
    }

    private static Map<String, String> extractKeyValuesFromConnectionString(String connectionString) {
        Objects.requireNonNull(connectionString);
        Map<String, String> keyValues = new HashMap<>();
        String[] splits = connectionString.split(";");
        for (String split : splits) {
            String[] keyValPair = split.split("=");
            if (keyValPair.length == 2) {
                keyValues.put(keyValPair[0], keyValPair[1]);
            }
        }
        return keyValues;
    }

    private void mapToConnectionConfiguration(Map<String, String> kvps) {

        // get ikey
        instrumentationKey = kvps.get(Keywords.INSTRUMENTATION_KEY);
        if (CoreUtils.isNullOrEmpty(instrumentationKey)) {
            throw new IllegalArgumentException("Missing '" + Keywords.INSTRUMENTATION_KEY + "'");
        }

        // resolve suffix
        String suffix = kvps.get(Keywords.ENDPOINT_SUFFIX);
        if (!CoreUtils.isNullOrEmpty(suffix)) {
            if (suffix.startsWith(".")) {
                suffix = suffix.substring(1);
            }
            setIngestionEndpoint("https://" + EndpointPrefixes.INGESTION_ENDPOINT_PREFIX + "." + suffix);
            setLiveEndpoint("https://" + EndpointPrefixes.LIVE_ENDPOINT_PREFIX + "." + suffix);
            setProfilerEndpoint("https://" + EndpointPrefixes.PROFILER_ENDPOINT_PREFIX + "." + suffix);
        }

        // set explicit endpoints
        String liveEndpoint = kvps.get(Keywords.LIVE_ENDPOINT);
        if (!CoreUtils.isNullOrEmpty(liveEndpoint)) {
            setLiveEndpoint(liveEndpoint);
        }

        String ingestionEndpoint = kvps.get(Keywords.INGESTION_ENDPOINT);
        if (!CoreUtils.isNullOrEmpty(ingestionEndpoint)) {
            setIngestionEndpoint(ingestionEndpoint);
        }

        String profilerEndpoint = kvps.get(Keywords.PROFILER_ENDPOINT);
        if (!CoreUtils.isNullOrEmpty(profilerEndpoint)) {
            setProfilerEndpoint(profilerEndpoint);
        }
    }

    void setIngestionEndpoint(String ingestionEndpoint) {
        this.ingestionEndpoint = toUrlOrThrow(ingestionEndpoint, Keywords.INGESTION_ENDPOINT);
    }

    void setLiveEndpoint(String liveEndpoint) {
        this.liveEndpoint = toUrlOrThrow(liveEndpoint, Keywords.LIVE_ENDPOINT);
    }

    void setProfilerEndpoint(String profilerEndpoint) {
        this.profilerEndpoint = toUrlOrThrow(profilerEndpoint, Keywords.PROFILER_ENDPOINT);
    }

    private static URL toUrlOrThrow(String url, String field) {
        if (!url.endsWith("/")) {
            url += "/";
        }
        try {
            URL result = new URL(url);
            String scheme = result.getProtocol();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new IllegalArgumentException(
                    field + " of the connection string must specify supported protocol, either 'http' or 'https': \""
                        + url + "\"");
            }
            return result;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(field + " is invalid: \"" + url + "\"", e);
        }
    }

    /**
     * All tokens are lowercase. Parsing should be case insensitive.
     */
    // visible for testing
    static class Keywords {
        private Keywords() {
        }

        static final String INSTRUMENTATION_KEY = "InstrumentationKey";
        static final String ENDPOINT_SUFFIX = "EndpointSuffix";
        static final String INGESTION_ENDPOINT = "IngestionEndpoint";
        static final String LIVE_ENDPOINT = "LiveEndpoint";
        static final String PROFILER_ENDPOINT = "ProfilerEndpoint";
    }

    // visible for testing
    static class EndpointPrefixes {
        private EndpointPrefixes() {
        }

        static final String INGESTION_ENDPOINT_PREFIX = "dc";
        static final String LIVE_ENDPOINT_PREFIX = "live";
        static final String PROFILER_ENDPOINT_PREFIX = "profiler";
    }
}
