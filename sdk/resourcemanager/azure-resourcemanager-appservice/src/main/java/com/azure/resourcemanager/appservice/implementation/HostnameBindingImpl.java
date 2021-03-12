// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.appservice.fluent.models.HostnameBindingInner;
import com.azure.resourcemanager.appservice.models.AppServiceDomain;
import com.azure.resourcemanager.appservice.models.AzureResourceType;
import com.azure.resourcemanager.appservice.models.CustomHostnameDnsRecordType;
import com.azure.resourcemanager.appservice.models.DeploymentSlot;
import com.azure.resourcemanager.appservice.models.HostnameBinding;
import com.azure.resourcemanager.appservice.models.HostnameType;
import com.azure.resourcemanager.appservice.models.WebAppBase;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation for {@link HostnameBinding} and its create and update interfaces.
 *
 * @param <FluentT> the fluent interface of the parent web app
 * @param <FluentImplT> the fluent implementation of the parent web app
 */
class HostnameBindingImpl<FluentT extends WebAppBase, FluentImplT extends WebAppBaseImpl<FluentT, FluentImplT>>
    extends IndexableWrapperImpl<HostnameBindingInner>
    implements Creatable<HostnameBinding>,
    HostnameBinding,
        HostnameBinding.Definition<WebAppBase.DefinitionStages.WithCreate<FluentT>>,
        HostnameBinding.UpdateDefinition<WebAppBase.Update<FluentT>> {

    private final ClientLogger logger = new ClientLogger(getClass());

    private final FluentImplT parent;
    private String domainName;
    private String name;

    HostnameBindingImpl(HostnameBindingInner innerObject, FluentImplT parent) {
        super(innerObject);
        this.parent = parent;
        this.name = innerObject.name();
        if (name != null && name.contains("/")) {
            this.name = name.replace(parent.name() + "/", "");
        }
    }

    @Override
    public String id() {
        return innerModel().id();
    }

    @Override
    public String type() {
        return innerModel().type();
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
        return innerModel().siteName();
    }

    @Override
    public String domainId() {
        return innerModel().domainId();
    }

    @Override
    public String azureResourceName() {
        return innerModel().azureResourceName();
    }

    @Override
    public AzureResourceType azureResourceType() {
        return innerModel().azureResourceType();
    }

    @Override
    public CustomHostnameDnsRecordType dnsRecordType() {
        return innerModel().customHostnameDnsRecordType();
    }

    @Override
    public HostnameType hostnameType() {
        return innerModel().hostnameType();
    }

    @Override
    public FluentImplT attach() {
        parent.withHostNameBinding(this);
        return parent;
    }

    @Override
    public HostnameBindingImpl<FluentT, FluentImplT> withDnsRecordType(
        CustomHostnameDnsRecordType hostnameDnsRecordType) {
        Pattern pattern = Pattern.compile("([.\\w-]+|\\*)\\.([\\w-]+\\.\\w+)");
        Matcher matcher = pattern.matcher(name);
        if (hostnameDnsRecordType == CustomHostnameDnsRecordType.CNAME && !matcher.matches()) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("root hostname cannot be assigned with a CName record"));
        }
        innerModel().withCustomHostnameDnsRecordType(hostnameDnsRecordType);
        return this;
    }

    @Override
    public HostnameBindingImpl<FluentT, FluentImplT> refresh() {

        return this;
    }

    @Override
    public Mono<HostnameBinding> refreshAsync() {
        final HostnameBindingImpl<FluentT, FluentImplT> self = this;
        Mono<HostnameBindingInner> observable = null;

        if (parent instanceof DeploymentSlot) {
            observable =
                this
                    .parent()
                    .manager()
                    .serviceClient()
                    .getWebApps()
                    .getHostnameBindingSlotAsync(
                        parent().resourceGroupName(),
                        ((DeploymentSlot) parent).parent().name(),
                        parent().name(),
                        name());
        } else {
            observable =
                this
                    .parent()
                    .manager()
                    .serviceClient()
                    .getWebApps()
                    .getHostnameBindingAsync(parent().resourceGroupName(), parent().name(), name());
        }

        return observable
            .map(
                hostnameBindingInner -> {
                    self.setInner(hostnameBindingInner);
                    return self;
                });
    }

    @Override
    public HostnameBinding create() {
        return createAsync().block();
    }

    @Override
    public Mono<HostnameBinding> createAsync() {
        final HostnameBinding self = this;
        Function<HostnameBindingInner, HostnameBinding> mapper =
            hostnameBindingInner -> {
                setInner(hostnameBindingInner);
                return self;
            };

        Mono<HostnameBinding> hostnameBindingObservable;
        if (parent instanceof DeploymentSlot) {
            hostnameBindingObservable =
                this
                    .parent()
                    .manager()
                    .serviceClient()
                    .getWebApps()
                    .createOrUpdateHostnameBindingSlotAsync(
                        parent().resourceGroupName(),
                        ((DeploymentSlot) parent).parent().name(),
                        name,
                        parent().name(),
                        innerModel())
                    .map(mapper);
        } else {
            hostnameBindingObservable =
                this
                    .parent()
                    .manager()
                    .serviceClient()
                    .getWebApps()
                    .createOrUpdateHostnameBindingAsync(
                        parent().resourceGroupName(), parent().name(), name, innerModel())
                    .map(mapper);
        }

        return hostnameBindingObservable;
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
    public HostnameBindingImpl<FluentT, FluentImplT> withAzureManagedDomain(AppServiceDomain domain) {
        innerModel().withDomainId(domain.id());
        innerModel().withHostnameType(HostnameType.MANAGED);
        this.domainName = domain.name();
        return this;
    }

    @Override
    public HostnameBindingImpl<FluentT, FluentImplT> withThirdPartyDomain(String domain) {
        innerModel().withHostnameType(HostnameType.VERIFIED);
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
    public HostnameBindingImpl<FluentT, FluentImplT> withSubDomain(String subDomain) {
        this.name = normalizeHostNameBindingName(subDomain, domainName);
        return this;
    }
}
