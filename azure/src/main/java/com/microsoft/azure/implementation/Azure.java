/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.implementation;

import com.microsoft.azure.AzureAuthenticated;
import com.microsoft.azure.Subscription;
import com.microsoft.azure.management.resources.SubscriptionClient;
import com.microsoft.azure.management.resources.SubscriptionClientImpl;
import com.microsoft.azure.management.resources.Subscriptions;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public final class Azure {
    public static AzureAuthenticated authenticate(ServiceClientCredentials credentials) {
        return new AzureAuthenticatedImpl(credentials);
    }
}
