// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.resources;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 * Entry point to subscription management API.
 */
@Fluent
public interface Subscriptions extends
        SupportsListing<Subscription>,
        SupportsGettingById<Subscription> {
}
