// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.cosmos.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.resourcemanager.cosmos.CosmosManager;
import com.azure.resourcemanager.cosmos.fluent.models.DatabaseAccountGetResultsInner;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsUpdatingPrivateEndpointConnection;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/** An immutable client-side representation of an Azure Cosmos DB. */
@Fluent
public interface CosmosDBAccount
    extends GroupableResource<CosmosManager, DatabaseAccountGetResultsInner>,
    Refreshable<CosmosDBAccount>,
    Updatable<CosmosDBAccount.Update>,
    SupportsUpdatingPrivateEndpointConnection {

    /** @return indicates the type of database account */
    DatabaseAccountKind kind();

    /** @return the connection endpoint for the CosmosDB database account */
    String documentEndpoint();

    /** @return the offer type for the CosmosDB database account */
    DatabaseAccountOfferType databaseAccountOfferType();

    /**
     * @return specifies the set of IP addresses or IP address ranges in CIDR form.
     * @deprecated use {@link #ipRules()}
     */
    @Deprecated
    String ipRangeFilter();

    /** @return specifies the set of IP addresses or IP address ranges in CIDR form. */
    List<IpAddressOrRange> ipRules();

    /** @return the consistency policy for the CosmosDB database account */
    ConsistencyPolicy consistencyPolicy();

    /** @return the default consistency level for the CosmosDB database account */
    DefaultConsistencyLevel defaultConsistencyLevel();

    /** @return an array that contains the writable georeplication locations enabled for the CosmosDB account */
    List<Location> writableReplications();

    /** @return an array that contains the readable georeplication locations enabled for the CosmosDB account */
    List<Location> readableReplications();

    /** @return the access keys for the specified Azure CosmosDB database account */
    DatabaseAccountListKeysResult listKeys();

    /** @return the access keys for the specified Azure CosmosDB database account */
    Mono<DatabaseAccountListKeysResult> listKeysAsync();

    /** @return the read-only access keys for the specified Azure CosmosDB database account */
    DatabaseAccountListReadOnlyKeysResult listReadOnlyKeys();

    /** @return the read-only access keys for the specified Azure CosmosDB database account */
    Mono<DatabaseAccountListReadOnlyKeysResult> listReadOnlyKeysAsync();

    /** @return the connection strings for the specified Azure CosmosDB database account */
    DatabaseAccountListConnectionStringsResult listConnectionStrings();

    /** @return the connection strings for the specified Azure CosmosDB database account */
    Mono<DatabaseAccountListConnectionStringsResult> listConnectionStringsAsync();

    /** @return the list of Azure Cosmos DB SQL databases */
    List<SqlDatabase> listSqlDatabases();

    /** @return the list of Azure Cosmos DB SQL databases */
    PagedFlux<SqlDatabase> listSqlDatabasesAsync();

    /** @return whether write is enabled for multiple locations or not */
    boolean multipleWriteLocationsEnabled();

    /** @return whether cassandra connector is enabled or not. */
    boolean cassandraConnectorEnabled();

    /** @return the current cassandra connector offer. */
    ConnectorOffer cassandraConnectorOffer();

    /** @return whether metadata write access is disabled or not. */
    boolean keyBasedMetadataWriteAccessDisabled();

    /** @return all private link resources in the account. */
    PagedFlux<PrivateLinkResource> listPrivateLinkResourcesAsync();

    /** @return all private link resources in the account. */
    List<PrivateLinkResource> listPrivateLinkResources();

    /**
     * @param groupName group name of private link resource
     * @return the specific private link resource group
     */
    Mono<PrivateLinkResource> getPrivateLinkResourceAsync(String groupName);

    /**
     * @param groupName group name of private link resource
     * @return the specific private link resource group
     */
    PrivateLinkResource getPrivateLinkResource(String groupName);

    /** @return all private endpoint connection in the account. */
    Mono<Map<String, PrivateEndpointConnection>> listPrivateEndpointConnectionAsync();

    /** @return all private endpoint connection in the account. */
    Map<String, PrivateEndpointConnection> listPrivateEndpointConnection();

    /**
     * @param name name of private endpoint connection
     * @return the specific private endpoint connection
     */
    Mono<PrivateEndpointConnection> getPrivateEndpointConnectionAsync(String name);

    /**
     * @param name name of private endpoint connection
     * @return the specific private endpoint connection
     */
    PrivateEndpointConnection getPrivateEndpointConnection(String name);

    /** @return a list that contains the Cosmos DB capabilities */
    List<Capability> capabilities();

    /** @return a list that contains the Cosmos DB Virtual Network ACL Rules (empty list if none is set) */
    List<VirtualNetworkRule> virtualNetworkRules();

    /**
     * It takes offline the specified region for the current Azure Cosmos DB database account.
     *
     * @param region Cosmos DB region
     */
    void offlineRegion(Region region);

    /**
     * Asynchronously it takes offline the specified region for the current Azure Cosmos DB database account.
     *
     * @param region Cosmos DB region
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> offlineRegionAsync(Region region);

    /**
     * It brings online the specified region for the current Azure Cosmos DB database account.
     *
     * @param region Cosmos DB region
     */
    void onlineRegion(Region region);

    /**
     * Asynchronously it brings online the specified region for the current Azure Cosmos DB database account.
     *
     * @param region Cosmos DB region
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> onlineRegionAsync(Region region);

    /** @param keyKind the key kind */
    void regenerateKey(KeyKind keyKind);

    /**
     * @param keyKind the key kind
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> regenerateKeyAsync(KeyKind keyKind);

    /** Grouping of cosmos db definition stages. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithKind,
            DefinitionStages.WithWriteReplication,
            DefinitionStages.WithReadReplication,
            DefinitionStages.WithCreate {
    }

    /** Grouping of cosmos db definition stages. */
    interface DefinitionStages {
        /** The first stage of a cosmos db definition. */
        interface Blank extends DefinitionWithRegion<WithGroup> {
        }

        /** The stage of the cosmos db definition allowing to specify the resource group. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithKind> {
        }

        /** The stage of the cosmos db definition allowing to set the database account kind. */
        interface WithKind {
            /**
             * The database account kind for the CosmosDB account.
             *
             * @param kind the account kind
             * @return the next stage of the definition
             */
            WithConsistencyPolicy withKind(DatabaseAccountKind kind);

            /**
             * The database account kind for the CosmosDB account.
             *
             * @param kind the account kind
             * @param capabilities the list of Cosmos DB capabilities for the account
             * @return the next stage of the definition
             */
            WithConsistencyPolicy withKind(DatabaseAccountKind kind, Capability... capabilities);

            /**
             * Creates a SQL CosmosDB account.
             *
             * @return the next stage of the definition
             */
            WithConsistencyPolicy withDataModelSql();

            /**
             * Creates a MongoDB CosmosDB account.
             *
             * @return the next stage of the definition
             */
            WithConsistencyPolicy withDataModelMongoDB();

            /**
             * Creates a Cassandra CosmosDB account.
             *
             * @return the next stage of the definition
             */
            WithConsistencyPolicy withDataModelCassandra();

            /**
             * Creates an Azure Table CosmosDB account.
             *
             * @return the next stage of the definition
             */
            WithConsistencyPolicy withDataModelAzureTable();

            /**
             * Creates a Gremlin CosmosDB account.
             *
             * @return the next stage of the definition
             */
            WithConsistencyPolicy withDataModelGremlin();
        }

        /** The stage of the cosmos db definition allowing to set the consistency policy. */
        interface WithConsistencyPolicy {
            /**
             * The eventual consistency policy for the CosmosDB account.
             *
             * @return the next stage of the definition
             */
            WithWriteReplication withEventualConsistency();

            /**
             * The session consistency policy for the CosmosDB account.
             *
             * @return the next stage of the definition
             */
            WithWriteReplication withSessionConsistency();

            /**
             * The bounded staleness consistency policy for the CosmosDB account.
             *
             * @param maxStalenessPrefix the max staleness prefix
             * @param maxIntervalInSeconds the max interval in seconds
             * @return the next stage of the definition
             */
            WithWriteReplication withBoundedStalenessConsistency(long maxStalenessPrefix, int maxIntervalInSeconds);

            /**
             * The strong consistency policy for the CosmosDB account.
             *
             * @return the next stage of the definition
             */
            WithCreate withStrongConsistency();
        }

        /** The stage of the cosmos db definition allowing to set the IP range filter. */
        interface WithIpRules {
            /**
             * CosmosDB Firewall Support: This value specifies the set of IP addresses or IP address ranges in CIDR form
             * to be included as the allowed list of client IPs for a given database account. IP addresses/ranges must
             * be comma separated and must not contain any spaces.
             *
             * @param ipRangeFilter specifies the set of IP addresses or IP address ranges
             * @return the next stage of the definition
             * @deprecated use {{@link #withIpRules(List)}}
             */
            @Deprecated
            WithCreate withIpRangeFilter(String ipRangeFilter);

            /**
             * CosmosDB Firewall Support: This value specifies the set of IP addresses or IP address ranges in CIDR form
             * to be included as the allowed list of client IPs for a given database account.
             *
             * @param ipRules specifies the set of IP addresses or IP address ranges
             * @return the next stage of the definition
             */
            WithCreate withIpRules(List<IpAddressOrRange> ipRules);
        }

        /** The stage of the cosmos db definition allowing the definition of a read location. */
        interface WithWriteReplication {
            /**
             * A georeplication location for the CosmosDB account.
             *
             * @param region the region for the location
             * @return the next stage
             */
            WithCreate withWriteReplication(Region region);
        }

        /** The stage of the cosmos db definition allowing the definition of a write location. */
        interface WithReadReplication {
            /**
             * A georeplication location for the CosmosDB account.
             *
             * @param region the region for the location
             * @return the next stage
             */
            WithCreate withReadReplication(Region region);
        }

        /** The stage of the cosmos db definition allowing the definition of a Virtual Network ACL Rule. */
        interface WithVirtualNetworkRule {
            /**
             * Specifies a Virtual Network ACL Rule for the CosmosDB account.
             *
             * @param virtualNetworkId the ID of a virtual network
             * @param subnetName the name of the subnet within the virtual network; the subnet must have the service
             *     endpoints enabled for 'Microsoft.AzureCosmosDB'.
             * @return the next stage
             */
            WithCreate withVirtualNetwork(String virtualNetworkId, String subnetName);

            /**
             * Specifies the list of Virtual Network ACL Rules for the CosmosDB account.
             *
             * @param virtualNetworkRules the list of Virtual Network ACL Rules.
             * @return the next stage
             */
            WithCreate withVirtualNetworkRules(List<VirtualNetworkRule> virtualNetworkRules);
        }

        /**
         * The stage of the cosmos db definition allowing to specify whether multiple write locations will be enabled.
         */
        interface WithMultipleLocations {

            /**
             * Specifies whether multiple write locations are enabled for this cosmos db account.
             *
             * @param enabled whether multiple write locations are enabled or not.
             * @return the next stage
             */
            WithCreate withMultipleWriteLocationsEnabled(boolean enabled);
        }

        /** The stage of the cosmos db definition allowing to specify cassandra connector offer. */
        interface WithConnector {
            /**
             * Specifies a connector offer for cassandra connector.
             *
             * @param connectorOffer connector offer to specify.
             * @return the next stage
             */
            WithCreate withCassandraConnector(ConnectorOffer connectorOffer);
        }

        /** The stage of the cosmos db definition allowing to specify metadata write access. */
        interface WithKeyBasedMetadataWriteAccess {
            /**
             * Specifies whether metadata write access should be disabled.
             *
             * @param disabled whether metadata write access is disabled or not.
             * @return the next stage
             */
            WithCreate withDisableKeyBaseMetadataWriteAccess(boolean disabled);
        }

        /** The stage of the cosmos db definition allowing to specify private endpoint connection. */
        interface WithPrivateEndpointConnection {
            /**
             * Starts the definition of a private endpoint connection to be attached to the cosmos db account.
             *
             * @param name the reference name for the private endpoint connection
             * @return the first stage of a private endpoint connection definition
             */
            PrivateEndpointConnection.DefinitionStages.Blank<WithCreate> defineNewPrivateEndpointConnection(
                String name);
        }
        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created,
         * but also allows for any other optional settings to be specified.
         */
        interface WithCreate
            extends Creatable<CosmosDBAccount>,
                WithConsistencyPolicy,
                WithReadReplication,
                WithIpRules,
                WithVirtualNetworkRule,
                WithMultipleLocations,
                WithConnector,
                WithKeyBasedMetadataWriteAccess,
                WithPrivateEndpointConnection,
                DefinitionWithTags<WithCreate> {
        }
    }

    /** Grouping of cosmos db update stages. */
    interface Update extends UpdateStages.WithReadLocations, UpdateStages.WithOptionals {
    }

    /** Grouping of cosmos db update stages. */
    interface UpdateStages {
        /** Grouping of cosmos db update stages. */
        interface WithOptionals
            extends Resource.UpdateWithTags<WithOptionals>,
                Appliable<CosmosDBAccount>,
                UpdateStages.WithConsistencyPolicy,
                UpdateStages.WithVirtualNetworkRule,
                UpdateStages.WithMultipleLocations,
                UpdateStages.WithConnector,
                UpdateStages.WithKeyBasedMetadataWriteAccess,
                UpdateStages.WithPrivateEndpointConnection,
                UpdateStages.WithIpRules {
        }

        /** The stage of the cosmos db definition allowing the definition of a write location. */
        interface WithReadLocations extends Appliable<CosmosDBAccount> {
            /**
             * A georeplication location for the CosmosDB account.
             *
             * @param region the region for the location
             * @return the next stage
             */
            WithReadLocations withReadReplication(Region region);

            /**
             * A georeplication location for the CosmosDB account.
             *
             * @param region the region for the location
             * @return the next stage
             */
            WithReadLocations withoutReadReplication(Region region);
        }

        /** The stage of the cosmos db update allowing to set the consistency policy. */
        interface WithConsistencyPolicy {
            /**
             * The consistency policy for the CosmosDB account.
             *
             * @return the next stage of the definition
             */
            WithOptionals withEventualConsistency();

            /**
             * The consistency policy for the CosmosDB account.
             *
             * @return the next stage of the definition
             */
            WithOptionals withSessionConsistency();

            /**
             * The consistency policy for the CosmosDB account.
             *
             * @param maxStalenessPrefix the max staleness prefix
             * @param maxIntervalInSeconds the max interval in seconds
             * @return the next stage of the definition
             */
            WithOptionals withBoundedStalenessConsistency(long maxStalenessPrefix, int maxIntervalInSeconds);

            /**
             * The consistency policy for the CosmosDB account.
             *
             * @return the next stage of the definition
             */
            WithOptionals withStrongConsistency();
        }

        /** The stage of the cosmos db definition allowing to set the IP range filter. */
        interface WithIpRules {
            /**
             * CosmosDB Firewall Support: This value specifies the set of IP addresses or IP address ranges in CIDR form
             * to be included as the allowed list of client IPs for a given database account. IP addresses/ranges must
             * be comma separated and must not contain any spaces.
             *
             * @param ipRangeFilter specifies the set of IP addresses or IP address ranges
             * @return the next stage of the update definition
             * @deprecated use {{@link #withIpRules(List)}}
             */
            @Deprecated
            WithOptionals withIpRangeFilter(String ipRangeFilter);

            /**
             * CosmosDB Firewall Support: This value specifies the set of IP addresses or IP address ranges in CIDR form
             * to be included as the allowed list of client IPs for a given database account.
             *
             * @param ipRules specifies the set of IP addresses or IP address ranges
             * @return the next stage of the update definition
             */
            WithOptionals withIpRules(List<IpAddressOrRange> ipRules);
        }

        /** The stage of the Cosmos DB update definition allowing the definition of a Virtual Network ACL Rule. */
        interface WithVirtualNetworkRule {
            /**
             * Specifies a new Virtual Network ACL Rule for the CosmosDB account.
             *
             * @param virtualNetworkId the ID of a virtual network
             * @param subnetName the name of the subnet within the virtual network; the subnet must have the service
             *     endpoints enabled for 'Microsoft.AzureCosmosDB'.
             * @return the next stage of the update definition
             */
            WithOptionals withVirtualNetwork(String virtualNetworkId, String subnetName);

            /**
             * Removes a Virtual Network ACL Rule for the CosmosDB account.
             *
             * @param virtualNetworkId the ID of a virtual network
             * @param subnetName the name of the subnet within the virtual network; the subnet must have the service
             *     endpoints enabled for 'Microsoft.AzureCosmosDB'.
             * @return the next stage of the update definition
             */
            WithOptionals withoutVirtualNetwork(String virtualNetworkId, String subnetName);

            /**
             * A Virtual Network ACL Rule for the CosmosDB account.
             *
             * @param virtualNetworkRules the list of Virtual Network ACL Rules (an empty list value will remove all the
             *     rules)
             * @return the next stage of the update definition-
             */
            WithOptionals withVirtualNetworkRules(List<VirtualNetworkRule> virtualNetworkRules);
        }

        /**
         * The stage of the Cosmos DB update definition allowing to specify whether multiple write locations are enabled
         * or not.
         */
        interface WithMultipleLocations {

            /**
             * Specifies whether multiple write locations are enabled or not for this cosmos db account.
             *
             * @param enabled whether multiple write locatiosn are enabled or not.
             * @return the next stage of the update definition
             */
            WithOptionals withMultipleWriteLocationsEnabled(boolean enabled);
        }

        /** The stage of the cosmos db update allowing to specify cassandra connector offer. */
        interface WithConnector {
            /**
             * Specifies a connector offer for cassandra connector.
             *
             * @param connectorOffer connector offer to specify.
             * @return the next stage
             */
            WithOptionals withCassandraConnector(ConnectorOffer connectorOffer);

            /**
             * Remove the connector offer.
             *
             * @return the next stage
             */
            WithOptionals withoutCassandraConnector();
        }

        /** The stage of the cosmos db update allowing to specify metadata write access. */
        interface WithKeyBasedMetadataWriteAccess {
            /**
             * Specifies whether metadata write access should be disabled.
             *
             * @param disabled whether metadata write access is disabled or not.
             * @return the next stage
             */
            WithOptionals withDisableKeyBaseMetadataWriteAccess(boolean disabled);
        }

        /** The stage of the cosmos db update allowing to specify private endpoint connection. */
        interface WithPrivateEndpointConnection {
            /**
             * Start the definition of a private endpoint connection to be attached to the cosmos db account.
             *
             * @param name the reference name for the private endpoint connection
             * @return the first stage of a private endpoint connection definition
             */
            PrivateEndpointConnection.UpdateDefinitionStages.Blank<WithOptionals> defineNewPrivateEndpointConnection(
                String name);

            /**
             * Start the update of an existing private endpoint connection.
             *
             * @param name the reference name for the private endpoint connection
             * @return the first stage of a private endpoint connection update
             */
            PrivateEndpointConnection.Update updatePrivateEndpointConnection(String name);

            /**
             * Remove an existing private endpoint connection.
             *
             * @param name the reference name for the private endpoint connection
             * @return the next stage
             */
            WithOptionals withoutPrivateEndpointConnection(String name);
        }
    }
}
