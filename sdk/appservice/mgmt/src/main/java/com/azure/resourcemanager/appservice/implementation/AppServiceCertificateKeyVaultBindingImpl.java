// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.models.AppServiceCertificateKeyVaultBinding;
import com.azure.resourcemanager.appservice.models.AppServiceCertificateOrder;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.KeyVaultSecretStatus;
import com.azure.resourcemanager.appservice.fluent.inner.AppServiceCertificateResourceInner;
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
            .getAppServiceCertificateOrders()
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
            .getAppServiceCertificateOrders()
            .getCertificateAsync(parent.resourceGroupName(), parent.name(), name());
    }
}
