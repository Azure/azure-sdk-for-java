// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import java.util.List;
import reactor.core.publisher.Mono;

/** A representation of the Azure SQL Encryption Protector operations. */
@Fluent
public interface SqlEncryptionProtectorOperations {
    /**
     * Gets the information about an Encryption Protector resource from Azure SQL server, identifying it by its resource
     * group and parent.
     *
     * @param resourceGroupName the name of resource group
     * @param sqlServerName the name of SQL server parent resource
     * @return an immutable representation of the resource
     */
    SqlEncryptionProtector getBySqlServer(String resourceGroupName, String sqlServerName);

    /**
     * Asynchronously gets the information about an Encryption Protector resource from Azure SQL server, identifying it
     * by its resource group and parent.
     *
     * @param resourceGroupName the name of resource group
     * @param sqlServerName the name of SQL server parent resource
     * @return a representation of the deferred computation of this call returning the found resource
     */
    Mono<SqlEncryptionProtector> getBySqlServerAsync(String resourceGroupName, String sqlServerName);

    /**
     * Gets the information about an Encryption Protector resource from Azure SQL server, identifying it by its resource
     * group and parent.
     *
     * @param sqlServer the SQL server parent resource
     * @return an immutable representation of the resource
     */
    SqlEncryptionProtector getBySqlServer(SqlServer sqlServer);

    /**
     * Asynchronously gets the information about an Encryption Protector resource from Azure SQL server, identifying it
     * by its resource group and parent.
     *
     * @param sqlServer the SQL server parent resource
     * @return a representation of the deferred computation of this call returning the found resource
     */
    Mono<SqlEncryptionProtector> getBySqlServerAsync(SqlServer sqlServer);

    /**
     * Gets the information about an Encryption Protector resource from Azure SQL server using the resource ID.
     *
     * @param id the ID of the resource.
     * @return an immutable representation of the resource
     */
    SqlEncryptionProtector getById(String id);

    /**
     * Asynchronously gets the information about an Encryption Protector resource from Azure SQL server using the
     * resource ID.
     *
     * @param id the ID of the resource.
     * @return a representation of the deferred computation of this call
     */
    Mono<SqlEncryptionProtector> getByIdAsync(String id);

    /**
     * Lists Azure SQL the Encryption Protector resources of the specified Azure SQL server in the specified resource
     * group.
     *
     * @param resourceGroupName the name of the resource group to list the resources from
     * @param sqlServerName the name of parent Azure SQL server.
     * @return the list of resources
     */
    List<SqlEncryptionProtector> listBySqlServer(String resourceGroupName, String sqlServerName);

    /**
     * Asynchronously lists Azure SQL the Encryption Protector resources of the specified Azure SQL server in the
     * specified resource group.
     *
     * @param resourceGroupName the name of the resource group to list the resources from
     * @param sqlServerName the name of parent Azure SQL server.
     * @return a representation of the deferred computation of this call
     */
    PagedFlux<SqlEncryptionProtector> listBySqlServerAsync(String resourceGroupName, String sqlServerName);

    /**
     * Lists Azure SQL the Encryption Protector resources of the specified Azure SQL server in the specified resource
     * group.
     *
     * @param sqlServer the parent Azure SQL server.
     * @return the list of resources
     */
    List<SqlEncryptionProtector> listBySqlServer(SqlServer sqlServer);

    /**
     * Asynchronously lists Azure SQL the Encryption Protector resources of the specified Azure SQL server in the
     * specified resource group.
     *
     * @param sqlServer the parent Azure SQL server.
     * @return a representation of the deferred computation of this call
     */
    PagedFlux<SqlEncryptionProtector> listBySqlServerAsync(SqlServer sqlServer);

    /** Grouping of the Azure SQL Server Key common actions. */
    interface SqlEncryptionProtectorActionsDefinition {
        /**
         * Gets the information about an Encryption Protector resource from Azure SQL server.
         *
         * @return an immutable representation of the resource
         */
        SqlEncryptionProtector get();

        /**
         * Asynchronously gets the information about an Encryption Protector resource from Azure SQL server.
         *
         * @return a representation of the deferred computation of this call returning the found resource
         */
        Mono<SqlEncryptionProtector> getAsync();

        /**
         * Lists Azure SQL the Encryption Protector resources.
         *
         * @return the list of resources
         */
        List<SqlEncryptionProtector> list();

        /**
         * Asynchronously lists Azure SQL the Encryption Protector resources.
         *
         * @return a representation of the deferred computation of this call
         */
        PagedFlux<SqlEncryptionProtector> listAsync();
    }
}
