// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.spring;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import static com.microsoft.azure.utils.Constants.AZURE_KEYVAULT_ALLOW_TELEMETRY;
import static com.microsoft.azure.utils.Constants.AZURE_KEYVAULT_CERTIFICATE_PASSWORD;
import static com.microsoft.azure.utils.Constants.AZURE_KEYVAULT_CERTIFICATE_PATH;
import static com.microsoft.azure.utils.Constants.AZURE_KEYVAULT_CLIENT_ID;
import static com.microsoft.azure.utils.Constants.AZURE_KEYVAULT_CLIENT_KEY;
import static com.microsoft.azure.utils.Constants.AZURE_KEYVAULT_PREFIX;
import static com.microsoft.azure.utils.Constants.AZURE_KEYVAULT_REFRESH_INTERVAL;
import static com.microsoft.azure.utils.Constants.AZURE_KEYVAULT_SECRET_KEYS;
import static com.microsoft.azure.utils.Constants.AZURE_KEYVAULT_TENANT_ID;
import static com.microsoft.azure.utils.Constants.AZURE_KEYVAULT_VAULT_URI;
import static com.microsoft.azure.utils.Constants.DEFAULT_REFRESH_INTERVAL_MS;
import static com.microsoft.azure.utils.Constants.SPRINGBOOT_KEY_VAULT_APPLICATION_ID;
import com.microsoft.azure.telemetry.TelemetrySender;
import com.microsoft.azure.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.microsoft.azure.telemetry.TelemetryData.SERVICE_NAME;
import static com.microsoft.azure.telemetry.TelemetryData.getClassPackageSimpleName;

/**
 * A helper class to initialize the key vault secret client depending on which authentication method users choose.
 * Then add key vault as a property source to the environment.
 */
class KeyVaultEnvironmentPostProcessorHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyVaultEnvironmentPostProcessorHelper.class);

    private final ConfigurableEnvironment environment;

    KeyVaultEnvironmentPostProcessorHelper(final ConfigurableEnvironment environment) {
        this.environment = environment;
        // As @PostConstructor not available when post processor, call it explicitly.
        sendTelemetry();
    }

    /**
     * Add a key vault property source.
     *
     * <p>
     * The normalizedName is used to target a specific key vault (note if the
     * name is the empty string it works as before with only one key vault
     * present). The normalized name is the name of the specific key vault plus
     * a trailing "." at the end.
     * </p>
     *
     * @param normalizedName the normalized name.
     */
    public void addKeyVaultPropertySource(String normalizedName) {
        final String vaultUri = getProperty(this.environment,
                AZURE_KEYVAULT_PREFIX + normalizedName + AZURE_KEYVAULT_VAULT_URI);
        final Long refreshInterval = Optional.ofNullable(
                this.environment.getProperty(
                        AZURE_KEYVAULT_PREFIX + normalizedName + AZURE_KEYVAULT_REFRESH_INTERVAL))
                .map(Long::valueOf).orElse(DEFAULT_REFRESH_INTERVAL_MS);
        final Binder binder = Binder.get(this.environment);
        final List<String> secretKeys = binder.bind(
                AZURE_KEYVAULT_PREFIX + normalizedName + AZURE_KEYVAULT_SECRET_KEYS, Bindable.listOf(String.class))
                .orElse(Collections.emptyList());

        final TokenCredential tokenCredential = getCredentials(normalizedName);
        final SecretClient secretClient = new SecretClientBuilder()
                .vaultUrl(vaultUri)
                .credential(tokenCredential)
                .httpLogOptions(new HttpLogOptions().setApplicationId(SPRINGBOOT_KEY_VAULT_APPLICATION_ID))
                .buildClient();
        try {
            final MutablePropertySources sources = this.environment.getPropertySources();
            final boolean caseSensitive = Boolean.parseBoolean(
                    this.environment.getProperty(Constants.AZURE_KEYVAULT_CASE_SENSITIVE_KEYS, "false"));
            final KeyVaultOperation kvOperation = new KeyVaultOperation(secretClient,
                    vaultUri,
                    refreshInterval,
                    secretKeys,
                    caseSensitive);

            if (normalizedName.equals("")) {
                if (sources.contains(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) {
                    sources.addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                        new KeyVaultPropertySource(kvOperation));
                } else {
                    sources.addFirst(new KeyVaultPropertySource(kvOperation));
                }
            } else {
                if (sources.contains(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) {
                    sources.addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                        new KeyVaultPropertySource(normalizedName, kvOperation));
                } else {
                    sources.addFirst(new KeyVaultPropertySource(normalizedName, kvOperation));
                }
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
        if (this.environment.containsProperty(
                AZURE_KEYVAULT_PREFIX + normalizedName + AZURE_KEYVAULT_CLIENT_ID)
                && this.environment.containsProperty(
                        AZURE_KEYVAULT_PREFIX + normalizedName + AZURE_KEYVAULT_CLIENT_KEY)
                && this.environment.containsProperty(
                        AZURE_KEYVAULT_PREFIX + normalizedName + AZURE_KEYVAULT_TENANT_ID)) {
            LOGGER.debug("Will use custom credentials");
            final String clientId = getProperty(this.environment,
                    AZURE_KEYVAULT_PREFIX + normalizedName + AZURE_KEYVAULT_CLIENT_ID);
            final String clientKey = getProperty(this.environment,
                    AZURE_KEYVAULT_PREFIX + normalizedName + AZURE_KEYVAULT_CLIENT_KEY);
            final String tenantId = getProperty(this.environment,
                    AZURE_KEYVAULT_PREFIX + normalizedName + AZURE_KEYVAULT_TENANT_ID);
            return new ClientSecretCredentialBuilder()
                    .clientId(clientId)
                    .clientSecret(clientKey)
                    .tenantId(tenantId)
                    .build();
        }
        //use certificate to authenticate
        if (this.environment.containsProperty(
                AZURE_KEYVAULT_PREFIX + normalizedName + AZURE_KEYVAULT_CLIENT_ID)
                && this.environment.containsProperty(
                        AZURE_KEYVAULT_PREFIX + normalizedName + AZURE_KEYVAULT_CERTIFICATE_PATH)
                && this.environment.containsProperty(
                        AZURE_KEYVAULT_PREFIX + normalizedName + AZURE_KEYVAULT_TENANT_ID)) {
            // Password can be empty
            final String certPwd = this.environment.getProperty(
                    AZURE_KEYVAULT_PREFIX + normalizedName + AZURE_KEYVAULT_CERTIFICATE_PASSWORD);
            final String certPath = getProperty(this.environment,
                    AZURE_KEYVAULT_PREFIX + normalizedName + AZURE_KEYVAULT_CERTIFICATE_PATH);

            if (StringUtils.isEmpty(certPwd)) {
                return new ClientCertificateCredentialBuilder()
                        .tenantId(getProperty(this.environment,
                                AZURE_KEYVAULT_PREFIX + normalizedName + AZURE_KEYVAULT_TENANT_ID))
                        .clientId(getProperty(this.environment,
                               AZURE_KEYVAULT_PREFIX + normalizedName + AZURE_KEYVAULT_CLIENT_ID))
                        .pemCertificate(certPath)
                        .build();
            } else {
                return new ClientCertificateCredentialBuilder()
                        .tenantId(getProperty(this.environment,
                                AZURE_KEYVAULT_PREFIX + normalizedName + AZURE_KEYVAULT_TENANT_ID))
                        .clientId(getProperty(this.environment,
                                AZURE_KEYVAULT_PREFIX + normalizedName + AZURE_KEYVAULT_CLIENT_ID))
                        .pfxCertificate(certPath, certPwd)
                        .build();
            }
        }
        //use MSI to authenticate
        if (this.environment.containsProperty(
                AZURE_KEYVAULT_PREFIX + normalizedName + AZURE_KEYVAULT_CLIENT_ID)) {
            LOGGER.debug("Will use MSI credentials with specified clientId");
            final String clientId = getProperty(this.environment,
                    AZURE_KEYVAULT_PREFIX + normalizedName + AZURE_KEYVAULT_CLIENT_ID);
            return new ManagedIdentityCredentialBuilder().clientId(clientId).build();
        }
        LOGGER.debug("Will use MSI credentials");
        return new ManagedIdentityCredentialBuilder().build();
    }

    private String getProperty(final ConfigurableEnvironment env, final String propertyName) {
        Assert.notNull(env, "env must not be null!");
        Assert.notNull(propertyName, "propertyName must not be null!");
        final String property = env.getProperty(propertyName);
        if (property == null || property.isEmpty()) {
            throw new IllegalArgumentException("property " + propertyName + " must not be null");
        }
        return property;
    }

    private boolean allowTelemetry(final ConfigurableEnvironment env) {
        Assert.notNull(env, "env must not be null!");
        return env.getProperty(AZURE_KEYVAULT_PREFIX + AZURE_KEYVAULT_ALLOW_TELEMETRY, Boolean.class, true);
    }

    private void sendTelemetry() {
        if (allowTelemetry(environment)) {
            final Map<String, String> events = new HashMap<>();
            final TelemetrySender sender = new TelemetrySender();

            events.put(SERVICE_NAME, getClassPackageSimpleName(KeyVaultEnvironmentPostProcessorHelper.class));

            sender.send(ClassUtils.getUserClass(getClass()).getSimpleName(), events);
        }
    }
}
