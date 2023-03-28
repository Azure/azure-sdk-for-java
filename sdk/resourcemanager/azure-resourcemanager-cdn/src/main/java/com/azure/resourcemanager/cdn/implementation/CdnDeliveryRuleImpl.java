package com.azure.resourcemanager.cdn.implementation;

import com.azure.resourcemanager.cdn.models.CdnDeliveryRule;
import com.azure.resourcemanager.cdn.models.CdnEndpoint;
import com.azure.resourcemanager.cdn.models.DeliveryRule;
import com.azure.resourcemanager.cdn.models.DeliveryRuleAction;
import com.azure.resourcemanager.cdn.models.DeliveryRuleCondition;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation for {@link CdnDeliveryRule}.
 */
class CdnDeliveryRuleImpl
    extends ChildResourceImpl<DeliveryRule, CdnEndpointImpl, CdnEndpoint>
    implements CdnDeliveryRule,
    CdnDeliveryRule.Definition<CdnEndpointImpl>,
    CdnDeliveryRule.Update<CdnEndpointImpl> {

    CdnDeliveryRuleImpl(CdnEndpointImpl parent, String name) {
        this(parent, new DeliveryRule().withName(name));
    }

    CdnDeliveryRuleImpl(CdnEndpointImpl parent, DeliveryRule deliveryRule) {
        super(deliveryRule, parent);
    }

    @Override
    public CdnDeliveryRuleImpl withOrder(int order) {
        innerModel().withOrder(order);
        return this;
    }

    @Override
    public CdnDeliveryRuleImpl withMatchConditions(DeliveryRuleCondition... matchConditions) {
        List<DeliveryRuleCondition> conditions = new ArrayList<>();
        if (matchConditions != null) {
            conditions.addAll(Arrays.asList(matchConditions));
        }
        innerModel().withConditions(conditions);
        return this;
    }

    @Override
    public CdnDeliveryRuleImpl withActions(DeliveryRuleAction... actions) {
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
