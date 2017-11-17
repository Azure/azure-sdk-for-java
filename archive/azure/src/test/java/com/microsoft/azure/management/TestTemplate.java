/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.ManagerBase;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import org.junit.Assert;

import java.io.IOException;

/**
 * Base class for CRUD test cases for top level Azure resource models.
 * @param <ResourceT> Top level resource type
 * @param <CollectionT> Type representing the collection of the top level resources
 */
public abstract class TestTemplate<
    ResourceT extends GroupableResource<? extends ManagerBase, ?>,
    CollectionT extends
        SupportsListing<ResourceT>
        & SupportsGettingByResourceGroup<ResourceT>
        & SupportsDeletingById
        & SupportsGettingById<ResourceT>
        & HasInner<?>
        & HasManager<? extends ManagerBase>> {

    protected final String testId;
    private ResourceT resource;
    private CollectionT collection;
    private ResourceGroups resourceGroups;

    protected TestTemplate() {
        this.testId = SdkContext.randomResourceName("", 8);
    }

    /**
     * Resource creation logic.
     * @param resources collection of resources
     * @return created resource
     * @throws Exception if anything goes wrong
     */
    public abstract ResourceT createResource(CollectionT resources) throws Exception;

    /**
     * Resource update logic.
     * @param resource the resource to update
     * @return the updated resource
     * @throws Exception if anything goes wrong
     */
    public abstract ResourceT updateResource(ResourceT resource) throws Exception;

    /**
     * Tests the listing logic.
     * @return number of resources in the list
     * @throws CloudException if anything goes wrong
     * @throws IOException if anything goes wrong
     */
    public int verifyListing() throws CloudException, IOException {
        PagedList<ResourceT> resources = this.collection.list();
        for (ResourceT r : resources) {
            System.out.println("resource id: " + r.id());
        }
        return resources.size();
    }

    /**
     * Tests the getting logic.
     * @return the gotten resource
     * @throws CloudException if anything goes wrong
     * @throws IOException if anything goes wrong
     */
    public ResourceT verifyGetting() throws CloudException, IOException {
        ResourceT resourceByGroup = this.collection.getByResourceGroup(this.resource.resourceGroupName(), this.resource.name());
        ResourceT resourceById = this.collection.getById(resourceByGroup.id());
        Assert.assertTrue(resourceById.id().equalsIgnoreCase(resourceByGroup.id()));
        return resourceById;
    }

    /**
     * Tests the deletion logic.
     * @throws Exception if anything goes wrong
     */
    public void verifyDeleting() throws Exception {
        final String groupName = this.resource.resourceGroupName();
        this.collection.deleteById(this.resource.id());
        this.resourceGroups.beginDeleteByName(groupName);
    }

    /**
     * Prints information about the resource.
     *
     * @param resource resource to print
     */
    public abstract void print(ResourceT resource);

    /**
     * Runs the test.
     * @param collection collection of resources to test
     * @param resourceGroups the resource groups collection
     * @throws Exception if anything goes wrong
     */
    public void runTest(CollectionT collection, ResourceGroups resourceGroups) throws Exception {
        this.collection = collection;
        this.resourceGroups = resourceGroups;

        // Initial listing
        verifyListing();

        // Verify creation
        this.resource = createResource(collection);
        System.out.println("\n------------\nAfter creation:\n");
        print(this.resource);

        // Verify listing
        verifyListing();

        // Verify getting
        this.resource = verifyGetting();
        Assert.assertTrue(this.resource != null);
        System.out.println("\n------------\nRetrieved resource:\n");
        print(this.resource);

        boolean failedUpdate = false;
        String message = "Update Failed";
        // Verify update
        try {
            this.resource = updateResource(this.resource);
            Assert.assertTrue(this.resource != null);
            System.out.println("\n------------\nUpdated resource:\n");
            message = "Print failed";
            print(this.resource);
        } catch (Exception e) {
            e.printStackTrace();
            failedUpdate = true;
        }

        // Verify deletion
        boolean failedDelete = false;
        try {
            message = "Delete failed";
            verifyDeleting();
        } catch (Exception e) {
            e.printStackTrace();
            failedDelete = true;
        }
        Assert.assertFalse(message, failedUpdate);
        Assert.assertFalse(message,  failedDelete);
    }
}
