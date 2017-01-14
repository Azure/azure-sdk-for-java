/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.appservice.AppServiceDomain;
import com.microsoft.azure.management.appservice.AzureResourceType;
import com.microsoft.azure.management.appservice.CustomHostNameDnsRecordType;
import com.microsoft.azure.management.appservice.DeploymentSlot;
import com.microsoft.azure.management.appservice.HostNameBinding;
import com.microsoft.azure.management.appservice.HostNameType;
import com.microsoft.azure.management.appservice.WebAppBase;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import rx.Observable;
import rx.functions.Func1;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Implementation for {@link HostNameBinding} and its create and update interfaces.
 *  @param <FluentT> the fluent interface of the parent web app
 *  @param <FluentImplT> the fluent implementation of the parent web app
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
class HostNameBindingImpl<
            FluentT extends WebAppBase,
            FluentImplT extends WebAppBaseImpl<FluentT, FluentImplT>>
        extends
            IndexableWrapperImpl<HostNameBindingInner>
        implements
            Creatable<HostNameBinding>,
            HostNameBinding,
            HostNameBinding.Definition<WebAppBase.DefinitionStages.WithHostNameSslBinding<FluentT>>,
            HostNameBinding.UpdateDefinition<WebAppBase.Update<FluentT>> {
    private WebAppsInner client;
    private final FluentImplT parent;
    private String domainName;
    private String name;

    HostNameBindingImpl(HostNameBindingInner innerObject, FluentImplT parent, WebAppsInner client) {
        super(innerObject);
        this.parent = parent;
        this.client = client;
        this.name = innerObject.name();
        if (name != null && name.contains("/")) {
            this.name = name.replace(parent.name() + "/", "");
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
    public CustomHostNameDnsRecordType dnsRecordType() {
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
        Matcher matcher = pattern.matcher(name);
        if (hostNameDnsRecordType == CustomHostNameDnsRecordType.CNAME && !matcher.matches()) {
            throw new IllegalArgumentException("root hostname cannot be assigned with a CName record");
        }
        inner().withCustomHostNameDnsRecordType(hostNameDnsRecordType);
        return this;
    }

    @Override
    public HostNameBindingImpl<FluentT, FluentImplT> refresh() {
        if (parent instanceof DeploymentSlot) {
            this.setInner(client.getHostNameBindingSlot(parent().resourceGroupName(), ((DeploymentSlot) parent).parent().name(), parent().name(), name()));
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
        Observable<Indexable> indexableObservable = createAsync();
        return ServiceCall.fromBody(Utils.<HostNameBinding>rootResource(indexableObservable), callback);
    }

    @Override
    public Observable<Indexable> createAsync() {
        final HostNameBinding self = this;
        Func1<HostNameBindingInner, HostNameBinding> mapper = new Func1<HostNameBindingInner, HostNameBinding>() {
            @Override
            public HostNameBinding call(HostNameBindingInner hostNameBindingInner) {
                setInner(hostNameBindingInner);
                return self;
            }
        };

        Observable<HostNameBinding> hostNameBindingObservable;
        if (parent instanceof DeploymentSlot) {
            hostNameBindingObservable = client.createOrUpdateHostNameBindingSlotAsync(parent().resourceGroupName(),
                    ((DeploymentSlot) parent).parent().name(),
                    name,
                    parent().name(), inner()).map(mapper);
        } else {
            hostNameBindingObservable = client.createOrUpdateHostNameBindingAsync(parent().resourceGroupName(), parent().name(), name, inner()).map(mapper);
        }

        return hostNameBindingObservable.map(new Func1<HostNameBinding, Indexable>() {
            @Override
            public Indexable call(HostNameBinding hostNameBinding) {
                return hostNameBinding;
            }
        });
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
        this.domainName = domain.name();
        return this;
    }

    @Override
    public HostNameBindingImpl<FluentT, FluentImplT> withThirdPartyDomain(String domain) {
        inner().withHostNameType(HostNameType.VERIFIED);
        this.domainName = domain;
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
        return name + ": " + dnsRecordType() + " " + azureResourceName() + suffix;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public WebAppBase parent() {
        return parent;
    }

    @Override
    public HostNameBindingImpl<FluentT, FluentImplT> withSubDomain(String subDomain) {
        this.name = normalizeHostNameBindingName(subDomain, domainName);
        return this;
    }
}