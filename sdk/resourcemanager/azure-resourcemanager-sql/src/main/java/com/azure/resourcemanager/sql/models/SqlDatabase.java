// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.sql.fluent.models.DatabaseInner;
import com.azure.resourcemanager.storage.models.StorageAccount;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/** An immutable client-side representation of an Azure SQL Server Database. */
@Fluent
public interface SqlDatabase
    extends ExternalChildResource<SqlDatabase, SqlServer>,
        HasInnerModel<DatabaseInner>,
        HasResourceGroup,
        Refreshable<SqlDatabase>,
        Updatable<SqlDatabase.Update> {

    /** @return name of the SQL Server to which this database belongs */
    String sqlServerName();

    /** @return the collation of the Azure SQL Database */
    String collation();

    /** @return the creation date of the Azure SQL Database */
    OffsetDateTime creationDate();

    /**
     * @return the current Service Level Objective Name of the Azure SQL Database, this is the Name of the Service Level
     *     Objective that is currently active
     */
    String currentServiceObjectiveName();

    /** @return the Id of the Azure SQL Database */
    String databaseId();

    /**
     * @return the recovery period start date of the Azure SQL Database. This records the start date and time when
     *     recovery is available for this Azure SQL Database.
     */
    OffsetDateTime earliestRestoreDate();

    /** @return the edition of the Azure SQL Database */
    DatabaseEdition edition();

    /** @return the max size of the Azure SQL Database expressed in bytes. */
    long maxSizeBytes();

    /**
     * @return the name of the configured Service Level Objective of the Azure SQL Database, this is the Service Level
     *     Objective that is being applied to the Azure SQL Database
     */
    String requestedServiceObjectiveName();

    /** @return the status of the Azure SQL Database */
    DatabaseStatus status();

    /** @return the elasticPoolId value */
    String elasticPoolId();

    /** @return the elasticPoolName value */
    String elasticPoolName();

    /** @return the defaultSecondaryLocation value */
    String defaultSecondaryLocation();

    /** @return the parent SQL server ID */
    String parentId();

    /** @return the name of the region the resource is in */
    String regionName();

    /** @return the region the resource is in */
    Region region();

    /** @return true if this Database is SqlWarehouse */
    boolean isDataWarehouse();

    /** @return SqlWarehouse instance for more operations */
    SqlWarehouse asWarehouse();

    /** @return the list of all restore points on this database */
    List<RestorePoint> listRestorePoints();

    /** @return the list of all restore points on this database */
    PagedFlux<RestorePoint> listRestorePointsAsync();

    /**
     * @param filter an OData filter expression that describes a subset of metrics to return.
     * @return the list of metrics for this database
     */
    List<SqlDatabaseMetric> listMetrics(String filter);

    /**
     * @param filter an OData filter expression that describes a subset of metrics to return.
     * @return a representation of the deferred computation of the metrics for this database
     */
    PagedFlux<SqlDatabaseMetric> listMetricsAsync(String filter);

    /** @return the list of metric definitions for this database */
    List<SqlDatabaseMetricDefinition> listMetricDefinitions();

    /** @return a representation of the deferred computation of the metric definitions for this database */
    PagedFlux<SqlDatabaseMetricDefinition> listMetricDefinitionsAsync();

    /**
     * Gets an Azure SQL Database Transparent Data Encryption for this database.
     *
     * @return an Azure SQL Database Transparent Data Encryption for this database
     */
    TransparentDataEncryption getTransparentDataEncryption();

    /**
     * Gets an Azure SQL Database Transparent Data Encryption for this database.
     *
     * @return a representation of the deferred computation of an Azure SQL Database Transparent Data Encryption for
     *     this database
     */
    Mono<TransparentDataEncryption> getTransparentDataEncryptionAsync();

    /** @return information about service tier advisors for the current database */
    Map<String, ServiceTierAdvisor> listServiceTierAdvisors();

    /**
     * @return a representation of the deferred computation of the information about service tier advisors for this
     *     database
     */
    PagedFlux<ServiceTierAdvisor> listServiceTierAdvisorsAsync();

    /** @return all the replication links associated with this database */
    Map<String, ReplicationLink> listReplicationLinks();

    /**
     * @return a representation of the deferred computation of all the replication links associated with this database
     */
    PagedFlux<ReplicationLink> listReplicationLinksAsync();

    /**
     * Exports the current database to a specified URI path.
     *
     * @param storageUri the storage URI to use
     * @return response object
     */
    SqlDatabaseExportRequest.DefinitionStages.WithStorageTypeAndKey exportTo(String storageUri);

    /**
     * Exports the current database to an existing storage account and relative path.
     *
     * @param storageAccount an existing storage account to be used
     * @param containerName the container name within the storage account to use
     * @param fileName the exported database file name
     * @return response object
     */
    SqlDatabaseExportRequest.DefinitionStages.WithAuthenticationTypeAndLoginPassword exportTo(
        StorageAccount storageAccount, String containerName, String fileName);

    /**
     * Exports the current database to a new storage account and relative path.
     *
     * @param storageAccountCreatable a storage account to be created as part of this execution flow
     * @param containerName the container name within the storage account to use
     * @param fileName the exported database file name
     * @return response object
     */
    SqlDatabaseExportRequest.DefinitionStages.WithAuthenticationTypeAndLoginPassword exportTo(
        Creatable<StorageAccount> storageAccountCreatable, String containerName, String fileName);

    /**
     * Imports into the current database from a specified URI path; the current database must be empty.
     *
     * @param storageUri the storage URI to use
     * @return response object
     */
    SqlDatabaseImportRequest.DefinitionStages.WithStorageTypeAndKey importBacpac(String storageUri);

    /**
     * Imports into the current database from an existing storage account and relative path; the current database must
     * be empty.
     *
     * @param storageAccount an existing storage account to be used
     * @param containerName the container name within the storage account to use
     * @param fileName the exported database file name
     * @return response object
     */
    SqlDatabaseImportRequest.DefinitionStages.WithAuthenticationTypeAndLoginPassword importBacpac(
        StorageAccount storageAccount, String containerName, String fileName);

    /**
     * Begins a definition for a security alert policy.
     *
     * @param policyName the name of the security alert policy
     * @return the first stage of the SqlDatabaseThreatDetectionPolicy definition
     */
    SqlDatabaseThreatDetectionPolicy.DefinitionStages.Blank defineThreatDetectionPolicy(String policyName);

    /**
     * Gets a SQL database threat detection policy.
     *
     * @return the SQL database threat detection policy for the current database
     */
    SqlDatabaseThreatDetectionPolicy getThreatDetectionPolicy();

    /**
     * Gets a SQL database automatic tuning state and options.
     *
     * @return the SQL database automatic tuning state and options
     */
    SqlDatabaseAutomaticTuning getDatabaseAutomaticTuning();

    /**
     * Lists the SQL database usage metrics.
     *
     * @return the SQL database usage metrics
     */
    List<SqlDatabaseUsageMetric> listUsageMetrics();

    /**
     * Asynchronously lists the SQL database usage metrics.
     *
     * @return a representation of the deferred computation of this call returning the SQL database usage metrics
     */
    PagedFlux<SqlDatabaseUsageMetric> listUsageMetricsAsync();

    /**
     * Renames the database.
     *
     * @param newDatabaseName the new name for the database
     * @return the renamed SQL database
     */
    SqlDatabase rename(String newDatabaseName);

    /**
     * Renames the database asynchronously.
     *
     * @param newDatabaseName the new name for the database
     * @return a representation of the deferred computation of this call
     */
    Mono<SqlDatabase> renameAsync(String newDatabaseName);

    /** Deletes the database from the server. */
    void delete();

    /**
     * Deletes the database asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> deleteAsync();

    /** @return the SQL Sync Group entry point for the current database */
    SqlSyncGroupOperations.SqlSyncGroupActionsDefinition syncGroups();

    /**************************************************************
     * Fluent interfaces to provision a SQL Database
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface SqlDatabaseDefinition<ParentT>
        extends SqlDatabase.DefinitionStages.Blank<ParentT>,
            SqlDatabase.DefinitionStages.WithAllDifferentOptions<ParentT>,
            SqlDatabase.DefinitionStages.WithElasticPoolName<ParentT>,
            SqlDatabase.DefinitionStages.WithRestorableDroppedDatabase<ParentT>,
            SqlDatabase.DefinitionStages.WithImportFrom<ParentT>,
            SqlDatabase.DefinitionStages.WithStorageKey<ParentT>,
            SqlDatabase.DefinitionStages.WithAuthentication<ParentT>,
            SqlDatabase.DefinitionStages.WithRestorePointDatabase<ParentT>,
            SqlDatabase.DefinitionStages.WithSourceDatabaseId<ParentT>,
            SqlDatabase.DefinitionStages.WithCreateMode<ParentT>,
            SqlDatabase.DefinitionStages.WithAttachAllOptions<ParentT>,
            SqlDatabase.DefinitionStages.WithAttachFinal<ParentT> {
    }

    /** Grouping of all the SQL Database definition stages. */
    interface DefinitionStages {
        /**
         * The first stage of the SQL Server Firewall rule definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends SqlDatabase.DefinitionStages.WithAllDifferentOptions<ParentT> {
        }

        /**
         * The SQL database interface with all starting options for definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAllDifferentOptions<ParentT>
            extends SqlDatabase.DefinitionStages.WithElasticPoolName<ParentT>,
                SqlDatabase.DefinitionStages.WithRestorableDroppedDatabase<ParentT>,
                SqlDatabase.DefinitionStages.WithImportFrom<ParentT>,
                SqlDatabase.DefinitionStages.WithRestorePointDatabase<ParentT>,
                SqlDatabase.DefinitionStages.WithSampleDatabase<ParentT>,
                SqlDatabase.DefinitionStages.WithSourceDatabaseId<ParentT>,
                SqlDatabase.DefinitionStages.WithEditionDefaults<ParentT>,
                SqlDatabase.DefinitionStages.WithAttachAllOptions<ParentT> {
        }

        /**
         * The SQL Database definition to set the elastic pool for database.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithElasticPoolName<ParentT> {
            /**
             * Sets the existing elastic pool for the SQLDatabase.
             *
             * @param elasticPoolName for the SQL Database
             * @return The next stage of the definition.
             */
            WithExistingDatabaseAfterElasticPool<ParentT> withExistingElasticPool(String elasticPoolName);

            /**
             * Sets the existing elastic pool for the SQLDatabase.
             *
             * @param elasticPoolId for the SQL Database
             * @return The next stage of the definition.
             */
            WithExistingDatabaseAfterElasticPool<ParentT> withExistingElasticPoolId(String elasticPoolId);

            /**
             * Sets the existing elastic pool for the SQLDatabase.
             *
             * @param sqlElasticPool for the SQL Database
             * @return The next stage of the definition.
             */
            WithExistingDatabaseAfterElasticPool<ParentT> withExistingElasticPool(SqlElasticPool sqlElasticPool);

            /**
             * Sets the new elastic pool for the SQLDatabase, this will create a new elastic pool while creating
             * database.
             *
             * @param sqlElasticPool creatable definition for new elastic pool to be created for the SQL Database
             * @return The next stage of the definition.
             */
            WithExistingDatabaseAfterElasticPool<ParentT> withNewElasticPool(Creatable<SqlElasticPool> sqlElasticPool);
        }

        /**
         * The stage to decide whether using existing database or not.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithExistingDatabaseAfterElasticPool<ParentT>
            extends SqlDatabase.DefinitionStages.WithImportFromAfterElasticPool<ParentT>,
                SqlDatabase.DefinitionStages.WithRestorePointDatabaseAfterElasticPool<ParentT>,
                SqlDatabase.DefinitionStages.WithSampleDatabaseAfterElasticPool<ParentT>,
                SqlDatabase.DefinitionStages.WithSourceDatabaseId<ParentT>,
                SqlDatabase.DefinitionStages.WithAttachAfterElasticPoolOptions<ParentT> {
        }

        /**
         * The SQL Database definition to import a BACPAC file as the source database.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithImportFrom<ParentT> {
            /**
             * Creates a new database from a BACPAC file.
             *
             * @param storageUri the source URI for the database to be imported
             * @return The next stage of the definition.
             */
            SqlDatabase.DefinitionStages.WithStorageKey<ParentT> importFrom(String storageUri);

            /**
             * Creates a new database from a BACPAC file.
             *
             * @param storageAccount an existing storage account to be used
             * @param containerName the container name within the storage account to use
             * @param fileName the exported database file name
             * @return The next stage of the definition.
             */
            SqlDatabase.DefinitionStages.WithAuthentication<ParentT> importFrom(
                StorageAccount storageAccount, String containerName, String fileName);
        }

        /**
         * Sets the storage key type and value to use.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithStorageKey<ParentT> {
            /**
             * @param storageAccessKey the storage access key to use
             * @return next definition stage
             */
            SqlDatabase.DefinitionStages.WithAuthentication<ParentT> withStorageAccessKey(String storageAccessKey);

            /**
             * @param sharedAccessKey the shared access key to use; it must be preceded with a "?."
             * @return next definition stage
             */
            SqlDatabase.DefinitionStages.WithAuthentication<ParentT> withSharedAccessKey(String sharedAccessKey);
        }

        /**
         * Sets the authentication type and SQL or Active Directory administrator login and password.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAuthentication<ParentT> {
            /**
             * @param administratorLogin the SQL administrator login
             * @param administratorPassword the SQL administrator password
             * @return next definition stage
             */
            SqlDatabase.DefinitionStages.WithAttachAllOptions<ParentT> withSqlAdministratorLoginAndPassword(
                String administratorLogin, String administratorPassword);

            /**
             * @param administratorLogin the Active Directory administrator login
             * @param administratorPassword the Active Directory administrator password
             * @return next definition stage
             */
            SqlDatabase.DefinitionStages.WithAttachAllOptions<ParentT> withActiveDirectoryLoginAndPassword(
                String administratorLogin, String administratorPassword);
        }

        /**
         * The SQL Database definition to import a BACPAC file as the source database within an elastic pool.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithImportFromAfterElasticPool<ParentT> {
            /**
             * Creates a new database from a BACPAC file.
             *
             * @param storageUri the source URI for the database to be imported
             * @return The next stage of the definition.
             */
            SqlDatabase.DefinitionStages.WithStorageKeyAfterElasticPool<ParentT> importFrom(String storageUri);

            /**
             * Creates a new database from a BACPAC file.
             *
             * @param storageAccount an existing storage account to be used
             * @param containerName the container name within the storage account to use
             * @param fileName the exported database file name
             * @return The next stage of the definition.
             */
            SqlDatabase.DefinitionStages.WithAuthenticationAfterElasticPool<ParentT> importFrom(
                StorageAccount storageAccount, String containerName, String fileName);
        }

        /**
         * Sets the storage key type and value to use.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithStorageKeyAfterElasticPool<ParentT> {
            /**
             * @param storageAccessKey the storage access key to use
             * @return next definition stage
             */
            SqlDatabase.DefinitionStages.WithAuthenticationAfterElasticPool<ParentT> withStorageAccessKey(
                String storageAccessKey);

            /**
             * @param sharedAccessKey the shared access key to use; it must be preceded with a "?."
             * @return next definition stage
             */
            SqlDatabase.DefinitionStages.WithAuthenticationAfterElasticPool<ParentT> withSharedAccessKey(
                String sharedAccessKey);
        }

        /**
         * Sets the authentication type and SQL or Active Directory administrator login and password.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAuthenticationAfterElasticPool<ParentT> {
            /**
             * @param administratorLogin the SQL administrator login
             * @param administratorPassword the SQL administrator password
             * @return next definition stage
             */
            SqlDatabase.DefinitionStages.WithAttachFinal<ParentT> withSqlAdministratorLoginAndPassword(
                String administratorLogin, String administratorPassword);

            /**
             * @param administratorLogin the Active Directory administrator login
             * @param administratorPassword the Active Directory administrator password
             * @return next definition stage
             */
            SqlDatabase.DefinitionStages.WithAttachFinal<ParentT> withActiveDirectoryLoginAndPassword(
                String administratorLogin, String administratorPassword);
        }

        /**
         * The SQL Database definition to set a restorable dropped database as the source database.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithRestorableDroppedDatabase<ParentT> {
            /**
             * Creates a new database from a previously deleted database (see restorable dropped database).
             *
             * <p>Collation, Edition, and MaxSizeBytes must remain the same while the link is active. Values specified
             * for these parameters will be ignored.
             *
             * @param restorableDroppedDatabase the restorable dropped database
             * @return The next stage of the definition.
             */
            SqlDatabase.DefinitionStages.WithAttachFinal<ParentT> fromRestorableDroppedDatabase(
                SqlRestorableDroppedDatabase restorableDroppedDatabase);
        }

        /**
         * The SQL Database definition to set a restore point as the source database within an elastic pool.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithRestorePointDatabaseAfterElasticPool<ParentT> {
            /**
             * Creates a new database from a restore point.
             *
             * @param restorePoint the restore point
             * @return The next stage of the definition.
             */
            SqlDatabase.DefinitionStages.WithAttachAfterElasticPoolOptions<ParentT> fromRestorePoint(
                RestorePoint restorePoint);

            /**
             * Creates a new database from a restore point.
             *
             * @param restorePoint the restore point
             * @param restorePointDateTime date and time to restore from
             * @return The next stage of the definition.
             */
            SqlDatabase.DefinitionStages.WithAttachAfterElasticPoolOptions<ParentT> fromRestorePoint(
                RestorePoint restorePoint, OffsetDateTime restorePointDateTime);
        }

        /**
         * The SQL Database definition to set a restore point as the source database.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithRestorePointDatabase<ParentT> {
            /**
             * Creates a new database from a restore point.
             *
             * @param restorePoint the restore point
             * @return The next stage of the definition.
             */
            SqlDatabase.DefinitionStages.WithAttachAllOptions<ParentT> fromRestorePoint(RestorePoint restorePoint);

            /**
             * Creates a new database from a restore point.
             *
             * @param restorePoint the restore point
             * @param restorePointDateTime date and time to restore from
             * @return The next stage of the definition.
             */
            SqlDatabase.DefinitionStages.WithAttachAllOptions<ParentT> fromRestorePoint(
                RestorePoint restorePoint, OffsetDateTime restorePointDateTime);
        }

        /**
         * The SQL Database definition to set a sample database as the source database within an elastic pool.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSampleDatabaseAfterElasticPool<ParentT> {
            /**
             * Creates a new database from a restore point.
             *
             * @param sampleName the sample database to use as the source
             * @return The next stage of the definition.
             */
            SqlDatabase.DefinitionStages.WithAttachAfterElasticPoolOptions<ParentT> fromSample(SampleName sampleName);
        }

        /**
         * The SQL Database definition to set a sample database as the source database.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSampleDatabase<ParentT> {
            /**
             * Creates a new database from a restore point.
             *
             * @param sampleName the sample database to use as the source
             * @return The next stage of the definition.
             */
            SqlDatabase.DefinitionStages.WithAttachAllOptions<ParentT> fromSample(SampleName sampleName);
        }

        /**
         * The SQL Database definition to set the source database id for database.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSourceDatabaseId<ParentT> {

            /**
             * Sets the resource if of source database for the SQL Database.
             *
             * <p>Collation, Edition, and MaxSizeBytes must remain the same while the link is active. Values specified
             * for these parameters will be ignored.
             *
             * @param sourceDatabaseId id of the source database
             * @return The next stage of the definition.
             */
            SqlDatabase.DefinitionStages.WithCreateMode<ParentT> withSourceDatabase(String sourceDatabaseId);

            /**
             * Sets the resource if of source database for the SQL Database.
             *
             * <p>Collation, Edition, and MaxSizeBytes must remain the same while the link is active. Values specified
             * for these parameters will be ignored.
             *
             * @param sourceDatabase instance of the source database
             * @return The next stage of the definition.
             */
            SqlDatabase.DefinitionStages.WithCreateMode<ParentT> withSourceDatabase(SqlDatabase sourceDatabase);
        }

        /**
         * The SQL Database definition to set the create mode for database.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithCreateMode<ParentT> {
            /**
             * Sets the create mode for the SQL Database.
             *
             * @param createMode create mode for the database, should not be default in this flow
             * @return The next stage of the definition.
             */
            SqlDatabase.DefinitionStages.WithAttachFinal<ParentT> withMode(CreateMode createMode);
        }

        /**
         * The final stage of the SQL Database definition after the SQL Elastic Pool definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttachAfterElasticPoolOptions<ParentT>
            extends SqlDatabase.DefinitionStages.WithCollationAfterElasticPoolOptions<ParentT>,
                SqlDatabase.DefinitionStages.WithMaxSizeBytesAfterElasticPoolOptions<ParentT>,
                SqlDatabase.DefinitionStages.WithAttachFinal<ParentT> {
        }

        /**
         * The SQL Database definition to set the collation for database.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithCollationAfterElasticPoolOptions<ParentT> {
            /**
             * Sets the collation for the SQL Database.
             *
             * @param collation collation to be set for database
             * @return The next stage of the definition
             */
            SqlDatabase.DefinitionStages.WithAttachAfterElasticPoolOptions<ParentT> withCollation(String collation);
        }

        /**
         * The SQL Database definition to set the Max Size in Bytes for database.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithMaxSizeBytesAfterElasticPoolOptions<ParentT> {
            /**
             * Sets the max size in bytes for SQL Database.
             *
             * @param maxSizeBytes max size of the Azure SQL Database expressed in bytes. Note: Only the following sizes
             *     are supported (in addition to limitations being placed on each edition): { 100 MB | 500 MB |1 GB | 5
             *     GB | 10 GB | 20 GB | 30 GB … 150 GB | 200 GB … 500 GB }
             * @return The next stage of the definition.
             */
            SqlDatabase.DefinitionStages.WithAttachAfterElasticPoolOptions<ParentT> withMaxSizeBytes(long maxSizeBytes);
        }

        /**
         * The SQL Database definition to set the sku for database.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSku<ParentT> {
            /**
             * Sets the sku for the SQL Database.
             *
             * @param sku sku to be set for database
             * @return The next stage of the definition
             */
            SqlDatabase.DefinitionStages.WithAttachAllOptions<ParentT> withSku(DatabaseSku sku);

            /**
             * Sets the sku for the SQL Database.
             *
             * @param sku sku/edition to be set for database, all possible capabilities could be found by
             *     {@link SqlServers#getCapabilitiesByRegion(Region)}
             * @return The next stage of the definition
             */
            SqlDatabase.DefinitionStages.WithAttachAllOptions<ParentT> withSku(Sku sku);
        }

        /**
         * The SQL Database definition to set the edition default for database.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithEditionDefaults<ParentT> extends WithAttachFinal<ParentT> {
            /**
             * Sets a "Basic" edition for the SQL Database.
             *
             * @return The next stage of the definition
             */
            SqlDatabase.DefinitionStages.WithEditionDefaults<ParentT> withBasicEdition();

            /**
             * Sets a "Basic" edition and maximum storage capacity for the SQL Database.
             *
             * @param maxStorageCapacity the maximum storage capacity
             * @return The next stage of the definition
             */
            SqlDatabase.DefinitionStages.WithEditionDefaults<ParentT> withBasicEdition(
                SqlDatabaseBasicStorage maxStorageCapacity);

            /**
             * Sets a "Standard" edition for the SQL Database.
             *
             * @param serviceObjective edition to be set for database
             * @return The next stage of the definition
             */
            SqlDatabase.DefinitionStages.WithEditionDefaults<ParentT> withStandardEdition(
                SqlDatabaseStandardServiceObjective serviceObjective);

            /**
             * Sets a "Standard" edition and maximum storage capacity for the SQL Database.
             *
             * @param serviceObjective edition to be set for database
             * @param maxStorageCapacity edition to be set for database
             * @return The next stage of the definition
             */
            SqlDatabase.DefinitionStages.WithEditionDefaults<ParentT> withStandardEdition(
                SqlDatabaseStandardServiceObjective serviceObjective, SqlDatabaseStandardStorage maxStorageCapacity);

            /**
             * Sets a "Premium" edition for the SQL Database.
             *
             * @param serviceObjective edition to be set for database
             * @return The next stage of the definition
             */
            SqlDatabase.DefinitionStages.WithEditionDefaults<ParentT> withPremiumEdition(
                SqlDatabasePremiumServiceObjective serviceObjective);

            /**
             * Sets a "Premium" edition and maximum storage capacity for the SQL Database.
             *
             * @param serviceObjective edition to be set for database
             * @param maxStorageCapacity edition to be set for database
             * @return The next stage of the definition
             */
            SqlDatabase.DefinitionStages.WithEditionDefaults<ParentT> withPremiumEdition(
                SqlDatabasePremiumServiceObjective serviceObjective, SqlDatabasePremiumStorage maxStorageCapacity);
        }

        /**
         * The SQL Database definition to set the collation for database.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithCollation<ParentT> {
            /**
             * Sets the collation for the SQL Database.
             *
             * @param collation collation to be set for database
             * @return The next stage of the definition
             */
            SqlDatabase.DefinitionStages.WithAttachAllOptions<ParentT> withCollation(String collation);
        }

        /**
         * The SQL Database definition to set the Max Size in Bytes for database.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithMaxSizeBytes<ParentT> {
            /**
             * Sets the max size in bytes for SQL Database.
             *
             * @param maxSizeBytes max size of the Azure SQL Database expressed in bytes. Note: Only the following sizes
             *     are supported (in addition to limitations being placed on each edition): { 100 MB | 500 MB |1 GB | 5
             *     GB | 10 GB | 20 GB | 30 GB … 150 GB | 200 GB … 500 GB }
             * @return The next stage of the definition.
             */
            SqlDatabase.DefinitionStages.WithAttachAllOptions<ParentT> withMaxSizeBytes(long maxSizeBytes);
        }

        /**
         * The final stage of the SQL Database definition with all the other options.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttachAllOptions<ParentT>
            extends SqlDatabase.DefinitionStages.WithSku<ParentT>,
                SqlDatabase.DefinitionStages.WithEditionDefaults<ParentT>,
                SqlDatabase.DefinitionStages.WithCollation<ParentT>,
                SqlDatabase.DefinitionStages.WithMaxSizeBytes<ParentT>,
                SqlDatabase.DefinitionStages.WithAttachFinal<ParentT> {
        }

        /**
         * The final stage of the SQL Database definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the SQL Database definition can be
         * attached to the parent SQL Server definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttachFinal<ParentT> extends Attachable.InDefinition<ParentT> {
        }
    }

    /** The template for a SqlDatabase update operation, containing all the settings that can be modified. */
    interface Update
        extends UpdateStages.WithEdition,
            UpdateStages.WithElasticPoolName,
            UpdateStages.WithMaxSizeBytes,
            Resource.UpdateWithTags<SqlDatabase.Update>,
            Appliable<SqlDatabase> {
    }

    /** Grouping of all the SqlDatabase update stages. */
    interface UpdateStages {

        /** The SQL Database definition to set the edition for database. */
        interface WithEdition {
            /**
             * Sets the sku for the SQL Database.
             *
             * @param sku sku to be set for database
             * @return The next stage of the update
             */
            Update withSku(DatabaseSku sku);

            /**
             * Sets the sku for the SQL Database.
             *
             * @param sku sku/edition to be set for database, all possible capabilities could be found by
             *     {@link SqlServers#getCapabilitiesByRegion(Region)}
             * @return The next stage of the update
             */
            Update withSku(Sku sku);

            /**
             * Sets a "Basic" edition for the SQL Database.
             *
             * @return The next stage of the definition
             */
            Update withBasicEdition();

            /**
             * Sets a "Basic" edition and maximum storage capacity for the SQL Database.
             *
             * @param maxStorageCapacity the maximum storage capacity
             * @return The next stage of the definition
             */
            Update withBasicEdition(SqlDatabaseBasicStorage maxStorageCapacity);

            /**
             * Sets a "Standard" edition for the SQL Database.
             *
             * @param serviceObjective edition to be set for database
             * @return The next stage of the definition
             */
            Update withStandardEdition(SqlDatabaseStandardServiceObjective serviceObjective);

            /**
             * Sets a "Standard" edition and maximum storage capacity for the SQL Database.
             *
             * @param serviceObjective edition to be set for database
             * @param maxStorageCapacity edition to be set for database
             * @return The next stage of the definition
             */
            Update withStandardEdition(
                SqlDatabaseStandardServiceObjective serviceObjective, SqlDatabaseStandardStorage maxStorageCapacity);

            /**
             * Sets a "Premium" edition for the SQL Database.
             *
             * @param serviceObjective edition to be set for database
             * @return The next stage of the definition
             */
            Update withPremiumEdition(SqlDatabasePremiumServiceObjective serviceObjective);

            /**
             * Sets a "Premium" edition and maximum storage capacity for the SQL Database.
             *
             * @param serviceObjective edition to be set for database
             * @param maxStorageCapacity edition to be set for database
             * @return The next stage of the definition
             */
            Update withPremiumEdition(
                SqlDatabasePremiumServiceObjective serviceObjective, SqlDatabasePremiumStorage maxStorageCapacity);
        }

        /** The SQL Database definition to set the Max Size in Bytes for database. */
        interface WithMaxSizeBytes {
            /**
             * Sets the max size in bytes for SQL Database.
             *
             * @param maxSizeBytes max size of the Azure SQL Database expressed in bytes. Note: Only the following sizes
             *     are supported (in addition to limitations being placed on each edition): { 100 MB | 500 MB |1 GB | 5
             *     GB | 10 GB | 20 GB | 30 GB … 150 GB | 200 GB … 500 GB }
             * @return The next stage of the update.
             */
            Update withMaxSizeBytes(long maxSizeBytes);
        }

        /** The SQL Database definition to set the elastic pool for database. */
        interface WithElasticPoolName {
            /**
             * Removes database from it's elastic pool.
             *
             * @return The next stage of the update.
             */
            WithEdition withoutElasticPool();

            /**
             * Sets the existing elastic pool for the SQLDatabase.
             *
             * @param elasticPoolName for the SQL Database
             * @return The next stage of the update.
             */
            Update withExistingElasticPool(String elasticPoolName);

            /**
             * Sets the existing elastic pool for the SQLDatabase.
             *
             * @param elasticPoolId for the SQL Database
             * @return The next stage of the definition.
             */
            Update withExistingElasticPoolId(String elasticPoolId);

            /**
             * Sets the existing elastic pool for the SQLDatabase.
             *
             * @param sqlElasticPool for the SQL Database
             * @return The next stage of the update.
             */
            Update withExistingElasticPool(SqlElasticPool sqlElasticPool);

            /**
             * Sets the new elastic pool for the SQLDatabase, this will create a new elastic pool while creating
             * database.
             *
             * @param sqlElasticPool creatable definition for new elastic pool to be created for the SQL Database
             * @return The next stage of the update.
             */
            Update withNewElasticPool(Creatable<SqlElasticPool> sqlElasticPool);
        }
    }
}
