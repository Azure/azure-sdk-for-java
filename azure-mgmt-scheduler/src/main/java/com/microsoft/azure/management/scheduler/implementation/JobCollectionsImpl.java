/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.scheduler.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupPagedList;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.scheduler.Job;
import com.microsoft.azure.management.scheduler.JobCollection;
import com.microsoft.azure.management.scheduler.JobCollections;
import com.microsoft.azure.management.scheduler.JobHistory;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;

/**
 * The implementation for JobCollections.
 */
@LangDefinition
public class JobCollectionsImpl
    extends GroupableResourcesImpl<
        JobCollection,
        JobCollectionImpl,
        JobCollectionDefinitionInner,
        JobCollectionsInner,
        ScheduleServiceManager>
    implements JobCollections {

    JobCollectionsImpl(final ScheduleServiceManager scheduleServiceManager) {
        super(scheduleServiceManager.inner().jobCollections(), scheduleServiceManager);
    }

    @Override
    public PagedList<JobCollection> list() {
        final JobCollectionsImpl self = this;
        return new GroupPagedList<JobCollection>(this.manager().resourceManager().resourceGroups().list()) {
            @Override
            public List<JobCollection> listNextGroup(String resourceGroupName) {
                return wrapList(self.inner().listByResourceGroup(resourceGroupName));
            }
        };
    }

    @Override
    public Observable<JobCollection> listAsync() {
        return this.manager().resourceManager().resourceGroups().listAsync()
            .flatMap(new Func1<ResourceGroup, Observable<JobCollection>>() {
                @Override
                public Observable<JobCollection> call(ResourceGroup resourceGroup) {
                    return wrapPageAsync(inner().listByResourceGroupAsync(resourceGroup.name()));
                }
            });
    }

    @Override
    public PagedList<JobCollection> listByResourceGroup(String resourceGroupName) {
        return wrapList(this.inner().listByResourceGroup(resourceGroupName));
    }

    @Override
    public Observable<JobCollection> listByResourceGroupAsync(String resourceGroupName) {
        return wrapPageAsync(this.inner().listByResourceGroupAsync(resourceGroupName));
    }

    @Override
    protected Observable<JobCollectionDefinitionInner> getInnerAsync(String resourceGroupName, String name) {
        return this.inner().getByResourceGroupAsync(resourceGroupName, name);
    }

    @Override
    public JobCollectionImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected Completable deleteInnerAsync(String resourceGroupName, String name) {
        return this.inner().deleteAsync(resourceGroupName, name).toCompletable();
    }

    /**************************************************************
     * Fluent model helpers.
     **************************************************************/

    @Override
    protected JobCollectionImpl wrapModel(String name) {
        return new JobCollectionImpl(name,
            new JobCollectionDefinitionInner(),
            this.manager());
    }

    @Override
    protected JobCollectionImpl wrapModel(JobCollectionDefinitionInner inner) {
        if (inner == null) {
            return null;
        }
        return new JobCollectionImpl(inner.name(),
            inner,
            this.manager());
    }

    @Override
    public void enable(String resourceGroupName, String jobCollectionName) {
        this.manager().inner().jobCollections().enable(resourceGroupName, jobCollectionName);
    }

    @Override
    public Completable enableAsync(String resourceGroupName, String jobCollectionName) {
        return this.manager().inner().jobCollections().enableAsync(resourceGroupName, jobCollectionName).toCompletable();
    }

    @Override
    public void disable(String resourceGroupName, String jobCollectionName) {
        this.manager().inner().jobCollections().disable(resourceGroupName, jobCollectionName);
    }

    @Override
    public Completable disableAsync(String resourceGroupName, String jobCollectionName) {
        return this.manager().inner().jobCollections().disableAsync(resourceGroupName, jobCollectionName).toCompletable();
    }

    @Override
    public void runJobByName(String resourceGroupName, String jobCollectionName, String jobName) {
        this.manager().inner().jobs().run(resourceGroupName, jobCollectionName, jobName);
    }

    @Override
    public Completable runJobByNameAsync(String resourceGroupName, String jobCollectionName, String jobName) {
        return this.manager().inner().jobs().runAsync(resourceGroupName, jobCollectionName, jobName).toCompletable();
    }

    @Override
    public Job getJobByName(String resourceGroupName, String jobCollectionName, String jobName) {
        return new JobImpl(resourceGroupName,
            jobCollectionName,
            jobName,
            this.manager().inner().jobs().get(resourceGroupName, jobCollectionName, jobName),
            this.manager());
    }

    @Override
    public Observable<Job> getJobByNameAsync(final String resourceGroupName, final String jobCollectionName, final String jobName) {
        return this.manager().inner().jobs().getAsync(resourceGroupName, jobCollectionName, jobName)
            .map(new Func1<JobDefinitionInner, Job>() {
                @Override
                public Job call(JobDefinitionInner jobDefinitionInner) {
                    return new JobImpl(resourceGroupName,
                        jobCollectionName,
                        jobName,
                        jobDefinitionInner,
                        manager());
                }
            });
    }

    @Override
    public void deleteJobByName(String resourceGroupName, String jobCollectionName, String jobName) {
        this.manager().inner().jobs().delete(resourceGroupName, jobCollectionName, jobName);
    }

    @Override
    public Completable deleteJobByNameAsync(String resourceGroupName, String jobCollectionName, String jobName) {
        return this.manager().inner().jobs().deleteAsync(resourceGroupName, jobCollectionName, jobName).toCompletable();
    }

    @Override
    public PagedList<JobHistory> listJobHistory(String resourceGroupName, String jobCollectionName, String jobName) {
        PagedListConverter<JobHistoryDefinitionInner, JobHistory> converter
            = new PagedListConverter<JobHistoryDefinitionInner, JobHistory>() {
            @Override
            public JobHistory typeConvert(JobHistoryDefinitionInner jobHistoryDefinitionInner) {
                return new JobHistoryImpl(jobHistoryDefinitionInner);
            }
        };
        return converter.convert(this.manager().inner().jobs().listJobHistory(resourceGroupName, jobCollectionName, jobName));
    }

    @Override
    public Observable<JobHistory> listJobHistoryAsync(String resourceGroupName, String jobCollectionName, String jobName) {
        return this.manager().inner().jobs().listJobHistoryAsync(resourceGroupName, jobCollectionName, jobName)
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
