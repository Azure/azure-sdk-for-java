/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.scheduler;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import org.joda.time.DateTime;

/**
 * Response containing the execution history for a given Azure Scheduler job.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_2_0)
public interface JobHistory {
    /**
     * @return the id of the job history resource
     */
    String id();

    /**
     * @return the type of the job history resource
     */
    String type();

    /**
     * @return the name of the job history resource
     */
    String name();

    /**
     * @return the execution start time for a job
     */
    DateTime startTime();

    /**
     * @return the execution end time for a job
     */
    DateTime endTime();

    /**
     * @return the expected execution time for a job
     */
    DateTime expectedExecutionTime();

    /**
     * @return the action name
     */
    JobHistoryActionName actionName();

    /**
     * @return the status
     */
    JobExecutionStatus status();

    /**
     * @return the message
     */
    String message();

    /**
     * @return the retry count
     */
    int retryCount();

    /**
     * @return the repeat count
     */
    int repeatCount();
}
