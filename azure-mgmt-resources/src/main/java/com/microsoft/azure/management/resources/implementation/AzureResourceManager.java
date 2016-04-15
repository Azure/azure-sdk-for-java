/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.*;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureBase;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public final class AzureResourceManager {
    public static Authenticated authenticate(ServiceClientCredentials credentials) {
        return new AzureAuthenticatedImpl(credentials);
    }

    public interface Authenticated extends AzureBase<Authenticated> {
        Subscriptions subscriptions();
        Tenants tenants();
        Subscription withSubscription(String subscriptionId);
    }

    public interface Subscription extends AzureBase<Subscription> {
        ResourceGroups resourceGroups();
        GenericResources genericResources();
        Deployments deployments();
        Deployments.InGroup deployments(String resourceGroupName);
    }
}
