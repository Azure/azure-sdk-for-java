// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.RefreshableWrapperImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.ReplicationLink;
import com.azure.resourcemanager.sql.models.ReplicationRole;
import com.azure.resourcemanager.sql.models.ReplicationState;
import com.azure.resourcemanager.sql.fluent.inner.ReplicationLinkInner;
import java.time.OffsetDateTime;
import reactor.core.publisher.Mono;

/** Implementation for SQL replication link interface. */
class ReplicationLinkImpl extends RefreshableWrapperImpl<ReplicationLinkInner, ReplicationLink>
    implements ReplicationLink {

    private final String sqlServerName;
    private final String resourceGroupName;
    private final SqlServerManager sqlServerManager;
    private final ResourceId resourceId;

    protected ReplicationLinkImpl(
        String resourceGroupName,
        String sqlServerName,
        ReplicationLinkInner innerObject,
        SqlServerManager sqlServerManager) {
        super(innerObject);
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.sqlServerManager = sqlServerManager;
        this.resourceId = ResourceId.fromString(this.inner().id());
    }

    @Override
    protected Mono<ReplicationLinkInner> getInnerAsync() {
        return this
            .sqlServerManager
            .inner()
            .getReplicationLinks()
            .getAsync(this.resourceGroupName, this.sqlServerName, this.databaseName(), this.name());
    }

    @Override
    public String sqlServerName() {
        return this.sqlServerName;
    }

    @Override
    public String databaseName() {
        return this.resourceId.parent().name();
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
    public OffsetDateTime startTime() {
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
    public String location() {
        return this.inner().location();
    }

    @Override
    public boolean isTerminationAllowed() {
        return this.inner().isTerminationAllowed();
    }

    @Override
    public String replicationMode() {
        return this.inner().replicationMode();
    }

    @Override
    public void delete() {
        this
            .sqlServerManager
            .inner()
            .getReplicationLinks()
            .delete(this.resourceGroupName, this.sqlServerName, this.databaseName(), this.name());
    }

    @Override
    public void failover() {
        this
            .sqlServerManager
            .inner()
            .getReplicationLinks()
            .failover(this.resourceGroupName, this.sqlServerName, this.databaseName(), this.name());
    }

    @Override
    public Mono<Void> failoverAsync() {
        return this
            .sqlServerManager
            .inner()
            .getReplicationLinks()
            .failoverAsync(this.resourceGroupName, this.sqlServerName, this.databaseName(), this.name());
    }

    @Override
    public void forceFailoverAllowDataLoss() {
        this
            .sqlServerManager
            .inner()
            .getReplicationLinks()
            .failoverAllowDataLoss(this.resourceGroupName, this.sqlServerName, this.databaseName(), this.name());
    }

    @Override
    public Mono<Void> forceFailoverAllowDataLossAsync() {
        return this
            .sqlServerManager
            .inner()
            .getReplicationLinks()
            .failoverAllowDataLossAsync(this.resourceGroupName, this.sqlServerName, this.databaseName(), this.name());
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String resourceGroupName() {
        return this.resourceGroupName;
    }
}
