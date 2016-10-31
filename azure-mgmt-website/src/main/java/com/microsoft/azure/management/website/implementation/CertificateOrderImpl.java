/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.website.AppServicePlan;
import com.microsoft.azure.management.website.CertificateOrder;
import com.microsoft.azure.management.website.CertificateOrderStatus;
import com.microsoft.azure.management.website.CertificateProductType;
import com.microsoft.azure.management.website.ProvisioningState;
import org.joda.time.DateTime;
import rx.Observable;
import rx.functions.Func1;

import java.util.Map;

/**
 * The implementation for {@link AppServicePlan}.
 */
class CertificateOrderImpl
        extends
        GroupableResourceImpl<
                CertificateOrder,
                AppServiceCertificateOrderInner,
                CertificateOrderImpl,
                AppServiceManager>
        implements
        CertificateOrder,
        CertificateOrder.Definition,
        CertificateOrder.Update {

    private final AppServiceCertificateOrdersInner client;
    String keyVaultId;

    CertificateOrderImpl(String key, AppServiceCertificateOrderInner innerObject, final AppServiceCertificateOrdersInner client, AppServiceManager manager) {
        super(key, innerObject, manager);
        this.client = client;
        this.withRegion("global");
    }

    @Override
    public CertificateOrder refresh() {
        this.setInner(client.get(resourceGroupName(), name()));
        return this;
    }

    @Override
    public Map<String, AppServiceCertificateInner> certificates() {
        return inner().certificates();
    }

    @Override
    public String distinguishedName() {
        return inner().distinguishedName();
    }

    @Override
    public String domainVerificationToken() {
        return inner().domainVerificationToken();
    }

    @Override
    public int validityInYears() {
        return inner().validityInYears();
    }

    @Override
    public int keySize() {
        return inner().keySize();
    }

    @Override
    public CertificateProductType productType() {
        return inner().productType();
    }

    @Override
    public boolean autoRenew() {
        return inner().autoRenew();
    }

    @Override
    public ProvisioningState provisioningState() {
        return inner().provisioningState();
    }

    @Override
    public CertificateOrderStatus status() {
        return inner().status();
    }

    @Override
    public CertificateDetailsImpl signedCertificate() {
        return new CertificateDetailsImpl(inner().signedCertificate());
    }

    @Override
    public String csr() {
        return inner().csr();
    }

    @Override
    public CertificateDetailsImpl intermediate() {
        return new CertificateDetailsImpl(inner().intermediate());
    }

    @Override
    public CertificateDetailsImpl root() {
        return new CertificateDetailsImpl(inner().root());
    }

    @Override
    public String serialNumber() {
        return null;
    }

    @Override
    public DateTime lastCertificateIssuanceTime() {
        return null;
    }

    @Override
    public DateTime expirationTime() {
        return null;
    }

    @Override
    public CertificateOrderImpl withHostName(String hostName) {
        inner().withDistinguishedName("CN=" + hostName);
        return this;
    }

    @Override
    public CertificateOrderImpl withSku(CertificateProductType sku) {
        inner().withProductType(sku);
        return this;
    }

    @Override
    public CertificateOrderImpl withValidYears(int years) {
        inner().withValidityInYears(years);
        return this;
    }

    @Override
    public Observable<CertificateOrder> createResourceAsync() {
        return client.createOrUpdateAsync(resourceGroupName(), name(), inner())
                .flatMap(new Func1<AppServiceCertificateOrderInner, Observable<AppServiceCertificateOrderInner>>() {
                    @Override
                    public Observable<AppServiceCertificateOrderInner> call(final AppServiceCertificateOrderInner appServiceCertificateOrderInner) {
                        AppServiceCertificateInner certificateInner = new AppServiceCertificateInner()
                                .withKeyVaultId(keyVaultId);
                        certificateInner.withKeyVaultSecretName(name() + "secret");
                        certificateInner.withLocation(regionName());
                        return client.createOrUpdateCertificateAsync(resourceGroupName(), name(), name(), certificateInner)
                                .map(new Func1<AppServiceCertificateInner, AppServiceCertificateOrderInner>() {
                                    @Override
                                    public AppServiceCertificateOrderInner call(AppServiceCertificateInner appServiceCertificateInner) {
                                        return appServiceCertificateOrderInner;
                                    }
                                });
                    }
                })
                .map(innerToFluentMap(this));
    }

    @Override
    public CertificateOrderImpl withExistingKeyVault(String keyVaultId) {
        this.keyVaultId = keyVaultId;
        return this;
    }
}