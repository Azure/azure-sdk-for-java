/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.microsoft.azure.management.website.AppServiceCertificate;
import com.microsoft.azure.management.website.AppServiceCertificateOrder;
import com.microsoft.azure.management.website.AppServicePlan;
import com.microsoft.azure.management.website.KeyVaultSecretStatus;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation for {@link AppServicePlan}.
 */
class AppServiceCertificateImpl
        extends
        IndependentChildResourceImpl<
                AppServiceCertificate,
                AppServiceCertificateOrder,
                AppServiceCertificateInner,
                AppServiceCertificateImpl>
        implements
                AppServiceCertificate {

    private final AppServiceCertificateOrdersInner innerCollection;
    private final AppServiceCertificateOrderImpl parent;

    AppServiceCertificateImpl(AppServiceCertificateInner innerObject, AppServiceCertificateOrderImpl parent) {
        super(innerObject.name(), innerObject);
        this.parent = parent;
        innerCollection = parent.client;
    }

    @Override
    public String id() {
        return inner().id();
    }

    @Override
    public Observable<AppServiceCertificate> createChildResourceAsync() {
        final AppServiceCertificate self = this;
        return innerCollection.createOrUpdateCertificateAsync(parent.resourceGroupName(), parent.name(), name(), inner())
                .map(new Func1<AppServiceCertificateInner, AppServiceCertificate>() {
                    @Override
                    public AppServiceCertificate call(AppServiceCertificateInner appServiceCertificateInner) {
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
    public AppServiceCertificate refresh() {
        setInner(innerCollection.getCertificate(parent.resourceGroupName(), parent.name(), name()));
        return this;
    }
}