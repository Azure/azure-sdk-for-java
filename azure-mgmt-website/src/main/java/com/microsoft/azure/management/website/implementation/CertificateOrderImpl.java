/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.website.AppServicePlan;
import com.microsoft.azure.management.website.CertificateDetails;
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
                WebsiteManager>
    implements
        CertificateOrder,
        CertificateOrder.Definition,
        CertificateOrder.Update {

    private final CertificateOrdersInner client;

    CertificateOrderImpl(String key, CertificateOrderInner innerObject, final CertificateOrdersInner client, WebsiteManager manager) {
        super(key, innerObject, manager);
        this.client = client;
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
        return null;
    }

    @Override
    public String distinguishedName() {
        return null;
    }

    @Override
    public String domainVerificationToken() {
        return null;
    }

    @Override
    public Integer validityInYears() {
        return null;
    }

    @Override
    public Integer keySize() {
        return null;
    }

    @Override
    public CertificateProductType productType() {
        return null;
    }

    @Override
    public Boolean autoRenew() {
        return null;
    }

    @Override
    public ProvisioningState provisioningState() {
        return null;
    }

    @Override
    public CertificateOrderStatus status() {
        return null;
    }

    @Override
    public CertificateDetails signedCertificate() {
        return null;
    }

    @Override
    public String csr() {
        return null;
    }

    @Override
    public CertificateDetails intermediate() {
        return null;
    }

    @Override
    public CertificateDetails root() {
        return null;
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
    public CertificateOrder.DefinitionStages.WithCreate withHostName(String hostName) {
        return null;
    }
}
