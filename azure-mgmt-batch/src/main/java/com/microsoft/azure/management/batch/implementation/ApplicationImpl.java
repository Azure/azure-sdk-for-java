/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.batch.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.batch.Application;
import com.microsoft.azure.management.batch.BatchAccount;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;

/**
 * Implementation for BatchAccount and its parent interfaces.
 */
@LangDefinition
public class ApplicationImpl
        extends ExternalChildResourceImpl<Application,
                            ApplicationInner,
                            BatchAccountImpl>
        implements Application,
            Application.Definition<BatchAccount.DefinitionStages.WithStorage>,
            Application.UpdateDefinition<BatchAccount.Update>,
            Application.Update {
    private final ApplicationsInner client;

    protected ApplicationImpl(String name, BatchAccountImpl batchAccount, ApplicationInner inner, ApplicationsInner client) {
        super(name, batchAccount, inner);
        this.client = client;
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public BatchAccountImpl parent() {
        return this.parent;
    }

    @Override
    public String displayName() {
        return this.inner().displayName();
    }

    @Override
    public List<ApplicationPackageInner> packages() {
        return this.inner().packages();
    }

    @Override
    public boolean allowUpdates() {
        return this.inner().allowUpdates();
    }

    @Override
    public String defaultVersion() {
        return this.inner().defaultVersion();
    }

    @Override
    public Observable<Application> createAsync() {
        final ApplicationImpl self = this;
        AddApplicationParametersInner createParameter = new AddApplicationParametersInner();
        createParameter.withDisplayName(this.inner().displayName());
        createParameter.withAllowUpdates(this.inner().allowUpdates());

        return this.client.createAsync(this.parent.resourceGroupName(),
                this.parent.name(),
                this.name(), createParameter)
                .map(new Func1<ApplicationInner, Application>() {
                    @Override
                    public Application call(ApplicationInner inner) {
                        self.setInner(inner);
                        return self;
                    }
                });
    }

    @Override
    public Observable<Application> updateAsync() {
        final ApplicationImpl self = this;

        String applicationId = ResourceUtils.nameFromResourceId(this.inner().id());
        UpdateApplicationParametersInner updateParameter = new UpdateApplicationParametersInner();
        updateParameter.withDisplayName(this.inner().displayName());
        updateParameter.withAllowUpdates(this.inner().allowUpdates());

        return this.client.updateAsync(this.parent.resourceGroupName(),
                this.parent.name(), applicationId, updateParameter)
                .map(new Func1<Void, Application>() {
                @Override
                public Application call(Void result) {
                    return self;
                }
        });
    }

    @Override
    public Observable<Void> deleteAsync() {
        return this.client.deleteAsync(this.parent.resourceGroupName(),
                this.parent.name(),
                this.name()).map(new Func1<Void, Void>() {
            @Override
            public Void call(Void result) {
                return result;
            }
        });
    }

    @Override
    public Application refresh() {
        ApplicationInner inner =
                this.client.get(this.parent.resourceGroupName(), this.parent.name(), this.inner().id());
        this.setInner(inner);
        return this;
    }

    @Override
    public BatchAccountImpl attach() {
        return this.parent.withApplication(this);
    }

    @Override
    public ApplicationImpl withAllowUpdates(boolean allowUpdates) {
        this.inner().withAllowUpdates(allowUpdates);
        return this;
    }

    @Override
    public ApplicationImpl withDisplayName(String displayName) {
        this.inner().withDisplayName(displayName);
        return this;
    }

    protected static ApplicationImpl newApplication(
            String name,
            BatchAccountImpl parent,
            ApplicationsInner client) {
        ApplicationInner inner = new ApplicationInner();
        ApplicationImpl applicationImpl = new ApplicationImpl(name,
                parent,
                inner,
                client);
        return applicationImpl;
    }
}
