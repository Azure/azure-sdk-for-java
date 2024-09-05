// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.environment;

import org.springframework.core.env.EnumerablePropertySource;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static com.azure.spring.cloud.autoconfigure.implementation.keyvault.environment.utils.KeyVaultPropertySourceUtils.toKeyVaultSecretName;


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
     * Stores the timer objects to schedule refresh task.
     */
    private static final ConcurrentHashMap<String, Timer> TIMER_MANAGER = new ConcurrentHashMap<>();

    /**
     * Create a new {@code KeyVaultPropertySource} with the given name and {@link KeyVaultOperation}.
     * @param name the associated name
     * @param refreshDuration the refresh in milliseconds (0 or less disables refresh).
     * @param keyVaultOperation the {@link KeyVaultOperation}
     */
    public KeyVaultPropertySource(String name,
                                  final Duration refreshDuration,
                                  KeyVaultOperation keyVaultOperation,
                                  boolean caseSensitive) {
        super(name, keyVaultOperation);
        this.caseSensitive = caseSensitive;
        this.keyVaultOperation = keyVaultOperation;
        refreshProperties();
        enableAutoRefreshProperties(refreshDuration);
    }

    void refreshProperties() {
        logger.debug("Refreshing the secrets in property source '" + name + "'.");
        properties = keyVaultOperation.refreshProperties();
        logger.debug("The secrets in property source '" + name + "' has been refreshed.");
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
                        refreshProperties();
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
}
