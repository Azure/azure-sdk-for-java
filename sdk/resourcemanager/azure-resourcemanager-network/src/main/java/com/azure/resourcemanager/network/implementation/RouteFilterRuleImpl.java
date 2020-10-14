// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.Access;
import com.azure.resourcemanager.network.models.RouteFilter;
import com.azure.resourcemanager.network.models.RouteFilterRule;
import com.azure.resourcemanager.network.models.RouteFilterRuleType;
import com.azure.resourcemanager.network.fluent.models.RouteFilterRuleInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Implementation for {@link RouteFilterRule} and its create and update interfaces. */
class RouteFilterRuleImpl extends ChildResourceImpl<RouteFilterRuleInner, RouteFilterImpl, RouteFilter>
    implements RouteFilterRule,
        RouteFilterRule.Definition<RouteFilter.DefinitionStages.WithCreate>,
        RouteFilterRule.UpdateDefinition<RouteFilter.Update>,
        RouteFilterRule.Update {

    RouteFilterRuleImpl(RouteFilterRuleInner inner, RouteFilterImpl parent) {
        super(inner, parent);
    }

    @Override
    public RouteFilterImpl attach() {
        return this.parent().withRule(this);
    }

    @Override
    public RouteFilterRuleImpl withBgpCommunities(String... communities) {
        innerModel().withCommunities(Arrays.asList(communities));
        return this;
    }

    @Override
    public RouteFilterRuleImpl withBgpCommunity(String community) {
        if (innerModel().communities() == null) {
            innerModel().withCommunities(new ArrayList<String>());
        }
        innerModel().communities().add(community);
        return this;
    }

    @Override
    public Update withoutBgpCommunity(String community) {
        if (innerModel().communities() != null) {
            innerModel().communities().remove(community);
        }
        return this;
    }

    @Override
    public RouteFilterRuleImpl allowAccess() {
        innerModel().withAccess(Access.ALLOW);
        return this;
    }

    @Override
    public RouteFilterRuleImpl denyAccess() {
        innerModel().withAccess(Access.DENY);
        return this;
    }

    @Override
    public String name() {
        return innerModel().name();
    }

    @Override
    public Access access() {
        return innerModel().access();
    }

    @Override
    public RouteFilterRuleType routeFilterRuleType() {
        return innerModel().routeFilterRuleType();
    }

    @Override
    public List<String> communities() {
        return Collections.unmodifiableList(innerModel().communities());
    }

    @Override
    public String provisioningState() {
        return innerModel().provisioningState().toString();
    }

    @Override
    public String location() {
        return innerModel().location();
    }
}
