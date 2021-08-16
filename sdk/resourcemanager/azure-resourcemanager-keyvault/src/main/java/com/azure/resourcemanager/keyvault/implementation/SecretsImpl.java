// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.keyvault.models.Secret;
import com.azure.resourcemanager.keyvault.models.Secrets;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.azure.security.keyvault.secrets.models.SecretProperties;
import reactor.core.publisher.Mono;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** The implementation of Secrets and its parent interfaces. */
class SecretsImpl extends CreatableWrappersImpl<Secret, SecretImpl, SecretProperties> implements Secrets {
    private final SecretAsyncClient inner;
    private final Vault vault;

    private final ClientLogger logger = new ClientLogger(SecretsImpl.class);

    SecretsImpl(SecretAsyncClient client, Vault vault) {
        this.inner = client;
        this.vault = vault;
    }

    @Override
    public SecretImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected SecretImpl wrapModel(String name) {
        return new SecretImpl(name, new SecretProperties(), vault);
    }

    @Override
    public Secret getById(String id) {
        return getByIdAsync(id).block();
    }

    @Override
    public Mono<Secret> getByIdAsync(String id) {
        String name = nameFromId(id);
        String version = versionFromId(id);
        return this.getByNameAndVersionAsync(name, version);
    }

    @Override
    protected SecretImpl wrapModel(SecretProperties secretProperties) {
        if (secretProperties == null) {
            return null;
        }
        return new SecretImpl(secretProperties.getName(), secretProperties, vault);
    }

    protected SecretImpl wrapModel(KeyVaultSecret keyVaultSecret) {
        if (keyVaultSecret == null) {
            return null;
        }
        return new SecretImpl(keyVaultSecret.getName(), keyVaultSecret, vault);
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        String name = nameFromId(id);
        return inner
            .beginDeleteSecret(name)
            .last()
            .flatMap(
                asyncPollResponse -> {
                    if (asyncPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
                        return asyncPollResponse.getFinalResult();
                    } else {
                        return Mono
                            .error(
                                new RuntimeException(
                                    "polling completed unsuccessfully with status:" + asyncPollResponse.getStatus()));
                    }
                });
    }

    @Override
    public PagedIterable<Secret> list() {
        return new PagedIterable<>(listAsync());
    }

    @Override
    public PagedFlux<Secret> listAsync() {
        return PagedConverter.mapPage(inner.listPropertiesOfSecrets(), this::wrapModel);
    }

    @Override
    public Mono<Secret> getByNameAsync(final String name) {
        return inner.getSecret(name).map(this::wrapModel);
    }

    @Override
    public Secret getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Secret getByNameAndVersion(String name, String version) {
        return getByNameAndVersionAsync(name, version).block();
    }

    @Override
    public Mono<Secret> getByNameAndVersionAsync(final String name, final String version) {
        Objects.requireNonNull(name);
        return (version == null ? inner.getSecret(name) : inner.getSecret(name, version)).map(this::wrapModel);
    }

    @Override
    public Secret enableByNameAndVersion(String name, String version) {
        return enableByNameAndVersionAsync(name, version).block();
    }

    @Override
    public Mono<Secret> enableByNameAndVersionAsync(String name, String version) {
        Objects.requireNonNull(name);
        return updateSecretEnableDisableAsync(name, version, true);
    }

    @Override
    public void disableByNameAndVersion(String name, String version) {
        disableByNameAndVersionAsync(name, version).block();
    }

    @Override
    public Mono<Void> disableByNameAndVersionAsync(String name, String version) {
        Objects.requireNonNull(name);
        return updateSecretEnableDisableAsync(name, version, false).then();
    }

    private Mono<Secret> updateSecretEnableDisableAsync(String name, String version, boolean enabled) {
        try {
            // create SecretProperties with name and version via JSON serialization.
            String mockId = "https://mock.vault.azure.net/secrets/" + name;
            if (!CoreUtils.isNullOrEmpty(version)) {
                mockId += "/" + version;
            }
            Map<String, String> mockSecret = new HashMap<>();
            mockSecret.put("id", mockId);
            SerializerAdapter serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();
            String json = serializerAdapter.serialize(mockSecret, SerializerEncoding.JSON);
            SecretProperties secretProperties = serializerAdapter.deserialize(json, SecretProperties.class,
                SerializerEncoding.JSON);

            secretProperties.setEnabled(enabled);

            return vault.secretClient().updateSecretProperties(secretProperties).map(this::wrapModel);
        } catch (IOException ioe) {
            throw logger.logExceptionAsError(new RuntimeException(ioe));
        }
    }

    private static String nameFromId(String id) {
        try {
            URL url = new URL(id);
            String[] tokens = url.getPath().split("/");
            String name = (tokens.length >= 3 ? tokens[2] : null);
            return name;
        } catch (MalformedURLException e) {
            // Should never come here.
            throw new IllegalStateException("Received Malformed Id URL from KV Service");
        }
    }

    private static String versionFromId(String id) {
        try {
            URL url = new URL(id);
            String[] tokens = url.getPath().split("/");
            String version = (tokens.length >= 4 ? tokens[3] : null);
            return version;
        } catch (MalformedURLException e) {
            // Should never come here.
            throw new IllegalStateException("Received Malformed Id URL from KV Service");
        }
    }
}
