/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingByName;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListingAsync;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.servicebus.implementation.NamespacesInner;
import com.microsoft.azure.management.servicebus.implementation.ServiceBusManager;

/**
 * Entry point to authorization rules management API.
 */
@Fluent
public interface AuthorizationRules<RuleT> extends
        SupportsGettingByName<RuleT>,
        SupportsListing<RuleT>,
        SupportsListingAsync<RuleT>,
        SupportsDeletingByName,
        HasManager<ServiceBusManager>,
        HasInner<NamespacesInner> {
}
