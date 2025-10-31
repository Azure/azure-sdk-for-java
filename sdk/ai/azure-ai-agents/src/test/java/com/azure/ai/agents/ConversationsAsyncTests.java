// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.core.http.HttpClient;
import com.openai.core.JsonValue;
import com.openai.models.conversations.Conversation;
import com.openai.models.conversations.ConversationDeletedResource;
import com.openai.models.conversations.ConversationUpdateParams;
import com.openai.models.conversations.Message;
import com.openai.models.conversations.items.*;
import com.openai.models.responses.EasyInputMessage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.StringUtils;

import java.util.concurrent.ExecutionException;

import static com.azure.ai.agents.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.*;

@Disabled("Disabled for lack of recordings. Needs to be enabled on the Public Preview release.")
public class ConversationsAsyncTests extends ClientTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void basicCRUDOperations(HttpClient httpClient, AgentsServiceVersion serviceVersion)
        throws ExecutionException, InterruptedException {
        ConversationsAsyncClient client = getConversationsAsyncClient(httpClient, serviceVersion);

        // creation
        Conversation createdConversation = client.getOpenAIClient().create().get();
        String conversationId = createdConversation.id();
        assertNotNull(conversationId);
        assertTrue(StringUtils.isNotBlank(conversationId));

        // update
        ConversationUpdateParams.Metadata metadata = ConversationUpdateParams.Metadata.builder()
            .putAdditionalProperty("metadata_key", JsonValue.from("metadata_value"))
            .build();
        ConversationUpdateParams.Builder params = ConversationUpdateParams.builder().metadata(metadata);
        Conversation updatedConversation = client.getOpenAIClient().update(conversationId, params.build()).get();

        assertEquals(JsonValue.from(metadata), updatedConversation._metadata());
        assertEquals(createdConversation.id(), updatedConversation.id());

        // retrieve
        Conversation retrievedConversation = client.getOpenAIClient().retrieve(conversationId).get();
        assertEquals(updatedConversation.id(), retrievedConversation.id());
        assertEquals(updatedConversation._metadata(), retrievedConversation._metadata());

        // deletion
        ConversationDeletedResource deletedConversationResource = client.getOpenAIClient().delete(conversationId).get();
        assertEquals(conversationId, deletedConversationResource.id());
        assertTrue(deletedConversationResource.deleted());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void basicItemCRUDOperations(HttpClient httpClient, AgentsServiceVersion serviceVersion)
        throws ExecutionException, InterruptedException {
        ConversationsAsyncClient client = getConversationsAsyncClient(httpClient, serviceVersion);

        // creation - conversation
        Conversation createdConversation = client.getOpenAIClient().create().get();
        String conversationId = createdConversation.id();
        assertNotNull(conversationId);
        assertTrue(StringUtils.isNotBlank(conversationId));

        // creation - conversation item
        ConversationItemList conversationItem = client.getOpenAIClient()
            .items()
            .create(ItemCreateParams.builder()
                .conversationId(conversationId)
                .addItem(EasyInputMessage.builder()
                    .content(EasyInputMessage.Content.ofTextInput("Hello, agent!"))
                    .type(EasyInputMessage.Type.MESSAGE)
                    .role(EasyInputMessage.Role.USER)
                    .build())
                .build())
            .get();

        assertNotNull(conversationItem);
        assertNotNull(conversationItem.data());
        assertFalse(conversationItem.data().isEmpty());
        assertTrue(conversationItem.data().get(0).isMessage());

        Message createdConversationItem = conversationItem.data().get(0).asMessage();
        assertTrue(createdConversationItem.content().get(0).isInputText());
        assertEquals("Hello, agent!", createdConversationItem.content().get(0).asInputText().text());

        // retrieve - conversation item
        ConversationItem retrievedItem = client.getOpenAIClient()
            .items()
            .retrieve(ItemRetrieveParams.builder()
                .itemId(createdConversationItem.id())
                .conversationId(conversationId)
                .build())
            .get();
        assertNotNull(retrievedItem);
        assertTrue(retrievedItem.isMessage());
        assertEquals("Hello, agent!", retrievedItem.asMessage().content().get(0).asInputText().text());

        // retrieve - conversation item with ID
        ConversationItem retrievedItemWithId = client.getOpenAIClient()
            .items()
            .retrieve(ItemRetrieveParams.builder()
                .conversationId(conversationId)
                .itemId(retrievedItem.asMessage().id())
                .build())
            .get();
        assertNotNull(retrievedItemWithId);
        assertTrue(retrievedItemWithId.isMessage());
        assertEquals("Hello, agent!", retrievedItemWithId.asMessage().content().get(0).asInputText().text());

        // deletion - conversation
        Conversation conversationWithDeletedItem = client.getOpenAIClient()
            .items()
            .delete(ItemDeleteParams.builder()
                .conversationId(conversationId)
                .itemId(retrievedItemWithId.asMessage().id())
                .build())
            .get();
        assertNotNull(conversationWithDeletedItem);
        assertEquals(conversationId, conversationWithDeletedItem.id());

    }
}
