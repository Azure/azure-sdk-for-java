// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.keyvault.models.Secret;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import java.util.Map;
import java.util.Objects;
import reactor.core.publisher.Mono;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** Implementation for Vault and its parent interfaces. */
class SecretImpl extends CreatableUpdatableImpl<Secret, SecretProperties, SecretImpl>
    implements Secret, Secret.Definition, Secret.Update {

    private final Vault vault;

    private String secretValueToSet;

    private String secretValue;

    SecretImpl(String name, SecretProperties innerObject, Vault vault) {
        super(name, innerObject);
        this.vault = vault;
    }

    SecretImpl(String name, KeyVaultSecret keyVaultSecret, Vault vault) {
        super(name, keyVaultSecret.getProperties());
        this.secretValue = keyVaultSecret.getValue();
        this.vault = vault;
    }

    private SecretImpl wrapModel(SecretProperties secret) {
        return new SecretImpl(secret.getName(), secret, vault);
    }

    @Override
    public String id() {
        return innerModel().getId();
    }

    @Override
    public String getValue() {
        return getValueAsync().block();
    }

    @Override
    public Mono<String> getValueAsync() {
        if (secretValue != null) {
            return Mono.just(secretValue);
        } else {
            return getInnerAsync().map(ignored -> secretValue);
        }
    }

    @Override
    public SecretProperties attributes() {
        return innerModel();
    }

    @Override
    public Map<String, String> tags() {
        return innerModel().getTags();
    }

    @Override
    public String contentType() {
        return innerModel().getContentType();
    }

    @Override
    public String kid() {
        return innerModel().getKeyId();
    }

    @Override
    public boolean managed() {
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().isManaged());
    }

    @Override
    public boolean enabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().isEnabled());
    }

    @Override
    public PagedIterable<Secret> listVersions() {
        return new PagedIterable<>(this.listVersionsAsync());
    }

    @Override
    public PagedFlux<Secret> listVersionsAsync() {
        return PagedConverter.mapPage(vault
            .secretClient()
            .listPropertiesOfSecretVersions(name()),
            this::wrapModel);
    }

    @Override
    protected Mono<SecretProperties> getInnerAsync() {
        return vault.secretClient().getSecret(name(), innerModel().getVersion()).map(secret -> {
            this.secretValue = secret.getValue();
            return secret.getProperties();
        });
    }

    @Override
    public SecretImpl withTags(Map<String, String> tags) {
        this.innerModel().setTags(tags);
        return this;
    }

    @Override
    public boolean isInCreateMode() {
        return id() == null;
    }

    @Override
    public Mono<Secret> createResourceAsync() {
        KeyVaultSecret newSecret = new KeyVaultSecret(this.name(), secretValueToSet);
        newSecret.setProperties(this.attributes());
        return vault
            .secretClient()
            .setSecret(newSecret)
            .map(
                keyVaultSecret -> {
                    this.setInner(keyVaultSecret.getProperties());
                    this.secretValue = keyVaultSecret.getValue();
                    secretValueToSet = null;
                    return this;
                });
    }

    @Override
    public Mono<Secret> updateResourceAsync() {
        if (secretValueToSet == null) {
            // if no update on value, just update properties
            return vault
                .secretClient()
                .updateSecretProperties(this.innerModel())
                .map(
                    p -> {
                        this.setInner(p);
                        if (!p.isEnabled()) {
                            secretValue = null;
                        }
                        return this;
                    });
        } else {
            return this.createResourceAsync();
        }
    }

    @Override
    public SecretImpl withAttributes(SecretProperties attributes) {
        this.setInner(attributes);
        return this;
    }

    @Override
    public SecretImpl withValue(String value) {
        Objects.requireNonNull(value);
        secretValueToSet = value;
        return this;
    }

    @Override
    public SecretImpl withContentType(String contentType) {
        this.innerModel().setContentType(contentType);
        return this;
    }
}
