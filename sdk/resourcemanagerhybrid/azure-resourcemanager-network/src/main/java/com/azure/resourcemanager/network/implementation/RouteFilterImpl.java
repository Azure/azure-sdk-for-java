// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.Access;
import com.azure.resourcemanager.network.models.ExpressRouteCircuitPeering;
import com.azure.resourcemanager.network.models.RouteFilter;
import com.azure.resourcemanager.network.models.RouteFilterRule;
import com.azure.resourcemanager.network.models.RouteFilterRuleType;
import com.azure.resourcemanager.network.fluent.models.ExpressRouteCircuitPeeringInner;
import com.azure.resourcemanager.network.fluent.models.RouteFilterInner;
import com.azure.resourcemanager.network.fluent.models.RouteFilterRuleInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableParentResourceImpl;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Implementation for RouteFilter and its create and update interfaces. */
class RouteFilterImpl
    extends GroupableParentResourceImpl<RouteFilter, RouteFilterInner, RouteFilterImpl, NetworkManager>
    implements RouteFilter, RouteFilter.Definition, RouteFilter.Update {
    private static final String RULE_TYPE = "Community";

    private Map<String, RouteFilterRule> rules;
    private Map<String, ExpressRouteCircuitPeering> peerings;

    RouteFilterImpl(final String name, final RouteFilterInner innerModel, final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
    }

    @Override
    protected Mono<RouteFilterInner> createInner() {
        return this
            .manager()
            .serviceClient()
            .getRouteFilters()
            .createOrUpdateAsync(resourceGroupName(), name(), innerModel());
    }

    @Override
    protected void initializeChildrenFromInner() {
        this.rules = new TreeMap<>();
        List<RouteFilterRuleInner> inners = this.innerModel().rules();
        if (inners != null) {
            for (RouteFilterRuleInner inner : inners) {
                this.rules.put(inner.name(), new RouteFilterRuleImpl(inner, this));
            }
        }

        if (this.innerModel().peerings() != null) {
            this.peerings =
                this
                    .innerModel()
                    .peerings()
                    .stream()
                    .collect(
                        Collectors
                            .toMap(
                                ExpressRouteCircuitPeeringInner::name,
                                peering ->
                                    new ExpressRouteCircuitPeeringImpl<>(
                                        this,
                                        peering,
                                        manager().serviceClient().getExpressRouteCircuitPeerings(),
                                        peering.peeringType())));
        } else {
            this.peerings = new HashMap<>();
        }
    }

    @Override
    protected void beforeCreating() {
        this.innerModel().withRules(innersFromWrappers(this.rules.values()));
    }

    @Override
    protected Mono<RouteFilterInner> getInnerAsync() {
        return this
            .manager()
            .serviceClient()
            .getRouteFilters()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public Mono<RouteFilter> refreshAsync() {
        return super
            .refreshAsync()
            .map(
                routeFilter -> {
                    RouteFilterImpl impl = (RouteFilterImpl) routeFilter;
                    impl.initializeChildrenFromInner();
                    return impl;
                });
    }

    @Override
    public String provisioningState() {
        return innerModel().provisioningState().toString();
    }

    @Override
    public Map<String, RouteFilterRule> rules() {
        return Collections.unmodifiableMap(this.rules);
    }

    @Override
    public Map<String, ExpressRouteCircuitPeering> peerings() {
        return Collections.unmodifiableMap(this.peerings);
    }

    RouteFilterImpl withRule(RouteFilterRuleImpl rule) {
        this.rules.put(rule.name(), rule);
        return this;
    }

    @Override
    public Update withoutRule(String name) {
        this.rules.remove(name);
        return this;
    }

    @Override
    public RouteFilterRuleImpl defineRule(String name) {
        RouteFilterRuleInner inner = new RouteFilterRuleInner();
        inner.withName(name);
        inner.withRouteFilterRuleType(RouteFilterRuleType.COMMUNITY);
        inner.withAccess(Access.ALLOW);
        return new RouteFilterRuleImpl(inner, this);
    }

    @Override
    public RouteFilterRule.Update updateRule(String name) {
        return (RouteFilterRuleImpl) this.rules.get(name);
    }
}
