// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.servicebus.fluent.TopicsClient;

/**
 * Entry point to topic authorization rules management API.
 */
@Fluent
public interface TopicAuthorizationRules extends
    AuthorizationRules<TopicAuthorizationRule>,
    SupportsCreating<TopicAuthorizationRule.DefinitionStages.Blank>,
    HasInner<TopicsClient> {
}
