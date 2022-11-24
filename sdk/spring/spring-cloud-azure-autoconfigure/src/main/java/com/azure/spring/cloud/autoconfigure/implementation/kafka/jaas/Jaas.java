//  Copyright (c) Microsoft Corporation. All rights reserved.
//  Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka.jaas;

import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.AZURE_CONFIGURED_JAAS_OPTIONS_KEY;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.AZURE_CONFIGURED_JAAS_OPTIONS_VALUE;

public class Jaas {

    public static final String JAAS_PREFIX = "%s %s";
    public static final String JAAS_OPTIONS_FORMAT = " %s=\"%s\"";
    public static final String TERMINATOR = ";";


    public Jaas(String loginModule) {
        this(loginModule, "required");
    }

    public Jaas(String loginModule, String controlFlag) {
        this.loginModule = loginModule;
        this.controlFlag = controlFlag;
        this.options.put(AZURE_CONFIGURED_JAAS_OPTIONS_KEY, AZURE_CONFIGURED_JAAS_OPTIONS_VALUE);
    }

    /**
     * Login module.
     */
    private String loginModule;

    /**
     * Control flag for login configuration.
     */
    private String controlFlag;

    /**
     * Additional JAAS options.
     */
    private final Map<String, String> options = new HashMap<>();

    public String getLoginModule() {
        return this.loginModule;
    }

    public void setLoginModule(String loginModule) {
        this.loginModule = loginModule;
    }

    public String getControlFlag() {
        return this.controlFlag;
    }

    public void setControlFlag(String controlFlag) {
        this.controlFlag = controlFlag;
    }

    public Map<String, String> getOptions() {
        return this.options;
    }

    public void setOptions(Map<String, String> options) {
        if (options != null) {
            this.options.putAll(options);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(String.format(JAAS_PREFIX, loginModule, controlFlag));
        options.forEach((k, v) -> builder.append(String.format(JAAS_OPTIONS_FORMAT, k, v)));
        builder.append(TERMINATOR);
        return builder.toString();
    }
}
