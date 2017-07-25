/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.scheduler.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChild;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildImpl;
import com.microsoft.azure.management.scheduler.Job;
import com.microsoft.azure.management.scheduler.JobAction;
import com.microsoft.azure.management.scheduler.JobCollection;
import com.microsoft.azure.management.scheduler.JobRecurrence;
import com.microsoft.azure.management.scheduler.JobState;
import com.microsoft.azure.management.scheduler.JobStatus;
import org.joda.time.DateTime;
import rx.Observable;
import rx.functions.Func1;

/**
 * Implementation for Azure Scheduler Job.
 */
@LangDefinition
public class JobImpl
        extends IndependentChildImpl<
                                Job,
                                JobCollection,
                                JobDefinitionInner,
                                JobImpl,
                                ScheduleServiceManager>
        implements
            Job,
            Job.Definition,
            Job.Update,
            IndependentChild.DefinitionStages.WithParentResource<Job, JobCollection> {

    /**
     * Creates a new instance of IndependentChildResourceImpl.
     *
     * @param name        the name of the resource
     * @param innerObject the inner object
     * @param manager
     */
    protected JobImpl(String resourceGroupName, String jobCollectionName, String name, JobDefinitionInner innerObject, ScheduleServiceManager manager) {
        super(name, innerObject, manager);
        this.withExistingParentResource(resourceGroupName, jobCollectionName);
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String jobCollectionName() {
        return this.parentName;
    }

    @Override
    public DateTime startTime() {
        return inner().properties().startTime();
    }

    @Override
    public JobAction action() {
        return inner().properties().action();
    }

    @Override
    public JobRecurrence recurrence() {
        return inner().properties().recurrence();
    }

    @Override
    public JobState state() {
        return inner().properties().state();
    }

    @Override
    public JobStatus status() {
        return inner().properties().status();
    }

    @Override
    protected Observable<JobDefinitionInner> getInnerAsync() {
        return this.manager().inner().jobs().getAsync(this.resourceGroupName(), this.jobCollectionName(), this.name());
    }

    @Override
    protected Observable<Job> createChildResourceAsync() {
        final JobImpl self = this;
        return this.manager().inner().jobs().createOrUpdateAsync(this.resourceGroupName(), this.jobCollectionName(), this.name(), this.inner().properties())
            .map(new Func1<JobDefinitionInner, Job>() {
                @Override
                public Job call(JobDefinitionInner jobDefinitionInner) {
                    setInner(jobDefinitionInner);
                    return self;
                }
            });
    }

    @Override
    public JobImpl withStartTime(DateTime startTime) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
        }
        this.inner().properties().withStartTime(startTime);

        return this;
    }

    @Override
    public JobImpl withRecurrence(JobRecurrence jobRecurrence) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
        }
        this.inner().properties().withRecurrence(jobRecurrence);

        return this;
    }

    @Override
    public JobImpl withAction(JobAction jobAction) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
        }
        this.inner().properties().withAction(jobAction);

        return this;
    }

    @Override
    public JobImpl withState(JobState state) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
        }
        this.inner().properties().withState(state);

        return this;
    }
}
