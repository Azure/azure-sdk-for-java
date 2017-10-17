/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.servicebus.implementation.TopicsInner;

/**
 * Entry point to topic authorization rules management API.
 */
@Fluent
public interface TopicAuthorizationRules extends
        AuthorizationRules<TopicAuthorizationRule>,
        SupportsCreating<TopicAuthorizationRule.DefinitionStages.Blank>,
        HasInner<TopicsInner> {
}