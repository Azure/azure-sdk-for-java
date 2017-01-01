/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.sql.ReplicationLink;
import com.microsoft.azure.management.sql.ReplicationRole;
import com.microsoft.azure.management.sql.ReplicationState;
import org.joda.time.DateTime;

/**
 * Implementation for SqlServer and its parent interfaces.
 */
@LangDefinition
class ReplicationLinkImpl
        extends WrapperImpl<ReplicationLinkInner>
        implements ReplicationLink {
    private final DatabasesInner innerCollection;
    private final ResourceId resourceId;

    protected ReplicationLinkImpl(ReplicationLinkInner innerObject, DatabasesInner innerCollection) {
        super(innerObject);
        this.resourceId = ResourceId.fromString(this.inner().id());
        this.innerCollection = innerCollection;
    }

    @Override
    public ReplicationLink refresh() {
        this.setInner(this.innerCollection.getReplicationLink(
                this.resourceGroupName(),
                this.sqlServerName(),
                this.databaseName(),
                this.name()));

        return this;
    }

    @Override
    public String sqlServerName() {
        return resourceId.parent().parent().name();
    }

    @Override
    public String databaseName() {
        return resourceId.parent().name();
    }

    @Override
    public String partnerServer() {
        return this.inner().partnerServer();
    }

    @Override
    public String partnerDatabase() {
        return this.inner().partnerDatabase();
    }

    @Override
    public String partnerLocation() {
        return this.inner().partnerLocation();
    }

    @Override
    public ReplicationRole role() {
        return this.inner().role();
    }

    @Override
    public ReplicationRole partnerRole() {
        return this.inner().partnerRole();
    }

    @Override
    public DateTime startTime() {
        return this.inner().startTime();
    }

    @Override
    public int percentComplete() {
        return Utils.toPrimitiveInt(this.inner().percentComplete());
    }

    @Override
    public ReplicationState replicationState() {
        return this.inner().replicationState();
    }

    @Override
    public void delete() {
        this.innerCollection.deleteReplicationLink(
                this.resourceGroupName(),
                this.sqlServerName(),
                this.databaseName(),
                this.name());
    }

    @Override
    public void failover() {
        this.innerCollection.failoverReplicationLink(
                this.resourceGroupName(),
                this.sqlServerName(),
                this.databaseName(),
                this.name());
    }

    @Override
    public void forceFailoverAllowDataLoss() {
        this.innerCollection.failoverReplicationLinkAllowDataLoss(
                this.resourceGroupName(),
                this.sqlServerName(),
                this.databaseName(),
                this.name());
    }

    @Override
    public String name() {
        return this.resourceId.name();
    }

    @Override
    public String id() {
        return this.resourceId.id();
    }

    @Override
    public String resourceGroupName() {
        return this.resourceId.resourceGroupName();
    }
}
