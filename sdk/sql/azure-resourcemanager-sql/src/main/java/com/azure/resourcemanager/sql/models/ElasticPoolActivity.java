// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.sql.fluent.models.ElasticPoolOperationInner;

import java.time.OffsetDateTime;

/** An immutable client-side representation of an Azure SQL ElasticPool's Activity. */
@Fluent
public interface ElasticPoolActivity
    extends HasInnerModel<ElasticPoolOperationInner>, HasResourceGroup, HasName, HasId {
    /**
     * Gets the time the operation finished.
     *
     * @return the time the operation finished (ISO8601 format)
     */
    OffsetDateTime endTime();

    /**
     * Gets the error code if available.
     *
     * @return the error code if available
     */
    int errorCode();

    /**
     * Gets the error message if available.
     *
     * @return the error message if available
     */
    String errorMessage();

    /**
     * Gets the error severity if available.
     *
     * @return the error severity if available
     */
    int errorSeverity();

    /**
     * Gets the operation name.
     *
     * @return the operation name
     */
    String operation();

    /**
     * Gets the unique operation ID.
     *
     * @return the unique operation ID
     */
    String operationId();

    /**
     * Gets the percentage complete if available.
     *
     * @return the percentage complete if available
     */
    int percentComplete();

    /**
     * Gets the name of the Elastic Pool.
     *
     * @return the name of the Elastic Pool
     */
    String elasticPoolName();

    /**
     * Gets the name of the Azure SQL Server the Elastic Pool is in.
     *
     * @return the name of the Azure SQL Server the Elastic Pool is in
     */
    String serverName();

    /**
     * Gets the time the operation started.
     *
     * @return the time the operation started (ISO8601 format)
     */
    OffsetDateTime startTime();

    /**
     * Gets the current state of the operation.
     *
     * @return the current state of the operation
     */
    String state();
}
