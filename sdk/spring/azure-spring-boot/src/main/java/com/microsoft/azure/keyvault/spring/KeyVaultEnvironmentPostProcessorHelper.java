// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.spring;

import static com.microsoft.azure.telemetry.TelemetryData.SERVICE_NAME;
import static com.microsoft.azure.telemetry.TelemetryData.getClassPackageSimpleName;
import static com.microsoft.azure.utils.ApplicationId.AZURE_SPRING_KEY_VAULT;
import static com.microsoft.azure.utils.Constants.AZURE_KEYVAULT_PROPERTYSOURCE_NAME;
import static com.microsoft.azure.utils.Constants.DEFAULT_REFRESH_INTERVAL_MS;
import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.microsoft.azure.keyvault.spring.KeyVaultProperties.Property;
import com.microsoft.azure.telemetry.TelemetrySender;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * A helper class to initialize the key vault secret client depending on which authentication method users choose. Then
 * add key vault as a property source to the environment.
 */
class KeyVaultEnvironmentPostProcessorHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyVaultEnvironmentPostProcessorHelper.class);

    private final ConfigurableEnvironment environment;

    KeyVaultEnvironmentPostProcessorHelper(final ConfigurableEnvironment environment) {
        this.environment = environment;
        Assert.notNull(environment, "environment must not be null!");
        // As @PostConstructor not available when post processor, call it explicitly.
        sendTelemetry();
    }

    /**
     * Add a key vault property source.
     *
     * <p>
     * The normalizedName is used to target a specific key vault (note if the name is the empty string it works as
     * before with only one key vault present). The normalized name is the name of the specific key vault plus a
     * trailing "." at the end.
     * </p>
     *
     * @param normalizedName the normalized name.
     */
    public void addKeyVaultPropertySource(String normalizedName) {
        final String vaultUri = getPropertyValue(normalizedName, Property.URI);
        Assert.notNull(vaultUri, "vaultUri must not be null!");
        final Long refreshInterval = Optional.ofNullable(getPropertyValue(normalizedName, Property.REFRESH_INTERVAL))
                .map(Long::valueOf)
                .orElse(DEFAULT_REFRESH_INTERVAL_MS);
        final List<String> secretKeys = Binder.get(this.environment)
                .bind(
                        KeyVaultProperties.getPropertyName(normalizedName, Property.SECRET_KEYS),
                        Bindable.listOf(String.class)
                )
                .orElse(Collections.emptyList());

        final TokenCredential tokenCredential = getCredentials(normalizedName);
        final SecretClient secretClient = new SecretClientBuilder()
                .vaultUrl(vaultUri)
                .credential(tokenCredential)
                .httpLogOptions(new HttpLogOptions().setApplicationId(AZURE_SPRING_KEY_VAULT))
                .buildClient();
        try {
            final MutablePropertySources sources = this.environment.getPropertySources();
            final boolean caseSensitive = Boolean
                    .parseBoolean(getPropertyValue(normalizedName, Property.CASE_SENSITIVE_KEYS));
            final KeyVaultOperation keyVaultOperation = new KeyVaultOperation(
                    secretClient,
                    refreshInterval,
                    secretKeys,
                    caseSensitive);

            String propertySourceName = Optional.of(normalizedName)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .orElse(AZURE_KEYVAULT_PROPERTYSOURCE_NAME);
            KeyVaultPropertySource keyVaultPropertySource =
                    new KeyVaultPropertySource(propertySourceName, keyVaultOperation);
            if (sources.contains(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) {
                sources.addAfter(
                        SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                        keyVaultPropertySource
                );
            } else {
                sources.addFirst(keyVaultPropertySource);
            }

        } catch (final Exception ex) {
            throw new IllegalStateException("Failed to configure KeyVault property source", ex);
        }
    }

    /**
     * Get the token credentials.
     *
     * @return the token credentials.
     */
    public TokenCredential getCredentials() {
        return getCredentials("");
    }

    /**
     * Get the token credentials.
     *
     * @param normalizedName the normalized name of the key vault.
     * @return the token credentials.
     */
    public TokenCredential getCredentials(String normalizedName) {
        //use service principle to authenticate
        final String clientId = getPropertyValue(normalizedName, Property.CLIENT_ID);
        final String clientKey = getPropertyValue(normalizedName, Property.CLIENT_KEY);
        final String tenantId = getPropertyValue(normalizedName, Property.TENANT_ID);
        final String certificatePath = getPropertyValue(normalizedName, Property.CERTIFICATE_PATH);
        final String certificatePassword = getPropertyValue(normalizedName, Property.CERTIFICATE_PASSWORD);
        if (clientId != null
                && tenantId != null
                && clientKey != null
        ) {
            LOGGER.debug("Will use custom credentials");
            return new ClientSecretCredentialBuilder()
                    .clientId(clientId)
                    .clientSecret(clientKey)
                    .tenantId(tenantId)
                    .build();
        }
        // Use certificate to authenticate
        // Password can be empty
        if (clientId != null
                && tenantId != null
                && certificatePath != null
        ) {
            if (StringUtils.isEmpty(certificatePassword)) {
                return new ClientCertificateCredentialBuilder()
                        .tenantId(tenantId)
                        .clientId(clientId)
                        .pemCertificate(certificatePath)
                        .build();
            } else {
                return new ClientCertificateCredentialBuilder()
                        .tenantId(tenantId)
                        .clientId(clientId)
                        .pfxCertificate(certificatePath, certificatePassword)
                        .build();
            }
        }
        //use MSI to authenticate
        if (clientId != null) {
            LOGGER.debug("Will use MSI credentials with specified clientId");
            return new ManagedIdentityCredentialBuilder().clientId(clientId).build();
        }
        LOGGER.debug("Will use MSI credentials");
        return new ManagedIdentityCredentialBuilder().build();
    }

    private String getPropertyValue(final Property property) {
        return Optional.of(property)
                .map(KeyVaultProperties::getPropertyName)
                .map(environment::getProperty)
                .orElse(null);
    }

    private String getPropertyValue(
            final String normalizedName,
            final Property property
    ) {
        return Optional.of(KeyVaultProperties.getPropertyName(normalizedName, property))
                .map(environment::getProperty)
                .orElse(null);
    }

    private boolean allowTelemetry() {
        return Boolean.parseBoolean(getPropertyValue(Property.ALLOW_TELEMETRY));
    }

    private void sendTelemetry() {
        if (allowTelemetry()) {
            final Map<String, String> events = new HashMap<>();
            final TelemetrySender sender = new TelemetrySender();

            events.put(SERVICE_NAME, getClassPackageSimpleName(KeyVaultEnvironmentPostProcessorHelper.class));

            sender.send(ClassUtils.getUserClass(getClass()).getSimpleName(), events);
        }
    }
}
