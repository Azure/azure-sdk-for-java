/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice.implementation;

import com.azure.management.appservice.AppServiceDomain;
import com.azure.management.appservice.AzureResourceType;
import com.azure.management.appservice.CustomHostNameDnsRecordType;
import com.azure.management.appservice.DeploymentSlot;
import com.azure.management.appservice.HostNameBinding;
import com.azure.management.appservice.HostNameType;
import com.azure.management.appservice.WebAppBase;
import com.azure.management.appservice.models.HostNameBindingInner;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.model.Creatable;
import com.azure.management.resources.fluentcore.model.Indexable;
import com.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.azure.management.resources.fluentcore.utils.Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Implementation for {@link HostNameBinding} and its create and update interfaces.
 *  @param <FluentT> the fluent interface of the parent web app
 *  @param <FluentImplT> the fluent implementation of the parent web app
 */
class HostNameBindingImpl<
            FluentT extends WebAppBase,
            FluentImplT extends WebAppBaseImpl<FluentT, FluentImplT>>
        extends
            IndexableWrapperImpl<HostNameBindingInner>
        implements
            Creatable<HostNameBinding>,
            HostNameBinding,
            HostNameBinding.Definition<WebAppBase.DefinitionStages.WithCreate<FluentT>>,
            HostNameBinding.UpdateDefinition<WebAppBase.Update<FluentT>> {
    private final FluentImplT parent;
    private String domainName;
    private String name;

    HostNameBindingImpl(HostNameBindingInner innerObject, FluentImplT parent) {
        super(innerObject);
        this.parent = parent;
        this.name = innerObject.getName();
        if (name != null && name.contains("/")) {
            this.name = name.replace(parent.name() + "/", "");
        }
    }

    @Override
    public String id() {
        return inner().getId();
    }

    @Override
    public String type() {
        return inner().getType();
    }

    @Override
    public String regionName() {
        return parent().regionName();
    }

    @Override
    public Region region() {
        return parent().region();
    }

    @Override
    public Map<String, String> tags() {
        return parent().tags();
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

        return this;
    }

    @Override
    public Mono<HostNameBinding> refreshAsync() {
        final HostNameBindingImpl<FluentT, FluentImplT> self = this;
        Mono<HostNameBindingInner> observable = null;

        if (parent instanceof DeploymentSlot) {
            observable = this.parent().manager().inner().webApps().getHostNameBindingSlotAsync(
                    parent().resourceGroupName(), ((DeploymentSlot) parent).parent().name(), parent().name(), name());
        } else {
            observable = this.parent().manager().inner().webApps().getHostNameBindingAsync(parent().resourceGroupName(),
                    parent().name(), name());
        }

        return observable.map(hostNameBindingInner -> {
            self.setInner(hostNameBindingInner);
            return self;
        });
    }

    @Override
    public HostNameBinding create() {
        createAsync().blockLast();
        return this;
    }

    @Override
    public Flux<Indexable> createAsync() {
        final HostNameBinding self = this;
        Function<HostNameBindingInner, HostNameBinding> mapper = hostNameBindingInner -> {
            setInner(hostNameBindingInner);
            return self;
        };

        Mono<Indexable> hostNameBindingObservable;
        if (parent instanceof DeploymentSlot) {
            hostNameBindingObservable = this.parent().manager().inner().webApps().createOrUpdateHostNameBindingSlotAsync(
                    parent().resourceGroupName(),
                    ((DeploymentSlot) parent).parent().name(),
                    name,
                    parent().name(), inner()).map(mapper);
        } else {
            hostNameBindingObservable = this.parent().manager().inner().webApps().createOrUpdateHostNameBindingAsync(
                    parent().resourceGroupName(), parent().name(), name, inner()).map(mapper);
        }

        return hostNameBindingObservable.flux();
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