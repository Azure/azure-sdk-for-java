package com.azure.resourcemanager.cdn.implementation;

import com.azure.resourcemanager.cdn.models.CdnDeliveryRule;
import com.azure.resourcemanager.cdn.models.DeliveryRuleAction;
import com.azure.resourcemanager.cdn.models.DeliveryRuleCondition;

/**
 * @author xiaofeicao
 * @createdAt 2023-03-28 1:24 PM
 */
class CdnDeliveryRuleImpl implements CdnDeliveryRule<CdnEndpointImpl>,
    CdnDeliveryRule.Definition<CdnEndpointImpl>,
    CdnDeliveryRule.Update<CdnEndpointImpl> {

    @Override
    public CdnDeliveryRuleImpl withOrder(int order) {
        // TODO (xiaofeicao, 2023-03-28 1:27 PM)
        throw new UnsupportedOperationException("method [withOrder] not implemented in class [com.azure.resourcemanager.cdn.implementation.CdnDeliveryRuleImpl]");
    }

    @Override
    public CdnDeliveryRuleImpl withMatchConditions(DeliveryRuleCondition... matchConditions) {
        // TODO (xiaofeicao, 2023-03-28 1:27 PM)
        throw new UnsupportedOperationException("method [withMatchConditions] not implemented in class [com.azure.resourcemanager.cdn.implementation.CdnDeliveryRuleImpl]");
    }

    @Override
    public CdnDeliveryRuleImpl withActions(DeliveryRuleAction... actions) {
        // TODO (xiaofeicao, 2023-03-28 1:27 PM)
        throw new UnsupportedOperationException("method [withActions] not implemented in class [com.azure.resourcemanager.cdn.implementation.CdnDeliveryRuleImpl]");
    }

    @Override
    public CdnEndpointImpl parent() {
        // TODO (xiaofeicao, 2023-03-28 1:27 PM)
        throw new UnsupportedOperationException("method [parent] not implemented in class [com.azure.resourcemanager.cdn.implementation.CdnDeliveryRuleImpl]");
    }

    @Override
    public CdnEndpointImpl attach() {
        // TODO (xiaofeicao, 2023-03-28 1:32 PM)
        throw new UnsupportedOperationException("method [attach] not implemented in class [com.azure.resourcemanager.cdn.implementation.CdnDeliveryRuleImpl]");
    }
}
