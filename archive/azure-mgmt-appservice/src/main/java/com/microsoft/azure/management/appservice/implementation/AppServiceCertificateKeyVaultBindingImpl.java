/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.microsoft.azure.management.appservice.AppServiceCertificateKeyVaultBinding;
import com.microsoft.azure.management.appservice.AppServiceCertificateOrder;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.KeyVaultSecretStatus;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation for {@link AppServicePlan}.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
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
        super(innerObject.name(), innerObject, (parent != null) ? parent.manager() : null);
        this.parent = parent;
    }

    @Override
    public String id() {
        return inner().id();
    }

    @Override
    public Observable<AppServiceCertificateKeyVaultBinding> createChildResourceAsync() {
        final AppServiceCertificateKeyVaultBinding self = this;
        return parent.manager().inner().appServiceCertificateOrders().createOrUpdateCertificateAsync(
                parent.resourceGroupName(), parent.name(), name(), inner())
                .map(new Func1<AppServiceCertificateResourceInner, AppServiceCertificateKeyVaultBinding>() {
                    @Override
                    public AppServiceCertificateKeyVaultBinding call(AppServiceCertificateResourceInner appServiceCertificateInner) {
                        setInner(appServiceCertificateInner);
                        return self;
                    }
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
    protected Observable<AppServiceCertificateResourceInner> getInnerAsync() {
        return parent.manager().inner().appServiceCertificateOrders().getCertificateAsync(
                parent.resourceGroupName(), parent.name(), name());
    }
}