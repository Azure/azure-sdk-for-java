// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.search.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.search.SearchServiceManager;
import com.azure.resourcemanager.search.fluent.models.SearchServiceInner;
import reactor.core.publisher.Mono;

/**
 * An immutable client-side representation of an Azure Cognitive Search service.
 */
@Fluent
public interface SearchService extends
    GroupableResource<SearchServiceManager, SearchServiceInner>,
    Refreshable<SearchService>,
    Updatable<SearchService.Update> {

    /**
     * The hosting mode value.
     * <p>
     * Applicable only for the standard3 SKU. You can set this property to enable up to
     *   3 high density partitions that allow up to 1000 indexes, which is much higher than
     *   the maximum indexes allowed for any other SKU. For the standard3 SKU, the value is
     *   either 'default' or 'highDensity'. For all other SKUs, this value must be 'default'.
     *
     * @return the hosting mode value.
     */
    HostingMode hostingMode();

    /**
     * @return the number of partitions used by the service
     */
    int partitionCount();

    /**
     * The state of the last provisioning operation performed on the Search service.
     * <p>
     * Provisioning is an intermediate state that occurs while service capacity is being established.
     *   After capacity is set up, provisioningState changes to either 'succeeded' or 'failed'.
     *   Client applications can poll provisioning status (the recommended polling interval is
     *   from 30 seconds to one minute) by using the Get Search Service operation to see when
     *   an operation is completed. If you are using the free service, this value tends to come
     *   back as 'succeeded' directly in the call to Create Search service. This is because
     *   the free service uses capacity that is already set up.
     *
     * @return the provisioning state of the resource
     */
    ProvisioningState provisioningState();

    /**
     * @return the number of replicas used by the service
     */
    int replicaCount();

    /**
     * @return the SKU type of the service
     */
    Sku sku();

    /**
     * The status of the Search service.
     * <p>
     * Possible values include:
     *   'running':  the Search service is running and no provisioning operations are underway.
     *   'provisioning': the Search service is being provisioned or scaled up or down.
     *   'deleting': the Search service is being deleted.
     *   'degraded': the Search service is degraded. This can occur when the underlying search
     *     units are not healthy. The Search service is most likely operational,
     *     but performance might be slow and some requests might be dropped.
     *   'disabled': the Search service is disabled. In this state, the service will reject all API requests.
     *   'error': the Search service is in an error state. If your service is in the degraded,
     *     disabled, or error states, it means the Azure Search team is actively investigating
     *     the underlying issue. Dedicated services in these states are still chargeable based
     *     on the number of search units provisioned.
     *
     * @return the status of the service
     */
    SearchServiceStatus status();

    /**
     * @return the details of the status.
     */
    String statusDetails();

    /**
     * The primary and secondary admin API keys for the specified Azure Search service.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ManagementException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the AdminKeys object if successful
     */
    AdminKeys getAdminKeys();

    /**
     * The primary and secondary admin API keys for the specified Azure Search service.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return a representation of the future computation of this call
     */
    Mono<AdminKeys> getAdminKeysAsync();

    /**
     * Returns the list of query API keys for the given Azure Search service.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ManagementException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;QueryKey&gt; object if successful
     */
    PagedIterable<QueryKey> listQueryKeys();

    /**
     * Returns the list of query API keys for the given Azure Search service.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;QueryKey&gt; object
     */
    PagedFlux<QueryKey> listQueryKeysAsync();

    /**
     * Regenerates either the primary or secondary admin API key.
     * <p>
     * You can only regenerate one key at a time.
     *
     * @param keyKind specifies which key to regenerate
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ManagementException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the AdminKeys object if successful
     */
    AdminKeys regenerateAdminKeys(AdminKeyKind keyKind);

    /**
     * Regenerates either the primary or secondary admin API key. You can only regenerate one key at a time.
     *
     * @param keyKind Specifies which key to regenerate
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return a representation of the future computation of this call
     */
    Mono<AdminKeys> regenerateAdminKeysAsync(AdminKeyKind keyKind);

    /**
     * Regenerates either the primary or secondary admin API key.
     * <p>
     * You can only regenerate one key at a time.
     *
     * @param name The name of the new query API key.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ManagementException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the &lt;QueryKey&gt; object if successful
     */
    QueryKey createQueryKey(String name);

    /**
     * Regenerates either the primary or secondary admin API key.
     * <p>
     * You can only regenerate one key at a time.
     *
     * @param name The name of the new query API key.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return a representation of the future computation of this call
     */
    Mono<QueryKey> createQueryKeyAsync(String name);

    /**
     * Deletes the specified query key.
     * <p>
     * Unlike admin keys, query keys are not regenerated. The process for regenerating a query
     *   key is to delete and then recreate it.
     *
     * @param key The query key to be deleted. Query keys are identified by value, not by name.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ManagementException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    void deleteQueryKey(String key);

    /**
     * Deletes the specified query key.
     * <p>
     * Unlike admin keys, query keys are not regenerated. The process for
     *   regenerating a query key is to delete and then recreate it.
     *
     * @param key The query key to be deleted. Query keys are identified by value, not by name.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return a representation of the future computation of this call
     */
    Mono<Void> deleteQueryKeyAsync(String key);


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
         * The stage of the Search service definition allowing to specify the SKU.
         */
        interface WithSku {
            /**
             * Specifies the SKU of the Search service.
             *
             * @param skuName the SKU
             * @return the next stage of the definition
             */
            WithCreate withSku(SkuName skuName);

            /**
             * Specifies to use a free SKU type for the Search service.
             *
             * @return the next stage of the definition
             */
            WithCreate withFreeSku();

            /**
             * Specifies to use a basic SKU type for the Search service.
             *
             * @return the next stage of the definition
             */
            WithReplicasAndCreate withBasicSku();

            /**
             * Specifies to use a standard SKU type for the Search service.
             *
             * @return the next stage of the definition
             */
            WithPartitionsAndCreate withStandardSku();
        }

        interface WithReplicasAndCreate extends WithCreate {
            /**
             * Specifies the SKU of the Search service.
             *
             * @param count the number of replicas to be created
             * @return the next stage of the definition
             */
            WithCreate withReplicaCount(int count);
        }

        interface WithPartitionsAndCreate extends WithReplicasAndCreate {
            /**
             * Specifies the SKU of the Search service.
             *
             * @param count the number of partitions to be created
             * @return the next stage of the definition
             */
            WithReplicasAndCreate withPartitionCount(int count);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created
         *   (via {@link WithCreate#create()}), but also allows for any other optional settings to be specified.
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
        UpdateStages.WithReplicaCount,
        UpdateStages.WithPartitionCount {
    }

    /**
     * Grouping of all the Search service update stages.
     */
    interface UpdateStages {

        /**
         * The stage of the Search service update allowing to modify the number of replicas used.
         */
        interface WithReplicaCount {
            /**
             * Specifies the replicas count of the Search service.
             *
             * @param count the replicas count; replicas distribute workloads across the service. You need 2 or more
             *             to support high availability (applies to Basic and Standard tiers only)
             * @return the next stage of the definition
             */
            Update withReplicaCount(int count);
        }

        /**
         * The stage of the Search service update allowing to modify the number of partitions used.
         */
        interface WithPartitionCount {
            /**
             * Specifies the Partitions count of the Search service.
             * @param count the partitions count; Partitions allow for scaling of document counts as well
             *              as faster data ingestion by spanning your index over multiple Azure
             *              Search Units (applies to Standard tiers only)
             * @return the next stage of the definition
             */
            Update withPartitionCount(int count);
        }
    }
}
