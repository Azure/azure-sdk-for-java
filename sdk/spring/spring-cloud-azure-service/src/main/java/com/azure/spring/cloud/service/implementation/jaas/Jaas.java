// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.jaas;

import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Jaas {

    public static final String DELIMITER = " ";
    public static final String TERMINATOR = ";";

    public Jaas(String loginModule) {
        this(loginModule, ControlFlag.REQUIRED);
    }

    public Jaas(String loginModule, ControlFlag controlFlag) {
        this(loginModule, controlFlag, new HashMap<>());
    }

    public Jaas(String loginModule, ControlFlag controlFlag, Map<String, String> options) {
        Assert.hasText(loginModule, "The login module of JAAS should not be null or empty");
        Assert.notNull(controlFlag, "The control flag of JAAS should not be null");
        this.loginModule = loginModule;
        this.controlFlag = controlFlag;
        this.options = options == null ? new HashMap<>() : options;
    }

    /**
     * Login module.
     */
    private String loginModule;

    /**
     * Control flag for login configuration.
     */
    private ControlFlag controlFlag;

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

    public ControlFlag getControlFlag() {
        return this.controlFlag;
    }

    public void setControlFlag(ControlFlag controlFlag) {
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
        StringBuilder builder = new StringBuilder();
        builder.append(loginModule).append(DELIMITER).append(controlFlag.name().toLowerCase(Locale.ROOT));
        for (Map.Entry<String, String> entry : options.entrySet()) {
            builder.append(DELIMITER).append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
        }
        return builder.append(TERMINATOR).toString();
    }

    public enum ControlFlag {
        REQUIRED,
        REQUISITE,
        SUFFICIENT,
        OPTIONAL;

        private static final Map<String, ControlFlag> CONTROL_FLAG_MAP = initMap();

        private static Map<String, ControlFlag> initMap() {
            return Collections.unmodifiableMap(Arrays.stream(ControlFlag.values())
                .collect(Collectors.toMap(f -> f.name(), Function.identity())));
        }

        public static ControlFlag fromString(String value) {
            return CONTROL_FLAG_MAP.get(value.toUpperCase(Locale.ROOT));
        }

    }
}
