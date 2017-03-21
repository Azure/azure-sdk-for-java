/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasParent;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.servicebus.implementation.SubscriptionsInner;

/**
 * Entry point to subscription authorization rules management API.
 */
@Fluent
public interface SubscriptionAuthorizationRules extends
        AuthorizationRules<SubscriptionAuthorizationRule>,
        SupportsCreating<SubscriptionAuthorizationRule.DefinitionStages.Blank>,
        HasParent<Subscription>,
        HasInner<SubscriptionsInner> {
}