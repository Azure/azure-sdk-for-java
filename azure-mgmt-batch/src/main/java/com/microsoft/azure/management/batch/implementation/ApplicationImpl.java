/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.batch.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.batch.Application;
import com.microsoft.azure.management.batch.ApplicationPackage;
import com.microsoft.azure.management.batch.BatchAccount;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import rx.Observable;
import rx.functions.Func1;

import java.util.Map;
import java.util.List;

/**
 * Implementation for BatchAccount Application and its parent interfaces.
 */
@LangDefinition
public class ApplicationImpl
        extends ExternalChildResourceImpl<Application,
                ApplicationInner,
                BatchAccountImpl,
                BatchAccount>
        implements Application,
                Application.Definition<BatchAccount.DefinitionStages.WithApplicationAndStorage>,
                Application.UpdateDefinition<BatchAccount.Update>,
                Application.Update {
    private final ApplicationsInner client;
    private final ApplicationPackagesImpl applicationPackages;

    protected ApplicationImpl(
            String name,
            BatchAccountImpl batchAccount,
            ApplicationInner inner,
            ApplicationsInner client,
            ApplicationPackagesInner applicationPackagesClient) {
        super(name, batchAccount, inner);
        this.client = client;
        applicationPackages = new ApplicationPackagesImpl(applicationPackagesClient, this);
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String displayName() {
        return this.inner().displayName();
    }

    @Override
    public Map<String, ApplicationPackage> applicationPackages() {
        return this.applicationPackages.asMap();
    }

    @Override
    public boolean updatesAllowed() {
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

        return this.client.createAsync(
                this.parent().resourceGroupName(),
                this.parent().name(),
                this.name(), createParameter)
                .map(new Func1<ApplicationInner, Application>() {
                    @Override
                    public Application call(ApplicationInner inner) {
                        self.setInner(inner);
                        return self;
                    }
                })
                .flatMap(new Func1<Application, Observable<? extends Application>>() {
                    @Override
                    public Observable<? extends Application> call(Application application) {
                        return self.applicationPackages.commitAndGetAllAsync()
                                .map(new Func1<List<ApplicationPackageImpl>, Application>() {
                                    @Override
                                    public Application call(List<ApplicationPackageImpl> applications) {
                                        return self;
                                    }
                                });
                    }
                });
    }

    @Override
    public Observable<Application> updateAsync() {
        final ApplicationImpl self = this;

        UpdateApplicationParametersInner updateParameter = new UpdateApplicationParametersInner();
        updateParameter.withDisplayName(this.inner().displayName());
        updateParameter.withAllowUpdates(this.inner().allowUpdates());

        return this.client.updateAsync(this.parent().resourceGroupName(),
                this.parent().name(), this.name(), updateParameter)
                .map(new Func1<Void, Application>() {
                    @Override
                    public Application call(Void result) {
                        return self;
                    }
                })
                .flatMap(new Func1<Application, Observable<? extends Application>>() {
                    @Override
                    public Observable<? extends Application> call(Application application) {
                        return self.applicationPackages.commitAndGetAllAsync()
                                .map(new Func1<List<ApplicationPackageImpl>, Application>() {
                                    @Override
                                    public Application call(List<ApplicationPackageImpl> applications) {
                                        return self;
                                    }
                                });
                    }
                });
    }

    @Override
    public Observable<Void> deleteAsync() {
        return this.client.deleteAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.name());
    }

    @Override
    public Application refresh() {
        ApplicationInner inner =
                this.client.get(this.parent().resourceGroupName(), this.parent().name(), this.inner().id());
        this.setInner(inner);
        this.applicationPackages.refresh();
        return this;
    }

    @Override
    public BatchAccountImpl attach() {
        return this.parent().withApplication(this);
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
            ApplicationsInner client,
            ApplicationPackagesInner applicationPackagesClient) {
        ApplicationInner inner = new ApplicationInner();
        inner.withId(name);
        ApplicationImpl applicationImpl = new ApplicationImpl(name,
                parent,
                inner,
                client, applicationPackagesClient);
        return applicationImpl;
    }

    @Override
    public Update withoutApplicationPackage(String applicationPackageName) {
        this.applicationPackages.remove(applicationPackageName);
        return this;

    }

    ApplicationImpl withApplicationPackage(ApplicationPackageImpl applicationPackage) {
        this.applicationPackages.addApplicationPackage(applicationPackage);
        return this;
    }

    @Override
    public ApplicationImpl defineNewApplicationPackage(String applicationPackageName) {
        this.withApplicationPackage(this.applicationPackages.define(applicationPackageName));
        return this;
    }
}
