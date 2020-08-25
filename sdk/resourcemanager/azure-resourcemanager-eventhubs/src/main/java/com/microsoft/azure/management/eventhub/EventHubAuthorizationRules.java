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
import com.microsoft.azure.management.eventhub.implementation.EventHubsInner;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import rx.Completable;
import rx.Observable;

/**
 * Entry point to manage event hub authorization rules.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_7_0)
public interface EventHubAuthorizationRules
        extends
        SupportsCreating<EventHubAuthorizationRule.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsGettingById<EventHubAuthorizationRule>,
        HasInner<EventHubsInner>,
        HasManager<EventHubManager> {
    /**
     * Lists the authorization rules of an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName namespace resource group name
     * @param namespaceName event hub parent namespace name
     * @param eventHubName event hub name
     * @return list of authorization rules
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    PagedList<EventHubAuthorizationRule> listByEventHub(String resourceGroupName, String namespaceName, String eventHubName);
    /**
     * Lists the authorization rules of an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName namespace resource group name
     * @param namespaceName event hub parent namespace name
     * @param eventHubName event hub name
     * @return observable that emits the authorization rules
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    Observable<EventHubAuthorizationRule> listByEventHubAsync(String resourceGroupName, String namespaceName, String eventHubName);
    /**
     * Gets an authorization rule of an event hub in a namespace in a resource group.
     *
     * @param resourceGroupName namespace resource group name
     * @param namespaceName event hub parent namespace name
     * @param eventHubName event hub name
     * @param name authorization rule name
     * @return observable that emits the authorization rule
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    Observable<EventHubAuthorizationRule> getByNameAsync(String resourceGroupName, String namespaceName, String eventHubName, String name);
    /**
     * Gets an authorization rule of an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName namespace resource group name
     * @param namespaceName event hub parent namespace name
     * @param eventHubName event hub name
     * @param name authorization rule name
     * @return the authorization rule
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    EventHubAuthorizationRule getByName(String resourceGroupName, String namespaceName, String eventHubName, String name);
    /**
     * Deletes an authorization rule of an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName namespace resource group name
     * @param namespaceName event hub parent namespace name
     * @param eventHubName event hub name
     * @param name authorization rule name
     * @return the completable representing the task
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    Completable deleteByNameAsync(String resourceGroupName, String namespaceName, String eventHubName, String name);
    /**
     * Deletes an authorization rule of an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName namespace resource group name
     * @param namespaceName event hub parent namespace name
     * @param eventHubName event hub name
     * @param name authorization rule name
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    void deleteByName(String resourceGroupName, String namespaceName, String eventHubName, String name);
}
