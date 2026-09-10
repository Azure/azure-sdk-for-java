// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.discovery;

import com.azure.ai.discovery.models.Conversation;
import com.azure.ai.discovery.models.PagedConversation;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link ConversationsAsyncClient}, covering create, get, list, update, and delete.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class ConversationsAsyncClientTest extends DiscoveryClientTestBase {

    @Test
    @Order(1)
    public void testCreate() {
        ConversationsAsyncClient client = getConversationsAsyncClient();
        Conversation conversation
            = client.create(projectName, investigationPath(projectName, investigationName), "Test conversation")
                .block();

        assertNotNull(conversation);
        assertEquals(projectName, conversation.getProjectName());
        assertNotNull(conversation.getName());
        assertNotNull(conversation.getCreatedAt());
    }

    @Test
    @Order(2)
    public void testList() {
        ConversationsAsyncClient client = getConversationsAsyncClient();
        PagedConversation page = client.list(null, projectName, null, null, null, null).block();

        assertNotNull(page);
        assertNotNull(page.getValue());
        assertFalse(page.getValue().isEmpty());
        for (Conversation conversation : page.getValue()) {
            assertEquals(projectName, conversation.getProjectName());
            assertNotNull(conversation.getCreatedAt());
        }
    }

    @Test
    @Order(3)
    public void testGet() {
        ConversationsAsyncClient client = getConversationsAsyncClient();
        Conversation created
            = client.create(projectName, investigationPath(projectName, investigationName), "Conversation for get test")
                .block();

        Conversation conversation = client.get(created.getName()).block();
        assertNotNull(conversation);
        assertEquals(created.getName(), conversation.getName());
        assertEquals(projectName, conversation.getProjectName());
    }

    @Test
    @Order(4)
    public void testStableUpdate() {
        ConversationsAsyncClient client = getConversationsAsyncClient();
        Conversation created = client
            .create(projectName, investigationPath(projectName, investigationName), "Conversation before update")
            .block();

        Conversation updated
            = client.update(created.getName(), new Conversation().setDisplayName("Conversation after update")).block();

        assertNotNull(updated);
        assertEquals("Conversation after update", updated.getDisplayName());
    }

    @Test
    @Order(5)
    public void testDelete() {
        ConversationsAsyncClient client = getConversationsAsyncClient();
        Conversation created
            = client.create(projectName, investigationPath(projectName, investigationName), "Conversation to delete")
                .block();

        client.delete(created.getName()).block();

        PagedConversation page = client.list(null, projectName, null, null, null, null).block();
        boolean stillPresent
            = page.getValue().stream().anyMatch(conversation -> created.getName().equals(conversation.getName()));
        assertFalse(stillPresent);
    }
}
