// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;

/**
 * Entry point to queue authorization rules management API.
 */
@Fluent
public interface QueueAuthorizationRules extends
    AuthorizationRules<QueueAuthorizationRule>,
    SupportsCreating<QueueAuthorizationRule.DefinitionStages.Blank> {
}
