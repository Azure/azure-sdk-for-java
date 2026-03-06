// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.discovery;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.discovery.models.BookshelfPrivateEndpointConnection;
import com.azure.resourcemanager.discovery.models.BookshelfPrivateLinkResource;
import com.azure.resourcemanager.discovery.models.WorkspacePrivateEndpointConnection;
import com.azure.resourcemanager.discovery.models.WorkspacePrivateLinkResource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live tests for Private Endpoint related operations against EUAP endpoint.
 * 
 * Covers:
 * - WorkspacePrivateEndpointConnections
 * - WorkspacePrivateLinkResources
 * - BookshelfPrivateEndpointConnections
 * - BookshelfPrivateLinkResources
 */
public class PrivateEndpointTests extends DiscoveryManagementTest {

    private static final String WORKSPACE_RESOURCE_GROUP = "newapiversiontest";
    private static final String WORKSPACE_NAME = "wrksptest44";
    private static final String BOOKSHELF_NAME = "test-bookshelf";
    private static final String PE_CONNECTION_NAME = "test-pe-connection";

    // ============ Workspace Private Endpoint Connection Tests ============

    @Test
    @Disabled("Requires existing private endpoint connections for the workspace")
    public void testListWorkspacePrivateEndpointConnections() {
        PagedIterable<WorkspacePrivateEndpointConnection> connections
            = discoveryManager.workspacePrivateEndpointConnections()
                .listByWorkspace(WORKSPACE_RESOURCE_GROUP, WORKSPACE_NAME);
        assertNotNull(connections);

        List<WorkspacePrivateEndpointConnection> connectionList = new ArrayList<>();
        for (WorkspacePrivateEndpointConnection connection : connections) {
            assertNotNull(connection.name());
            connectionList.add(connection);
        }

        assertNotNull(connectionList);
    }

    @Test
    @Disabled("Requires existing private endpoint connection")
    public void testGetWorkspacePrivateEndpointConnection() {
        WorkspacePrivateEndpointConnection connection = discoveryManager.workspacePrivateEndpointConnections()
            .get(WORKSPACE_RESOURCE_GROUP, WORKSPACE_NAME, PE_CONNECTION_NAME);
        assertNotNull(connection);
        assertNotNull(connection.name());
    }

    @Test
    @Disabled("Create requires private endpoint configuration and network setup")
    public void testCreateWorkspacePrivateEndpointConnection() {
        // Private endpoint connection creation requires proper network setup
        // This test is a placeholder for integration testing
    }

    @Test
    @Disabled("Delete requires existing private endpoint connection")
    public void testDeleteWorkspacePrivateEndpointConnection() {
        // Private endpoint connection deletion requires an existing connection
        // This test is a placeholder for integration testing
    }

    // ============ Workspace Private Link Resource Tests ============

    @Test
    @Disabled("Requires workspace configured for private endpoint")
    public void testListWorkspacePrivateLinkResources() {
        PagedIterable<WorkspacePrivateLinkResource> linkResources = discoveryManager.workspacePrivateLinkResources()
            .listByWorkspace(WORKSPACE_RESOURCE_GROUP, WORKSPACE_NAME);
        assertNotNull(linkResources);

        List<WorkspacePrivateLinkResource> resourceList = new ArrayList<>();
        for (WorkspacePrivateLinkResource resource : linkResources) {
            assertNotNull(resource.name());
            resourceList.add(resource);
        }

        assertNotNull(resourceList);
    }

    @Test
    @Disabled("Requires workspace configured for private endpoint")
    public void testGetWorkspacePrivateLinkResource() {
        WorkspacePrivateLinkResource linkResource = discoveryManager.workspacePrivateLinkResources()
            .get(WORKSPACE_RESOURCE_GROUP, WORKSPACE_NAME, "workspace");
        assertNotNull(linkResource);
    }

    // ============ Bookshelf Private Endpoint Connection Tests ============

    @Test
    @Disabled("Requires existing private endpoint connections for the bookshelf")
    public void testListBookshelfPrivateEndpointConnections() {
        PagedIterable<BookshelfPrivateEndpointConnection> connections
            = discoveryManager.bookshelfPrivateEndpointConnections()
                .listByBookshelf(WORKSPACE_RESOURCE_GROUP, BOOKSHELF_NAME);
        assertNotNull(connections);

        List<BookshelfPrivateEndpointConnection> connectionList = new ArrayList<>();
        for (BookshelfPrivateEndpointConnection connection : connections) {
            assertNotNull(connection.name());
            connectionList.add(connection);
        }

        assertNotNull(connectionList);
    }

    @Test
    @Disabled("Requires existing private endpoint connection")
    public void testGetBookshelfPrivateEndpointConnection() {
        BookshelfPrivateEndpointConnection connection = discoveryManager.bookshelfPrivateEndpointConnections()
            .get(WORKSPACE_RESOURCE_GROUP, BOOKSHELF_NAME, PE_CONNECTION_NAME);
        assertNotNull(connection);
        assertNotNull(connection.name());
    }

    @Test
    @Disabled("Create requires private endpoint configuration and network setup")
    public void testCreateBookshelfPrivateEndpointConnection() {
        // Private endpoint connection creation requires proper network setup
        // This test is a placeholder for integration testing
    }

    @Test
    @Disabled("Delete requires existing private endpoint connection")
    public void testDeleteBookshelfPrivateEndpointConnection() {
        // Private endpoint connection deletion requires an existing connection
        // This test is a placeholder for integration testing
    }

    // ============ Bookshelf Private Link Resource Tests ============

    @Test
    @Disabled("Requires bookshelf configured for private endpoint")
    public void testListBookshelfPrivateLinkResources() {
        PagedIterable<BookshelfPrivateLinkResource> linkResources = discoveryManager.bookshelfPrivateLinkResources()
            .listByBookshelf(WORKSPACE_RESOURCE_GROUP, BOOKSHELF_NAME);
        assertNotNull(linkResources);

        List<BookshelfPrivateLinkResource> resourceList = new ArrayList<>();
        for (BookshelfPrivateLinkResource resource : linkResources) {
            assertNotNull(resource.name());
            resourceList.add(resource);
        }

        assertNotNull(resourceList);
    }

    @Test
    @Disabled("Requires bookshelf configured for private endpoint")
    public void testGetBookshelfPrivateLinkResource() {
        BookshelfPrivateLinkResource linkResource = discoveryManager.bookshelfPrivateLinkResources()
            .get(WORKSPACE_RESOURCE_GROUP, BOOKSHELF_NAME, "bookshelf");
        assertNotNull(linkResource);
    }
}
