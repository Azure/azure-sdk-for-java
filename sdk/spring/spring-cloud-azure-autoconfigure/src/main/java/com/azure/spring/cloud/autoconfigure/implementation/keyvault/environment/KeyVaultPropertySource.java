// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.environment;

import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.lang.NonNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * A key vault implementation of {@link EnumerablePropertySource} to enumerate all property pairs in Key Vault.
 *
 * @since 4.0.0
 */
public class KeyVaultPropertySource extends EnumerablePropertySource<KeyVaultOperation> {

    /**
     * Stores the properties.
     */
    private Map<String, String> properties = new HashMap<>();

    private final KeyVaultOperation keyVaultOperation;

    /**
     * Stores the case-sensitive flag.
     */
    private final boolean caseSensitive;

    /**
     * Stores the secret keys.
     */
    private final List<String> secretKeys;

    private final List<String> keyVaultSecretKeys;

    /**
     * Stores the timer objects to schedule refresh task.
     */
    private static final ConcurrentHashMap<String, Timer> TIMER_MANAGER = new ConcurrentHashMap<>();

    /**
     * Create a new {@code KeyVaultPropertySource} with the given name and {@link KeyVaultOperation}.
     * @param name the associated name.
     * @param refreshDuration the refresh in milliseconds (0 or less disables refresh).
     * @param keyVaultOperation the {@link KeyVaultOperation}.
     * @param secretKeys the secret keys to look for.
     * @param caseSensitive the case-sensitive flag.
     */
    public KeyVaultPropertySource(String name,
                                  final Duration refreshDuration,
                                  KeyVaultOperation keyVaultOperation,
                                  List<String> secretKeys,
                                  boolean caseSensitive) {
        super(name, keyVaultOperation);
        this.caseSensitive = caseSensitive;
        this.secretKeys = secretKeys;
        this.keyVaultSecretKeys = convertToKeyVaultSecretNames(secretKeys, caseSensitive);
        this.keyVaultOperation = keyVaultOperation;
        loadProperties();
        enableAutoRefreshProperties(refreshDuration);
    }

    void loadProperties() {
        logger.debug("Loading the secrets in property source '" + name + "'.");
        properties = keyVaultOperation.listSecrets(this.keyVaultSecretKeys)
            .stream()
            .collect(Collectors.toMap(
                s -> toKeyVaultSecretName(caseSensitive, s.getName()),
                KeyVaultSecret::getValue)
            );
        logger.debug("The secrets loading in property source '" + name + "' has finished.");
    }

    void enableAutoRefreshProperties(Duration refreshDuration) {
        final long refreshInMillis = refreshDuration.toMillis();
        if (refreshInMillis > 0) {
            synchronized (KeyVaultPropertySource.class) {
                Timer timer;
                if (TIMER_MANAGER.containsKey(name)) {
                    timer = TIMER_MANAGER.get(name);
                    try {
                        timer.cancel();
                        timer.purge();
                    } catch (RuntimeException runtimeException) {
                        logger.error("Error of terminating Timer", runtimeException);
                    }
                }
                timer = new Timer(name, true);
                TIMER_MANAGER.put(name, timer);

                final TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        loadProperties();
                    }
                };
                timer.scheduleAtFixedRate(task, refreshInMillis, refreshInMillis);
            }
        }
    }

    @Override
    public String getProperty(String property) {
        return properties.get(toKeyVaultSecretName(caseSensitive,  property));
    }

    @Override
    public boolean containsProperty(String name) {
        return getProperty(name) != null;
    }

    @Override
    public String[] getPropertyNames() {
        if (!caseSensitive) {
            return properties
                .keySet()
                .stream()
                .flatMap(p -> Stream.of(p, p.replace("-", ".")))
                .distinct()
                .toArray(String[]::new);
        } else {
            return properties
                .keySet()
                .toArray(new String[0]);
        }
    }

    private static List<String> convertToKeyVaultSecretNames(List<String> secretKeys, boolean caseSensitive) {
        if (secretKeys == null) {
            return null;
        }
        return secretKeys.stream()
            .map(key -> toKeyVaultSecretName(caseSensitive, key))
            .toList();
    }

    /**
     * For convention, we need to support all relaxed binding format from spring, these may include:
     * <table>
     * <tr><td>Spring relaxed binding names</td></tr>
     * <tr><td>acme.my-project.person.first-name</td></tr>
     * <tr><td>acme.myProject.person.firstName</td></tr>
     * <tr><td>acme.my_project.person.first_name</td></tr>
     * <tr><td>ACME_MYPROJECT_PERSON_FIRSTNAME</td></tr>
     * </table>
     * But azure key vault only allows ^[0-9a-zA-Z-]+$ and case-insensitive, so
     * there must be some conversion between spring names and azure key vault
     * names. For example, the 4 properties stated above should be converted to
     * acme-myproject-person-firstname in key vault.
     * @param caseSensitive the case-sensitive flag.
     * @param property of secret instance.
     * @return the value of secret with given name or null.
     */
    public static String toKeyVaultSecretName(boolean caseSensitive, @NonNull String property) {
        if (!caseSensitive) {
            if (property.matches("[a-z0-9A-Z-]+")) {
                return property.toLowerCase(Locale.US);
            } else if (property.matches("[A-Z0-9_]+")) {
                return property.toLowerCase(Locale.US).replace("_", "-");
            } else {
                return property.toLowerCase(Locale.US)
                    .replace("-", "") // my-project -> myproject
                    .replace("_", "") // my_project -> myproject
                    .replace(".", "-"); // acme.myproject -> acme-myproject
            }
        } else {
            return property;
        }
    }
}
