/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.collection.SupportsGetting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 * Defines an interface for accessing subscriptions in Azure.
 */
public interface Subscriptions extends
        SupportsListing<Subscription>,
        SupportsGetting<Subscription> {
}
