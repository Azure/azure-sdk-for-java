// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.implementation;

import com.azure.resourcemanager.cdn.fluent.models.RouteInner;
import com.azure.resourcemanager.cdn.models.AfdEndpoint;
import com.azure.resourcemanager.cdn.models.AfdEndpointProtocols;
import com.azure.resourcemanager.cdn.models.AfdProvisioningState;
import com.azure.resourcemanager.cdn.models.AfdRouteCacheConfiguration;
import com.azure.resourcemanager.cdn.models.CdnProfile;
import com.azure.resourcemanager.cdn.models.DeploymentStatus;
import com.azure.resourcemanager.cdn.models.EnabledState;
import com.azure.resourcemanager.cdn.models.ForwardingProtocol;
import com.azure.resourcemanager.cdn.models.HttpsRedirect;
import com.azure.resourcemanager.cdn.models.LinkToDefaultDomain;
import com.azure.resourcemanager.cdn.models.ResourceReference;
import com.azure.resourcemanager.cdn.models.Route;
import com.azure.resourcemanager.cdn.models.RouteUpdateParameters;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation for {@link Route}.
 */
class RouteImpl extends ExternalChildResourceImpl<Route, RouteInner, AfdEndpointImpl, AfdEndpoint> implements Route,
    Route.DefinitionStages.Blank<AfdEndpoint.DefinitionStages.WithAttach<CdnProfile.DefinitionStages.WithStandardCreate>>,
    Route.DefinitionStages.WithOriginGroup<AfdEndpoint.DefinitionStages.WithAttach<CdnProfile.DefinitionStages.WithStandardCreate>>,
    Route.DefinitionStages.WithAttach<AfdEndpoint.DefinitionStages.WithAttach<CdnProfile.DefinitionStages.WithStandardCreate>>,
    Route.UpdateDefinitionStages.Blank<AfdEndpoint.Update>,
    Route.UpdateDefinitionStages.WithOriginGroup<AfdEndpoint.Update>,
    Route.UpdateDefinitionStages.WithAttach<AfdEndpoint.Update>, Route.Update {

    RouteImpl(String name, AfdEndpointImpl parent, RouteInner inner) {
        super(name, parent, inner);
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public String endpointName() {
        return this.innerModel().endpointName();
    }

    @Override
    public String originGroupResourceId() {
        ResourceReference ref = this.innerModel().originGroup();
        return ref == null ? null : ref.id();
    }

    @Override
    public String originPath() {
        return this.innerModel().originPath();
    }

    @Override
    public List<String> ruleSetResourceIds() {
        List<ResourceReference> refs = this.innerModel().ruleSets();
        if (refs == null) {
            return Collections.emptyList();
        }
        return refs.stream().map(ResourceReference::id).collect(Collectors.toList());
    }

    @Override
    public List<AfdEndpointProtocols> supportedProtocols() {
        return this.innerModel().supportedProtocols();
    }

    @Override
    public List<String> patternsToMatch() {
        return this.innerModel().patternsToMatch();
    }

    @Override
    public AfdRouteCacheConfiguration cacheConfiguration() {
        return this.innerModel().cacheConfiguration();
    }

    @Override
    public ForwardingProtocol forwardingProtocol() {
        return this.innerModel().forwardingProtocol();
    }

    @Override
    public LinkToDefaultDomain linkToDefaultDomain() {
        return this.innerModel().linkToDefaultDomain();
    }

    @Override
    public HttpsRedirect httpsRedirect() {
        return this.innerModel().httpsRedirect();
    }

    @Override
    public EnabledState enabledState() {
        return this.innerModel().enabledState();
    }

    @Override
    public AfdProvisioningState provisioningState() {
        return this.innerModel().provisioningState();
    }

    @Override
    public DeploymentStatus deploymentStatus() {
        return this.innerModel().deploymentStatus();
    }

    @Override
    public Mono<Route> createResourceAsync() {
        final RouteImpl self = this;
        return this.parent()
            .parent()
            .manager()
            .serviceClient()
            .getRoutes()
            .createAsync(this.parent().parent().resourceGroupName(), this.parent().parent().name(),
                this.parent().name(), this.name(), this.innerModel())
            .map(inner -> {
                self.setInner(inner);
                return self;
            });
    }

    @Override
    public Mono<Route> updateResourceAsync() {
        final RouteImpl self = this;
        RouteUpdateParameters parameters = new RouteUpdateParameters().withOriginGroup(this.innerModel().originGroup())
            .withOriginPath(this.innerModel().originPath())
            .withRuleSets(this.innerModel().ruleSets())
            .withSupportedProtocols(this.innerModel().supportedProtocols())
            .withPatternsToMatch(this.innerModel().patternsToMatch())
            .withCacheConfiguration(this.innerModel().cacheConfiguration())
            .withForwardingProtocol(this.innerModel().forwardingProtocol())
            .withLinkToDefaultDomain(this.innerModel().linkToDefaultDomain())
            .withHttpsRedirect(this.innerModel().httpsRedirect())
            .withEnabledState(this.innerModel().enabledState());
        return this.parent()
            .parent()
            .manager()
            .serviceClient()
            .getRoutes()
            .updateAsync(this.parent().parent().resourceGroupName(), this.parent().parent().name(),
                this.parent().name(), this.name(), parameters)
            .map(inner -> {
                self.setInner(inner);
                return self;
            });
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return this.parent()
            .parent()
            .manager()
            .serviceClient()
            .getRoutes()
            .deleteAsync(this.parent().parent().resourceGroupName(), this.parent().parent().name(),
                this.parent().name(), this.name());
    }

    @Override
    protected Mono<RouteInner> getInnerAsync() {
        return this.parent()
            .parent()
            .manager()
            .serviceClient()
            .getRoutes()
            .getAsync(this.parent().parent().resourceGroupName(), this.parent().parent().name(), this.parent().name(),
                this.name());
    }

    @Override
    public AfdEndpointImpl attach() {
        return this.parent().withRoute(this);
    }

    // ---- Fluent setters ----

    @Override
    public RouteImpl withOriginGroupResourceId(String originGroupResourceId) {
        this.innerModel()
            .withOriginGroup(
                originGroupResourceId == null ? null : new ResourceReference().withId(originGroupResourceId));
        return this;
    }

    @Override
    public RouteImpl withOriginPath(String originPath) {
        this.innerModel().withOriginPath(originPath);
        return this;
    }

    @Override
    public RouteImpl withRuleSetResourceIds(List<String> ruleSetResourceIds) {
        if (ruleSetResourceIds == null) {
            this.innerModel().withRuleSets(null);
        } else {
            this.innerModel()
                .withRuleSets(ruleSetResourceIds.stream()
                    .map(id -> new ResourceReference().withId(id))
                    .collect(Collectors.toList()));
        }
        return this;
    }

    @Override
    public RouteImpl withSupportedProtocols(List<AfdEndpointProtocols> supportedProtocols) {
        this.innerModel().withSupportedProtocols(supportedProtocols);
        return this;
    }

    @Override
    public RouteImpl withPatternsToMatch(List<String> patternsToMatch) {
        this.innerModel().withPatternsToMatch(patternsToMatch);
        return this;
    }

    @Override
    public RouteImpl withCacheConfiguration(AfdRouteCacheConfiguration cacheConfiguration) {
        this.innerModel().withCacheConfiguration(cacheConfiguration);
        return this;
    }

    @Override
    public RouteImpl withForwardingProtocol(ForwardingProtocol forwardingProtocol) {
        this.innerModel().withForwardingProtocol(forwardingProtocol);
        return this;
    }

    @Override
    public RouteImpl withLinkToDefaultDomain(LinkToDefaultDomain linkToDefaultDomain) {
        this.innerModel().withLinkToDefaultDomain(linkToDefaultDomain);
        return this;
    }

    @Override
    public RouteImpl withHttpsRedirect(HttpsRedirect httpsRedirect) {
        this.innerModel().withHttpsRedirect(httpsRedirect);
        return this;
    }

    @Override
    public RouteImpl withEnabledState(EnabledState enabledState) {
        this.innerModel().withEnabledState(enabledState);
        return this;
    }

}
