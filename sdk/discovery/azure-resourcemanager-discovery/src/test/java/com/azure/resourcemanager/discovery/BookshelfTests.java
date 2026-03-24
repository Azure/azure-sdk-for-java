// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.discovery;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.discovery.models.Bookshelf;
import com.azure.resourcemanager.discovery.fluent.models.BookshelfInner;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live tests for Bookshelf operations against EUAP endpoint.
 *
 * Tests match the comprehensive coverage in Python SDK.
 * Java-specific resource name: test-bs-java01 (different from Python's test-bookshelf-*).
 */
public class BookshelfTests extends DiscoveryManagementTest {

    private static final String BOOKSHELF_RESOURCE_GROUP = "olawal";
    private static final String BOOKSHELF_NAME = "test-bs-java03";

    @Test
    public void testListBookshelvesBySubscription() {
        PagedIterable<Bookshelf> bookshelves = discoveryManager.bookshelves().list();
        assertNotNull(bookshelves);

        List<Bookshelf> bookshelfList = new ArrayList<>();
        for (Bookshelf bookshelf : bookshelves) {
            assertNotNull(bookshelf.name());
            assertNotNull(bookshelf.id());
            assertNotNull(bookshelf.type());
            bookshelfList.add(bookshelf);
        }

        assertNotNull(bookshelfList);
    }

    @Test
    public void testListBookshelvesByResourceGroup() {
        PagedIterable<Bookshelf> bookshelves
            = discoveryManager.bookshelves().listByResourceGroup(BOOKSHELF_RESOURCE_GROUP);
        assertNotNull(bookshelves);

        List<Bookshelf> bookshelfList = new ArrayList<>();
        for (Bookshelf bookshelf : bookshelves) {
            assertNotNull(bookshelf.name());
            assertNotNull(bookshelf.id());
            bookshelfList.add(bookshelf);
        }

        assertNotNull(bookshelfList);
    }

    @Test
    public void testGetBookshelf() {
        Bookshelf bookshelf
            = discoveryManager.bookshelves().getByResourceGroup(BOOKSHELF_RESOURCE_GROUP, BOOKSHELF_NAME);
        assertNotNull(bookshelf);
        assertNotNull(bookshelf.name());
        assertNotNull(bookshelf.location());
        assertTrue(bookshelf.type().equalsIgnoreCase("Microsoft.Discovery/bookshelves"));
    }

    @Test
    public void testCreateBookshelf() {
        // Bookshelf only requires location (matching Python payload)
        Bookshelf bookshelf = discoveryManager.bookshelves()
            .define(BOOKSHELF_NAME)
            .withRegion("uksouth")
            .withExistingResourceGroup(BOOKSHELF_RESOURCE_GROUP)
            .create();

        assertNotNull(bookshelf);
        assertNotNull(bookshelf.id());
        assertNotNull(bookshelf.name());
    }

    @Test
    public void testUpdateBookshelf() {
        // Use service client directly with a fresh inner model to avoid sending
        // read-only fields (location, workloadIdentities) in the PATCH body
        Map<String, String> tags = new HashMap<>();
        tags.put("SkipAutoDeleteTill", "2026-12-31");

        BookshelfInner patchBody = new BookshelfInner().withTags(tags);

        BookshelfInner updated = discoveryManager.serviceClient()
            .getBookshelves()
            .update(BOOKSHELF_RESOURCE_GROUP, BOOKSHELF_NAME, patchBody);

        assertNotNull(updated);
        assertNotNull(updated.id());
    }

    @Test
    public void testDeleteBookshelf() {
        discoveryManager.bookshelves().deleteByResourceGroup(BOOKSHELF_RESOURCE_GROUP, BOOKSHELF_NAME);
    }
}
