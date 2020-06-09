// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.AppServiceCertificateKeyVaultBinding;
import com.azure.resourcemanager.appservice.AppServiceCertificateOrder;
import com.azure.resourcemanager.appservice.AppServicePlan;
import com.azure.resourcemanager.appservice.KeyVaultSecretStatus;
import com.azure.resourcemanager.appservice.models.AppServiceCertificateResourceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import reactor.core.publisher.Mono;

/** The implementation for {@link AppServicePlan}. */
class AppServiceCertificateKeyVaultBindingImpl
    extends IndependentChildResourceImpl<
        AppServiceCertificateKeyVaultBinding,
        AppServiceCertificateOrder,
        AppServiceCertificateResourceInner,
        AppServiceCertificateKeyVaultBindingImpl,
        AppServiceManager>
    implements AppServiceCertificateKeyVaultBinding {

    private final AppServiceCertificateOrderImpl parent;

    AppServiceCertificateKeyVaultBindingImpl(
        AppServiceCertificateResourceInner innerObject, AppServiceCertificateOrderImpl parent) {
        super(innerObject.name(), innerObject, (parent != null) ? parent.manager() : null);
        this.parent = parent;
    }

    @Override
    public String id() {
        return inner().id();
    }

    @Override
    public Mono<AppServiceCertificateKeyVaultBinding> createChildResourceAsync() {
        final AppServiceCertificateKeyVaultBinding self = this;
        return parent
            .manager()
            .inner()
            .appServiceCertificateOrders()
            .createOrUpdateCertificateAsync(parent.resourceGroupName(), parent.name(), name(), inner())
            .map(
                appServiceCertificateInner -> {
                    setInner(appServiceCertificateInner);
                    return self;
                });
    }

    @Override
    public String keyVaultId() {
        return inner().keyVaultId();
    }

    @Override
    public String keyVaultSecretName() {
        return inner().keyVaultSecretName();
    }

    @Override
    public KeyVaultSecretStatus provisioningState() {
        return inner().provisioningState();
    }

    @Override
    protected Mono<AppServiceCertificateResourceInner> getInnerAsync() {
        return parent
            .manager()
            .inner()
            .appServiceCertificateOrders()
            .getCertificateAsync(parent.resourceGroupName(), parent.name(), name());
    }
}
