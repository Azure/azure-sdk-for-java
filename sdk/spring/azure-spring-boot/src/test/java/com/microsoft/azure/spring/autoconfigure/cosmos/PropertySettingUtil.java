// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.autoconfigure.cosmos;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.microsoft.azure.utils.PropertyLoader;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class PropertySettingUtil {
    public static final String URI = "https://test.documents.azure.com:443/";
    public static final String KEY = "KeyString";
    public static final String DATABASE_NAME = "test";
    public static final boolean ALLOW_TELEMETRY_TRUE = true;
    public static final boolean ALLOW_TELEMETRY_FALSE = false;
    public static final boolean POPULATE_QUERY_METRICS = true;
    public static final ConsistencyLevel CONSISTENCY_LEVEL = ConsistencyLevel.STRONG;
    public static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(4);
    public static final int MEDIA_REQUEST_TIMEOUT = 3;
    public static final ConnectionMode CONNECTION_MODE = ConnectionMode.DIRECT;
    public static final int MAX_CONNECTION_POOL_SIZE = 1;
    public static final Duration IDLE_HTTP_CONNECTION_TIMEOUT = Duration.ofSeconds(2);
    public static final Duration IDLE_TCP_CONNECTION_TIMEOUT = Duration.ZERO;
    public static final String USER_AGENT_SUFFIX = "suffix";
    public static final String DEFAULT_USER_AGENT_SUFFIX = "spring-data/" + PropertyLoader.getProjectVersion();
    public static final int RETRY_OPTIONS_MAX_RETRY_ATTEMPTS_ON_THROTTLED_REQUESTS = 5;
    public static final Duration MAX_RETRY_WAIT_TIME = Duration.ofSeconds(6);
    public static final boolean ENDPOINT_DISCOVERY_ENABLED = false;
    public static final List<String> PREFERRED_REGIONS = Arrays.asList("East US", "West US", "North Europe");
    private static final String PROPERTY_URI = "azure.cosmos.uri";
    private static final String PROPERTY_KEY = "azure.cosmos.key";
    private static final String PROPERTY_DBNAME = "azure.cosmos.database";
    private static final String PROPERTY_CONSISTENCY_LEVEL = "azure.cosmos.consistency-level";
    private static final String PROPERTY_ALLOW_TELEMETRY = "azure.cosmos.allow-telemetry";
    private static final String PROPERTY_POPULATE_QUERY_METRICS = "azure.cosmos.populateQueryMetrics";
    private static final String PROPERTY_CONNECTION_MODE = "azure.cosmos.connection-mode";

    public static void setProperties() {
        System.setProperty(PROPERTY_URI, URI);
        System.setProperty(PROPERTY_KEY, KEY);
        System.setProperty(PROPERTY_DBNAME, DATABASE_NAME);
        System.setProperty(PROPERTY_CONSISTENCY_LEVEL, CONSISTENCY_LEVEL.name());
        System.setProperty(PROPERTY_ALLOW_TELEMETRY, Boolean.toString(ALLOW_TELEMETRY_TRUE));
        System.setProperty(PROPERTY_POPULATE_QUERY_METRICS, Boolean.toString(POPULATE_QUERY_METRICS));
        System.setProperty(PROPERTY_CONNECTION_MODE, CONNECTION_MODE.name());
    }

    public static void setAllowTelemetryFalse() {
        setProperties();
        System.setProperty(PROPERTY_ALLOW_TELEMETRY, Boolean.toString(ALLOW_TELEMETRY_FALSE));
    }

    public static void unsetProperties() {
        System.clearProperty(PROPERTY_URI);
        System.clearProperty(PROPERTY_KEY);
        System.clearProperty(PROPERTY_DBNAME);
        System.clearProperty(PROPERTY_CONSISTENCY_LEVEL);
        System.clearProperty(PROPERTY_ALLOW_TELEMETRY);
        System.clearProperty(PROPERTY_POPULATE_QUERY_METRICS);
        System.clearProperty(PROPERTY_CONNECTION_MODE);
    }

    public static void unsetAllowTelemetry() {
        System.clearProperty(PROPERTY_ALLOW_TELEMETRY);
    }
}
