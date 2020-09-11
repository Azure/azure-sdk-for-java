// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.sql.fluent.inner.SyncMemberInner;
import reactor.core.publisher.Mono;

/** An immutable client-side representation of an Azure SQL Server Sync Member. */
@Fluent
public interface SqlSyncMember
    extends ExternalChildResource<SqlSyncMember, SqlSyncGroup>,
        HasInner<SyncMemberInner>,
        HasResourceGroup,
        Refreshable<SqlSyncMember>,
        Updatable<SqlSyncMember.Update> {
    /** @return name of the SQL Server to which this Sync Member belongs */
    String sqlServerName();

    /** @return name of the SQL Database to which this Sync Member belongs */
    String sqlDatabaseName();

    /** @return name of the SQL Sync Group to which this Sync Member belongs */
    String sqlSyncGroupName();

    /** @return the parent SQL Sync Group ID */
    String parentId();

    /** @return the Database type of the sync member */
    SyncMemberDbType databaseType();

    /** @return the ARM resource id of the sync agent in the sync member */
    String syncAgentId();

    /** @return the SQL Database id of the sync member */
    String sqlServerDatabaseId();

    /** @return the SQL Server name of the member database in the sync member */
    String memberServerName();

    /** @return Database name of the member database in the sync member */
    String memberDatabaseName();

    /** @return the user name of the member database in the sync member */
    String username();

    /** @return the sync direction of the sync member */
    SyncDirection syncDirection();

    /** @return the sync state of the sync member */
    SyncMemberState syncState();

    /** Deletes the Sync Member resource. */
    void delete();

    /**
     * Deletes the SQL Member resource asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> deleteAsync();

    /**
     * Lists the sync member database schemas.
     *
     * @return the paged list object if successful.
     */
    PagedIterable<SqlSyncFullSchemaProperty> listMemberSchemas();

    /**
     * Lists the sync member database schemas asynchronously.
     *
     * @return a representation of the deferred computation of this call.
     */
    PagedFlux<SqlSyncFullSchemaProperty> listMemberSchemasAsync();

    /** Refreshes a sync member database schema. */
    void refreshMemberSchema();

    /**
     * Refreshes a sync member database schema asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> refreshMemberSchemaAsync();

    /**************************************************************
     * Fluent interfaces to provision a SQL Sync Group
     **************************************************************/

    /** The template for a SQL Sync Group update operation, containing all the settings that can be modified. */
    interface Update
        extends SqlSyncMember.UpdateStages.WithMemberUserName,
            SqlSyncMember.UpdateStages.WithMemberPassword,
            SqlSyncMember.UpdateStages.WithMemberDatabaseType,
            SqlSyncMember.UpdateStages.WithSyncDirection,
            Appliable<SqlSyncMember> {
    }

    /** Grouping of all the SQL Sync Group update stages. */
    interface UpdateStages {
        /** The SQL Sync Member definition to set the member database user name. */
        interface WithMemberUserName {
            /**
             * Sets the member SQL Database username.
             *
             * @param userName the member SQL Database username value to set
             * @return The next stage of the definition.
             */
            SqlSyncMember.Update withMemberUserName(String userName);
        }

        /** The SQL Sync Member definition to set the member database password. */
        interface WithMemberPassword {
            /**
             * Sets the member SQL Database password.
             *
             * @param password the member SQL Database password value to set
             * @return The next stage of the definition.
             */
            SqlSyncMember.Update withMemberPassword(String password);
        }

        /** The SQL Sync Member definition to set the database type. */
        interface WithMemberDatabaseType {
            /**
             * Sets the member database type.
             *
             * @param databaseType the database type value to set
             * @return The next stage of the definition.
             */
            SqlSyncMember.Update withMemberDatabaseType(SyncMemberDbType databaseType);
        }

        /** The SQL Sync Member definition to set the sync direction. */
        interface WithSyncDirection {
            /**
             * Sets the sync direction.
             *
             * @param syncDirection the sync direction value to set
             * @return The next stage of the definition.
             */
            SqlSyncMember.Update withDatabaseType(SyncDirection syncDirection);
        }
    }
}
