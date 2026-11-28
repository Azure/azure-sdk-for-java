// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.kafka.connect.CosmosAccessTokenProvider;
import org.apache.kafka.common.utils.Utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

/**
 * Auth config for the {@code AccessTokenProvider} auth type.
 * <p>
 * The cache key is the provider class name and the connector configs, never a token value, so the same Cosmos client
 * is reused while the provider refreshes tokens.
 */
public class CosmosAccessTokenProviderAuthConfig implements CosmosAuthConfig {
    private final String providerClassName;
    private final Map<String, ?> providerConfigs;

    public CosmosAccessTokenProviderAuthConfig(String providerClassName, Map<String, ?> providerConfigs) {
        checkArgument(StringUtils.isNotEmpty(providerClassName), "Argument 'providerClassName' should not be null or empty");

        this.providerClassName = providerClassName;
        this.providerConfigs = providerConfigs == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(new HashMap<>(providerConfigs));
    }

    public String getProviderClassName() {
        return providerClassName;
    }

    public Map<String, ?> getProviderConfigs() {
        return providerConfigs;
    }

    /**
     * Creates and configures a new provider instance.
     *
     * @return the configured provider.
     */
    public CosmosAccessTokenProvider createProvider() {
        CosmosAccessTokenProvider provider;
        try {
            // Load through the context class loader (the connector plugin class loader under Kafka Connect), but
            // instantiate from this module so that JPMS access checks are made against the connector, not kafka-clients.
            Class<? extends CosmosAccessTokenProvider> providerClass =
                Utils.loadClass(providerClassName, CosmosAccessTokenProvider.class);
            provider = providerClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new IllegalArgumentException(
                "Unable to create access token provider '" + providerClassName + "'. The class must be loadable, "
                    + "implement " + CosmosAccessTokenProvider.class.getName() + " and have a public no-argument constructor.",
                e);
        }

        try {
            provider.configure(providerConfigs);
        } catch (RuntimeException e) {
            closeQuietly(provider);
            throw e;
        }

        return provider;
    }

    /**
     * Validates that the class can be loaded and implements {@link CosmosAccessTokenProvider}.
     *
     * @param providerClassName the provider class name.
     * @return an error message, or null when the class is valid.
     */
    public static String validateProviderClass(String providerClassName) {
        if (StringUtils.isEmpty(providerClassName)) {
            return "AccessTokenProvider class is required for AccessTokenProvider auth type";
        }

        try {
            Utils.loadClass(providerClassName, CosmosAccessTokenProvider.class);
            return null;
        } catch (ClassNotFoundException e) {
            return "AccessTokenProvider class '" + providerClassName + "' can not be found";
        } catch (ClassCastException e) {
            return "AccessTokenProvider class '" + providerClassName + "' must implement "
                + CosmosAccessTokenProvider.class.getName();
        }
    }

    private static void closeQuietly(CosmosAccessTokenProvider provider) {
        try {
            provider.close();
        } catch (Exception ignored) {
            // best effort
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CosmosAccessTokenProviderAuthConfig that = (CosmosAccessTokenProviderAuthConfig) o;
        return Objects.equals(providerClassName, that.providerClassName)
            && Objects.equals(providerConfigs, that.providerConfigs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(providerClassName, providerConfigs);
    }

    @Override
    public String toString() {
        // Connector configs may contain secrets; only expose the provider class.
        return "CosmosAccessTokenProviderAuthConfig{providerClassName='" + providerClassName + "'}";
    }
}
