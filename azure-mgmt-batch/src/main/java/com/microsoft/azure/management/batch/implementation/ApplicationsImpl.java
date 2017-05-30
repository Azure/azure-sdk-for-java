/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.batch.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.batch.Application;
import com.microsoft.azure.management.batch.BatchAccount;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesCachedImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a application collection associated with a batch account.
 */
@LangDefinition
class ApplicationsImpl extends
        ExternalChildResourcesCachedImpl<ApplicationImpl,
                Application,
                ApplicationInner,
                BatchAccountImpl,
                BatchAccount> {

    ApplicationsImpl(BatchAccountImpl parent) {
        super(parent, "Application");
        this.cacheCollection();
    }

    public Map<String, Application> asMap() {
        Map<String, Application> result = new HashMap<>();
        for (Map.Entry<String, ApplicationImpl> entry : this.collection().entrySet()) {
            ApplicationImpl application = entry.getValue();
            result.put(entry.getKey(), application);
        }
        return Collections.unmodifiableMap(result);
    }

    public ApplicationImpl define(String name) {
        return this.prepareDefine(name);
    }

    public ApplicationImpl update(String name) {
        return this.prepareUpdate(name);
    }

    public void remove(String name) {
        this.prepareRemove(name);
    }

    public void addApplication(ApplicationImpl application) {
        this.addChildResource(application);
    }

    @Override
    protected List<ApplicationImpl> listChildResources() {
        List<ApplicationImpl> childResources = new ArrayList<>();
        if (this.parent().inner().id() == null || this.parent().autoStorage() == null) {
            return childResources;
        }

        PagedList<ApplicationInner> applicationList = this.parent().manager().inner().applications().list(
                this.parent().resourceGroupName(), this.parent().name());

        for (ApplicationInner application: applicationList) {
            childResources.add(new ApplicationImpl(
                    application.id(),
                    this.parent(),
                    application));
        }

        return childResources;
    }

    @Override
    protected ApplicationImpl newChildResource(String name) {
        ApplicationImpl application = ApplicationImpl.newApplication(name, this.parent());
        return application;
    }
}
