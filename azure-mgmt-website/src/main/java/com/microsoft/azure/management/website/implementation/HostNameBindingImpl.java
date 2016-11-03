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
import com.microsoft.azure.management.website.Domain;
import com.microsoft.azure.management.website.HostNameBinding;
import com.microsoft.azure.management.website.HostNameType;
import com.microsoft.azure.management.website.WebApp;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;
import rx.functions.Func1;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A host name binding object.
 */
class HostNameBindingImpl
        extends ExternalChildResourceImpl<HostNameBinding,
                HostNameBindingInner,
                WebAppImpl,
                WebApp>
        implements
        Creatable<HostNameBinding>,
        HostNameBinding,
        HostNameBinding.Definition<WebApp.DefinitionStages.WithHostNameBinding>,
        HostNameBinding.UpdateDefinition<WebApp.Update> {
    private WebAppsInner client;
    private String fqdn;

    HostNameBindingImpl(String name, HostNameBindingInner innerObject, WebAppImpl parent, WebAppsInner client) {
        super(name, parent, innerObject);
        this.client = client;
        this.fqdn = name;
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
    public String hostName() {
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
    public HostNameBindingImpl withDnsRecordType(CustomHostNameDnsRecordType hostNameDnsRecordType) {
        Pattern pattern = Pattern.compile("([.\\w-]+)\\.([\\w-]+\\.\\w+)");
        Matcher matcher = pattern.matcher(fqdn);
        if (hostNameDnsRecordType == CustomHostNameDnsRecordType.CNAME && !matcher.matches()) {
            throw new IllegalArgumentException("root hostname cannot be assigned with a CName record");
        }
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
        return client.createOrUpdateHostNameBindingAsync(parent().resourceGroupName(), parent().name(), fqdn, inner())
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

    private String normalizeHostNameBindingName(String hostname, String domainName) {
        if (!hostname.endsWith(domainName)) {
            hostname = hostname + "." + domainName;
        }
        if (hostname.startsWith("@")) {
            hostname = hostname.replace("@.", "");
        }
        return hostname;
    }

    @Override
    public HostNameBindingImpl withAzureManagedDomain(Domain domain) {
        inner().withDomainId(domain.id());
        inner().withHostNameType(HostNameType.MANAGED);
        this.fqdn = normalizeHostNameBindingName(name(), domain.name());
        return this;
    }

    @Override
    public HostNameBindingImpl withThirdPartyDomain(String domain) {
        inner().withHostNameType(HostNameType.VERIFIED);
        this.fqdn = normalizeHostNameBindingName(name(), domain);
        return this;
    }
}