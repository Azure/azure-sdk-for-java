// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.servicebus.fluent.models.AccessKeysInner;

/**
 * Authorization key and connection string of authorization rule associated with Service Bus entities.
 */
@Fluent
public interface AuthorizationKeys extends HasInnerModel<AccessKeysInner> {
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
