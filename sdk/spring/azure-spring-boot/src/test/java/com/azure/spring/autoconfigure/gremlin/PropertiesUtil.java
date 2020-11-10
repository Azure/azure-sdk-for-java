// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.gremlin;

public class PropertiesUtil {
    private static final String PROPERTY_ENDPOINT = "gremlin.endpoint";
    private static final String PROPERTY_PORT = "gremlin.port";
    private static final String PROPERTY_USERNAME = "gremlin.username";
    private static final String PROPERTY_PASSWORD = "gremlin.password";
    private static final String PROPERTY_TELEMETRY = "gremlin.telemetryAllowed";

    public static final String ENDPOINT = "localhost";
    public static final int PORT = 8090;
    public static final String USERNAME = "fake-username";
    public static final String PASSWORD = "fake-passowrd";

    public static final String GREMLIN_ENDPOINT_CONFIG = PROPERTY_ENDPOINT + "=" + ENDPOINT;
    public static final String GREMLIN_PORT_CONFIG = PROPERTY_PORT + "=" + String.valueOf(PORT);
    public static final String GREMLIN_USERNAME_CONFIG = PROPERTY_USERNAME + "=" + USERNAME;
    public static final String GREMLIN_PASSWORD_CONFIG = PROPERTY_PASSWORD + "=" + PASSWORD;
    public static final String GREMLIN_TELEMETRY_CONFIG_NOT_ALLOWED = PROPERTY_TELEMETRY + "=" + "false";
    public static final String GREMLIN_TELEMETRY_CONFIG_ALLOWED = PROPERTY_TELEMETRY + "=" + "true";
}
