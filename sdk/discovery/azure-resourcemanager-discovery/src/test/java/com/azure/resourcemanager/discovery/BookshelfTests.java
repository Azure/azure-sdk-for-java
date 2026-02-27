// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.discovery;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.resourcemanager.discovery.models.Bookshelf;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live tests for Bookshelf operations against EUAP endpoint.
 * 
 * Tests match the comprehensive coverage in Python SDK:
 * - test_list_bookshelves_by_subscription
 * - test_list_bookshelves_by_resource_group
 */
public class BookshelfTests extends DiscoveryManagementTest {

    // Resource group that has bookshelves
    private static final String BOOKSHELF_RESOURCE_GROUP = "olawal";

    @Test
    public void testListBookshelvesBySubscription() {
        // Test listing bookshelves in the subscription
        PagedIterable<Bookshelf> bookshelves = discoveryManager.bookshelves().list();
        assertNotNull(bookshelves);

        // Collect all bookshelves
        List<Bookshelf> bookshelfList = new ArrayList<>();
        for (Bookshelf bookshelf : bookshelves) {
            assertNotNull(bookshelf.name());
            assertNotNull(bookshelf.id());
            assertNotNull(bookshelf.type());
            bookshelfList.add(bookshelf);
        }

        // Bookshelves list should be a valid list (may be empty)
        assertNotNull(bookshelfList);
    }

    @Test
    public void testListBookshelvesByResourceGroup() {
        // Test listing bookshelves in a specific resource group
        PagedIterable<Bookshelf> bookshelves
            = discoveryManager.bookshelves().listByResourceGroup(BOOKSHELF_RESOURCE_GROUP);
        assertNotNull(bookshelves);

        // Collect all bookshelves
        List<Bookshelf> bookshelfList = new ArrayList<>();
        for (Bookshelf bookshelf : bookshelves) {
            assertNotNull(bookshelf.name());
            assertNotNull(bookshelf.id());
            bookshelfList.add(bookshelf);
        }

        // Bookshelves list should be a valid list (may be empty)
        assertNotNull(bookshelfList);
    }

    @Test
    public void testGetBookshelfIfExists() {
        // First list bookshelves to find one to get
        PagedIterable<Bookshelf> bookshelves = discoveryManager.bookshelves().list();

        Bookshelf firstBookshelf = null;
        for (Bookshelf bookshelf : bookshelves) {
            firstBookshelf = bookshelf;
            break;
        }

        if (firstBookshelf != null) {
            // Extract resource group and name from the ID
            String bookshelfId = firstBookshelf.id();
            String resourceGroup = extractResourceGroup(bookshelfId);
            String bookshelfName = firstBookshelf.name();

            // Get the bookshelf by name
            Bookshelf retrieved = discoveryManager.bookshelves().getByResourceGroup(resourceGroup, bookshelfName);

            assertNotNull(retrieved);
            assertNotNull(retrieved.name());
            assertNotNull(retrieved.location());
            // Type may be lowercase or PascalCase
            assertTrue(retrieved.type().equalsIgnoreCase("Microsoft.Discovery/bookshelves"));
        }
        // If no bookshelves exist, test passes (nothing to get)
    }

    /**
     * Helper to extract resource group name from a resource ID.
     */
    private String extractResourceGroup(String resourceId) {
        // Format: /subscriptions/{sub}/resourceGroups/{rg}/providers/...
        String[] parts = resourceId.split("/");
        for (int i = 0; i < parts.length - 1; i++) {
            if ("resourceGroups".equalsIgnoreCase(parts[i])) {
                return parts[i + 1];
            }
        }
        throw new IllegalArgumentException("Could not extract resource group from ID: " + resourceId);
    }
}
