/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.website.AzureResourceType;
import com.microsoft.azure.management.website.CustomHostNameDnsRecordType;
import com.microsoft.azure.management.website.HostNameBinding;
import com.microsoft.azure.management.website.HostNameType;
import com.microsoft.azure.management.website.WebApp;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;
import rx.functions.Func1;

import java.util.Map;

/**
 * A host name binding object.
 */
class HostNameBindingImpl
        extends ExternalChildResourceImpl<HostNameBinding, HostNameBindingInner, WebAppImpl, WebApp>
        implements
        Creatable<HostNameBinding>,
        HostNameBinding,
        HostNameBinding.Definition<WebApp.Update> {
    private WebAppsInner client;
    HostNameBindingImpl(String name, HostNameBindingInner innerObject, WebAppImpl parent, WebAppsInner client) {
        super(name, parent, innerObject);
        this.client = client;
    }

    @Override
    public String id() {
        return inner().id();
    }

    @Override
    public String type() {
        return inner().type();
    }

    @Override
    public String regionName() {
        return inner().location();
    }

    @Override
    public Region region() {
        return Region.fromName(inner().location());
    }

    @Override
    public Map<String, String> tags() {
        return inner().getTags();
    }

    @Override
    public String hostNameBindingName() {
        return inner().hostNameBindingName();
    }

    @Override
    public String siteName() {
        return inner().siteName();
    }

    @Override
    public String domainId() {
        return inner().domainId();
    }

    @Override
    public String azureResourceName() {
        return inner().azureResourceName();
    }

    @Override
    public AzureResourceType azureResourceType() {
        return inner().azureResourceType();
    }

    @Override
    public CustomHostNameDnsRecordType customHostNameDnsRecordType() {
        return inner().customHostNameDnsRecordType();
    }

    @Override
    public HostNameType hostNameType() {
        return inner().hostNameType();
    }

    @Override
    public WebAppImpl attach() {
        parent().withHostNameBinding(this);
        return parent();
    }

    @Override
    public HostNameBindingImpl withHostNameType(HostNameType hostNameType) {
        inner().withHostNameType(hostNameType);
        return this;
    }

    @Override
    public HostNameBindingImpl withHostNameDnsRecordType(CustomHostNameDnsRecordType hostNameDnsRecordType) {
        inner().withCustomHostNameDnsRecordType(hostNameDnsRecordType);
        return this;
    }

    @Override
    public HostNameBindingImpl refresh() {
        this.setInner(client.getHostNameBinding(parent().resourceGroupName(), parent().name(), name()));
        return this;
    }

    @Override
    public HostNameBinding create() {
        createAsync().toBlocking().subscribe();
        return this;
    }

    @Override
    public ServiceCall<HostNameBinding> createAsync(ServiceCallback<HostNameBinding> callback) {
        return ServiceCall.create(createAsync().map(new Func1<HostNameBinding, ServiceResponse<HostNameBinding>>() {
            @Override
            public ServiceResponse<HostNameBinding> call(HostNameBinding hostNameBinding) {
                return new ServiceResponse<>(hostNameBinding, null);
            }
        }), callback);
    }

    @Override
    public Observable<HostNameBinding> createAsync() {
        final HostNameBinding self = this;
        return client.createOrUpdateHostNameBindingAsync(parent().resourceGroupName(), parent().name(), name(), inner())
                .map(new Func1<HostNameBindingInner, HostNameBinding>() {
                    @Override
                    public HostNameBinding call(HostNameBindingInner hostNameBindingInner) {
                        setInner(hostNameBindingInner);
                        return self;
                    }
                });
    }

    @Override
    public Observable<HostNameBinding> updateAsync() {
        return null;
    }

    @Override
    public Observable<Void> deleteAsync() {
        return null;
    }
}