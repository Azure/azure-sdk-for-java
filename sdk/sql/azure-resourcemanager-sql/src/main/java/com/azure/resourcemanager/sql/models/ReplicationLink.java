// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.sql.fluent.models.ReplicationLinkInner;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

/** An immutable client-side representation of an Azure SQL Replication link. */
@Fluent
public interface ReplicationLink
    extends Refreshable<ReplicationLink>, HasInnerModel<ReplicationLinkInner>, HasResourceGroup, HasName, HasId {

    /**
     * Gets name of the SQL Server to which this replication belongs.
     *
     * @return name of the SQL Server to which this replication belongs
     */
    String sqlServerName();

    /**
     * Gets name of the SQL Database to which this replication belongs.
     *
     * @return name of the SQL Database to which this replication belongs
     */
    String databaseName();

    /**
     * Gets the name of the Azure SQL Server hosting the partner Azure SQL Database.
     *
     * @return the name of the Azure SQL Server hosting the partner Azure SQL Database.
     */
    String partnerServer();

    /**
     * Gets the name of the partner Azure SQL Database.
     *
     * @return the name of the partner Azure SQL Database
     */
    String partnerDatabase();

    /**
     * Gets the Azure Region of the partner Azure SQL Database.
     *
     * @return the Azure Region of the partner Azure SQL Database
     */
    String partnerLocation();

    /**
     * Gets the role of the SQL Database in the replication link.
     *
     * @return the role of the SQL Database in the replication link
     */
    ReplicationRole role();

    /**
     * Gets the role of the partner SQL Database in the replication link.
     *
     * @return the role of the partner SQL Database in the replication link
     */
    ReplicationRole partnerRole();

    /**
     * Gets start time for the replication link.
     *
     * @return start time for the replication link (ISO8601 format)
     */
    OffsetDateTime startTime();

    /**
     * Gets the percentage of the seeding completed for the replication link.
     *
     * @return the percentage of the seeding completed for the replication link
     */
    int percentComplete();

    /**
     * Gets the replication state for the replication link.
     *
     * @return the replication state for the replication link
     */
    ReplicationState replicationState();

    /**
     * Checks whether termination is allowed.
     *
     * @return the legacy value indicating whether termination is allowed (currently always returns true)
     */
    boolean isTerminationAllowed();

    /**
     * Gets the replication mode of this replication link.
     *
     * @return the replication mode of this replication link
     */
    String replicationMode();

    /** Deletes the replication link. */
    void delete();

    /** Fails over the Azure SQL Database Replication Link. */
    void failover();

    /**
     * Fails over the Azure SQL Database Replication Link.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> failoverAsync();

    /** Forces fail over the Azure SQL Database Replication Link which may result in data loss. */
    void forceFailoverAllowDataLoss();

    /**
     * Forces fail over the Azure SQL Database Replication Link which may result in data loss.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> forceFailoverAllowDataLossAsync();
}
