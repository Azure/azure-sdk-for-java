// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.eventhubs.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.eventhubs.fluent.models.AccessKeysInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

/**
 * Type representing access key of {@link EventHubNamespaceAuthorizationRule}.
 */
@Fluent
public interface EventHubAuthorizationKey extends HasInnerModel<AccessKeysInner> {
    /**
     * Gets primary access key.
     *
     * @return primary access key
     */
    String primaryKey();

    /**
     * Gets secondary access key.
     *
     * @return secondary access key
     */
    String secondaryKey();

    /**
     * Gets primary connection string.
     *
     * @return primary connection string
     */
    String primaryConnectionString();

    /**
     * Gets secondary connection string.
     *
     * @return secondary connection string
     */
    String secondaryConnectionString();
}
