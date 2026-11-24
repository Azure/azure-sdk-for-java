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
     * Gets the primary key associated with the rule.
     *
     * @return primary key associated with the rule
     */
    String primaryKey();

    /**
     * Gets the secondary key associated with the rule.
     *
     * @return secondary key associated with the rule
     */
    String secondaryKey();

    /**
     * Gets the primary connection string.
     *
     * @return primary connection string
     */
    String primaryConnectionString();

    /**
     * Gets the secondary connection string.
     *
     * @return secondary connection string
     */
    String secondaryConnectionString();
}
