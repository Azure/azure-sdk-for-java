/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.eventhub.implementation.EventHubManager;
import com.microsoft.azure.management.eventhub.implementation.NamespacesInner;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import rx.Completable;
import rx.Observable;

/**
 * Entry point to manage event hub namespace authorization rules.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_7_0)
public interface EventHubNamespaceAuthorizationRules
        extends
        SupportsCreating<EventHubNamespaceAuthorizationRule.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsGettingById<EventHubNamespaceAuthorizationRule>,
        HasInner<NamespacesInner>,
        HasManager<EventHubManager> {
    /**
     * Lists the authorization rules under a namespace in a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @return list of authorization rules
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    PagedList<EventHubNamespaceAuthorizationRule> listByNamespace(String resourceGroupName, String namespaceName);
    /**
     * Lists the authorization rules under a namespace in a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @return observable that emits the authorization rules
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    Observable<EventHubNamespaceAuthorizationRule> listByNamespaceAsync(String resourceGroupName, String namespaceName);
    /**
     * Gets an authorization rule under a namespace in a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @param name authorization rule name
     * @return observable that emits the authorization rule
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    Observable<EventHubNamespaceAuthorizationRule> getByNameAsync(String resourceGroupName, String namespaceName, String name);
    /**
     * Gets an authorization rule under a namespace in a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @param name authorization rule name
     * @return the authorization rule
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    EventHubNamespaceAuthorizationRule getByName(String resourceGroupName, String namespaceName, String name);
    /**
     * Deletes an authorization rule under a namespace in a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @param name authorization rule name
     * @return the completable representing the task
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    Completable deleteByNameAsync(String resourceGroupName, String namespaceName, String name);
    /**
     * Deletes an authorization rule under a namespace in a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @param name authorization rule name
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    void deleteByName(String resourceGroupName, String namespaceName, String name);
}
