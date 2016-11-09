/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.sql.ReplicationLink;
import com.microsoft.azure.management.sql.SqlDatabase;

/**
 * Implementation of SqlDatabase.ReplicationLinks, which enables handling of Replication Links from the SQL Database directly.
 */
public class ReplicationLinksImpl
        extends ReadableWrappersImpl<ReplicationLink, ReplicationLinkImpl, ReplicationLinkInner>
        implements SqlDatabase.ReplicationLinks {

    private final String resourceGroupName;
    private final String sqlServerName;
    private final String databaseName;
    private final DatabasesInner innerCollection;

    ReplicationLinksImpl(DatabasesInner innerCollection, String resourceGroupName, String sqlServerName, String databaseName) {
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.databaseName = databaseName;
        this.innerCollection = innerCollection;
    }

    @Override
    public ReplicationLink get(String linkId) {
        return wrapModel(this.innerCollection.getReplicationLink(this.resourceGroupName, this.sqlServerName, this.databaseName, linkId));
    }

    @Override
    public PagedList<ReplicationLink> list() {
        return this.wrapList(this.innerCollection.listReplicationLinks(this.resourceGroupName, this.sqlServerName, this.databaseName));
    }


    @Override
    public void delete(String linkId) {
        this.innerCollection.deleteReplicationLink(this.resourceGroupName, this.sqlServerName, this.databaseName, linkId);
    }

    @Override
    protected ReplicationLinkImpl wrapModel(ReplicationLinkInner inner) {
        return new ReplicationLinkImpl(inner, this.innerCollection);
    }
}
