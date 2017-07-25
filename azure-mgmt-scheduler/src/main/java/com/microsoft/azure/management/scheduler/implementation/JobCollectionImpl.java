/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.scheduler.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.scheduler.JobCollection;
import com.microsoft.azure.management.scheduler.JobCollectionProperties;
import com.microsoft.azure.management.scheduler.JobCollectionQuota;
import com.microsoft.azure.management.scheduler.JobCollectionState;
import com.microsoft.azure.management.scheduler.JobMaxRecurrence;
import com.microsoft.azure.management.scheduler.Jobs;
import com.microsoft.azure.management.scheduler.RecurrenceFrequency;
import com.microsoft.azure.management.scheduler.Sku;
import com.microsoft.azure.management.scheduler.SkuDefinition;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation for Azure Scheduler Job Collection and its create and update interfaces.
 */
@LangDefinition
class JobCollectionImpl
    extends
    GroupableResourceImpl<
        JobCollection,
        JobCollectionDefinitionInner,
        JobCollectionImpl,
        ScheduleServiceManager>
    implements
        JobCollection,
        JobCollection.Definition,
        JobCollection.Update {

    JobCollectionImpl(String name, JobCollectionDefinitionInner jobCollectionInner, ScheduleServiceManager manager) {
        super(name, jobCollectionInner, manager);
    }

    @Override
    protected Observable<JobCollectionDefinitionInner> getInnerAsync() {
        return this.manager().inner().jobCollections().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public Observable<JobCollection> createResourceAsync() {
        final JobCollectionImpl self = this;

        return this.manager().inner().jobCollections().createOrUpdateAsync(resourceGroupName(), name(), inner())
            .map(new Func1<JobCollectionDefinitionInner, JobCollection>() {
                @Override
                public JobCollection call(JobCollectionDefinitionInner jobCollectionInner) {
                    self.setInner(jobCollectionInner);
                    return self;
                }
            });
    }

    @Override
    public JobCollectionImpl withSku(SkuDefinition skuDefinition) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobCollectionProperties());
        }
        this.inner().properties().withSku(new Sku().withName(skuDefinition));

        return this;
    }

    @Override
    public JobCollectionImpl withJobCollectionQuota(JobCollectionQuota quota) {
        // No need to "null" check property member since SKU property should always be valid at the time of this call
        this.inner().properties().withQuota(quota);

        return this;
    }

    @Override
    public JobCollectionImpl withJobCollectionQuota(int maxJobCount, int maxJobOccurrence, RecurrenceFrequency recurrenceFrequency, int retriesInterval) {
        // No need to "null" check property member since SKU property should always be valid at the time of this call
        JobCollectionQuota tempQuota = this.inner().properties().quota();
        if (tempQuota == null) {
            this.inner().properties().withQuota(new JobCollectionQuota()
                .withMaxJobCount(maxJobCount)
                .withMaxJobOccurrence(maxJobOccurrence)
                .withMaxRecurrence(new JobMaxRecurrence()
                    .withFrequency(recurrenceFrequency)
                    .withInterval(retriesInterval)
                )
            );
        } else {
            tempQuota.withMaxJobCount(maxJobCount);
            tempQuota.withMaxJobOccurrence(maxJobOccurrence);

            if (tempQuota.maxRecurrence() == null) {
                tempQuota.withMaxRecurrence(new JobMaxRecurrence().withFrequency(recurrenceFrequency).withInterval(retriesInterval));
            } else {
                tempQuota.maxRecurrence().withFrequency(recurrenceFrequency);
                tempQuota.maxRecurrence().withInterval(retriesInterval);
            }
        }

        return this;
    }

    @Override
    public JobCollectionImpl withState(JobCollectionState state) {
        // No need to "null" check property member since SKU property should always be valid at the time of this call
        this.inner().properties().withState(state);

        return this;
    }

    @Override
    public Sku sku() {
        return this.inner().properties().sku();
    }

    @Override
    public JobCollectionState state() {
        return this.inner().properties().state();
    }

    @Override
    public JobCollectionQuota quota() {
        return this.inner().properties().quota();
    }

    @Override
    public int maxJobCount() {
        // No need to "null" check property member since SKU property should always be valid at the time of this call
        if (this.inner().properties().quota() == null) {
            switch (this.inner().properties().sku().name()) {
                case FREE:
                    return 5;
                case STANDARD:
                    return 50;
                case P10PREMIUM:
                    return 50;
                case P20PREMIUM:
                    return 1000;
                default:
                    return 0;
            }
        } else {
            return this.inner().properties().quota().maxJobCount();
        }
    }

    @Override
    public RecurrenceFrequency maxRecurrenceFrequency() {
        // No need to "null" check property member since SKU property should always be valid at the time of this call
        if (this.inner().properties().quota() == null || this.inner().properties().quota().maxRecurrence() == null) {
            if (this.inner().properties().sku().name() == SkuDefinition.FREE) {
                return RecurrenceFrequency.HOUR;
            } else {
                return RecurrenceFrequency.MINUTE;
            }
        } else {
            return this.inner().properties().quota().maxRecurrence().frequency();
        }
    }

    @Override
    public Jobs jobs() {
        return new JobsImpl(
            this.resourceGroupName(),
            this.name(),
            this.manager()
        );
    }

    @Override
    public void enable() {
        this.manager().jobCollections().enable(this.resourceGroupName(), this.name());
        this.refresh();
    }

    @Override
    public Completable enableAsync() {
        Observable<Void> o = this.manager().inner().jobCollections().enableAsync(this.resourceGroupName(), this.name());
        Observable<JobCollection> r = this.refreshAsync();

        return Observable.concat(o, r).toCompletable();
    }

    @Override
    public void disable() {
        this.manager().jobCollections().disable(this.resourceGroupName(), this.name());
        this.refresh();
    }

    @Override
    public Completable disableAsync() {
        Observable<Void> o = this.manager().inner().jobCollections().disableAsync(this.resourceGroupName(), this.name());
        Observable<JobCollection> r = this.refreshAsync();

        return Observable.concat(o, r).toCompletable();
    }
}
