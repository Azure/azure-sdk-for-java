// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingByName;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.servicebus.ServiceBusManager;

/**
 * Entry point to authorization rules management API.
 *
 * @param <RuleT> the specific rule type
 */
@Fluent
public interface AuthorizationRules<RuleT> extends
    SupportsListing<RuleT>,
    SupportsGettingByName<RuleT>,
    SupportsDeletingByName,
    HasManager<ServiceBusManager> {
}
