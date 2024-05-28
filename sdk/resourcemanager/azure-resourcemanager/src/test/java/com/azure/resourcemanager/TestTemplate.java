// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.resourcemanager.resources.fluentcore.arm.Manager;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.resources.models.ResourceGroups;
import com.azure.resourcemanager.test.utils.TestUtilities;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;

/**
 * Base class for CRUD test cases for top level Azure resource models.
 *
 * @param <ResourceT> Top level resource type
 * @param <CollectionT> Type representing the collection of the top level resources
 */
public abstract class TestTemplate<ResourceT extends GroupableResource<? extends Manager<?>, ?>,
    CollectionT extends
        SupportsListing<ResourceT> & SupportsGettingByResourceGroup<ResourceT> & SupportsDeletingById
            & SupportsGettingById<ResourceT> & HasManager<?>> {

    private static final ClientLogger LOGGER = new ClientLogger(TestTemplate.class);

    private ResourceT resource;
    private CollectionT collection;
    private ResourceGroups resourceGroups;

    protected TestTemplate() {
    }

    /**
     * Resource creation logic.
     *
     * @param resources collection of resources
     * @return created resource
     * @throws Exception if anything goes wrong
     */
    public abstract ResourceT createResource(CollectionT resources) throws Exception;

    /**
     * Resource update logic.
     *
     * @param resource the resource to update
     * @return the updated resource
     * @throws Exception if anything goes wrong
     */
    public abstract ResourceT updateResource(ResourceT resource) throws Exception;

    /**
     * Tests the listing logic.
     *
     * @return number of resources in the list
     * @throws ManagementException if anything goes wrong
     * @throws IOException if anything goes wrong
     */
    public int verifyListing() throws ManagementException, IOException {
        PagedIterable<ResourceT> resources = this.collection.list();
        for (ResourceT r : resources) {
            LOGGER.log(LogLevel.VERBOSE, () -> "resource id: " + r.id());
        }
        return TestUtilities.getSize(resources);
    }

    /**
     * Tests the getting logic.
     *
     * @return the gotten resource
     * @throws ManagementException if anything goes wrong
     * @throws IOException if anything goes wrong
     */
    public ResourceT verifyGetting() throws ManagementException, IOException {
        ResourceT resourceByGroup =
            this.collection.getByResourceGroup(this.resource.resourceGroupName(), this.resource.name());
        ResourceT resourceById = this.collection.getById(resourceByGroup.id());
        Assertions.assertTrue(resourceById.id().equalsIgnoreCase(resourceByGroup.id()));
        return resourceById;
    }

    /**
     * Tests the deletion logic.
     *
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
     *
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
        LOGGER.log(LogLevel.VERBOSE, () -> "\n------------\nAfter creation:\n");
        print(this.resource);

        // Verify listing
        verifyListing();

        // Verify getting
        this.resource = verifyGetting();
        Assertions.assertNotNull(this.resource);
        LOGGER.log(LogLevel.VERBOSE, () -> "\n------------\nRetrieved resource:\n");
        print(this.resource);

        boolean failedUpdate = false;
        String message = "Update Failed";
        // Verify update
        try {
            this.resource = updateResource(this.resource);
            Assertions.assertNotNull(this.resource);
            LOGGER.log(LogLevel.VERBOSE, () -> "\n------------\nUpdated resource:\n");
            message = "Print failed";
            print(this.resource);
        } catch (Exception e) {
            LOGGER.log(LogLevel.VERBOSE, () -> "Update failed", e);
            failedUpdate = true;
        }

        // Verify deletion
        boolean failedDelete = false;
        try {
            message = "Delete failed";
            verifyDeleting();
        } catch (Exception e) {
            LOGGER.log(LogLevel.VERBOSE, () -> "Delete failed", e);
            failedDelete = true;
        }
        Assertions.assertFalse(failedUpdate, message);
        Assertions.assertFalse(failedDelete, message);
    }
}
