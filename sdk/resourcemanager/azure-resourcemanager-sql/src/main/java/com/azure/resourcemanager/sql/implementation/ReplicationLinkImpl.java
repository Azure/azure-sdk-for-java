// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.RefreshableWrapperImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.ReplicationLink;
import com.azure.resourcemanager.sql.models.ReplicationRole;
import com.azure.resourcemanager.sql.models.ReplicationState;
import com.azure.resourcemanager.sql.fluent.models.ReplicationLinkInner;
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
        this.resourceId = ResourceId.fromString(this.innerModel().id());
    }

    @Override
    protected Mono<ReplicationLinkInner> getInnerAsync() {
        return this
            .sqlServerManager
            .serviceClient()
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
        return this.innerModel().partnerServer();
    }

    @Override
    public String partnerDatabase() {
        return this.innerModel().partnerDatabase();
    }

    @Override
    public String partnerLocation() {
        return this.innerModel().partnerLocation();
    }

    @Override
    public ReplicationRole role() {
        return this.innerModel().role();
    }

    @Override
    public ReplicationRole partnerRole() {
        return this.innerModel().partnerRole();
    }

    @Override
    public OffsetDateTime startTime() {
        return this.innerModel().startTime();
    }

    @Override
    public int percentComplete() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().percentComplete());
    }

    @Override
    public ReplicationState replicationState() {
        return this.innerModel().replicationState();
    }

    @Override
    public String location() {
        return this.innerModel().location();
    }

    @Override
    public boolean isTerminationAllowed() {
        return this.innerModel().isTerminationAllowed();
    }

    @Override
    public String replicationMode() {
        return this.innerModel().replicationMode();
    }

    @Override
    public void delete() {
        this
            .sqlServerManager
            .serviceClient()
            .getReplicationLinks()
            .delete(this.resourceGroupName, this.sqlServerName, this.databaseName(), this.name());
    }

    @Override
    public void failover() {
        this
            .sqlServerManager
            .serviceClient()
            .getReplicationLinks()
            .failover(this.resourceGroupName, this.sqlServerName, this.databaseName(), this.name());
    }

    @Override
    public Mono<Void> failoverAsync() {
        return this
            .sqlServerManager
            .serviceClient()
            .getReplicationLinks()
            .failoverAsync(this.resourceGroupName, this.sqlServerName, this.databaseName(), this.name());
    }

    @Override
    public void forceFailoverAllowDataLoss() {
        this
            .sqlServerManager
            .serviceClient()
            .getReplicationLinks()
            .failoverAllowDataLoss(this.resourceGroupName, this.sqlServerName, this.databaseName(), this.name());
    }

    @Override
    public Mono<Void> forceFailoverAllowDataLossAsync() {
        return this
            .sqlServerManager
            .serviceClient()
            .getReplicationLinks()
            .failoverAllowDataLossAsync(this.resourceGroupName, this.sqlServerName, this.databaseName(), this.name());
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public String resourceGroupName() {
        return this.resourceGroupName;
    }
}
