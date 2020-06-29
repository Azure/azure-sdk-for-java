// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.sql.fluent.inner.ElasticPoolDatabaseActivityInner;
import java.time.OffsetDateTime;

/** An immutable client-side representation of an Azure SQL ElasticPool's Database Activity. */
@Fluent
public interface ElasticPoolDatabaseActivity
    extends HasInner<ElasticPoolDatabaseActivityInner>, HasResourceGroup, HasName, HasId {
    /** @return the database name */
    String databaseName();

    /** @return the time the operation finished (ISO8601 format) */
    OffsetDateTime endTime();

    /** @return the error code if available */
    int errorCode();

    /** @return the error message if available */
    String errorMessage();

    /** @return the error severity if available */
    int errorSeverity();

    /** @return the operation name */
    String operation();

    /** @return the unique operation ID */
    String operationId();

    /** @return the percentage complete if available */
    int percentComplete();

    /** @return the name for the Elastic Pool the database is moving into if available */
    String requestedElasticPoolName();

    /** @return the name of the current Elastic Pool the database is in if available */
    String currentElasticPoolName();

    /** @return the name of the current service objective if available */
    String currentServiceObjective();

    /** @return the name of the requested service objective if available */
    String requestedServiceObjective();

    /** @return the name of the Azure SQL Server the Elastic Pool is in */
    String serverName();

    /** @return the time the operation started (ISO8601 format) */
    OffsetDateTime startTime();

    /** @return the current state of the operation */
    String state();

    /** @return the geo-location where the resource lives. */
    String location();
}
