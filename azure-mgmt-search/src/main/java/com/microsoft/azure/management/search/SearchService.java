/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.search;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.search.implementation.SearchServiceInner;
import com.microsoft.azure.management.search.implementation.SearchServiceManager;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;

import java.util.List;

/**
 * An immutable client-side representation of an Azure registry.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_1_0) // TODO: change to Beta.SinceVersion.V1_2_0
public interface SearchService extends
    GroupableResource<SearchServiceManager, SearchServiceInner>,
    Refreshable<SearchService>,
    Updatable<SearchService.Update> {

  /***********************************************************
   * Getters
   ***********************************************************/

  /**
   * Get the hostingMode value.
   *
   * @return the hostingMode value
   */
  HostingMode hostingMode();

  /**
   * Get the partitionCount value.
   *
   * @return the partitions count of the Search service.
   */
  int partitionCount();

  /**
   * Get the provisioningState value.
   *
   * @return the provisioningState value
   */
  ProvisioningState provisioningState();

  /**
   * Get the replicaCount value.
   *
   * @return the replicas count of the Search service.
   */
  int replicaCount();

  /**
   * @return the SKU of the Search service.
   */
  Sku sku();

  /**
   * Get the status value.
   *
   * @return the status value
   */
  SearchServiceStatus status();

  /**
   * Get the statusDetails value.
   *
   * @return the statusDetails value
   */
  String statusDetails();

  /**
   * Gets the primary and secondary admin API keys for the specified Azure Search service.
   *
   * @throws IllegalArgumentException thrown if parameters fail the validation
   * @throws CloudException thrown if the request is rejected by server
   * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
   * @return the AdminKeys object if successful.
   */
  AdminKeys getAdminKeys();

  /**
   * Gets the primary and secondary admin API keys for the specified Azure Search service.
   *
   * @throws IllegalArgumentException thrown if parameters fail the validation
   * @return the observable to the AdminKeys object
   */
  Observable<AdminKeys> getAdminKeysAsync();

  /**
   * Returns the list of query API keys for the given Azure Search service.
   *
   * @throws IllegalArgumentException thrown if parameters fail the validation
   * @throws CloudException thrown if the request is rejected by server
   * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
   * @return the List&lt;QueryKey&gt; object if successful.
   */
  List<QueryKey> listQueryKeys();

  /**
   * Returns the list of query API keys for the given Azure Search service.
   *
   * @throws IllegalArgumentException thrown if parameters fail the validation
   * @return the observable to the List&lt;QueryKeyInner&gt; object
   */
  Observable<QueryKey> listQueryKeysAsync();


  /***********************************************************
   * Actions
   ***********************************************************/

  /**
   * Regenerates either the primary or secondary admin API key. You can only regenerate one key at a time.
   *
   * @param keyKind Specifies which key to regenerate. Valid values include 'primary' and 'secondary'.
   *                Possible values include: 'primary', 'secondary'
   * @throws IllegalArgumentException thrown if parameters fail the validation
   * @throws CloudException thrown if the request is rejected by server
   * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
   * @return the AdminKeys object if successful.
   */
  AdminKeys regenerateAdminKeys(AdminKeyKind keyKind);

  /**
   * Regenerates either the primary or secondary admin API key. You can only regenerate one key at a time.
   *
   * @param keyKind Specifies which key to regenerate. Valid values include 'primary' and 'secondary'.
   *                Possible values include: 'primary', 'secondary'
   * @throws IllegalArgumentException thrown if parameters fail the validation
   * @return the observable to the AdminKeyResultInner object
   */
  Observable<AdminKeys> regenerateAdminKeysAsync(AdminKeyKind keyKind);

  /**
   * Regenerates either the primary or secondary admin API key. You can only regenerate one key at a time.
   *
   * @param name The name of the new query API key.
   * @throws IllegalArgumentException thrown if parameters fail the validation
   * @throws CloudException thrown if the request is rejected by server
   * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
   * @return the List&lt;QueryKey&gt; object if successful.
   */
  QueryKey createQueryKey(String name);

  /**
   * Regenerates either the primary or secondary admin API key. You can only regenerate one key at a time.
   *
   * @param name The name of the new query API key.
   * @throws IllegalArgumentException thrown if parameters fail the validation
   * @return the observable to the List&lt;QueryKey&gt; object
   */
  Observable<QueryKey> createQueryKeyAsync(String name);

  /**
   * Deletes the specified query key. Unlike admin keys, query keys are not regenerated. The process for
   * regenerating a query key is to delete and then recreate it.
   *
   * @param key The query key to be deleted. Query keys are identified by value, not by name.
   * @throws IllegalArgumentException thrown if parameters fail the validation
   * @throws CloudException thrown if the request is rejected by server
   * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
   */
  void deleteQueryKey(String key);

  /**
   * Deletes the specified query key. Unlike admin keys, query keys are not regenerated. The process for
   * regenerating a query key is to delete and then recreate it.
   *
   * @param key The query key to be deleted. Query keys are identified by value, not by name.
   * @throws IllegalArgumentException thrown if parameters fail the validation
   * @return the Observable to {@link Void} object if successful.
   */
  Observable<Void> deleteQueryKeyAsync(String key);


  /**
   * The entirety of the Search service definition.
   */
  interface Definition extends
      DefinitionStages.Blank,
      DefinitionStages.WithGroup,
      DefinitionStages.WithSku,
      DefinitionStages.WithPartitionsAndCreate,
      DefinitionStages.WithReplicasAndCreate,
      DefinitionStages.WithCreate {
  }

  /**
   * Grouping of virtual network definition stages.
   */
  interface DefinitionStages {
    /**
     * The first stage of the Search service definition.
     */
    interface Blank
        extends GroupableResource.DefinitionWithRegion<WithGroup> {
    }

    /**
     * The stage of the Search service definition allowing to specify the resource group.
     */
    interface WithGroup
        extends GroupableResource.DefinitionStages.WithGroup<DefinitionStages.WithSku> {
    }

    /**
     * The stage of the Search service definition allowing to specify the sku.
     */
    interface WithSku {
      /**
       * Specifies the sku of the Search service.
       * @param skuName the sku
       * @return the next stage of the definition
       */
      WithCreate withSku(SkuName skuName);

      /**
       * Specifies to use a free sku type for the Search service.
       * @return the next stage of the definition
       */
      WithCreate withFreeSku();

      /**
       * Specifies to use a basic sku type for the Search service.
       * @return the next stage of the definition
       */
      WithReplicasAndCreate withBasicSku();

      /**
       * Specifies to use a standard sku type for the Search service.
       *
       * @param
       * @return the next stage of the definition
       */
      WithPartitionsAndCreate withStandardSku();
    }

    interface WithReplicasAndCreate extends WithCreate {
      /**
       * Specifies the sku of the Search service.
       * @param replicaCount the number of replicas to be created
       * @return the next stage of the definition
       */
     WithCreate withReplicas(int replicaCount);
    }

    interface WithPartitionsAndCreate extends WithReplicasAndCreate {
      /**
       * Specifies the sku of the Search service.
       * @param partitionCount the number of partitions to be created
       * @return the next stage of the definition
       */
      WithReplicasAndCreate withPartitions(int partitionCount);
    }

    /**
     * The stage of the definition which contains all the minimum required inputs for
     * the resource to be created (via {@link WithCreate#create()}), but also allows
     * for any other optional settings to be specified.
     */
    interface WithCreate extends
        Creatable<SearchService>,
        Resource.DefinitionWithTags<WithCreate> {
    }
  }

  /**
   * The template for a Search service update operation, containing all the settings that can be modified.
   */
  interface Update extends
      Appliable<SearchService>,
      Resource.UpdateWithTags<Update>,
      SearchService.UpdateStages.WithReplicas,
      SearchService.UpdateStages.WithPartitions {
  }

  /**
   * Grouping of all the Search service update stages.
   */
  interface UpdateStages {

    /**
     * The stage of the Search service update allowing to modify the number of replicas used.
     */
    interface WithReplicas {
      /**
        * Specifies the Replicas count of the Search service.
        * @param replicaCount the replicas count; replicas distribute workloads across the service. You need 2 or more to support high availability (applies to Basic and Standard tiers only)
        * @return the next stage of the definition
       */
      Update withReplicas(int replicaCount);
    }

    /**
     * The stage of the Search service update allowing to modify the number of partitions used.
     */
    interface WithPartitions {
      /**
       * Specifies the Partitions count of the Search service.
       * @param partitionCount the partitions count; Partitions allow for scaling of document counts as well as faster data ingestion by spanning your index over multiple Azure Search Units (applies to Standard tiers only)
       * @return the next stage of the definition
       */
      Update withPartitions(int partitionCount);
    }
  }
}
