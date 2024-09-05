// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.environment;

import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.paging.ContinuablePagedIterable;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.azure.spring.cloud.autoconfigure.implementation.keyvault.environment.utils.KeyVaultPropertySourceUtils.toKeyVaultSecretName;

/**
 * KeyVaultOperation wraps the operations to access Key Vault.
 *
 * @since 4.0.0
 */
public class KeyVaultOperation {

    /**
     * Stores the case-sensitive flag.
     */
    private final boolean caseSensitive;

    /**
     * Stores the secret client.
     */
    private final SecretClient secretClient;

    /**
     * Stores the secret keys.
     */
    private final List<String> secretKeys;

    /**
     * Constructor.
     * @param secretClient the Key Vault secret client.
     * @param secretKeys the secret keys to look for.
     * @param caseSensitive the case-sensitive flag.
     */
    public KeyVaultOperation(final SecretClient secretClient,
                             List<String> secretKeys,
                             boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        this.secretClient = secretClient;
        this.secretKeys = secretKeys;
    }

    /**
     * Refresh the properties by accessing key vault.
     */
    Map<String, String> refreshProperties() {
        Map<String, String> properties;
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
                                     s -> toKeyVaultSecretName(this.caseSensitive, s.getName()),
                                     KeyVaultSecret::getValue
                                 ));
        } else {
            properties = secretKeys.stream()
                                   .map(key -> toKeyVaultSecretName(this.caseSensitive, key))
                                   .map(secretClient::getSecret)
                                   .filter(Objects::nonNull)
                                   .collect(Collectors.toMap(
                                       s -> toKeyVaultSecretName(this.caseSensitive, s.getName()),
                                       KeyVaultSecret::getValue
                                   ));
        }
        return properties;
    }
}
