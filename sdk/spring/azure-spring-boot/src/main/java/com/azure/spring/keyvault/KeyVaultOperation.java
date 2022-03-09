// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.keyvault;

import com.azure.core.exception.HttpRequestException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.paging.ContinuablePagedIterable;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * KeyVaultOperation wraps the operations to access Key Vault.
 */
@SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
public class KeyVaultOperation {

    private static final Logger LOG = LoggerFactory.getLogger(KeyVaultOperation.class);

    /**
     * Stores the case sensitive flag.
     */
    private final boolean caseSensitive;

    /**
     * Stores the properties.
     */
    private Map<String, String> properties = new HashMap<>();

    /**
     * Stores the secret client.
     */
    private final SecretClient secretClient;

    /**
     * Stores the secret keys.
     */
    private final List<String> secretKeys;
    /**
     * Stores the timer object to schedule refresh task.
     */
    private static Timer timer;

    /**
     * Constructor.
     * @param secretClient the Key Vault secret client.
     * @param refreshInMillis the refresh in milliseconds (0 or less disables refresh).
     * @param secretKeys the secret keys to look for.
     * @param caseSensitive the case sensitive flag.
     */
    public KeyVaultOperation(
        final SecretClient secretClient,
        final long refreshInMillis,
        List<String> secretKeys,
        boolean caseSensitive
    ) {

        this.caseSensitive = caseSensitive;
        this.secretClient = secretClient;
        this.secretKeys = secretKeys;

        refreshProperties();

        if (refreshInMillis > 0) {
            synchronized (KeyVaultOperation.class) {
                if (timer != null) {
                    try {
                        timer.cancel();
                        timer.purge();
                    } catch (RuntimeException runtimeException) {
                        LOG.error("Error of terminating Timer", runtimeException);
                    }
                }
                timer = new Timer(true);
                final TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        refreshProperties();
                    }
                };
                timer.scheduleAtFixedRate(task, refreshInMillis, refreshInMillis);
            }
        }
    }

    /**
     * Get the property.
     *
     * @param property the property to get.
     * @return the property value.
     */
    public String getProperty(String property) {
        return properties.get(toKeyVaultSecretName(property));
    }

    /**
     * Get the property names.
     *
     * @return the property names.
     */
    public String[] getPropertyNames() {
        if (!caseSensitive) {
            return properties
                .keySet()
                .stream()
                .flatMap(p -> Stream.of(p, p.replaceAll("-", ".")))
                .distinct()
                .toArray(String[]::new);
        } else {
            return properties
                .keySet()
                .toArray(new String[0]);
        }
    }

    /**
     * Refresh the properties by accessing key vault.
     */
    private void refreshProperties() {
        if (secretKeys == null || secretKeys.isEmpty()) {
            properties = Optional.of(secretClient)
                .map(SecretClient::listPropertiesOfSecrets)
                .map(ContinuablePagedIterable::iterableByPage)
                .map(i -> StreamSupport.stream(i.spliterator(), false))
                .orElseGet(Stream::empty)
                .map(PagedResponse::getElements)
                .flatMap(i -> StreamSupport.stream(i.spliterator(), false))
                .filter(SecretProperties::isEnabled)
                .map(p -> secretClient.getSecret(p.getName(), p.getVersion()))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                    s -> toKeyVaultSecretName(s.getName()),
                    KeyVaultSecret::getValue
                ));
        } else {
            properties = secretKeys.stream()
                .map(this::toKeyVaultSecretName)
                .map(secretClient::getSecret)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                    s -> toKeyVaultSecretName(s.getName()),
                    KeyVaultSecret::getValue
                ));
        }
    }

    /**
     * For convention we need to support all relaxed binding format from spring, these may include:
     * <table>
     * <tr><td>Spring relaxed binding names</td></tr>
     * <tr><td>acme.my-project.person.first-name</td></tr>
     * <tr><td>acme.myProject.person.firstName</td></tr>
     * <tr><td>acme.my_project.person.first_name</td></tr>
     * <tr><td>ACME_MYPROJECT_PERSON_FIRSTNAME</td></tr>
     * </table>
     * But azure keyvault only allows ^[0-9a-zA-Z-]+$ and case insensitive, so
     * there must be some conversion between spring names and azure keyvault
     * names. For example, the 4 properties stated above should be convert to
     * acme-myproject-person-firstname in keyvault.
     *
     * @param property of secret instance.
     * @return the value of secret with given name or null.
     */
    private String toKeyVaultSecretName(@NonNull String property) {
        if (!caseSensitive) {
            if (property.matches("[a-z0-9A-Z-]+")) {
                return property.toLowerCase(Locale.US);
            } else if (property.matches("[A-Z0-9_]+")) {
                return property.toLowerCase(Locale.US).replaceAll("_", "-");
            } else {
                return property.toLowerCase(Locale.US)
                    .replaceAll("-", "") // my-project -> myproject
                    .replaceAll("_", "") // my_project -> myproject
                    .replaceAll("\\.", "-"); // acme.myproject -> acme-myproject
            }
        } else {
            return property;
        }
    }

    /**
     * Set the properties.
     *
     * @param properties the properties.
     */
    void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }

    boolean isUp() {
        boolean result;
        try {
            //noinspection ResultOfMethodCallIgnored
            secretClient.listPropertiesOfSecrets();
            result = true;
        } catch (HttpResponseException httpResponseException) {
            result = httpResponseException.getResponse().getStatusCode() < 500;
        } catch (HttpRequestException httpRequestException) {
            LOG.error("An HTTP error occurred while checking key vault connectivity", httpRequestException);
            result = false;
        } catch (RuntimeException runtimeException) {
            LOG.error("A runtime error occurred while checking key vault connectivity", runtimeException);
            result = false;
        }
        return result;
    }
}
