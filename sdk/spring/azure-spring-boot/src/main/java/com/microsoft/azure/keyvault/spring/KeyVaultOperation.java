// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.spring;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Encapsulate key vault secret client in this class to provide a delegate of key vault operations.
 */
public class KeyVaultOperation {

    /**
     * Stores the case sensitive flag.
     */
    private final boolean caseSensitive;

    private final SecretClient keyVaultClient;
    private final String vaultUri;
    private volatile List<String> secretNames;
    private final boolean secretNamesAlreadyConfigured;
    private final long secretNamesRefreshIntervalInMs;
    private volatile long secretNamesLastUpdateTime;

    public KeyVaultOperation(
        final SecretClient keyVaultClient,
        String vaultUri,
        final long secretKeysRefreshIntervalInMs,
        final List<String> secretNames,
        boolean caseSensitive
    ) {
        this.caseSensitive = caseSensitive;
        this.keyVaultClient = keyVaultClient;
        // TODO(pan): need to validate why last '/' need to be truncated.
        this.vaultUri = StringUtils.trimTrailingCharacter(vaultUri.trim(), '/');
        this.secretNames = Optional.ofNullable(secretNames)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .map(this::toKeyVaultSecretName)
                .distinct()
                .collect(Collectors.toList());
        this.secretNamesAlreadyConfigured = !this.secretNames.isEmpty();
        this.secretNamesRefreshIntervalInMs = secretKeysRefreshIntervalInMs;
        this.secretNamesLastUpdateTime = 0;
    }

    public String[] getPropertyNames() {
        refreshSecretKeysIfNeeded();
        if (!caseSensitive) {
            return Optional.ofNullable(secretNames)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .flatMap(p -> Stream.of(p, p.replaceAll("-", ".")))
                .distinct()
                .toArray(String[]::new);
        } else {
            return Optional.ofNullable(secretNames)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .distinct()
                .toArray(String[]::new);
        }
    }


    /**
     * For convention we need to support all relaxed binding format from spring, these may include:
     * <ul>
     * <li>Spring relaxed binding names</li>
     * <li>acme.my-project.person.first-name</li>
     * <li>acme.myProject.person.firstName</li>
     * <li>acme.my_project.person.first_name</li>
     * <li>ACME_MYPROJECT_PERSON_FIRSTNAME</li>
     * </ul>
     * But azure keyvault only allows ^[0-9a-zA-Z-]+$ and case insensitive, so there must be some conversion
     * between spring names and azure keyvault names.
     * For example, the 4 properties stated above should be convert to acme-myproject-person-firstname in keyvault.
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
                        .replaceAll("-", "")     // my-project -> myproject
                        .replaceAll("_", "")     // my_project -> myproject
                        .replaceAll("\\.", "-"); // acme.myproject -> acme-myproject
            }
        } else {
            return property;
        }
    }

    public String get(final String property) {
        Assert.hasText(property, "property should contain text.");
        refreshSecretKeysIfNeeded();
        return Optional.of(property)
                .map(this::toKeyVaultSecretName)
                .filter(secretNames::contains)
                .map(this::getValueFromKeyVault)
                .orElse(null);
    }

    private synchronized void refreshSecretKeysIfNeeded() {
        if (needRefreshSecretKeys()) {
            refreshKeyVaultSecretNames();
        }
    }

    private boolean needRefreshSecretKeys() {
        return !secretNamesAlreadyConfigured
                && System.currentTimeMillis() - this.secretNamesLastUpdateTime > this.secretNamesRefreshIntervalInMs;
    }

    private void refreshKeyVaultSecretNames() {
        secretNames = Optional.of(keyVaultClient)
                .map(SecretClient::listPropertiesOfSecrets)
                .map(secretProperties -> {
                    final List<String> secretNameList = new ArrayList<>();
                    secretProperties.forEach(s -> {
                        final String secretName = s.getName().replace(vaultUri + "/secrets/", "");
                        secretNameList.add(secretName);
                    });
                    return secretNameList;
                })
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .map(this::toKeyVaultSecretName)
                .distinct()
                .collect(Collectors.toList());
        this.secretNamesLastUpdateTime = System.currentTimeMillis();
    }

    private String getValueFromKeyVault(String name) {
        return Optional.ofNullable(name)
                .map(keyVaultClient::getSecret)
                .map(KeyVaultSecret::getValue)
                .orElse(null);
    }

}
