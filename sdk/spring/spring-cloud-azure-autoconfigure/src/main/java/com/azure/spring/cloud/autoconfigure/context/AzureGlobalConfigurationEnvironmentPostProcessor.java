// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import org.apache.commons.logging.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_AUTHORITY_HOST;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_CLIENT_ID;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_CLIENT_SECRET;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_CLOUD;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_HTTP_LOG_DETAIL_LEVEL;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_PASSWORD;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_READ_TIMEOUT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_RETRY_COUNT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_TENANT_ID;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_USERNAME;
import static com.azure.core.util.Configuration.PROPERTY_NO_PROXY;

/**
 * An EnvironmentPostProcessor to convert environment variables predefined by Azure Core and Azure SDKs to Azure Spring
 * properties, and add a property source for them as well.
 */
public class AzureGlobalConfigurationEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private final Log logger;

    /**
     * Creates a new instance of {@link AzureGlobalConfigurationEnvironmentPostProcessor}.
     * @param logger The logger used in this class.
     */
    public AzureGlobalConfigurationEnvironmentPostProcessor(Log logger) {
        this.logger = logger;
        AzureCoreEnvMapping.setLogger(logger);
    }

    /**
     * Construct a {@link AzureGlobalConfigurationEnvironmentPostProcessor} instance with default value.
     */
    public AzureGlobalConfigurationEnvironmentPostProcessor() {
        this.logger = new DeferredLog();
        AzureCoreEnvMapping.setLogger(logger);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }


    enum AzureCoreEnvMapping {

        clientId(PROPERTY_AZURE_CLIENT_ID, "credential.client-id"),

        clientSecret(PROPERTY_AZURE_CLIENT_SECRET, "credential.client-secret"),

        clientCertificatePath(PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH, "credential.client-certificate-path"),

        username(PROPERTY_AZURE_USERNAME, "credential.username"),

        password(PROPERTY_AZURE_PASSWORD, "credential.password"),

        tenantId(PROPERTY_AZURE_TENANT_ID, "profile.tenant-id"),

        subscriptionId(PROPERTY_AZURE_SUBSCRIPTION_ID, "profile.subscription-id"),

        azureCloud(PROPERTY_AZURE_CLOUD, "profile.cloud-type"),

        authorityHost(PROPERTY_AZURE_AUTHORITY_HOST, "profile.environment.active-directory-endpoint"),

        maxFixedRetry(PROPERTY_AZURE_REQUEST_RETRY_COUNT, "retry.exponential.max-retries"),

        maxExponentialRetry(PROPERTY_AZURE_REQUEST_RETRY_COUNT, "retry.fixed.max-retries"),

        httpLogLevel(PROPERTY_AZURE_HTTP_LOG_DETAIL_LEVEL, "client.http.logging.level"),

        httpConnectTimeout(PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT, "client.http.connect-timeout", convertMillisToDuration()),

        httpReadTimeout(PROPERTY_AZURE_REQUEST_READ_TIMEOUT, "client.http.read-timeout", convertMillisToDuration()),

        httpWriteTimeout(PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT, "client.http.write-timeout", convertMillisToDuration()),

        httpResponseTimeout(PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT, "client.http.response-timeout", convertMillisToDuration()),

        httpNoProxy(PROPERTY_NO_PROXY, "proxy.http.non-proxy-hosts");


        // TODO (xiada): how to set this proxy?
        // proxy(PROPERTY_HTTP_PROXY, PROPERTY_HTTPS_PROXY)

        private static Log logger;
        private final String coreEnvName;
        private final String springPropertyName;
        private final Function<String, Object> converter;

        AzureCoreEnvMapping(String coreEnvName, String springPropertyName) {
            this(coreEnvName, springPropertyName, a -> a);
        }

        AzureCoreEnvMapping(String coreEnvName, String springPropertyName, Function<String, Object> converter) {
            this.coreEnvName = coreEnvName;
            this.springPropertyName = "spring.cloud.azure." + springPropertyName;
            this.converter = converter;
        }

        private static Function<String, Object> convertMillisToDuration() {
            return ms -> {
                try {
                    return Duration.ofMillis(Integer.parseInt(ms));
                } catch (Exception ignore) {
                    logger.debug("The millisecond value " + ms + " is malformed.");
                    return null;
                }
            };
        }

        public static void setLogger(Log logger) {
            AzureCoreEnvMapping.logger = logger;
        }

    }

    enum AzureSdkEnvMapping {
        keyVaultSecretEndpoint("AZURE_KEYVAULT_ENDPOINT", "keyvault.secret.endpoint"),
        keyVaultCertificateEndpoint("AZURE_KEYVAULT_ENDPOINT", "keyvault.certificate.endpoint"),
        eventHubsConnectionString("AZURE_EVENT_HUBS_CONNECTION_STRING", "eventhubs.connection-string");

        private final String sdkEnvName;
        private final String springPropertyName;
        private final Function<String, Object> converter;

        AzureSdkEnvMapping(String sdkEnvName, String springPropertyName) {
            this(sdkEnvName, springPropertyName, a -> a);
        }

        AzureSdkEnvMapping(String sdkEnvName, String springPropertyName, Function<String, Object> converter) {
            this.sdkEnvName = sdkEnvName;
            this.springPropertyName = "spring.cloud.azure." + springPropertyName;
            this.converter = converter;
        }
    }
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> source = new HashMap<>();

        for (AzureCoreEnvMapping mapping : AzureCoreEnvMapping.values()) {
            if (environment.containsProperty(mapping.coreEnvName)) {
                String property = environment.getProperty(mapping.coreEnvName);
                source.put(mapping.springPropertyName, mapping.converter.apply(property));
            }
        }

        for (AzureSdkEnvMapping mapping : AzureSdkEnvMapping.values()) {
            if (environment.containsProperty(mapping.sdkEnvName)) {
                String property = environment.getProperty(mapping.sdkEnvName);
                source.put(mapping.springPropertyName, mapping.converter.apply(property));
            }
        }

        if (!source.isEmpty()) {
            environment.getPropertySources().addLast(new AzureCoreEnvPropertySource("Azure Core/SDK", source));
        } else {
            logger.debug("No env predefined by Azure Core/SDKs are set, skip adding the AzureCoreEnvPropertySource.");
        }
    }

    private static class AzureCoreEnvPropertySource extends MapPropertySource {

        /**
         * Create a new {@code MapPropertySource} with the given name and {@code Map}.
         *
         * @param name the associated name
         * @param source the Map source (without {@code null} values in order to get consistent {@link #getProperty}
         * and {@link
         * #containsProperty} behavior)
         */
        AzureCoreEnvPropertySource(String name, Map<String, Object> source) {
            super(name, source);
        }
    }


}
