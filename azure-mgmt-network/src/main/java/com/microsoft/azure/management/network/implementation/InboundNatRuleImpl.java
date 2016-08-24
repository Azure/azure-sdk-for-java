/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.network.InboundNatRule;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 *  Implementation for {@link InboundNatRule}.
 */
class InboundNatRuleImpl
    extends ChildResourceImpl<InboundNatRuleInner, LoadBalancerImpl>
    implements
        InboundNatRule,
        InboundNatRule.Definition<LoadBalancer.DefinitionStages.WithCreate>,
        InboundNatRule.UpdateDefinition<LoadBalancer.Update>,
        InboundNatRule.Update {

    protected InboundNatRuleImpl(InboundNatRuleInner inner, LoadBalancerImpl parent) {
        super(inner, parent);
    }

    // Getters

    @Override
    public String name() {
        return this.inner().name();
    }

    // Fluent setters

    // Verbs

    @Override
    public LoadBalancerImpl attach() {
        return this.parent().withInboundNatRule(this);
    }
}
