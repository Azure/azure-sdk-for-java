// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.web;

/**
 * Constants used for validating refresh requests.
 */
public final class AppConfigurationWebConstants {

    /**
     * Validation Code Key value
     */
    public static final String VALIDATION_CODE_KEY = "validationCode";
    /**
     * Validation Topic for push refresh.
     */
    public static final String VALIDATION_TOPIC = "topic";

    /**
     * Json field name for Data  section
     */
    public static final String DATA = "data";

    /**
     * Json field name for SyncToken value
     */
    public static final String SYNC_TOKEN = "syncToken";

    /**
     * Prefix of the validation code.
     */
    public static final String VALIDATION_CODE_FORMAT_START = "{ \"validationResponse\": \"";

    /**
     * Actuator endpoint
     */
    public static final String ACTUATOR = "/actuator/";

    /**
     * Azure App Configuration push refresh endpoint
     */
    public static final String APPCONFIGURATION_REFRESH = "appconfiguration-refresh";

    /**
     * Azure App Configuration push bus refresh endpoint
     */
    public static final String APPCONFIGURATION_REFRESH_BUS = "appconfiguration-refresh-bus";
}
