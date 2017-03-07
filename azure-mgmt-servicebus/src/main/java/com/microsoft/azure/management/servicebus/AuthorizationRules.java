/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.servicebus;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.servicebus.implementation.ServiceBusManager;
import com.microsoft.azure.management.servicebus.implementation.SharedAccessAuthorizationRuleInner;


/**
 * Entry point to load balancer management API in Azure.
 */
@Fluent()
public interface AuthorizationRules extends
    SupportsCreating<AuthorizationRule.DefinitionStages.Blank>,
    HasManager<ServiceBusManager>,
    HasInner<SharedAccessAuthorizationRuleInner>,
    SupportsBatchCreation<AuthorizationRule>,
    SupportsGettingById<AuthorizationRule>,
    SupportsDeletingById,
    SupportsDeletingByGroup {

    /**
     * Primary and secondary connection strings to the queue.
     */
    void listKeys(String groupName, String name);

    /**
     * Regenerates the primary or secondary connection strings to the queue.
     */
     void regenerateKeys(String groupName, String name);
}
