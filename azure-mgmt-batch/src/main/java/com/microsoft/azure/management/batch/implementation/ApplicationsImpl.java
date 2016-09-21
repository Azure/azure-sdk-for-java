package com.microsoft.azure.management.batch.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.batch.Application;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a extension collection associated with a virtual machine.
 */
class ApplicationsImpl extends
        ExternalChildResourcesImpl<ApplicationImpl,
                Application,
                ApplicationInner,
                BatchAccountImpl> {
    private final ApplicationsInner client;

    /**
     * Creates new ApplicationsImpl.
     *
     * @param client the client to perform REST calls on applications
     * @param parent the parent virtual machine of the applications
     */
    ApplicationsImpl(ApplicationsInner client, BatchAccountImpl parent) {
        super(parent, "Application");
        this.client = client;
        this.initializeCollection();
    }

    /**
     * @return the extension as a map indexed by name.
     */
    public Map<String, Application> asMap() {
        Map<String, Application> result = new HashMap<>();
        for (Map.Entry<String, ApplicationImpl> entry : this.collection().entrySet()) {
            ApplicationImpl extension = entry.getValue();
            result.put(entry.getKey(), extension);
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Starts an extension definition chain.
     *
     * @param name the reference name of the extension to be added
     * @return the extension
     */
    public ApplicationImpl define(String name) {
        return this.prepareDefine(name);
    }

    /**
     * Starts an extension update chain.
     *
     * @param name the reference name of the extension to be updated
     * @return the extension
     */
    public ApplicationImpl update(String name) {
        return this.prepareUpdate(name);
    }

    /**
     * Mark the extension with given name as to be removed.
     *
     * @param name the reference name of the extension to be removed
     */
    public void remove(String name) {
        this.prepareRemove(name);
    }

    /**
     * Adds the extension to the collection.
     *
     * @param application the application
     */
    public void addApplication(ApplicationImpl application) {
        this.addChildResource(application);
    }

    @Override
    protected List<ApplicationImpl> listChildResources() {
        List<ApplicationImpl> childResources = new ArrayList<>();
        if (this.parent().inner().id() == null || this.parent().inner().autoStorage() == null) {
            return childResources;
        }

        PagedList<ApplicationInner> applicationList = this.client.list(this.parent().resourceGroupName(), this.parent().name());

        for (ApplicationInner application: applicationList) {
            childResources.add(new ApplicationImpl(application.id(), this.parent(), application, this.client));
        }

        return childResources;
    }

    @Override
    protected ApplicationImpl newChildResource(String name) {
        ApplicationImpl extension = ApplicationImpl
                .newApplication(name, this.parent(), this.client);
        return extension;
    }
}
