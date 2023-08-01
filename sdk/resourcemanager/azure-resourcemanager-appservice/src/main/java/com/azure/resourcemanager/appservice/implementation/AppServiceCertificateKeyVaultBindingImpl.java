// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.models.AppServiceCertificateKeyVaultBinding;
import com.azure.resourcemanager.appservice.models.AppServiceCertificateOrder;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.KeyVaultSecretStatus;
import com.azure.resourcemanager.appservice.fluent.models.AppServiceCertificateResourceInner;
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
        return innerModel().id();
    }

    @Override
    public Mono<AppServiceCertificateKeyVaultBinding> createChildResourceAsync() {
        final AppServiceCertificateKeyVaultBinding self = this;
        return parent
            .manager()
            .serviceClient()
            .getAppServiceCertificateOrders()
            .createOrUpdateCertificateAsync(parent.resourceGroupName(), parent.name(), name(), innerModel())
            .map(
                appServiceCertificateInner -> {
                    setInner(appServiceCertificateInner);
                    return self;
                });
    }

    @Override
    public String keyVaultId() {
        return innerModel().keyVaultId();
    }

    @Override
    public String keyVaultSecretName() {
        return innerModel().keyVaultSecretName();
    }

    @Override
    public KeyVaultSecretStatus provisioningState() {
        return innerModel().provisioningState();
    }

    @Override
    protected Mono<AppServiceCertificateResourceInner> getInnerAsync() {
        return parent
            .manager()
            .serviceClient()
            .getAppServiceCertificateOrders()
            .getCertificateAsync(parent.resourceGroupName(), parent.name(), name());
    }
}
