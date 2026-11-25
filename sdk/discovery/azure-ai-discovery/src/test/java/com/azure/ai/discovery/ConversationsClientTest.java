// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.discovery;

import com.azure.ai.discovery.models.Conversation;
import com.azure.ai.discovery.models.PagedConversation;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link ConversationsClient}, covering all convenience operations: create, get, list, update, and
 * delete.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class ConversationsClientTest extends DiscoveryClientTestBase {

    @Test
    @Order(1)
    public void testCreate() {
        ConversationsClient client = getConversationsClient();
        Conversation conversation
            = client.create(projectName, investigationPath(projectName, investigationName), "Test conversation");

        assertNotNull(conversation);
        assertEquals(projectName, conversation.getProjectName());
        assertNotNull(conversation.getName());
        assertNotNull(conversation.getCreatedAt());
    }

    @Test
    @Order(2)
    public void testList() {
        ConversationsClient client = getConversationsClient();
        PagedConversation page = client.list(null, projectName, null, null, null, null);

        assertNotNull(page.getValue());
        List<Conversation> conversations = page.getValue();
        assertFalse(conversations.isEmpty());
        for (Conversation conversation : conversations) {
            assertEquals(projectName, conversation.getProjectName());
            assertNotNull(conversation.getCreatedAt());
            assertNotNull(conversation.getInvestigationName());
        }
    }

    @Test
    @Order(3)
    public void testGet() {
        ConversationsClient client = getConversationsClient();
        Conversation created = client.create(projectName, investigationPath(projectName, investigationName),
            "Conversation for get test");

        Conversation conversation = client.get(created.getName());
        assertNotNull(conversation);
        assertEquals(created.getName(), conversation.getName());
        assertEquals(projectName, conversation.getProjectName());
        assertNotNull(conversation.getCreatedAt());
    }

    @Test
    @Order(4)
    public void testStableUpdate() {
        ConversationsClient client = getConversationsClient();
        Conversation created = client.create(projectName, investigationPath(projectName, investigationName),
            "Conversation before update");

        Conversation updated
            = client.update(created.getName(), new Conversation().setDisplayName("Conversation after update"));

        assertNotNull(updated);
        assertEquals(created.getName(), updated.getName());
        assertEquals("Conversation after update", updated.getDisplayName());
    }

    @Test
    @Order(5)
    public void testDelete() {
        ConversationsClient client = getConversationsClient();
        Conversation created
            = client.create(projectName, investigationPath(projectName, investigationName), "Conversation to delete");

        client.delete(created.getName());

        // Listing after deletion should not contain the deleted conversation.
        PagedConversation page = client.list(null, projectName, null, null, null, null);
        boolean stillPresent
            = page.getValue().stream().anyMatch(conversation -> created.getName().equals(conversation.getName()));
        assertFalse(stillPresent);
    }
}
