// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.web;

/**
 * Constants used for validating refresh requests.
 */
public class Constants {

    public static final String VALIDATION_CODE_KEY = "validationCode";
    public static final String VALIDATION_TOPIC = "topic";
    public static final String VALIDATION_CODE_FORMAT_START = "{ \"validationResponse\": \"";

    public static final String ACTUATOR = "/actuator/";
    public static final String APPCONFIGURATION_REFRESH = "appconfiguration-refresh";
    public static final String APPCONFIGURATION_REFRESH_BUS = "appconfiguration-refresh-bus";
}
