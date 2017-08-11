/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.scheduler.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.IndependentChildrenImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.scheduler.Job;
import com.microsoft.azure.management.scheduler.JobCollection;
import com.microsoft.azure.management.scheduler.JobHistory;
import com.microsoft.azure.management.scheduler.Jobs;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

/**
 * Provides access to all the Job operations for an Azure Scheduler service.
 */
public class JobsImpl
    extends IndependentChildrenImpl<
                                    Job,
                                    JobImpl,
                                    JobDefinitionInner,
                                    JobsInner,
                                    ScheduleServiceManager,
                                    JobCollection> implements Jobs {
    private final String resourceGroupName;
    private final String jobCollectionName;

    protected JobsImpl(String resourceGroupName, String jobCollectionName, ScheduleServiceManager manager) {
        super(manager.inner().jobs(), manager);
        this.resourceGroupName = resourceGroupName;
        this.jobCollectionName = jobCollectionName;
    }

    @Override
    public Job.DefinitionStages.Blank define(String name) {
        return wrapModel(name);
    }

    @Override
    protected JobImpl wrapModel(String name) {
        return new JobImpl(this.resourceGroupName,
                this.jobCollectionName,
                name,
                new JobDefinitionInner(),
                this.manager());
    }

    @Override
    protected JobImpl wrapModel(JobDefinitionInner inner) {
        return new JobImpl(this.resourceGroupName,
                this.jobCollectionName,
                inner.name(),
                inner,
                this.manager());
    }

    @Override
    public Observable<Job> getByParentAsync(String resourceGroup, String parentName, String name) {
        return this.manager().inner().jobs().getAsync(resourceGroup, parentName, name)
            .map(new Func1<JobDefinitionInner, Job>() {
            @Override
            public Job call(JobDefinitionInner jobDefinitionInner) {
                return wrapModel(jobDefinitionInner);
            }
        });
    }

    @Override
    public PagedList<Job> listByParent(String resourceGroupName, String parentName) {
        return wrapList(this.manager().inner().jobs().list(resourceGroupName, parentName));
    }

    @Override
    public Completable deleteByParentAsync(String groupName, String parentName, String name) {
        return this.manager().inner().jobs().deleteAsync(groupName, parentName, name).toCompletable();
    }

    @Override
    public void deleteByName(String name) {
        this.manager().inner().jobs().delete(this.resourceGroupName, this.jobCollectionName, name);
    }

    @Override
    public ServiceFuture<Void> deleteByNameAsync(String name, ServiceCallback<Void> callback) {
        return this.manager().inner().jobs().deleteAsync(this.resourceGroupName, this.jobCollectionName, name, callback);
    }

    @Override
    public Completable deleteByNameAsync(String name) {
        return this.manager().inner().jobs().deleteAsync(this.resourceGroupName, this.jobCollectionName, name).toCompletable();
    }

    @Override
    public PagedList<Job> list() {
        return listByParent(this.resourceGroupName, this.jobCollectionName);
    }

    @Override
    public Observable<Job> listAsync() {
        return this.manager().inner().jobs().listAsync(this.resourceGroupName, this.jobCollectionName)
            .flatMap(new Func1<Page<JobDefinitionInner>, Observable<Job>>() {
                @Override
                public Observable<Job> call(Page<JobDefinitionInner> jobDefinitionInnerPage) {
                    return Observable.from(jobDefinitionInnerPage.items()).map(new Func1<JobDefinitionInner, Job>() {
                        @Override
                        public Job call(JobDefinitionInner inner) {
                            return wrapModel(inner);
                        }
                    });
                }
            });
    }

    @Override
    public Observable<Job> getByNameAsync(String name) {
        return getByParentAsync(this.resourceGroupName, this.jobCollectionName, name);
    }

    @Override
    public JobImpl getByName(String name) {
        return wrapModel(this.manager().inner().jobs().get(this.resourceGroupName, this.jobCollectionName, name));
    }

    @Override
    public void run(String jobName) {
        manager().inner().jobs().run(this.resourceGroupName, this.jobCollectionName, jobName);
    }

    @Override
    public Completable runAsync(String jobName) {
        return this.manager().inner().jobs().runAsync(this.resourceGroupName, this.jobCollectionName, jobName).toCompletable();
    }

    @Override
    public PagedList<JobHistory> listJobHistory(String jobName) {
        PagedListConverter<JobHistoryDefinitionInner, JobHistory> converter
            = new PagedListConverter<JobHistoryDefinitionInner, JobHistory>() {
            @Override
            public JobHistory typeConvert(JobHistoryDefinitionInner jobHistoryDefinitionInner) {
                return new JobHistoryImpl(jobHistoryDefinitionInner);
            }
        };
        return converter.convert(this.manager().inner().jobs().listJobHistory(this.resourceGroupName, this.jobCollectionName, jobName));
    }

    @Override
    public Observable<JobHistory> listJobHistoryAsync(String jobName) {
        return this.manager().inner().jobs().listJobHistoryAsync(this.resourceGroupName, this.jobCollectionName, jobName)
            .flatMap(new Func1<Page<JobHistoryDefinitionInner>, Observable<JobHistory>>() {
                @Override
                public Observable<JobHistory> call(Page<JobHistoryDefinitionInner> jobHistoryDefinitionInnerPage) {
                    return Observable.from(jobHistoryDefinitionInnerPage.items()).map(new Func1<JobHistoryDefinitionInner, JobHistory>() {
                        @Override
                        public JobHistory call(JobHistoryDefinitionInner jobHistoryDefinitionInner) {
                            return new JobHistoryImpl(jobHistoryDefinitionInner);
                        }
                    });
                }
            });
    }
}
