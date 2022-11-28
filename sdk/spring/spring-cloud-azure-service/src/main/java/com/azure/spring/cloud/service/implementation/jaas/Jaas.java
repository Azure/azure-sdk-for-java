// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.jaas;

import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

public class Jaas {

    public static final String JAAS_PREFIX_FORMAT = "%s %s";
    public static final String JAAS_OPTIONS_FORMAT = " %s=\"%s\"";
    public static final String TERMINATOR = ";";

    public Jaas(String loginModule) {
        this(loginModule, "required");
    }

    public Jaas(String loginModule, String controlFlag) {
        this(loginModule, controlFlag, new HashMap<>());
    }

    public Jaas(String loginModule, String controlFlag, Map<String, String> options) {
        Assert.hasText(loginModule, "The login module of JAAS should not be null");
        Assert.hasText(controlFlag, "The control flag of JAAS should not be null");
        this.loginModule = loginModule;
        this.controlFlag = controlFlag;
        this.options = options;
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
    private Map<String, String> options;

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
        this.options = options;
    }

    public void putOptions(Map<String, String> options) {
        if (options != null) {
            this.options.putAll(options);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(String.format(JAAS_PREFIX_FORMAT,
            loginModule, controlFlag));
        options.forEach((k, v) -> builder.append(String.format(JAAS_OPTIONS_FORMAT, k, v)));
        builder.append(TERMINATOR);
        return builder.toString();
    }
}
