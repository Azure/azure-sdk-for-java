/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice.implementation;

import com.azure.management.appservice.AppServiceCertificateKeyVaultBinding;
import com.azure.management.appservice.AppServiceCertificateOrder;
import com.azure.management.appservice.AppServicePlan;
import com.azure.management.appservice.KeyVaultSecretStatus;
import com.azure.management.appservice.models.AppServiceCertificateResourceInner;
import com.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import reactor.core.publisher.Mono;

/**
 * The implementation for {@link AppServicePlan}.
 */
class AppServiceCertificateKeyVaultBindingImpl
        extends
        IndependentChildResourceImpl<
                AppServiceCertificateKeyVaultBinding,
                AppServiceCertificateOrder,
                AppServiceCertificateResourceInner,
                AppServiceCertificateKeyVaultBindingImpl,
                AppServiceManager>
        implements
        AppServiceCertificateKeyVaultBinding {

    private final AppServiceCertificateOrderImpl parent;

    AppServiceCertificateKeyVaultBindingImpl(AppServiceCertificateResourceInner innerObject, AppServiceCertificateOrderImpl parent) {
        super(innerObject.getName(), innerObject, (parent != null) ? parent.manager() : null);
        this.parent = parent;
    }

    @Override
    public String id() {
        return inner().getId();
    }

    @Override
    public Mono<AppServiceCertificateKeyVaultBinding> createChildResourceAsync() {
        final AppServiceCertificateKeyVaultBinding self = this;
        return parent.manager().inner().appServiceCertificateOrders().createOrUpdateCertificateAsync(
                parent.resourceGroupName(), parent.name(), name(), inner())
                .map(appServiceCertificateInner -> {
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
        return parent.manager().inner().appServiceCertificateOrders().getCertificateAsync(
                parent.resourceGroupName(), parent.name(), name());
    }
}