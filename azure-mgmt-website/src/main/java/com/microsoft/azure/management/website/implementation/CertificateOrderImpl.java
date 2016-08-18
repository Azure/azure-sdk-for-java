/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.website.AppServicePlan;
import com.microsoft.azure.management.website.CertificateOrder;
import com.microsoft.azure.management.website.CertificateOrderStatus;
import com.microsoft.azure.management.website.CertificateProductType;
import com.microsoft.azure.management.website.ProvisioningState;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import org.joda.time.DateTime;

import java.util.Map;

/**
 * The implementation for {@link AppServicePlan}.
 */
class CertificateOrderImpl
    extends
        GroupableResourceImpl<
                CertificateOrder,
                CertificateOrderInner,
                CertificateOrderImpl,
                AppServiceManager>
    implements
        CertificateOrder,
        CertificateOrder.Definition,
        CertificateOrder.Update {

    private final CertificateOrdersInner client;

    CertificateOrderImpl(String key, CertificateOrderInner innerObject, final CertificateOrdersInner client, AppServiceManager manager) {
        super(key, innerObject, manager);
        this.client = client;
        this.withRegion("global");
    }

    @Override
    protected void createResource() throws Exception {
        this.setInner(client.createOrUpdateCertificateOrder(resourceGroupName(), name(), inner()).getBody());
    }

    @Override
    protected ServiceCall createResourceAsync(ServiceCallback<Void> callback) {
        return client.createOrUpdateCertificateOrderAsync(resourceGroupName(), name(), inner(), Utils.fromVoidCallback(this, callback));
    }

    @Override
    public CertificateOrder refresh() throws Exception {
        this.setInner(client.getCertificateOrder(resourceGroupName(), name()).getBody());
        return this;
    }

    @Override
    public Map<String, CertificateOrderCertificateInner> certificates() {
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
        return new CertificateDetailsImpl(inner().signedCertificate().name(), inner().signedCertificate());
    }

    @Override
    public String csr() {
        return inner().csr();
    }

    @Override
    public CertificateDetailsImpl intermediate() {
        return new CertificateDetailsImpl(inner().intermediate().name(), inner().intermediate());
    }

    @Override
    public CertificateDetailsImpl root() {
        return new CertificateDetailsImpl(inner().root().name(), inner().root());
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
}
