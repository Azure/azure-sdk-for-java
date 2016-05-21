/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.microsoft.azure.implementation.Azure;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

public abstract class TestTemplate<
    T extends GroupableResource,
    C extends SupportsListing<T> & SupportsGettingByGroup<T> & SupportsDeleting> {

    protected String testId = String.valueOf(System.currentTimeMillis());
    private T resource;
    private Azure azure;
    private C collection;

    /**
     * Resource creation logic
     * @param azure authenticated Azure instance
     * @return created resource
     * @throws Exception 
     */
    public abstract T createResource(Azure azure) throws Exception;

    /** 
     * Resource update logic
     * @param resource the resource to update
     * @throws Exception 
     */
    public abstract T updateResource(T resource) throws Exception;

    /**
     * Tests the listing logic
     * @throws CloudException
     * @throws IOException
     */
    public void verifyListing() throws CloudException, IOException {
        int count = this.collection.list().size();
        System.out.println("Collection size: " + count);
        Assert.assertTrue(0 < count);
    }

    /**
     * Tests the getting logic
     * @throws CloudException
     * @throws IOException
     */
    public T verifyGetting() throws CloudException, IOException {
        return this.collection.get(this.resource.resourceGroupName(), this.resource.name());
    }

    /**
     * Tests the deletion logic
     * @throws Exception
     */
    public void verifyDeleting() throws Exception {
        final String groupName = this.resource.resourceGroupName();
        this.collection.delete(this.resource.id());
        this.azure.resourceGroups().delete(groupName);
    }

    /**
     * Prints information about the resource
     * @param resource
     */
    public abstract void print(T resource);

    /**
     * Runs the test
     * @param azure authenticated Azure instance
     * @param collection collection of resources to test
     * @throws Exception
     */
    @Test
    public void runTest(Azure azure, C collection) throws Exception {
        this.azure = azure;
        this.collection = collection;

        // Verify creation
        this.resource = createResource(azure);
        System.out.println("\n------------\nAfter creation:\n");
        print(this.resource);

        // Verify listing
        verifyListing();

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
