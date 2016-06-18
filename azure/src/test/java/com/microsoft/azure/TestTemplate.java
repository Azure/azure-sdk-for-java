/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure;

import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import org.junit.Assert;

import java.io.IOException;

/**
 * Base class for CRUD test cases for top level Azure resource models.
 * @param <T> Top level resource type
 * @param <C> Type representing the collection of the top level resources
 */
public abstract class TestTemplate<
    T extends GroupableResource,
    C extends SupportsListing<T> & SupportsGettingByGroup<T> & SupportsDeleting & SupportsGettingById<T>> {

    protected String testId = String.valueOf(System.currentTimeMillis() % 100000L);
    private T resource;
    private C collection;
    private ResourceGroups resourceGroups;

    /**
     * Resource creation logic.
     * @param resources collection of resources
     * @return created resource
     * @throws Exception if anything goes wrong
     */
    public abstract T createResource(C resources) throws Exception;

    /**
     * Resource update logic.
     * @param resource the resource to update
     * @return the updated resource
     * @throws Exception if anything goes wrong
     */
    public abstract T updateResource(T resource) throws Exception;

    /**
     * Tests the listing logic.
     * @return number of resources in the list
     * @throws CloudException if anything goes wrong
     * @throws IOException if anything goes wrong
     */
    public int verifyListing() throws CloudException, IOException {
        return this.collection.list().size();
    }

    /**
     * Tests the getting logic.
     * @return the gotten resource
     * @throws CloudException if anything goes wrong
     * @throws IOException if anything goes wrong
     */
    public T verifyGetting() throws CloudException, IOException {
        T resourceByGroup = this.collection.getByGroup(this.resource.resourceGroupName(), this.resource.name());
        T resourceById = this.collection.getById(resourceByGroup.id());
        Assert.assertTrue(resourceById.id().equalsIgnoreCase(resourceByGroup.id()));
        return resourceById;
    }

    /**
     * Tests the deletion logic.
     * @throws Exception if anything goes wrong
     */
    public void verifyDeleting() throws Exception {
        final String groupName = this.resource.resourceGroupName();
        this.collection.delete(this.resource.id());
        this.resourceGroups.delete(groupName);
    }

    /**
     * Prints information about the resource.
     *
     * @param resource resource to print
     */
    public abstract void print(T resource);

    /**
     * Runs the test.
     * @param collection collection of resources to test
     * @param resourceGroups the resource groups collection
     * @throws Exception if anything goes wrong
     */
    public void runTest(C collection, ResourceGroups resourceGroups) throws Exception {
        this.collection = collection;
        this.resourceGroups = resourceGroups;

        // Initial listing
        final int initialCount = verifyListing();

        // Verify creation
        this.resource = createResource(collection);
        System.out.println("\n------------\nAfter creation:\n");
        print(this.resource);

        // Verify listing
        Assert.assertTrue(verifyListing() - initialCount == 1);

        // Verify getting
        this.resource = verifyGetting();
        Assert.assertTrue(this.resource != null);
        System.out.println("\n------------\nRetrieved resource:\n");
        print(this.resource);

        // Verify update
        this.resource = updateResource(this.resource);
        Assert.assertTrue(this.resource != null);
        System.out.println("\n------------\nUpdated resource:\n");
        print(this.resource);

        // Verify deletion
        verifyDeleting();
    }
}
