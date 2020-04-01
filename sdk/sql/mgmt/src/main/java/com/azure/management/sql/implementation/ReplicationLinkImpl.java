/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql.implementation;

import com.azure.management.resources.fluentcore.arm.ResourceId;
import com.azure.management.resources.fluentcore.model.implementation.RefreshableWrapperImpl;
import com.azure.management.resources.fluentcore.utils.Utils;
import com.azure.management.sql.ReplicationLink;
import com.azure.management.sql.ReplicationRole;
import com.azure.management.sql.ReplicationState;
import com.azure.management.sql.models.ReplicationLinkInner;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;


/**
 * Implementation for SQL replication link interface.
 */
class ReplicationLinkImpl
        extends RefreshableWrapperImpl<ReplicationLinkInner, ReplicationLink>
        implements ReplicationLink {

    private final String sqlServerName;
    private final String resourceGroupName;
    private final SqlServerManager sqlServerManager;
    private final ResourceId resourceId;

    protected ReplicationLinkImpl(String resourceGroupName, String sqlServerName, ReplicationLinkInner innerObject, SqlServerManager sqlServerManager) {
        super(innerObject);
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.sqlServerManager = sqlServerManager;
        this.resourceId = ResourceId.fromString(this.inner().getId());
    }

    @Override
    protected Mono<ReplicationLinkInner> getInnerAsync() {
        return this.sqlServerManager.inner().replicationLinks()
            .getAsync(this.resourceGroupName,
                this.sqlServerName,
                this.databaseName(),
                this.name());
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
        this.sqlServerManager.inner().replicationLinks()
            .delete(this.resourceGroupName,
                this.sqlServerName,
                this.databaseName(),
                this.name());
    }

    @Override
    public void failover() {
        this.sqlServerManager.inner().replicationLinks()
            .failover(this.resourceGroupName,
                this.sqlServerName,
                this.databaseName(),
                this.name());
    }

    @Override
    public Mono<Void> failoverAsync() {
        return this.sqlServerManager.inner().replicationLinks()
            .failoverAsync(this.resourceGroupName,
                this.sqlServerName,
                this.databaseName(),
                this.name());
    }

    @Override
    public void forceFailoverAllowDataLoss() {
        this.sqlServerManager.inner().replicationLinks()
            .failoverAllowDataLoss(this.resourceGroupName,
                this.sqlServerName,
                this.databaseName(),
                this.name());
    }

    @Override
    public Mono<Void> forceFailoverAllowDataLossAsync() {
        return this.sqlServerManager.inner().replicationLinks()
            .failoverAllowDataLossAsync(this.resourceGroupName,
                this.sqlServerName,
                this.databaseName(),
                this.name());
    }

    @Override
    public String name() {
        return this.inner().getName();
    }

    @Override
    public String id() {
        return this.inner().getId();
    }

    @Override
    public String resourceGroupName() {
        return this.resourceGroupName;
    }
}
