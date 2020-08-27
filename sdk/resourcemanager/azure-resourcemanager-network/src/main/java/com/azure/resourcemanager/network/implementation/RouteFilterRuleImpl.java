// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.Access;
import com.azure.resourcemanager.network.models.RouteFilter;
import com.azure.resourcemanager.network.models.RouteFilterRule;
import com.azure.resourcemanager.network.models.RouteFilterRuleType;
import com.azure.resourcemanager.network.fluent.inner.RouteFilterRuleInner;
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
        inner().withCommunities(Arrays.asList(communities));
        return this;
    }

    @Override
    public RouteFilterRuleImpl withBgpCommunity(String community) {
        if (inner().communities() == null) {
            inner().withCommunities(new ArrayList<String>());
        }
        inner().communities().add(community);
        return this;
    }

    @Override
    public Update withoutBgpCommunity(String community) {
        if (inner().communities() != null) {
            inner().communities().remove(community);
        }
        return this;
    }

    @Override
    public RouteFilterRuleImpl allowAccess() {
        inner().withAccess(Access.ALLOW);
        return this;
    }

    @Override
    public RouteFilterRuleImpl denyAccess() {
        inner().withAccess(Access.DENY);
        return this;
    }

    @Override
    public String name() {
        return inner().name();
    }

    @Override
    public Access access() {
        return inner().access();
    }

    @Override
    public RouteFilterRuleType routeFilterRuleType() {
        return inner().routeFilterRuleType();
    }

    @Override
    public List<String> communities() {
        return Collections.unmodifiableList(inner().communities());
    }

    @Override
    public String provisioningState() {
        return inner().provisioningState().toString();
    }

    @Override
    public String location() {
        return inner().location();
    }
}
