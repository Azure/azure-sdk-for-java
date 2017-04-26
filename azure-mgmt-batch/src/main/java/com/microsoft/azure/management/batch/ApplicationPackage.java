/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.batch;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.batch.implementation.ApplicationPackageInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ExternalChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import org.joda.time.DateTime;
import rx.Completable;

/**
 * An immutable client-side representation of an Azure Batch application package.
 */
@Fluent
public interface ApplicationPackage extends
        ExternalChildResource<ApplicationPackage, Application>,
        HasInner<ApplicationPackageInner> {

    /**
     * @return the state of the application package
     */
    PackageState state();

    /**
     * @return the format of the application package
     */
    String format();

    /**
     * @return the storage URL of the application package where teh application should be uploaded
     */
    String storageUrl();

    /**
     * @return the expiry of the storage URL for the application package
     */
    DateTime storageUrlExpiry();

    /**
     * @return the last time this application package was activated
     */
    DateTime lastActivationTime();

    /**
     * Activates the application package.
     *
     * @param format the format of the uploaded Batch application package, either "zip" or "tar"
     */
    // TODO: this should take an enum
    @Beta
    void activate(String format);

    /**
     * Activates the application package asynchronously.
     *
     * @param format the format of the uploaded Batch application package, either "zip" or "tar"
     * @return a representation of the deferred computation of this call
     */
    // TODO: this should take an enum
    @Beta
    Completable activateAsync(String format);

    /**
     * Activates the application package asynchronously.
     *
     * @param format the format of the uploaded Batch application package, either "zip" or "tar"
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    @Beta
    ServiceFuture<Void> activateAsync(String format, ServiceCallback<Void> callback);

    /**
     * Deletes the application package.
     */
    void delete();
}

