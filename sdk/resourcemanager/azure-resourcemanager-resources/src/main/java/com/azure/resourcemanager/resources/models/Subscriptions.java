// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;

/**
 * Entry point to subscription management API.
 */
@Fluent
public interface Subscriptions extends
        SupportsListing<Subscription>,
        SupportsGettingById<Subscription> {
}
