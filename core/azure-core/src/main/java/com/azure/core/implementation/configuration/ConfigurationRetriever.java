package com.azure.core.implementation.configuration;

import com.azure.core.implementation.util.ImplUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ConfigurationRetriever {

    public static String getConfiguration(Configurations configuration) {
        String configurationName = configuration.toString().toUpperCase(Locale.US);

        String configurationValue = getRuntimeConfiguration(configurationName);
        if (!ImplUtils.isNullOrEmpty(configurationValue)) {
            return configurationValue;
        }

        configurationValue = getStoreConfiguration(configurationName);
        if (!ImplUtils.isNullOrEmpty(configurationValue)) {
            return configurationValue;
        }

        configurationValue = getEnvironmentConfiguration(configurationName);
        if (!ImplUtils.isNullOrEmpty(configurationValue)) {
            return configurationValue;
        }

        configurationValue = getSystemConfiguration(configurationName);
        return !ImplUtils.isNullOrEmpty(configurationValue) ? configurationValue : null;
    }

    // This has the question on whether we should favor Application parameters of JVM parameters
    private static String getRuntimeConfiguration(String configurationName) {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMXBean.getInputArguments();
        return System.getProperty(configurationName);
    }

    // Is configuration store the same thing as App Configuration?
    // When a configuration is found here log something.
    private static String getStoreConfiguration(String configurationName) {
        return "";
    }

    // Should these methods be default or concrete? I feel that the only thing that should be configurable is the
    // ordering which we find the configuration.
    // When a configuration is found here log something.
    private static String getEnvironmentConfiguration(String configurationName) {
        return System.getenv(configurationName);
    }

    private static String getSystemConfiguration(String configurationName) {
        return "";
    }
}
