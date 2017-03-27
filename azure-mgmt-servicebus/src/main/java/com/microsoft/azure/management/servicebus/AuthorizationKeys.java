/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.servicebus.implementation.ResourceListKeysInner;

/**
 * Authorization key and connection string of authorization rule associated with Service Bus entities.
 */
@Fluent
@Beta
public interface AuthorizationKeys extends HasInner<ResourceListKeysInner> {
    /**
     * @return primary key associated with the rule
     */
    String primaryKey();
    /**
     * @return secondary key associated with the rule
     */
    String secondaryKey();
    /**
     * @return primary connection string
     */
    String primaryConnectionString();
    /**
     * @return secondary connection string
     */
    String secondaryConnectionString();
}