/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.batch;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.batch.implementation.ApplicationPackageInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ExternalChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import org.joda.time.DateTime;

/**
 * An immutable client-side representation of an Azure batch account application.
 */
@Fluent
public interface ApplicationPackage extends
        ExternalChildResource<ApplicationPackage, Application>,
        Wrapper<ApplicationPackageInner> {

    /**
     * @return the name of application package.
     */
    String name();

    /**
     * @return the sate of the application package
     */
    PackageState state();

    /**
     * @return the format of application package
     */
    String format();

    /**
     * @return the storage Url of application package where application should be uploaded
     */
    String storageUrl();

    /**
     * @return the expiry of the storage url for application package
     */
    DateTime storageUrlExpiry();

    /**
     * @return the date when last time this application package was activate.
     */
    DateTime lastActivationTime();

    /**
     * Activates the application package.
     *
     * @param format format of the uploaded package supported values zip, tar
     */
    void activate(String format);

    /**
     * Deletes the application package.
     */
    void delete();
}

