/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.microsoft.azure.management.website.AzureResourceType;
import com.microsoft.azure.management.website.CustomHostNameDnsRecordType;
import com.microsoft.azure.management.website.DeploymentSlot;
import com.microsoft.azure.management.website.AppServiceDomain;
import com.microsoft.azure.management.website.HostNameBinding;
import com.microsoft.azure.management.website.HostNameType;
import com.microsoft.azure.management.website.WebAppBase;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;
import rx.functions.Func1;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Implementation for {@link HostNameBinding} and its create and update interfaces.
 */
class HostNameBindingImpl<
            FluentT extends WebAppBase<FluentT>,
            FluentImplT extends WebAppBaseImpl<FluentT, FluentImplT>>
        extends
            IndexableWrapperImpl<HostNameBindingInner>
        implements
            Creatable<HostNameBinding>,
            HostNameBinding,
            HostNameBinding.Definition<WebAppBase.DefinitionStages.WithHostNameSslBinding<FluentT>>,
            HostNameBinding.UpdateDefinition<WebAppBase.Update<FluentT>> {
    private WebAppsInner client;
    private String fqdn;
    private final FluentImplT parent;
    private final String name;

    HostNameBindingImpl(String name, HostNameBindingInner innerObject, FluentImplT parent, WebAppsInner client) {
        super(innerObject);
        this.name = name;
        this.parent = parent;
        this.client = client;
        this.fqdn = name;
        if (name.contains("/")) {
            this.fqdn = name.replace(parent.name() + "/", "");
        }
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
    public String webAppName() {
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
    public CustomHostNameDnsRecordType DnsRecordType() {
        return inner().customHostNameDnsRecordType();
    }

    @Override
    public HostNameType hostNameType() {
        return inner().hostNameType();
    }

    @Override
    public FluentImplT attach() {
        parent.withHostNameBinding(this);
        return parent;
    }

    @Override
    public HostNameBindingImpl<FluentT, FluentImplT> withDnsRecordType(CustomHostNameDnsRecordType hostNameDnsRecordType) {
        Pattern pattern = Pattern.compile("([.\\w-]+)\\.([\\w-]+\\.\\w+)");
        Matcher matcher = pattern.matcher(fqdn);
        if (hostNameDnsRecordType == CustomHostNameDnsRecordType.CNAME && !matcher.matches()) {
            throw new IllegalArgumentException("root hostname cannot be assigned with a CName record");
        }
        inner().withCustomHostNameDnsRecordType(hostNameDnsRecordType);
        return this;
    }

    @Override
    public HostNameBindingImpl<FluentT, FluentImplT> refresh() {
        if (parent instanceof DeploymentSlot) {
            this.setInner(client.getHostNameBindingSlot(parent().resourceGroupName(), ((DeploymentSlot)parent).parent().name(), parent().name(), name()));
        } else {
            this.setInner(client.getHostNameBinding(parent().resourceGroupName(), parent().name(), name()));
        }
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
        Func1<HostNameBindingInner, HostNameBinding> mapper = new Func1<HostNameBindingInner, HostNameBinding>() {
            @Override
            public HostNameBinding call(HostNameBindingInner hostNameBindingInner) {
                setInner(hostNameBindingInner);
                return self;
            }
        };
        if (parent instanceof DeploymentSlot) {
            return client.createOrUpdateHostNameBindingSlotAsync(parent().resourceGroupName(), ((DeploymentSlot) parent).parent().name(), fqdn, parent().name(), inner()).map(mapper);
        } else {
            return client.createOrUpdateHostNameBindingAsync(parent().resourceGroupName(), parent().name(), fqdn, inner()).map(mapper);
        }
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
    public HostNameBindingImpl<FluentT, FluentImplT> withAzureManagedDomain(AppServiceDomain domain) {
        inner().withDomainId(domain.id());
        inner().withHostNameType(HostNameType.MANAGED);
        this.fqdn = normalizeHostNameBindingName(name(), domain.name());
        return this;
    }

    @Override
    public HostNameBindingImpl<FluentT, FluentImplT> withThirdPartyDomain(String domain) {
        inner().withHostNameType(HostNameType.VERIFIED);
        this.fqdn = normalizeHostNameBindingName(name(), domain);
        return this;
    }

    @Override
    public String toString() {
        String suffix;
        if (azureResourceType() == AzureResourceType.TRAFFIC_MANAGER) {
            suffix = ".trafficmanager.net";
        } else {
            suffix = ".azurewebsites.net";
        }
        return fqdn + ": " + DnsRecordType() + " " + azureResourceName() + suffix;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public WebAppBase<FluentT> parent() {
        return parent;
    }
}