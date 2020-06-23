// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.dag.TaskGroup;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlDatabaseStandardServiceObjective;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.fluent.inner.DatabaseInner;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Represents a SQL Database collection associated with an Azure SQL server. */
public class SqlDatabasesAsExternalChildResourcesImpl
    extends ExternalChildResourcesNonCachedImpl<SqlDatabaseImpl, SqlDatabase, DatabaseInner, SqlServerImpl, SqlServer> {

    SqlServerManager sqlServerManager;

    /**
     * Creates a new ExternalChildResourcesNonCachedImpl.
     *
     * @param parent the parent Azure resource
     * @param childResourceName the child resource name
     */
    protected SqlDatabasesAsExternalChildResourcesImpl(SqlServerImpl parent, String childResourceName) {
        super(parent, parent.taskGroup(), childResourceName);

        this.sqlServerManager = parent.manager();
    }

    /**
     * Creates a new ExternalChildResourcesNonCachedImpl.
     *
     * @param sqlServerManager the manager
     * @param childResourceName the child resource name (for logging)
     */
    protected SqlDatabasesAsExternalChildResourcesImpl(SqlServerManager sqlServerManager, String childResourceName) {
        super(null, null, childResourceName);

        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
    }

    /**
     * Creates a new ExternalChildResourcesNonCachedImpl.
     *
     * @param parentTaskGroup the parent task group
     * @param sqlServerManager the manager
     * @param childResourceName the child resource name (for logging)
     */
    protected SqlDatabasesAsExternalChildResourcesImpl(
        TaskGroup parentTaskGroup, SqlServerManager sqlServerManager, String childResourceName) {
        super(null, parentTaskGroup, childResourceName);

        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
    }

    SqlDatabaseImpl defineIndependentDatabase(String name) {
        // resource group and server name will be set by the next method in the Fluent flow
        return prepareIndependentDefine(
            new SqlDatabaseImpl(null, null, null, name, new DatabaseInner(), this.sqlServerManager));
    }

    SqlDatabaseImpl defineInlineDatabase(String name) {
        if (this.getParent() == null) {
            // resource group and server name will be set by the next method in the Fluent flow
            return prepareInlineDefine(
                new SqlDatabaseImpl(null, null, null, name, new DatabaseInner(), this.sqlServerManager));
        } else {
            return prepareInlineDefine(
                new SqlDatabaseImpl(name, this.getParent(), new DatabaseInner(), this.getParent().manager())
                    .withStandardEdition(SqlDatabaseStandardServiceObjective.S0));
        }
    }

    SqlDatabaseImpl patchUpdateDatabase(String name) {
        if (this.getParent() == null) {
            // resource group and server name will be set by the next method in the Fluent flow
            return prepareInlineUpdate(
                    new SqlDatabaseImpl(null, null, null, name, new DatabaseInner(), this.sqlServerManager))
                .withPatchUpdate();
        } else {
            return prepareInlineUpdate(
                    new SqlDatabaseImpl(name, this.getParent(), new DatabaseInner(), this.getParent().manager()))
                .withPatchUpdate();
        }
    }

    SqlDatabaseImpl updateInlineDatabase(String name) {
        if (this.getParent() == null) {
            // resource group and server name will be set by the next method in the Fluent flow
            return prepareInlineUpdate(
                new SqlDatabaseImpl(null, null, null, name, new DatabaseInner(), this.sqlServerManager));
        } else {
            return prepareInlineUpdate(
                new SqlDatabaseImpl(name, this.getParent(), new DatabaseInner(), this.getParent().manager()));
        }
    }

    void removeInlineDatabase(String name) {
        if (this.getParent() == null) {
            // resource group and server name will be set by the next method in the Fluent flow
            prepareInlineRemove(
                new SqlDatabaseImpl(null, null, null, name, new DatabaseInner(), this.sqlServerManager));
        } else {
            prepareInlineRemove(
                new SqlDatabaseImpl(name, this.getParent(), new DatabaseInner(), this.getParent().manager()));
        }
    }

    List<SqlDatabaseImpl> getChildren(ExternalChildResourceImpl.PendingOperation pendingOperation) {
        List<SqlDatabaseImpl> results = new ArrayList<>();
        for (SqlDatabaseImpl child : this.childCollection.values()) {
            if (child.pendingOperation() == pendingOperation) {
                results.add(child);
            }
        }

        return results;
    }
}
