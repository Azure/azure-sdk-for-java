// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.dag.TaskGroup;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.SqlElasticPool;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.fluent.models.ElasticPoolInner;
import java.util.ArrayList;
import java.util.List;

/** Represents a SQL Elastic Pool collection associated with an Azure SQL server. */
public class SqlElasticPoolsAsExternalChildResourcesImpl
    extends ExternalChildResourcesNonCachedImpl<
        SqlElasticPoolImpl, SqlElasticPool, ElasticPoolInner, SqlServerImpl, SqlServer> {

    SqlServerManager sqlServerManager;

    /**
     * Creates a new ExternalChildResourcesNonCachedImpl.
     *
     * @param parent the parent Azure resource
     * @param childResourceName the child resource name
     */
    protected SqlElasticPoolsAsExternalChildResourcesImpl(SqlServerImpl parent, String childResourceName) {
        super(parent, parent.taskGroup(), childResourceName);
        this.sqlServerManager = parent.manager();
    }

    /**
     * Creates a new ExternalChildResourcesNonCachedImpl.
     *
     * @param sqlServerManager the manager
     * @param childResourceName the child resource name (for logging)
     */
    protected SqlElasticPoolsAsExternalChildResourcesImpl(SqlServerManager sqlServerManager, String childResourceName) {
        super(null, null, childResourceName);
        this.sqlServerManager = sqlServerManager;
    }

    /**
     * Creates a new ExternalChildResourcesNonCachedImpl.
     *
     * @param parentTaskGroup the parent task group
     * @param sqlServerManager the manager
     * @param childResourceName the child resource name (for logging)
     */
    protected SqlElasticPoolsAsExternalChildResourcesImpl(
        TaskGroup parentTaskGroup, SqlServerManager sqlServerManager, String childResourceName) {
        super(null, parentTaskGroup, childResourceName);
        this.sqlServerManager = sqlServerManager;
    }

    SqlElasticPoolImpl defineIndependentElasticPool(String name) {
        // resource group, server name and location will be set by the next method in the Fluent flow
        return prepareIndependentDefine(new SqlElasticPoolImpl(name, new ElasticPoolInner(), this.sqlServerManager));
    }

    SqlElasticPoolImpl defineInlineElasticPool(String name) {
        if (this.getParent() == null) {
            // resource group and server name will be set by the next method in the Fluent flow
            return prepareInlineDefine(new SqlElasticPoolImpl(name, new ElasticPoolInner(), this.sqlServerManager));
        } else {
            return prepareInlineDefine(
                new SqlElasticPoolImpl(name, this.getParent(), new ElasticPoolInner(), this.getParent().manager()));
        }
    }

    SqlElasticPoolImpl updateInlineElasticPool(String name) {
        if (this.getParent() == null) {
            // resource group and server name will be set by the next method in the Fluent flow
            return prepareInlineUpdate(new SqlElasticPoolImpl(name, new ElasticPoolInner(), this.sqlServerManager));
        } else {
            return prepareInlineUpdate(
                new SqlElasticPoolImpl(name, this.getParent(), new ElasticPoolInner(), this.getParent().manager()));
        }
    }

    void removeInlineElasticPool(String name) {
        if (this.getParent() == null) {
            // resource group and server name will be set by the next method in the Fluent flow
            prepareInlineRemove(new SqlElasticPoolImpl(name, new ElasticPoolInner(), this.sqlServerManager));
        } else {
            prepareInlineRemove(
                new SqlElasticPoolImpl(name, this.getParent(), new ElasticPoolInner(), this.getParent().manager()));
        }
    }

    List<SqlElasticPoolImpl> getChildren(ExternalChildResourceImpl.PendingOperation pendingOperation) {
        List<SqlElasticPoolImpl> results = new ArrayList<>();
        for (SqlElasticPoolImpl child : this.childCollection.values()) {
            if (child.pendingOperation() == pendingOperation) {
                results.add(child);
            }
        }

        return results;
    }
}
