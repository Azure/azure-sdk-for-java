/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.batch.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.batch.Application;
import com.microsoft.azure.management.batch.ApplicationPackage;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesImpl;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;

/**
 * Represents a applicationPackage collection associated with an application.
 */
@LangDefinition
class ApplicationPackagesImpl extends
        ExternalChildResourcesImpl<ApplicationPackageImpl,
                ApplicationPackage,
                ApplicationPackageInner,
                ApplicationImpl,
                        Application> {
    private final ApplicationPackagesInner client;
    private final ApplicationImpl parent;

    ApplicationPackagesImpl(ApplicationPackagesInner client, ApplicationImpl parent) {
        super(parent, "ApplicationPackage");
        this.client = client;
        this.parent = parent;
        this.initializeCollection();
    }

    public ApplicationPackageImpl define(String name) {
        return this.prepareDefine(name);
    }

    public void remove(String applicationPackageName) {
        this.prepareRemove(applicationPackageName);
    }

    @Override
    protected List<ApplicationPackageImpl> listChildResources() {
        List<ApplicationPackageImpl> childResources = new ArrayList<>();

        if (this.parent().inner().packages() == null || this.parent().inner().packages().size() == 0) {
            return childResources;
        }

        List<ApplicationPackageInner> applicationPackageList = this.parent.inner().packages();

        for (ApplicationPackageInner applicationPackage: applicationPackageList) {
            childResources.add(new ApplicationPackageImpl(applicationPackage.version(), this.parent(), applicationPackage, this.client));
        }

        return childResources;
    }

    @Override
    protected ApplicationPackageImpl newChildResource(String name) {
        ApplicationPackageImpl applicationPackage = ApplicationPackageImpl
                .newApplicationPackage(name, this.parent(), this.client);
        return applicationPackage;
    }

    public void addApplicationPackage(ApplicationPackageImpl applicationPackage) {
        this.addChildResource(applicationPackage);
    }

    public Map<String, ApplicationPackage> asMap() {
        Map<String, ApplicationPackage> result = new HashMap<>();

        for (Map.Entry<String, ApplicationPackageImpl> entry : this.collection().entrySet()) {
            ApplicationPackageImpl applicationPackage = entry.getValue();
            result.put(entry.getKey(), applicationPackage);
        }
        return Collections.unmodifiableMap(result);
    }
}
