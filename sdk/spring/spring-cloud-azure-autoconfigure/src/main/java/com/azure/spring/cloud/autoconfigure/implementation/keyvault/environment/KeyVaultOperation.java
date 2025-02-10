// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.environment;

import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.paging.ContinuablePagedIterable;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * KeyVaultOperation wraps the operations to access Key Vault.
 * This operation can list secrets with given filter, and convert it to spring property name and value.
 *
 * @since 4.0.0
 */
public class KeyVaultOperation {

    /**
     * Stores the secret client.
     */
    private final SecretClient secretClient;

    /**
     * Constructor.
     * @param secretClient the Key Vault secret client.
     */
    public KeyVaultOperation(final SecretClient secretClient) {
        this.secretClient = secretClient;
    }

    /**
     * Get the Key Vault secrets filtered by given secret keys.
     * If the secret keys is empty, return all the secrets in Key Vault.
     */
    List<KeyVaultSecret> listSecrets(List<String> secretKeys) {
        List<KeyVaultSecret> keyVaultSecrets;
        if (secretKeys == null || secretKeys.isEmpty()) {
            keyVaultSecrets = Optional.of(secretClient)
                                 .map(SecretClient::listPropertiesOfSecrets)
                                 .map(ContinuablePagedIterable::iterableByPage)
                                 .map(i -> StreamSupport.stream(i.spliterator(), false))
                                 .orElseGet(Stream::empty)
                                 .map(PagedResponse::getElements)
                                 .flatMap(i -> StreamSupport.stream(i.spliterator(), false))
                                 .filter(SecretProperties::isEnabled)
                                 .map(p -> secretClient.getSecret(p.getName(), p.getVersion()))
                                 .filter(Objects::nonNull)
                                 .toList();
        } else {
            keyVaultSecrets = secretKeys.stream()
                                   .map(secretClient::getSecret)
                                   .filter(Objects::nonNull)
                                   .toList();
        }
        return keyVaultSecrets;
    }
}
