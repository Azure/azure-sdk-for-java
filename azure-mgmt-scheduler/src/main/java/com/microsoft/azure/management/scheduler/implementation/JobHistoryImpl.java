/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.scheduler.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.scheduler.JobExecutionStatus;
import com.microsoft.azure.management.scheduler.JobHistory;
import com.microsoft.azure.management.scheduler.JobHistoryActionName;
import org.joda.time.DateTime;

/**
 * Describes a job history object for a given job in Azure Scheduler service.
 */
public class JobHistoryImpl extends WrapperImpl<JobHistoryDefinitionInner> implements JobHistory {
    protected JobHistoryImpl(JobHistoryDefinitionInner innerObject) {
        super(innerObject);
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String type() {
        return this.inner().type();
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public DateTime startTime() {
        if (this.inner().properties() != null) {
            return this.inner().properties().startTime();
        } else {
            return null;
        }
    }

    @Override
    public DateTime endTime() {
        if (this.inner().properties() != null) {
            return this.inner().properties().endTime();
        } else {
            return null;
        }
    }

    @Override
    public DateTime expectedExecutionTime() {
        if (this.inner().properties() != null) {
            return this.inner().properties().expectedExecutionTime();
        } else {
            return null;
        }
    }

    @Override
    public JobHistoryActionName actionName() {
        if (this.inner().properties() != null) {
            return this.inner().properties().actionName();
        } else {
            return null;
        }
    }

    @Override
    public JobExecutionStatus status() {
        if (this.inner().properties() != null) {
            return this.inner().properties().status();
        } else {
            return null;
        }
    }

    @Override
    public String message() {
        if (this.inner().properties() != null) {
            return this.inner().properties().message();
        } else {
            return null;
        }
    }

    @Override
    public int retryCount() {
        if (this.inner().properties() != null) {
            return this.inner().properties().retryCount();
        } else {
            return 0;
        }
    }

    @Override
    public int repeatCount() {
        if (this.inner().properties() != null) {
            return this.inner().properties().repeatCount();
        } else {
            return 0;
        }
    }
}
