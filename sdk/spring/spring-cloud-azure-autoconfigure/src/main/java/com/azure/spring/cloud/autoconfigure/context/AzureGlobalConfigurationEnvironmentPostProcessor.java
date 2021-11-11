// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

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
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_RETRY_COUNT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_TENANT_ID;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_USERNAME;

/**
 * An EnvironmentPostProcessor to convert environment variables predefined by Azure Core and Azure SDKs to Azure Spring
 * properties, and add a property source for them as well.
 */
public class AzureGlobalConfigurationEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureGlobalConfigurationEnvironmentPostProcessor.class);

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

        azureCloud(PROPERTY_AZURE_CLOUD, "profile.cloud"),

        authorityHost(PROPERTY_AZURE_AUTHORITY_HOST, "profile.environment.active-directory-endpoint"),

        // TODO (xiada): PROPERTY_AZURE_LOG_LEVEL, how to set this to env?

        httpLogLevel(PROPERTY_AZURE_HTTP_LOG_DETAIL_LEVEL, "client.logging.level"),

        maxRetry(PROPERTY_AZURE_REQUEST_RETRY_COUNT, "retry.max-attempts");

        // TODO (xiada): we can't configure http at global level:
        // PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT,
        // PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT,
        // PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT,
        // PROPERTY_AZURE_REQUEST_READ_TIMEOUT,
        // PROPERTY_NO_PROXY

        // TODO (xiada): how to set this proxy?
        // proxy(PROPERTY_HTTP_PROXY, PROPERTY_HTTPS_PROXY)


        private final String coreEnvName;
        private final String springPropertyName;
        private final Function<String, String> converter;

        AzureCoreEnvMapping(String coreEnvName, String springPropertyName) {
            this(coreEnvName, springPropertyName, Function.identity());
        }

        AzureCoreEnvMapping(String coreEnvName, String springPropertyName, Function<String, String> converter) {
            this.coreEnvName = coreEnvName;
            this.springPropertyName = "spring.cloud.azure." + springPropertyName;
            this.converter = converter;
        }
    }

    enum AzureSdkEnvMapping {
        keyVaultSecretEndpoint("AZURE_KEYVAULT_ENDPOINT", "keyvault.secret.endpoint"),
        keyVaultCertificateEndpoint("AZURE_KEYVAULT_ENDPOINT", "keyvault.certificate.endpoint");

        private final String sdkEnvName;
        private final String springPropertyName;
        private final Function<String, String> converter;

        AzureSdkEnvMapping(String sdkEnvName, String springPropertyName) {
            this(sdkEnvName, springPropertyName, Function.identity());
        }

        AzureSdkEnvMapping(String sdkEnvName, String springPropertyName, Function<String, String> converter) {
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
            LOGGER.debug("No env predefined by Azure Core/SDKs are set, skip adding the AzureCoreEnvPropertySource.");
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
