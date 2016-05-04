/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.*;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigureBase;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public final class AzureResourceManager {
    public static Configure configure() {
        return new AzureConfigureImpl();
    }

    public static Authenticated authenticate(ServiceClientCredentials credentials) {
        return new AzureAuthenticatedImpl(credentials);
    }

    public interface Configure extends AzureConfigureBase<Configure> {
        Authenticated authenticate(ServiceClientCredentials credentials);
    }

    public interface Authenticated {
        Subscriptions subscriptions();
        Tenants tenants();
        Subscription withSubscription(String subscriptionId);
    }

    public interface Subscription {
        ResourceGroups resourceGroups();
        GenericResources genericResources();
        Deployments deployments();
        Deployments.InGroup deployments(String resourceGroupName);
    }
}
