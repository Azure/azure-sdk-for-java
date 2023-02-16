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
public interface ElasticPoolActivity extends HasInnerModel<ElasticPoolOperationInner>, HasResourceGroup, HasName, HasId {
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

    /** @return the name of the Elastic Pool */
    String elasticPoolName();

    /** @return the name of the Azure SQL Server the Elastic Pool is in */
    String serverName();

    /** @return the time the operation started (ISO8601 format) */
    OffsetDateTime startTime();

    /** @return the current state of the operation */
    String state();
}
