/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.search;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.search.implementation.SearchServiceManager;
import com.microsoft.azure.management.search.implementation.ServicesInner;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;

import java.util.List;


/**
 * Entry point to Search service management API in Azure.
 */
@Fluent()
@Beta(Beta.SinceVersion.V1_2_0)
public interface SearchServices extends
    SupportsCreating<SearchService.DefinitionStages.Blank>,
    SupportsListing<SearchService>,
    SupportsListingByResourceGroup<SearchService>,
    SupportsGettingByResourceGroup<SearchService>,
    SupportsGettingById<SearchService>,
    SupportsDeletingById,
    SupportsDeletingByResourceGroup,
    SupportsBatchCreation<SearchService>,
    HasManager<SearchServiceManager>,
    HasInner<ServicesInner> {
  /**
   * Checks if the specified Search service name is valid and available.
   *
   * @param name the Search service name to check
   * @return whether the name is available and other info if not
   */
  CheckNameAvailabilityResult checkNameAvailability(String name);

  /**
   * Checks if Search service name is valid and is not in use asynchronously.
   *
   * @param name the Search service name to check
   * @return a representation of the deferred computation of this call, returning whether the name is available or other info if not
   */
  Observable<CheckNameAvailabilityResult> checkNameAvailabilityAsync(String name);

  /**
   * Checks if Search service name is valid and is not in use asynchronously.
   *
   * @param name the Search service name to check
   * @param callback the callback to call on success or failure
   * @return a handle to cancel the request
   */
  ServiceFuture<CheckNameAvailabilityResult> checkNameAvailabilityAsync(String name, ServiceCallback<CheckNameAvailabilityResult> callback);

  /***********************************************************
   * Getters
   ***********************************************************/

  /**
   * Gets the primary and secondary admin API keys for the specified Azure Search service.
   *
   * @param resourceGroupName The name of the resource group within the current subscription; you can obtain this value from the Azure Resource Manager API or the portal
   * @param searchServiceName The name of the Azure Search service associated with the specified resource group
   * @throws IllegalArgumentException thrown if parameters fail the validation
   * @throws CloudException thrown if the request is rejected by server
   * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
   * @return the AdminKeys object if successful
   */
  AdminKeys getAdminKeys(String resourceGroupName, String searchServiceName);

  /**
   * Gets the primary and secondary admin API keys for the specified Azure Search service.
   *
   * @param resourceGroupName The name of the resource group within the current subscription; you can obtain this value from the Azure Resource Manager API or the portal
   * @param searchServiceName The name of the Azure Search service associated with the specified resource group
   * @throws IllegalArgumentException thrown if parameters fail the validation
   * @return a representation of the future computation of this call
   */
  Observable<AdminKeys> getAdminKeysAsync(String resourceGroupName, String searchServiceName);

  /**
   * Returns the list of query API keys for the given Azure Search service.
   *
   * @param resourceGroupName The name of the resource group within the current subscription; you can obtain this value from the Azure Resource Manager API or the portal
   * @param searchServiceName The name of the Azure Search service associated with the specified resource group
   * @throws IllegalArgumentException thrown if parameters fail the validation
   * @throws CloudException thrown if the request is rejected by server
   * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
   * @return the List&lt;QueryKey&gt; object if successful
   */
  List<QueryKey> listQueryKeys(String resourceGroupName, String searchServiceName);

  /**
   * Returns the list of query API keys for the given Azure Search service.
   *
   * @param resourceGroupName The name of the resource group within the current subscription. You can obtain this value from the Azure Resource Manager API or the portal.
   * @param searchServiceName The name of the Azure Search service associated with the specified resource group.
   * @throws IllegalArgumentException thrown if parameters fail the validation
   * @return a representation of the future computation of this call
   */
  Observable<QueryKey> listQueryKeysAsync(String resourceGroupName, String searchServiceName);


  /***********************************************************
   * Actions
   ***********************************************************/

  /**
   * Regenerates either the primary or secondary admin API key. You can only regenerate one key at a time.
   *
   * @param resourceGroupName The name of the resource group within the current subscription. You can obtain this value from the Azure Resource Manager API or the portal.
   * @param searchServiceName The name of the Azure Search service associated with the specified resource group.
   * @param keyKind Specifies which key to regenerate. Valid values include 'primary' and 'secondary'.
   *                Possible values include: 'primary', 'secondary'
   * @throws IllegalArgumentException thrown if parameters fail the validation
   * @throws CloudException thrown if the request is rejected by server
   * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
   * @return the AdminKeys object if successful.
   */
  AdminKeys regenerateAdminKeys(String resourceGroupName, String searchServiceName, AdminKeyKind keyKind);

  /**
   * Regenerates either the primary or secondary admin API key. You can only regenerate one key at a time.
   *
   * @param resourceGroupName The name of the resource group within the current subscription. You can obtain this value from the Azure Resource Manager API or the portal.
   * @param searchServiceName The name of the Azure Search service associated with the specified resource group.
   * @param keyKind Specifies which key to regenerate. Valid values include 'primary' and 'secondary'.
   *                Possible values include: 'primary', 'secondary'
   * @throws IllegalArgumentException thrown if parameters fail the validation
   * @return the observable to the AdminKeyResultInner object
   */
  Observable<AdminKeys> regenerateAdminKeysAsync(String resourceGroupName, String searchServiceName, AdminKeyKind keyKind);

  /**
   * Regenerates either the primary or secondary admin API key. You can only regenerate one key at a time.
   *
   * @param resourceGroupName The name of the resource group within the current subscription. You can obtain this value from the Azure Resource Manager API or the portal.
   * @param searchServiceName The name of the Azure Search service associated with the specified resource group.
   * @param name The name of the new query API key.
   * @throws IllegalArgumentException thrown if parameters fail the validation
   * @throws CloudException thrown if the request is rejected by server
   * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
   * @return the QueryKey object if successful.
   */
  QueryKey createQueryKey(String resourceGroupName, String searchServiceName, String name);

  /**
   * Regenerates either the primary or secondary admin API key. You can only regenerate one key at a time.
   *
   * @param resourceGroupName The name of the resource group within the current subscription. You can obtain this value from the Azure Resource Manager API or the portal.
   * @param searchServiceName The name of the Azure Search service associated with the specified resource group.
   * @param name The name of the new query API key.
   * @throws IllegalArgumentException thrown if parameters fail the validation
   * @return a representation of the future computation of this call
   */
  Observable<QueryKey> createQueryKeyAsync(String resourceGroupName, String searchServiceName, String name);

  /**
   * Deletes the specified query key. Unlike admin keys, query keys are not regenerated. The process for
   * regenerating a query key is to delete and then recreate it.
   *
   * @param resourceGroupName The name of the resource group within the current subscription. You can obtain this value from the Azure Resource Manager API or the portal.
   * @param searchServiceName The name of the Azure Search service associated with the specified resource group.
   * @param key The query key to be deleted. Query keys are identified by value, not by name.
   * @throws IllegalArgumentException thrown if parameters fail the validation
   * @throws CloudException thrown if the request is rejected by server
   * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
   */
  void deleteQueryKey(String resourceGroupName, String searchServiceName, String key);

  /**
   * Deletes the specified query key. Unlike admin keys, query keys are not regenerated. The process for
   * regenerating a query key is to delete and then recreate it.
   *
   * @param resourceGroupName The name of the resource group within the current subscription. You can obtain this value from the Azure Resource Manager API or the portal.
   * @param searchServiceName The name of the Azure Search service associated with the specified resource group.
   * @param key The query key to be deleted. Query keys are identified by value, not by name.
   * @throws IllegalArgumentException thrown if parameters fail the validation
   * @return a representation of the future computation of this call
   */
  Completable deleteQueryKeyAsync(String resourceGroupName, String searchServiceName, String key);

}