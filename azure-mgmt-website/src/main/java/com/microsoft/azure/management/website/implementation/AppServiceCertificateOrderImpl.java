/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.website.AppServiceCertificateKeyVaultBinding;
import com.microsoft.azure.management.website.AppServiceCertificateOrder;
import com.microsoft.azure.management.website.AppServicePlan;
import com.microsoft.azure.management.website.CertificateOrderStatus;
import com.microsoft.azure.management.website.CertificateProductType;
import org.joda.time.DateTime;
import rx.Observable;
import rx.functions.Func1;

import java.util.HashMap;
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
    Map<String, AppServiceCertificateKeyVaultBinding> keyVaultBindings;

    AppServiceCertificateOrderImpl(String key, AppServiceCertificateOrderInner innerObject, final AppServiceCertificateOrdersInner client, AppServiceManager manager) {
        super(key, innerObject, manager);
        this.client = client;
        this.withRegion("global");
        keyVaultBindings = new HashMap<>();
        if (inner().certificates() != null) {
            for (Map.Entry<String, AppServiceCertificateInner> binding: inner().certificates().entrySet()) {
                keyVaultBindings.put(binding.getKey(), new AppServiceCertificateKeyVaultBindingImpl(binding.getValue(), this));
            }
        }
    }

    @Override
    public AppServiceCertificateOrder refresh() {
        this.setInner(client.get(resourceGroupName(), name()));
        if (inner().certificates() != null) {
            for (Map.Entry<String, AppServiceCertificateInner> binding: inner().certificates().entrySet()) {
                keyVaultBindings.put(binding.getKey(), new AppServiceCertificateKeyVaultBindingImpl(binding.getValue(), this));
            }
        }
        return this;
    }

    @Override
    public Map<String, AppServiceCertificateKeyVaultBinding> keyVaultBindings() {
        return keyVaultBindings;
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
        return Utils.toPrimitiveBoolean(inner().autoRenew());
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
    public AppServiceCertificateKeyVaultBinding createKeyVaultBinding(String certificateName, Vault vault) {
        return createKeyVaultBindingAsync(certificateName, vault).toBlocking().single();
    }

    @Override
    public Observable<AppServiceCertificateKeyVaultBinding> createKeyVaultBindingAsync(String certificateName, Vault vault) {
        AppServiceCertificateInner certInner = new AppServiceCertificateInner();
        certInner.withLocation(vault.regionName());
        certInner.withKeyVaultId(vault.id());
        certInner.withKeyVaultSecretName(certificateName);
        final AppServiceCertificateOrderImpl self = this;
        return client.createOrUpdateCertificateAsync(resourceGroupName(), name(), certificateName, certInner)
                .map(new Func1<AppServiceCertificateInner, AppServiceCertificateKeyVaultBinding>() {
                    @Override
                    public AppServiceCertificateKeyVaultBinding call(AppServiceCertificateInner appServiceCertificateInner) {
                        return new AppServiceCertificateKeyVaultBindingImpl(appServiceCertificateInner, self);
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

    @Override
    public AppServiceCertificateOrderImpl withAutoRenew(boolean enabled) {
        inner().withAutoRenew(enabled);
        return this;
    }
}