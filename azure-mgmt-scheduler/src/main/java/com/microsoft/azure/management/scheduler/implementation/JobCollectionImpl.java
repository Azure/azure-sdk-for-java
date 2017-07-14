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
import com.microsoft.azure.management.scheduler.Sku;
import com.microsoft.azure.management.scheduler.SkuDefinition;
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
    public JobCollection.DefinitionStages.WithCreate withSku(SkuDefinition skuDefinition) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobCollectionProperties());
        }
        this.inner().properties().withSku(new Sku().withName(skuDefinition));

        return this;
    }
}
