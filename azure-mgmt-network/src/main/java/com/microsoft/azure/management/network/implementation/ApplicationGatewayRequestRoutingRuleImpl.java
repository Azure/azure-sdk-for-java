/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayRequestRoutingRule;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 *  Implementation for ApplicationGatewayRequestRoutingRule.
 */
@LangDefinition
class ApplicationGatewayRequestRoutingRuleImpl
    extends ChildResourceImpl<ApplicationGatewayRequestRoutingRuleInner, ApplicationGatewayImpl, ApplicationGateway>
    implements
        ApplicationGatewayRequestRoutingRule,
        ApplicationGatewayRequestRoutingRule.Definition<ApplicationGateway.DefinitionStages.WithRequestRoutingRuleOrCreate>,
        ApplicationGatewayRequestRoutingRule.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewayRequestRoutingRule.Update {

    ApplicationGatewayRequestRoutingRuleImpl(ApplicationGatewayRequestRoutingRuleInner inner, ApplicationGatewayImpl parent) {
        super(inner, parent);
    }

    // Getters

    @Override
    public String name() {
        return this.inner().name();
    }

    // Verbs

    @Override
    public ApplicationGatewayImpl attach() {
        this.parent().withRequestRoutingRule(this);
        return this.parent();
    }
}
