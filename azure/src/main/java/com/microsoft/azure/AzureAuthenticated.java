/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

import com.microsoft.azure.management.resources.collection.Subscriptions;

public interface AzureAuthenticated {
    Subscriptions subscriptions();

    Subscription subscription(String subscriptionId);
}
