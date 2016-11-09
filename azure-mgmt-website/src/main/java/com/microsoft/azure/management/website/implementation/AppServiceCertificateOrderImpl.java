/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.website.AppServiceCertificate;
import com.microsoft.azure.management.website.AppServicePlan;
import com.microsoft.azure.management.website.AppServiceCertificateOrder;
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
class AppServiceCertificateOrderImpl
        extends
        GroupableResourceImpl<
                AppServiceCertificateOrder,
                AppServiceCertificateOrderInner,
                AppServiceCertificateOrderImpl,
                AppServiceManager>
        implements
        AppServiceCertificateOrder,
        AppServiceCertificateOrder.Definition,
        AppServiceCertificateOrder.Update {

    final AppServiceCertificateOrdersInner client;

    AppServiceCertificateOrderImpl(String key, AppServiceCertificateOrderInner innerObject, final AppServiceCertificateOrdersInner client, AppServiceManager manager) {
        super(key, innerObject, manager);
        this.client = client;
        this.withRegion("global");
    }

    @Override
    public AppServiceCertificateOrder refresh() {
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
    public AppServiceCertificate createCertificate(String certificateName, Vault vault) {
        return createCertificateAsync(certificateName, vault).toBlocking().single();
    }

    @Override
    public Observable<AppServiceCertificate> createCertificateAsync(String certificateName, Vault vault) {
        AppServiceCertificateInner certInner = new AppServiceCertificateInner();
        certInner.withLocation(vault.regionName());
        certInner.withKeyVaultId(vault.id());
        certInner.withKeyVaultSecretName(certificateName.replace("_", ""));
        final AppServiceCertificateOrderImpl self = this;
        return client.beginCreateOrUpdateCertificateAsync(resourceGroupName(), name(), certificateName, certInner)
                .map(new Func1<AppServiceCertificateInner, AppServiceCertificate>() {
                    @Override
                    public AppServiceCertificate call(AppServiceCertificateInner appServiceCertificateInner) {
                        return new AppServiceCertificateImpl(appServiceCertificateInner, self);
                    }
                });
    }

    @Override
    public AppServiceCertificateOrderImpl withHostName(String hostName) {
        inner().withDistinguishedName("CN=" + hostName);
        return this;
    }

    @Override
    public AppServiceCertificateOrderImpl withSku(CertificateProductType sku) {
        inner().withProductType(sku);
        return this;
    }

    @Override
    public AppServiceCertificateOrderImpl withValidYears(int years) {
        inner().withValidityInYears(years);
        return this;
    }

    @Override
    public Observable<AppServiceCertificateOrder> createResourceAsync() {
        return client.createOrUpdateAsync(resourceGroupName(), name(), inner())
                .map(innerToFluentMap(this));
    }
}