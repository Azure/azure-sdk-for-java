// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.storage.models.StorageAccount;
import java.time.OffsetDateTime;

/** A representation of the Azure SQL Database operations. */
@Fluent
public interface SqlDatabaseOperations
    extends SupportsCreating<SqlDatabaseOperations.DefinitionStages.WithSqlServer>, SqlChildrenOperations<SqlDatabase> {

    /** Container interface for all the definitions that need to be implemented. */
    interface SqlDatabaseOperationsDefinition
        extends SqlDatabaseOperations.DefinitionStages.Blank,
            SqlDatabaseOperations.DefinitionStages.WithSqlServer,
            SqlDatabaseOperations.DefinitionStages.WithAllDifferentOptions,
            SqlDatabaseOperations.DefinitionStages.WithElasticPoolName,
            SqlDatabaseOperations.DefinitionStages.WithRestorableDroppedDatabase,
            SqlDatabaseOperations.DefinitionStages.WithImportFrom,
            SqlDatabaseOperations.DefinitionStages.WithStorageKey,
            SqlDatabaseOperations.DefinitionStages.WithAuthentication,
            SqlDatabaseOperations.DefinitionStages.WithRestorePointDatabase,
            SqlDatabaseOperations.DefinitionStages.WithSourceDatabaseId,
            SqlDatabaseOperations.DefinitionStages.WithCreateMode,
            SqlDatabaseOperations.DefinitionStages.WithCreateAllOptions,
            SqlDatabaseOperations.DefinitionStages.WithCreateFinal {
    }

    /** Grouping of all the SQL database definition stages. */
    interface DefinitionStages {
        /** The first stage of the SQL database definition. */
        interface Blank extends SqlDatabaseOperations.DefinitionStages.WithAllDifferentOptions {
        }

        /**
         * The stage of the SQL Database rule definition allowing to specify the parent resource group, SQL server and
         * location.
         */
        interface WithSqlServer {
            /**
             * Sets the parent SQL server name and resource group it belongs to.
             *
             * @param resourceGroupName the name of the resource group the parent SQL server
             * @param sqlServerName the parent SQL server name
             * @param location the parent SQL server location
             * @return The next stage of the definition.
             */
            SqlDatabaseOperations.DefinitionStages.WithAllDifferentOptions withExistingSqlServer(
                String resourceGroupName, String sqlServerName, String location);

            /**
             * Sets the parent SQL server for the new SQL Database.
             *
             * @param sqlServer the parent SQL server
             * @return The next stage of the definition.
             */
            SqlDatabaseOperations.DefinitionStages.WithAllDifferentOptions withExistingSqlServer(SqlServer sqlServer);
        }

        /** The SQL database interface with all starting options for definition. */
        interface WithAllDifferentOptions
            extends SqlDatabaseOperations.DefinitionStages.WithElasticPoolName,
                SqlDatabaseOperations.DefinitionStages.WithRestorableDroppedDatabase,
                SqlDatabaseOperations.DefinitionStages.WithImportFrom,
                SqlDatabaseOperations.DefinitionStages.WithRestorePointDatabase,
                SqlDatabaseOperations.DefinitionStages.WithSampleDatabase,
                SqlDatabaseOperations.DefinitionStages.WithSourceDatabaseId,
                SqlDatabaseOperations.DefinitionStages.WithCreateAllOptions {
        }

        /** The SQL Database definition to set the elastic pool for database. */
        interface WithElasticPoolName {
            /**
             * Sets the existing elastic pool for the SQLDatabase.
             *
             * @param elasticPoolName for the SQL Database
             * @return The next stage of the definition.
             */
            WithExistingDatabaseAfterElasticPool withExistingElasticPool(String elasticPoolName);

            /**
             * Sets the existing elastic pool for the SQLDatabase.
             *
             * @param elasticPoolId for the SQL Database
             * @return The next stage of the definition.
             */
            WithExistingDatabaseAfterElasticPool withExistingElasticPoolId(String elasticPoolId);

            /**
             * Sets the existing elastic pool for the SQLDatabase.
             *
             * @param sqlElasticPool for the SQL Database
             * @return The next stage of the definition.
             */
            WithExistingDatabaseAfterElasticPool withExistingElasticPool(SqlElasticPool sqlElasticPool);

            /**
             * Sets the new elastic pool for the SQLDatabase, this will create a new elastic pool while creating
             * database.
             *
             * @param sqlElasticPool creatable definition for new elastic pool to be created for the SQL Database
             * @return The next stage of the definition.
             */
            WithExistingDatabaseAfterElasticPool withNewElasticPool(Creatable<SqlElasticPool> sqlElasticPool);

            /**
             * Begins the definition of a new SQL Elastic Pool to be added to this database parent SQL server.
             *
             * @param elasticPoolName the name of the new SQL Elastic Pool
             * @return the first stage of the new SQL Elastic Pool definition
             */
            SqlElasticPool.DefinitionStages.Blank<WithExistingDatabaseAfterElasticPool> defineElasticPool(
                String elasticPoolName);
        }

        /** The stage to decide whether using existing database or not. */
        interface WithExistingDatabaseAfterElasticPool
            extends SqlDatabaseOperations.DefinitionStages.WithImportFromAfterElasticPool,
                SqlDatabaseOperations.DefinitionStages.WithRestorePointDatabaseAfterElasticPool,
                SqlDatabaseOperations.DefinitionStages.WithSampleDatabaseAfterElasticPool,
                SqlDatabaseOperations.DefinitionStages.WithSourceDatabaseId,
                SqlDatabaseOperations.DefinitionStages.WithCreateAfterElasticPoolOptions {
        }

        /** The SQL Database definition to import a BACPAC file as the source database. */
        interface WithImportFrom {
            /**
             * Creates a new database from a BACPAC file.
             *
             * @param storageUri the source URI for the database to be imported
             * @return The next stage of the definition.
             */
            SqlDatabaseOperations.DefinitionStages.WithStorageKey importFrom(String storageUri);

            /**
             * Creates a new database from a BACPAC file.
             *
             * @param storageAccount an existing storage account to be used
             * @param containerName the container name within the storage account to use
             * @param fileName the exported database file name
             * @return The next stage of the definition.
             */
            SqlDatabaseOperations.DefinitionStages.WithAuthentication importFrom(
                StorageAccount storageAccount, String containerName, String fileName);
        }

        /** Sets the storage key type and value to use. */
        interface WithStorageKey {
            /**
             * @param storageAccessKey the storage access key to use
             * @return next definition stage
             */
            SqlDatabaseOperations.DefinitionStages.WithAuthentication withStorageAccessKey(String storageAccessKey);

            /**
             * @param sharedAccessKey the shared access key to use; it must be preceded with a "?."
             * @return next definition stage
             */
            SqlDatabaseOperations.DefinitionStages.WithAuthentication withSharedAccessKey(String sharedAccessKey);
        }

        /** Sets the authentication type and SQL or Active Directory administrator login and password. */
        interface WithAuthentication {
            /**
             * @param administratorLogin the SQL administrator login
             * @param administratorPassword the SQL administrator password
             * @return next definition stage
             */
            SqlDatabaseOperations.DefinitionStages.WithEditionDefaults withSqlAdministratorLoginAndPassword(
                String administratorLogin, String administratorPassword);

            /**
             * @param administratorLogin the Active Directory administrator login
             * @param administratorPassword the Active Directory administrator password
             * @return next definition stage
             */
            SqlDatabaseOperations.DefinitionStages.WithEditionDefaults withActiveDirectoryLoginAndPassword(
                String administratorLogin, String administratorPassword);
        }

        /** The SQL Database definition to import a BACPAC file as the source database. */
        interface WithImportFromAfterElasticPool {
            /**
             * Creates a new database from a BACPAC file.
             *
             * @param storageUri the source URI for the database to be imported
             * @return The next stage of the definition.
             */
            SqlDatabaseOperations.DefinitionStages.WithStorageKeyAfterElasticPool importFrom(String storageUri);

            /**
             * Creates a new database from a BACPAC file.
             *
             * @param storageAccount an existing storage account to be used
             * @param containerName the container name within the storage account to use
             * @param fileName the exported database file name
             * @return The next stage of the definition.
             */
            SqlDatabaseOperations.DefinitionStages.WithAuthenticationAfterElasticPool importFrom(
                StorageAccount storageAccount, String containerName, String fileName);
        }

        /** Sets the storage key type and value to use. */
        interface WithStorageKeyAfterElasticPool {
            /**
             * @param storageAccessKey the storage access key to use
             * @return next definition stage
             */
            SqlDatabaseOperations.DefinitionStages.WithAuthenticationAfterElasticPool withStorageAccessKey(
                String storageAccessKey);

            /**
             * @param sharedAccessKey the shared access key to use; it must be preceded with a "?."
             * @return next definition stage
             */
            SqlDatabaseOperations.DefinitionStages.WithAuthenticationAfterElasticPool withSharedAccessKey(
                String sharedAccessKey);
        }

        /** Sets the authentication type and SQL or Active Directory administrator login and password. */
        interface WithAuthenticationAfterElasticPool {
            /**
             * @param administratorLogin the SQL administrator login
             * @param administratorPassword the SQL administrator password
             * @return next definition stage
             */
            SqlDatabaseOperations.DefinitionStages.WithCreateAfterElasticPoolOptions
                withSqlAdministratorLoginAndPassword(String administratorLogin, String administratorPassword);

            /**
             * @param administratorLogin the Active Directory administrator login
             * @param administratorPassword the Active Directory administrator password
             * @return next definition stage
             */
            SqlDatabaseOperations.DefinitionStages.WithCreateAfterElasticPoolOptions
                withActiveDirectoryLoginAndPassword(String administratorLogin, String administratorPassword);
        }

        /** The SQL Database definition to set a restorable dropped database as the source database. */
        interface WithRestorableDroppedDatabase {
            /**
             * Creates a new database from a previously deleted database (see restorable dropped database).
             *
             * <p>Collation, Edition, and MaxSizeBytes must remain the same while the link is active. Values specified
             * for these parameters will be ignored.
             *
             * @param restorableDroppedDatabase the restorable dropped database
             * @return The next stage of the definition.
             */
            SqlDatabaseOperations.DefinitionStages.WithCreateFinal fromRestorableDroppedDatabase(
                SqlRestorableDroppedDatabase restorableDroppedDatabase);
        }

        /** The SQL Database definition to set a restore point as the source database. */
        interface WithRestorePointDatabase {
            /**
             * Creates a new database from a restore point.
             *
             * @param restorePoint the restore point
             * @return The next stage of the definition.
             */
            SqlDatabaseOperations.DefinitionStages.WithCreateAllOptions fromRestorePoint(RestorePoint restorePoint);

            /**
             * Creates a new database from a restore point.
             *
             * @param restorePoint the restore point
             * @param restorePointDateTime date and time to restore from
             * @return The next stage of the definition.
             */
            SqlDatabaseOperations.DefinitionStages.WithCreateAllOptions fromRestorePoint(
                RestorePoint restorePoint, OffsetDateTime restorePointDateTime);
        }

        /** The SQL Database definition to set a restore point as the source database within an elastic pool. */
        interface WithRestorePointDatabaseAfterElasticPool {
            /**
             * Creates a new database from a restore point.
             *
             * @param restorePoint the restore point
             * @return The next stage of the definition.
             */
            SqlDatabaseOperations.DefinitionStages.WithCreateAfterElasticPoolOptions fromRestorePoint(
                RestorePoint restorePoint);

            /**
             * Creates a new database from a restore point.
             *
             * @param restorePoint the restore point
             * @param restorePointDateTime date and time to restore from
             * @return The next stage of the definition.
             */
            SqlDatabaseOperations.DefinitionStages.WithCreateAfterElasticPoolOptions fromRestorePoint(
                RestorePoint restorePoint, OffsetDateTime restorePointDateTime);
        }

        /** The SQL Database definition to set a sample database as the source database within an elastic pool. */
        interface WithSampleDatabaseAfterElasticPool {
            /**
             * Creates a new database from a restore point.
             *
             * @param sampleName the sample database to use as the source
             * @return The next stage of the definition.
             */
            SqlDatabaseOperations.DefinitionStages.WithCreateAfterElasticPoolOptions fromSample(SampleName sampleName);
        }

        /** The SQL Database definition to set a sample database as the source database. */
        interface WithSampleDatabase {
            /**
             * Creates a new database from a restore point.
             *
             * @param sampleName the sample database to use as the source
             * @return The next stage of the definition.
             */
            SqlDatabaseOperations.DefinitionStages.WithCreateAllOptions fromSample(SampleName sampleName);
        }

        /** The SQL Database definition to set the source database id for database. */
        interface WithSourceDatabaseId {

            /**
             * Sets the resource if of source database for the SQL Database.
             *
             * <p>Collation, Edition, and MaxSizeBytes must remain the same while the link is active. Values specified
             * for these parameters will be ignored.
             *
             * @param sourceDatabaseId id of the source database
             * @return The next stage of the definition.
             */
            SqlDatabaseOperations.DefinitionStages.WithCreateMode withSourceDatabase(String sourceDatabaseId);

            /**
             * Sets the resource if of source database for the SQL Database.
             *
             * <p>Collation, Edition, and MaxSizeBytes must remain the same while the link is active. Values specified
             * for these parameters will be ignored.
             *
             * @param sourceDatabase instance of the source database
             * @return The next stage of the definition.
             */
            SqlDatabaseOperations.DefinitionStages.WithCreateMode withSourceDatabase(SqlDatabase sourceDatabase);
        }

        /** The SQL Database definition to set the create mode for database. */
        interface WithCreateMode {
            /**
             * Sets the create mode for the SQL Database.
             *
             * @param createMode create mode for the database, should not be default in this flow
             * @return The next stage of the definition.
             */
            SqlDatabaseOperations.DefinitionStages.WithCreateAllOptions withMode(CreateMode createMode);
        }

        /** The final stage of the SQL Database definition after the SQL Elastic Pool definition. */
        interface WithCreateAfterElasticPoolOptions
            extends SqlDatabaseOperations.DefinitionStages.WithCollationAfterElasticPoolOptions,
                SqlDatabaseOperations.DefinitionStages.WithMaxSizeBytesAfterElasticPoolOptions,
                SqlDatabaseOperations.DefinitionStages.WithCreateFinal {
        }

        /** The SQL Database definition to set the collation for database. */
        interface WithCollationAfterElasticPoolOptions {
            /**
             * Sets the collation for the SQL Database.
             *
             * @param collation collation to be set for database
             * @return The next stage of the definition
             */
            WithCreateAfterElasticPoolOptions withCollation(String collation);
        }

        /** The SQL Database definition to set the Max Size in Bytes for database. */
        interface WithMaxSizeBytesAfterElasticPoolOptions {
            /**
             * Sets the max size in bytes for SQL Database.
             *
             * @param maxSizeBytes max size of the Azure SQL Database expressed in bytes. Note: Only the following sizes
             *     are supported (in addition to limitations being placed on each edition): { 100 MB | 500 MB |1 GB | 5
             *     GB | 10 GB | 20 GB | 30 GB … 150 GB | 200 GB … 500 GB }
             * @return The next stage of the definition.
             */
            WithCreateAfterElasticPoolOptions withMaxSizeBytes(long maxSizeBytes);
        }

        /** The SQL Database definition to set the edition for database. */
        interface WithEdition {
            /**
             * Sets the edition for the SQL Database.
             *
             * @param edition edition to be set for database
             * @return The next stage of the definition
             */
            SqlDatabaseOperations.DefinitionStages.WithCreateAllOptions withEdition(DatabaseEdition edition);
        }

        /** The SQL Database definition to set the edition for database with defaults. */
        interface WithEditionDefaults extends SqlDatabaseOperations.DefinitionStages.WithCreateFinal {
            /**
             * Sets a "Basic" edition for the SQL Database.
             *
             * @return The next stage of the definition
             */
            SqlDatabaseOperations.DefinitionStages.WithEditionDefaults withBasicEdition();

            /**
             * Sets a "Basic" edition for the SQL Database.
             *
             * @param maxStorageCapacity the maximum storage capacity
             * @return The next stage of the definition
             */
            SqlDatabaseOperations.DefinitionStages.WithEditionDefaults withBasicEdition(
                SqlDatabaseBasicStorage maxStorageCapacity);

            /**
             * Sets a "Standard" edition for the SQL Database.
             *
             * @param serviceObjective edition to be set for database
             * @return The next stage of the definition
             */
            SqlDatabaseOperations.DefinitionStages.WithEditionDefaults withStandardEdition(
                SqlDatabaseStandardServiceObjective serviceObjective);

            /**
             * Sets a "Standard" edition and maximum storage capacity for the SQL Database.
             *
             * @param serviceObjective edition to be set for database
             * @param maxStorageCapacity edition to be set for database
             * @return The next stage of the definition
             */
            SqlDatabaseOperations.DefinitionStages.WithEditionDefaults withStandardEdition(
                SqlDatabaseStandardServiceObjective serviceObjective, SqlDatabaseStandardStorage maxStorageCapacity);

            /**
             * Sets a "Premium" edition for the SQL Database.
             *
             * @param serviceObjective edition to be set for database
             * @return The next stage of the definition
             */
            SqlDatabaseOperations.DefinitionStages.WithEditionDefaults withPremiumEdition(
                SqlDatabasePremiumServiceObjective serviceObjective);

            /**
             * Sets a "Premium" edition and maximum storage capacity for the SQL Database.
             *
             * @param serviceObjective edition to be set for database
             * @param maxStorageCapacity edition to be set for database
             * @return The next stage of the definition
             */
            SqlDatabaseOperations.DefinitionStages.WithEditionDefaults withPremiumEdition(
                SqlDatabasePremiumServiceObjective serviceObjective, SqlDatabasePremiumStorage maxStorageCapacity);

            /** The SQL Database definition to set the collation for database. */
            interface WithCollation {
                /**
                 * Sets the collation for the SQL Database.
                 *
                 * @param collation collation to be set for database
                 * @return The next stage of the definition
                 */
                SqlDatabaseOperations.DefinitionStages.WithEditionDefaults withCollation(String collation);
            }
        }

        /** The SQL Database definition to set the collation for database. */
        interface WithCollation {
            /**
             * Sets the collation for the SQL Database.
             *
             * @param collation collation to be set for database
             * @return The next stage of the definition
             */
            SqlDatabaseOperations.DefinitionStages.WithCreateAllOptions withCollation(String collation);
        }

        /** The SQL Database definition to set the Max Size in Bytes for database. */
        interface WithMaxSizeBytes {
            /**
             * Sets the max size in bytes for SQL Database.
             *
             * @param maxSizeBytes max size of the Azure SQL Database expressed in bytes. Note: Only the following sizes
             *     are supported (in addition to limitations being placed on each edition): { 100 MB | 500 MB |1 GB | 5
             *     GB | 10 GB | 20 GB | 30 GB … 150 GB | 200 GB … 500 GB }
             * @return The next stage of the definition.
             */
            SqlDatabaseOperations.DefinitionStages.WithCreateAllOptions withMaxSizeBytes(long maxSizeBytes);
        }

        /** The SQL Database definition to set the service level objective. */
        interface WithServiceObjective {
            /**
             * Sets the service level objective for the SQL Database.
             *
             * @param serviceLevelObjective service level objected for the SQL Database
             * @return The next stage of the definition.
             */
            SqlDatabaseOperations.DefinitionStages.WithCreateAllOptions withServiceObjective(
                ServiceObjectiveName serviceLevelObjective);
        }

        /**
         * A SQL Database definition with sufficient inputs to create a new SQL database in the cloud, but exposing
         * additional optional settings to specify.
         */
        interface WithCreateAllOptions
            extends SqlDatabaseOperations.DefinitionStages.WithServiceObjective,
                SqlDatabaseOperations.DefinitionStages.WithEdition,
                SqlDatabaseOperations.DefinitionStages.WithEditionDefaults,
                SqlDatabaseOperations.DefinitionStages.WithCollation,
                SqlDatabaseOperations.DefinitionStages.WithMaxSizeBytes,
                SqlDatabaseOperations.DefinitionStages.WithCreateFinal {
        }

        /**
         * A SQL Database definition with sufficient inputs to create a new SQL Server in the cloud, but exposing
         * additional optional inputs to specify.
         */
        interface WithCreateFinal
            extends Resource.DefinitionWithTags<SqlDatabaseOperations.DefinitionStages.WithCreateFinal>,
                Creatable<SqlDatabase> {
        }
    }

    /** Grouping of the Azure SQL Database rule common actions. */
    interface SqlDatabaseActionsDefinition extends SqlChildrenActionsDefinition<SqlDatabase> {
        /**
         * Begins the definition of a new SQL Database to be added to this server.
         *
         * @param databaseName the name of the new SQL Database
         * @return the first stage of the new SQL Database definition
         */
        SqlDatabaseOperations.DefinitionStages.WithAllDifferentOptions define(String databaseName);
    }
}
