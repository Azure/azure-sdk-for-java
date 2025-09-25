// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.implementation;

import com.azure.resourcemanager.cdn.models.CdnStandardRulesEngineRule;
import com.azure.resourcemanager.cdn.models.CdnEndpoint;
import com.azure.resourcemanager.cdn.models.DeliveryRule;
import com.azure.resourcemanager.cdn.models.DeliveryRuleAction;
import com.azure.resourcemanager.cdn.models.DeliveryRuleCondition;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation for {@link CdnStandardRulesEngineRule}.
 */
class CdnStandardRulesEngineRuleImpl extends ChildResourceImpl<DeliveryRule, CdnEndpointImpl, CdnEndpoint>
    implements CdnStandardRulesEngineRule, CdnStandardRulesEngineRule.Definition<CdnEndpointImpl>,
    CdnStandardRulesEngineRule.Update<CdnEndpointImpl> {

    CdnStandardRulesEngineRuleImpl(CdnEndpointImpl parent, String name) {
        this(parent, new DeliveryRule().withName(name));
    }

    CdnStandardRulesEngineRuleImpl(CdnEndpointImpl parent, DeliveryRule deliveryRule) {
        super(deliveryRule, parent);
    }

    @Override
    public CdnStandardRulesEngineRuleImpl withOrder(int order) {
        innerModel().withOrder(order);
        return this;
    }

    @Override
    public CdnStandardRulesEngineRuleImpl withMatchConditions(DeliveryRuleCondition... matchConditions) {
        List<DeliveryRuleCondition> conditions = new ArrayList<>();
        if (matchConditions != null) {
            conditions.addAll(Arrays.asList(matchConditions));
        }
        innerModel().withConditions(conditions);
        return this;
    }

    @Override
    public CdnStandardRulesEngineRuleImpl withActions(DeliveryRuleAction... actions) {
        List<DeliveryRuleAction> actionList = new ArrayList<>();
        if (actions != null) {
            actionList.addAll(Arrays.asList(actions));
        }
        innerModel().withActions(actionList);
        return this;
    }

    @Override
    public String name() {
        return innerModel().name();
    }

    @Override
    public CdnEndpointImpl attach() {
        return parent();
    }
}
