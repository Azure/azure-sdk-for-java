/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.batch.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.batch.Application;
import com.microsoft.azure.management.batch.ApplicationPackage;
import com.microsoft.azure.management.batch.PackageState;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import org.joda.time.DateTime;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

/**
 * Implementation for BatchAccount Application Package and its parent interfaces.
 */
@LangDefinition
public class ApplicationPackageImpl
        extends ExternalChildResourceImpl<ApplicationPackage,
        ApplicationPackageInner,
        ApplicationImpl,
        Application>
        implements ApplicationPackage {
    private final ApplicationPackagesInner client;

    protected ApplicationPackageImpl(String name, ApplicationImpl parent, ApplicationPackageInner inner, ApplicationPackagesInner client) {
        super(name, parent, inner);

        this.client = client;
    }

    protected static ApplicationPackageImpl newApplicationPackage(String name, ApplicationImpl parent, ApplicationPackagesInner client) {
        ApplicationPackageInner inner = new ApplicationPackageInner();
        inner.withVersion(name);
        return new ApplicationPackageImpl(name, parent, inner, client);
    }

    @Override
    public PackageState state() {
        return this.inner().state();
    }

    @Override
    public String id() {
        return this.parent().parent().id() + "/applications/" + this.parent().name() + "/versions/" + this.name();
    }

    @Override
    public String name() {
        return this.inner().version();
    }

    @Override
    public Observable<ApplicationPackage> createAsync() {
        final ApplicationPackageImpl self = this;

        return this.client.createAsync(this.parent().parent().resourceGroupName(), this.parent().parent().name(), this.parent().name(), this.name())
                .map(new Func1<ApplicationPackageInner, ApplicationPackage>() {

                    @Override
                    public ApplicationPackage call(ApplicationPackageInner applicationPackageInner) {
                        self.setInner(applicationPackageInner);
                        return self;
                    }
                });
    }

    @Override
    public Observable<ApplicationPackage> updateAsync() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Observable<Void> deleteAsync() {
        return this.client.deleteAsync(this.parent().parent().resourceGroupName(), this.parent().parent().name(), this.parent().name(), this.name());
    }

    @Override
    public String format() {
        return this.inner().format();
    }

    @Override
    public String storageUrl() {
        return this.inner().storageUrl();
    }

    @Override
    public DateTime storageUrlExpiry() {
        return this.inner().storageUrlExpiry();
    }

    @Override
    public DateTime lastActivationTime() {
        return this.inner().lastActivationTime();
    }

    @Override
    public void activate(String format) {
        this.activateAsync(format).await();
    }

    @Override
    public Completable activateAsync(String format) {
        return this.client.activateAsync(this.parent().parent().resourceGroupName(), this.parent().parent().name(), this.parent().name(), this.name(), format).toCompletable();
    }

    @Override
    public ServiceFuture<Void> activateAsync(String format, ServiceCallback<Void> callback) {
        return ServiceFuture.fromBody(this.activateAsync(format).<Void>toObservable(), callback);
    }

    @Override
    public void delete() {
        this.deleteAsync().toBlocking().last();
    }

    @Override
    protected Observable<ApplicationPackageInner> getInnerAsync() {
        return this.client.getAsync(this.parent().parent().resourceGroupName(), this.parent().parent().name(), this.parent().name(), this.name());
    }
}
