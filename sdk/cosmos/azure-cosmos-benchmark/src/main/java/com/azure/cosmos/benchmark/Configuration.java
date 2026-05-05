// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.beust.jcommander.Parameter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.function.Function;

public class Configuration {

    @Parameter(names = "-suppressCleanup", description = "Skip deleting database/container on shutdown (for multi-cycle CHURN)")
    private boolean suppressCleanup = false;

    @Parameter(names = "-workloadConfig", description = "Path to workload configuration JSON file")
    private String workloadConfig;

    @Parameter(names = "-cycles", description = "Number of create/destroy cycles (1 = single run)")
    private int cycles = 1;

    @Parameter(names = "-settleTimeMs", description = "Milliseconds to wait between cycles for thread/resource cleanup. Default: 90000 when cycles>1, 0 otherwise.")
    private long settleTimeMs = -1;

    @Parameter(names = "-gcBetweenCycles", description = "Force GC during settle period between cycles")
    private boolean gcBetweenCycles = true;

    @Parameter(names = {"-h", "-help", "--help"}, description = "Help", help = true)
    private boolean help = false;

    public boolean isHelp() {
        return help;
    }

    public boolean isGcBetweenCycles() {
        return gcBetweenCycles;
    }

    public String getWorkloadConfig() {
        return workloadConfig;
    }

    public int getCycles() {
        return cycles;
    }

    public long getSettleTimeMs() {
        return settleTimeMs;
    }

    public boolean isSuppressCleanup() {
        return this.suppressCleanup;
    }

    public void setSuppressCleanup(boolean suppressCleanup) {
        this.suppressCleanup = suppressCleanup;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public static String getAadLoginUri() {
        return getOptionalConfigProperty(
                "AAD_LOGIN_ENDPOINT",
                "https://login.microsoftonline.com/",
                v -> v);
    }

    public static String getAadManagedIdentityId() {
        return getOptionalConfigProperty("AAD_MANAGED_IDENTITY_ID", null, v -> v);
    }

    public static String getAadTenantId() {
        return getOptionalConfigProperty("AAD_TENANT_ID", null, v -> v);
    }

    private static <T> T getOptionalConfigProperty(String name, T defaultValue, Function<String, T> conversion) {
        String textValue = getConfigPropertyOrNull(name);

        if (textValue == null) {
            return defaultValue;
        }

        T returnValue = conversion.apply(textValue);
        return returnValue != null ? returnValue : defaultValue;
    }

    private static String getConfigPropertyOrNull(String name) {
        String systemPropertyName = "COSMOS." + name;
        String environmentVariableName = "COSMOS_" + name;
        String fromSystemProperty = emptyToNull(System.getProperty(systemPropertyName));
        if (fromSystemProperty != null) {
            return fromSystemProperty;
        }

        return emptyToNull(System.getenv().get(environmentVariableName));
    }

    /**
     * Returns the given string if it is nonempty; {@code null} otherwise.
     *
     * @param string the string to test and possibly return
     * @return {@code string} itself if it is nonempty; {@code null} if it is empty or null
     */
    private static String emptyToNull(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }

        return string;
    }
}
