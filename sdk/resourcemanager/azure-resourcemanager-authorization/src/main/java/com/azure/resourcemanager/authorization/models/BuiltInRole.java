// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for roles. */
public final class BuiltInRole extends ExpandableStringEnum<BuiltInRole> {
    /** A role that can manage API Management service and the APIs. */
    public static final BuiltInRole API_MANAGEMENT_SERVICE_CONTRIBUTOR =
        BuiltInRole.fromString("API Management Service Contributor");

    /** A role that can manage API Management service, but not the APIs themselves. */
    public static final BuiltInRole API_MANAGEMENT_SERVICE_OPERATOR_ROLE =
        BuiltInRole.fromString("API Management Service Operator Role");

    /** A role that has read-only access to API Management service and APIs. */
    public static final BuiltInRole API_MANAGEMENT_SERVICE_READER_ROLE =
        BuiltInRole.fromString("API Management Service Reader Role");

    /** A role that can manage Application Insights components. */
    public static final BuiltInRole APPLICATION_INSIGHTS_COMPONENT_CONTRIBUTOR =
        BuiltInRole.fromString("Application Insights Component Contributor");

    /** A role that is able to start, stop, suspend, and resume jobs. */
    public static final BuiltInRole AUTOMATION_OPERATOR = BuiltInRole.fromString("Automation Operator");

    /** A role that can manage backup in Recovery Services vault. */
    public static final BuiltInRole BACKUP_CONTRIBUTOR = BuiltInRole.fromString("Backup Contributor");

    /** A role that can manage backup except removing backup, in Recovery Services vault. */
    public static final BuiltInRole BACKUP_OPERATOR = BuiltInRole.fromString("Backup Operator");

    /** A role that can view all backup management services. */
    public static final BuiltInRole BACKUP_READER = BuiltInRole.fromString("Backup Reader");

    /** A role that can view all billing information. */
    public static final BuiltInRole BILLING_READER = BuiltInRole.fromString("Billing Reader");

    /** A role that can manage BizTalk services. */
    public static final BuiltInRole BIZTALK_CONTRIBUTOR = BuiltInRole.fromString("BizTalk Contributor");

    /** A role that can manage ClearDB MySQL databases. */
    public static final BuiltInRole CLEARDB_MYSQL_DB_CONTRIBUTOR =
        BuiltInRole.fromString("ClearDB MySQL DB Contributor");

    /** A role that can manage everything except access.. */
    public static final BuiltInRole CONTRIBUTOR = BuiltInRole.fromString("Contributor");

    /** A role that can create and manage data factories, and child resources within them.. */
    public static final BuiltInRole DATA_FACTORY_CONTRIBUTOR = BuiltInRole.fromString("Data Factory Contributor");

    /** A role that can view everything and connect, start, restart, and shutdown virtual machines. */
    public static final BuiltInRole DEVTEST_LABS_USER = BuiltInRole.fromString("DevTest Labs User");

    /** A role that can manage DNS zones and records. */
    public static final BuiltInRole DNS_ZONE_CONTRIBUTOR = BuiltInRole.fromString("DNS Zone Contributor");

    /** A role that can manage Azure Cosmos DB accounts. */
    public static final BuiltInRole AZURE_COSMOS_DB_ACCOUNT_CONTRIBUTOR =
        BuiltInRole.fromString("Azure Cosmos DB Account Contributor");

    /** A role that can manage Intelligent Systems accounts. */
    public static final BuiltInRole INTELLIGENT_SYSTEMS_ACCOUNT_CONTRIBUTOR =
        BuiltInRole.fromString("Intelligent Systems Account Contributor");

    /** A role that can manage user assigned identities. */
    public static final BuiltInRole MANAGED_IDENTITY_CONTRIBUTOR =
        BuiltInRole.fromString("Managed Identity Contributor");

    /** A role that can read and assign user assigned identities. */
    public static final BuiltInRole MANAGED_IDENTITY_OPERATOR = BuiltInRole.fromString("Managed Identity Operator");

    /** A role that can read all monitoring data. */
    public static final BuiltInRole MONITORING_READER = BuiltInRole.fromString("Monitoring Reader");

    /** A role that can read monitoring data and edit monitoring settings. */
    public static final BuiltInRole MONITORING_CONTRIBUTOR = BuiltInRole.fromString("Monitoring Contributor");

    /** A role that can manage all network resources. */
    public static final BuiltInRole NETWORK_CONTRIBUTOR = BuiltInRole.fromString("Network Contributor");

    /** A role that can manage New Relic Application Performance Management accounts and applications. */
    public static final BuiltInRole NEW_RELIC_APM_ACCOUNT_CONTRIBUTOR =
        BuiltInRole.fromString("New Relic APM Account Contributor");

    /** A role that can manage everything, including access. */
    public static final BuiltInRole OWNER = BuiltInRole.fromString("Owner");

    /** A role that can view everything, but can't make changes. */
    public static final BuiltInRole READER = BuiltInRole.fromString("Reader");

    /** A role that can manage Redis caches. */
    public static final BuiltInRole REDIS_CACHE_CONTRIBUTOR = BuiltInRole.fromString("Redis Cache Contributor");

    /** A role that can manage scheduler job collections. */
    public static final BuiltInRole SCHEDULER_JOB_COLLECTIONS_CONTRIBUTOR =
        BuiltInRole.fromString("Scheduler Job Collections Contributor");

    /** A role that can manage search services. */
    public static final BuiltInRole SEARCH_SERVICE_CONTRIBUTOR = BuiltInRole.fromString("Search Service Contributor");

    /** A role that can manage security components, security policies, and virtual machines. */
    public static final BuiltInRole SECURITY_MANAGER = BuiltInRole.fromString("Security Manager");

    /** A role that can manage SQL databases, but not their security-related policies. */
    public static final BuiltInRole SQL_DB_CONTRIBUTOR = BuiltInRole.fromString("SQL DB Contributor");

    /** A role that can manage the security-related policies of SQL servers and databases. */
    public static final BuiltInRole SQL_SECURITY_MANAGER = BuiltInRole.fromString("SQL Security Manager");

    /** A role that can manage SQL servers and databases, but not their security-related policies. */
    public static final BuiltInRole SQL_SERVER_CONTRIBUTOR = BuiltInRole.fromString("SQL Server Contributor");

    /** A role that can manage classic storage accounts. */
    public static final BuiltInRole CLASSIC_STORAGE_ACCOUNT_CONTRIBUTOR =
        BuiltInRole.fromString("Classic Storage Account Contributor");

    /** A role that can manage storage accounts. */
    public static final BuiltInRole STORAGE_ACCOUNT_CONTRIBUTOR = BuiltInRole.fromString("Storage Account Contributor");

    /** A role that can manage user access to Azure resources. */
    public static final BuiltInRole USER_ACCESS_ADMINISTRATOR = BuiltInRole.fromString("User Access Administrator");

    /**
     * A role that can manage classic virtual machines, but not the virtual network or storage account to which they are
     * connected.
     */
    public static final BuiltInRole CLASSIC_VIRTUAL_MACHINE_CONTRIBUTOR =
        BuiltInRole.fromString("Classic Virtual Machine Contributor");

    /**
     * A role that can manage virtual machines, but not the virtual network or storage account to which they are
     * connected.
     */
    public static final BuiltInRole VIRTUAL_MACHINE_CONTRIBUTOR = BuiltInRole.fromString("Virtual Machine Contributor");

    /** A role that can manage classic virtual networks and reserved IPs. */
    public static final BuiltInRole CLASSIC_NETWORK_CONTRIBUTOR = BuiltInRole.fromString("Classic Network Contributor");

    /** A role that can manage web plans. */
    public static final BuiltInRole WEB_PLAN_CONTRIBUTOR = BuiltInRole.fromString("Web Plan Contributor");

    /** A role that can manage websites, but not the web plans to which they are connected. */
    public static final BuiltInRole WEBSITE_CONTRIBUTOR = BuiltInRole.fromString("Website Contributor");

    /**
     * Finds or creates a role instance based on the specified name.
     *
     * @param name a name
     * @return a BuiltInRole instance
     */
    public static BuiltInRole fromString(String name) {
        return fromString(name, BuiltInRole.class);
    }

    /** @return known roles */
    public static Collection<BuiltInRole> values() {
        return values(BuiltInRole.class);
    }
}
