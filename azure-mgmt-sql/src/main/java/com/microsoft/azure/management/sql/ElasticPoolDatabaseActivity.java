/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasId;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.sql.implementation.ElasticPoolDatabaseActivityInner;
import org.joda.time.DateTime;


/**
 * An immutable client-side representation of an Azure SQL ElasticPool's Database Activity.
 */
@Fluent
public interface ElasticPoolDatabaseActivity extends
        Wrapper<ElasticPoolDatabaseActivityInner>,
        HasResourceGroup,
        HasName,
        HasId {
    /**
     * @return the database name
     */
    String databaseName();

    /**
     * @return the time the operation finished (ISO8601 format)
     */
    DateTime endTime();

    /**
     * @return the error code if available
     */
    int errorCode();

    /**
     * @return the error message if available
     */
    String errorMessage();

    /**
     * @return the error severity if available
     */
    int errorSeverity();

    /**
     * @return the operation name
     */
    String operation();

    /**
     * @return the unique operation ID
     */
    String operationId();

    /**
     * @return the percentage complete if available
     */
    int percentComplete();

    /**
     * @return the name for the Elastic Pool the database is moving into if available
     */
    String requestedElasticPoolName();

    /**
     * @return the name of the current Elastic Pool the database is in if available
     */
    String currentElasticPoolName();

    /**
     * @return the name of the current service objective if available
     */
    String currentServiceObjective();

    /**
     * @return the name of the requested service objective if available
     */
    String requestedServiceObjective();

    /**
     * @return the name of the Azure SQL Server the Elastic Pool is in
     */
    String serverName();

    /**
     * @return the time the operation started (ISO8601 format)
     */
    DateTime startTime();

    /**
     * @return the current state of the operation
     */
    String state();

}

