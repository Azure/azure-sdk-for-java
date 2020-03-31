/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.keyvault.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.management.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import com.azure.management.resources.fluentcore.utils.PagedConverter;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.management.keyvault.Key;
import com.azure.management.keyvault.Keys;
import com.azure.management.keyvault.Vault;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * The implementation of Vaults and its parent interfaces.
 */
class KeysImpl
        extends CreatableWrappersImpl<
                Key,
                KeyImpl,
                KeyVaultKey>
        implements Keys {
    private final KeyAsyncClient inner;
    private final Vault vault;

    KeysImpl(KeyAsyncClient client, Vault vault) {
        this.inner = client;
        this.vault = vault;
    }

    @Override
    public KeyImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected KeyImpl wrapModel(String name) {
        // No valid KeyVaultKey object until service created one.
        return new KeyImpl(name, null, vault);
    }

    @Override
    public Key getById(String id) {
        return getByIdAsync(id).block();
    }

    @Override
    public Mono<Key> getByIdAsync(String id) {
        String name = nameFromId(id);
        return inner.getKey(name).map(this::wrapModel);
    }

    @Override
    protected KeyImpl wrapModel(KeyVaultKey inner) {
        if (inner == null) {
            return null;
        }
        return new KeyImpl(inner.getName(), inner, vault);
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        String name = nameFromId(id);
        return inner.beginDeleteKey(name).last().flatMap(asyncPollResponse -> {
            if (asyncPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
                return asyncPollResponse.getFinalResult();
            } else {
                return Mono.error(new RuntimeException("polling completed unsuccessfully with status:" + asyncPollResponse.getStatus()));
            }
        });
    }

    @Override
    public PagedIterable<Key> list() {
        return new PagedIterable<>(listAsync());
    }

    @Override
    public PagedFlux<Key> listAsync() {
        return PagedConverter.flatMapPage(inner.listPropertiesOfKeys(), p -> inner.getKey(p.getName()).map(this::wrapModel));
    }

    @Override
    public Key getByNameAndVersion(String name, String version) {
        return getByNameAndVersionAsync(name, version).block();
    }

    @Override
    public Mono<Key> getByNameAndVersionAsync(final String name, final String version) {
        return inner.getKey(name, version).map(this::wrapModel);
    }

    @Override
    public Key restore(byte[] backup) {
        return restoreAsync(backup).block();
    }

    @Override
    public Mono<Key> restoreAsync(final byte[] backup) {
        return inner.restoreKeyBackup(backup).map(this::wrapModel);
    }

    @Override
    public Mono<Key> getByNameAsync(final String name) {
        return inner.getKey(name).map(this::wrapModel);
    }

    @Override
    public Key getByName(String name) {
        return getByNameAsync(name).block();
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
}
