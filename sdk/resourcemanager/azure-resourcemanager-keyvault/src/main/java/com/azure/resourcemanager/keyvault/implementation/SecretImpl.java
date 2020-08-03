// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault.implementation;

import com.azure.resourcemanager.keyvault.models.Secret;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import java.util.Map;
import java.util.Objects;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Implementation for Vault and its parent interfaces. */
class SecretImpl extends CreatableUpdatableImpl<Secret, KeyVaultSecret, SecretImpl>
    implements Secret, Secret.Definition, Secret.Update {

    private final Vault vault;

    private String valueToSet;

    SecretImpl(String name, KeyVaultSecret innerObject, Vault vault) {
        super(name, innerObject);
        this.vault = vault;
    }

    private SecretImpl wrapModel(KeyVaultSecret secret) {
        return new SecretImpl(secret.getName(), secret, vault);
    }

    @Override
    public String id() {
        return inner().getId();
    }

    @Override
    public String value() {
        return inner().getValue();
    }

    @Override
    public SecretProperties attributes() {
        return inner().getProperties();
    }

    @Override
    public Map<String, String> tags() {
        return inner().getProperties().getTags();
    }

    @Override
    public String contentType() {
        return inner().getProperties().getContentType();
    }

    @Override
    public String kid() {
        return inner().getProperties().getKeyId();
    }

    @Override
    public boolean managed() {
        return Utils.toPrimitiveBoolean(inner().getProperties().isManaged());
    }

    @Override
    public Iterable<Secret> listVersions() {
        return this.listVersionsAsync().toIterable();
    }

    @Override
    public Flux<Secret> listVersionsAsync() {
        return vault
            .secretClient()
            .listPropertiesOfSecretVersions(name())
            .flatMap(p -> {
                if (p.isEnabled()) {
                    return vault.secretClient().getSecret(p.getName(), p.getVersion());
                } else {
                    return Mono.just(new KeyVaultSecret(p.getName(), null).setProperties(p));
                }
            })
            .map(this::wrapModel);
    }

    @Override
    protected Mono<KeyVaultSecret> getInnerAsync() {
        return vault.secretClient().getSecret(name(), null);
    }

    @Override
    public SecretImpl withTags(Map<String, String> tags) {
        this.inner().getProperties().setTags(tags);
        return this;
    }

    @Override
    public boolean isInCreateMode() {
        return id() == null;
    }

    @Override
    public Mono<Secret> createResourceAsync() {
        KeyVaultSecret newSecret = new KeyVaultSecret(this.name(), valueToSet);
        newSecret.setProperties(this.attributes());
        return vault
            .secretClient()
            .setSecret(newSecret)
            .map(
                inner -> {
                    this.setInner(inner);
                    valueToSet = null;
                    return this;
                });
    }

    @Override
    public Mono<Secret> updateResourceAsync() {
        if (valueToSet == null) {
            // if no update on value, just update properties
            return vault
                .secretClient()
                .updateSecretProperties(this.attributes())
                .map(
                    p -> {
                        this.inner().setProperties(p);
                        return this;
                    });
        } else {
            return this.createResourceAsync();
        }
    }

    @Override
    public SecretImpl withAttributes(SecretProperties attributes) {
        this.inner().setProperties(attributes);
        return this;
    }

    @Override
    public SecretImpl withValue(String value) {
        Objects.requireNonNull(value);
        valueToSet = value;
        return this;
    }

    @Override
    public SecretImpl withContentType(String contentType) {
        this.inner().getProperties().setContentType(contentType);
        return this;
    }
}
