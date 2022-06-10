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
public interface EventHubAuthorizationKey
    extends HasInnerModel<AccessKeysInner> {
    /**
     * @return primary access key
     */
    String primaryKey();

    /**
     * @return secondary access key
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
