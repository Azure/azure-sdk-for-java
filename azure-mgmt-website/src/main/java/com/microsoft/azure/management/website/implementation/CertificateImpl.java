/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.website.AppServicePlan;
import com.microsoft.azure.management.website.Certificate;
import com.microsoft.azure.management.website.HostingEnvironmentProfile;
import org.joda.time.DateTime;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

/**
 * The implementation for {@link AppServicePlan}.
 */
class CertificateImpl
        extends
        GroupableResourceImpl<
                Certificate,
                CertificateInner,
                CertificateImpl,
                AppServiceManager>
        implements
        Certificate,
        Certificate.Definition,
        Certificate.Update {

    private final CertificatesInner client;

    CertificateImpl(String key, CertificateInner innerObject, final CertificatesInner client, AppServiceManager manager) {
        super(key, innerObject, manager);
        this.client = client;
    }

    @Override
    public String friendlyName() {
        return inner().friendlyName();
    }

    @Override
    public String subjectName() {
        return inner().subjectName();
    }

    @Override
    public List<String> hostNames() {
        return inner().hostNames();
    }

    @Override
    public String pfxBlob() {
        return inner().pfxBlob();
    }

    @Override
    public String siteName() {
        return inner().siteName();
    }

    @Override
    public String selfLink() {
        return inner().selfLink();
    }

    @Override
    public String issuer() {
        return inner().issuer();
    }

    @Override
    public DateTime issueDate() {
        return inner().issueDate();
    }

    @Override
    public DateTime expirationDate() {
        return inner().expirationDate();
    }

    @Override
    public String password() {
        return inner().password();
    }

    @Override
    public String thumbprint() {
        return inner().thumbprint();
    }

    @Override
    public Boolean valid() {
        return inner().valid();
    }

    @Override
    public String cerBlob() {
        return inner().cerBlob();
    }

    @Override
    public String publicKeyHash() {
        return inner().publicKeyHash();
    }

    @Override
    public HostingEnvironmentProfile hostingEnvironmentProfile() {
        return inner().hostingEnvironmentProfile();
    }

    @Override
    public Certificate refresh() {
        this.setInner(client.get(resourceGroupName(), name()));
        return this;
    }

    @Override
    public CertificateImpl withHostName(String hostName) {
        if (inner().hostNames() == null) {
            inner().withHostNames(new ArrayList<String>());
        }
        inner().hostNames().add(hostName);
        return this;
    }

    @Override
    public Observable<Certificate> createResourceAsync() {
        return client.createOrUpdateAsync(resourceGroupName(), name(), inner())
                .map(innerToFluentMap(this));
    }
}