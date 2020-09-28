// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.sql.SqlServerManager;
import reactor.core.publisher.Mono;

import java.util.List;

/** Entry point to SQL Server management API. */
@Fluent
public interface SqlServers
    extends SupportsCreating<SqlServer.DefinitionStages.Blank>,
        SupportsListing<SqlServer>,
        SupportsListingByResourceGroup<SqlServer>,
        SupportsGettingByResourceGroup<SqlServer>,
        SupportsGettingById<SqlServer>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<SqlServer>,
        SupportsBatchDeletion,
        HasManager<SqlServerManager> {

    /** @return the SQL Server Firewall Rules API entry point */
    SqlFirewallRuleOperations firewallRules();

    /** @return the SQL Server VirtualNetwork Rules API entry point */
    SqlVirtualNetworkRuleOperations virtualNetworkRules();

    /** @return the SQL Server DNS aliases API entry point */
    SqlServerDnsAliasOperations dnsAliases();

    /** @return the SQL Failover Group API entry point */
    SqlFailoverGroupOperations failoverGroups();

    /** @return the SQL Server Key entry point */
    SqlServerKeyOperations serverKeys();

    /** @return the SQL Encryption Protector entry point */
    SqlEncryptionProtectorOperations encryptionProtectors();

    /** @return entry point to manage SQL Server Security Alert Policy */
    SqlServerSecurityAlertPolicyOperations serverSecurityAlertPolicies();

    /** @return the SQL Sync Group entry point */
    SqlSyncGroupOperations syncGroups();

    /** @return the SQL Sync Group entry point */
    SqlSyncMemberOperations syncMembers();

    /** @return the SQL Server Elastic Pools API entry point */
    SqlElasticPoolOperations elasticPools();

    /** @return the SQL Server Database API entry point */
    SqlDatabaseOperations databases();

    /**
     * Checks if the specified container registry name is valid and available.
     *
     * @param name the container registry name to check
     * @return whether the name is available and other info if not
     */
    CheckNameAvailabilityResult checkNameAvailability(String name);

    /**
     * Checks if container registry name is valid and is not in use asynchronously.
     *
     * @param name the container registry name to check
     * @return a representation of the future computation of this call, returning whether the name is available or other
     *     info if not
     */
    Mono<CheckNameAvailabilityResult> checkNameAvailabilityAsync(String name);

    /**
     * Gets the Azure SQL server capabilities for a given Azure region.
     *
     * @param region the location to get the Azure SQL server capabilities for
     * @return the server capabilities object
     */
    RegionCapabilities getCapabilitiesByRegion(Region region);

    /**
     * Gets the Azure SQL server capabilities for a given Azure region asynchronously.
     *
     * @param region the location to get the Azure SQL server capabilities for
     * @return a representation of the future computation of this call, returning the server capabilities object
     */
    Mono<RegionCapabilities> getCapabilitiesByRegionAsync(Region region);

    /**
     * Lists the Azure SQL server usages for a given Azure region.
     *
     * @param region the location to get the Azure SQL server usages for
     * @return the SQL usage object
     */
    List<SqlSubscriptionUsageMetric> listUsageByRegion(Region region);

    /**
     * Lists the Azure SQL server usages for a given Azure region asynchronously.
     *
     * @param region the location to get the Azure SQL server usages for
     * @return a representation of the future computation of this call, returning the server usages object
     */
    PagedFlux<SqlSubscriptionUsageMetric> listUsageByRegionAsync(Region region);
}
