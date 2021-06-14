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

    /** A role that can manage key vaults, but not access to them. */
    public static final BuiltInRole KEY_VAULT_CONTRIBUTOR = BuiltInRole.fromString("Key Vault Contributor");

    // Storage data related roles
    /** Storage Account Key Operators are allowed to list and regenerate keys on Storage Accounts. */
    public static final BuiltInRole STORAGE_ACCOUNT_KEY_OPERATOR_SERVICE_ROLE =
        BuiltInRole.fromString("Storage Account Key Operator Service Role");
    /** Allows for read, write and delete access to Azure Storage blob containers and data. */
    public static final BuiltInRole STORAGE_BLOB_DATA_CONTRIBUTOR =
        BuiltInRole.fromString("Storage Blob Data Contributor");
    /** Allows for full access to Azure Storage blob containers and data, including assigning POSIX access control. */
    public static final BuiltInRole STORAGE_BLOB_DATA_OWNER =
        BuiltInRole.fromString("Storage Blob Data Owner");
    /** Allows for read access to Azure Storage blob containers and data. */
    public static final BuiltInRole STORAGE_BLOB_DATA_READER =
        BuiltInRole.fromString("Storage Blob Data Reader");
    /** Allows for read, write, and delete access to Azure Storage queues and queue messages. */
    public static final BuiltInRole STORAGE_QUEUE_DATA_CONTRIBUTOR =
        BuiltInRole.fromString("Storage Queue Data Contributor");
    /** Allows for peek, receive, and delete access to Azure Storage queue messages. */
    public static final BuiltInRole STORAGE_QUEUE_DATA_MESSAGE_PROCESSOR =
        BuiltInRole.fromString("Storage Queue Data Message Processor");
    /** Allows for sending of Azure Storage queue messages. */
    public static final BuiltInRole STORAGE_QUEUE_DATA_MESSAGE_SENDER =
        BuiltInRole.fromString("Storage Queue Data Message Sender");
    /** Allows for read access to Azure Storage queues and queue messages. */
    public static final BuiltInRole STORAGE_QUEUE_DATA_READER =
        BuiltInRole.fromString("Storage Queue Data Reader");
    /** Allows for read access to Azure File Share over SMB. */
    public static final BuiltInRole STORAGE_FILE_DATA_SMB_SHARE_READER =
        BuiltInRole.fromString("Storage File Data SMB Share Reader");
    /** Allows for read, write, and delete access in Azure Storage file shares over SMB. */
    public static final BuiltInRole STORAGE_FILE_DATA_SMB_SHARE_CONTRIBUTOR =
        BuiltInRole.fromString("Storage File Data SMB Share Contributor");

    // Key Vault data related roles
    /**
     * Perform all data plane operations on a key vault and all objects in it, including certificates, keys, and
     * secrets.
     */
    public static final BuiltInRole KEY_VAULT_ADMINISTRATOR =
        BuiltInRole.fromString("Key Vault Administrator");
    /** Perform any action on the keys of a key vault, except manage permissions. */
    public static final BuiltInRole KEY_VAULT_CRYPTO_OFFICER =
        BuiltInRole.fromString("Key Vault Crypto Officer");
    /** Perform cryptographic operations using keys. */
    public static final BuiltInRole KEY_VAULT_CRYPTO_USER =
        BuiltInRole.fromString("Key Vault Crypto User");
    /** Perform any action on the secrets of a key vault, except manage permissions. */
    public static final BuiltInRole KEY_VAULT_SECRETS_OFFICER =
        BuiltInRole.fromString("Key Vault Secrets Officer");
    /** Read secret contents. */
    public static final BuiltInRole KEY_VAULT_SECRETS_USER =
        BuiltInRole.fromString("Key Vault Secrets User");
    /** Perform any action on the certificates of a key vault, except manage permissions. */
    public static final BuiltInRole KEY_VAULT_CERTIFICATES_OFFICER =
        BuiltInRole.fromString("Key Vault Certificates Officer");
    /** Read metadata of key vaults and its certificates, keys, and secrets. */
    public static final BuiltInRole KEY_VAULT_READER =
        BuiltInRole.fromString("Key Vault Reader");
    /** Read metadata of keys and perform wrap/unwrap operations. */
    public static final BuiltInRole KEY_VAULT_CRYPTO_SERVICE_ENCRYPTION_USER =
        BuiltInRole.fromString("Key Vault Crypto Service Encryption User");

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
