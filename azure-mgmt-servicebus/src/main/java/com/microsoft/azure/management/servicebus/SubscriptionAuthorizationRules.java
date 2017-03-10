/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;

/**
 * Entry point to subscription authorization rules management API.
 */
@Fluent
public interface SubscriptionAuthorizationRules extends
        SupportsCreating<SubscriptionAuthorizationRule.DefinitionStages.Blank>,
        AuthorizationRules<SubscriptionAuthorizationRule> {
}
