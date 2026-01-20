// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.core.http.HttpClient;
import com.openai.core.JsonValue;
import com.openai.core.RequestOptions;
import com.openai.core.Timeout;
import com.openai.models.conversations.*;
import com.openai.models.conversations.items.*;
import com.openai.models.responses.EasyInputMessage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.StringUtils;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static com.azure.ai.agents.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.*;

public class ConversationsTests extends ClientTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void basicCRUDOperations(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        ConversationsClient client = getConversationsSyncClient(httpClient, serviceVersion);

        // creation
        Conversation createdConversation = client.getConversationService().create();
        String conversationId = createdConversation.id();
        assertNotNull(conversationId);
        assertTrue(StringUtils.isNotBlank(conversationId));

        // update
        ConversationUpdateParams.Metadata metadata = ConversationUpdateParams.Metadata.builder()
            .putAdditionalProperty("metadata_key", JsonValue.from("metadata_value"))
            .build();
        ConversationUpdateParams.Builder params = ConversationUpdateParams.builder().metadata(metadata);
        Conversation updatedConversation = client.getConversationService().update(conversationId, params.build());

        assertEquals(JsonValue.from(metadata), updatedConversation._metadata());
        assertEquals(createdConversation.id(), updatedConversation.id());

        // retrieve
        Conversation retrievedConversation = client.getConversationService().retrieve(conversationId);
        assertEquals(updatedConversation.id(), retrievedConversation.id());
        assertEquals(updatedConversation._metadata(), retrievedConversation._metadata());

        // deletion
        ConversationDeletedResource deletedConversationResource
            = client.getConversationService().delete(conversationId);
        assertEquals(conversationId, deletedConversationResource.id());
        assertTrue(deletedConversationResource.deleted());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void basicItemCRUDOperations(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        ConversationsClient client = getConversationsSyncClient(httpClient, serviceVersion);

        // creation - conversation
        Conversation createdConversation = client.getConversationService().create();
        String conversationId = createdConversation.id();
        assertNotNull(conversationId);
        assertTrue(StringUtils.isNotBlank(conversationId));

        // creation - conversation item
        ConversationItemList conversationItem = client.getConversationService()
            .items()
            .create(ItemCreateParams.builder()
                .conversationId(conversationId)
                .addItem(EasyInputMessage.builder()
                    .content(EasyInputMessage.Content.ofTextInput("Hello, agent!"))
                    .type(EasyInputMessage.Type.MESSAGE)
                    .role(EasyInputMessage.Role.USER)
                    .build())
                .build());

        assertNotNull(conversationItem);
        assertNotNull(conversationItem.data());
        assertFalse(conversationItem.data().isEmpty());
        assertTrue(conversationItem.data().get(0).isMessage());

        Message createdConversationItem = conversationItem.data().get(0).asMessage();
        assertTrue(createdConversationItem.content().get(0).isInputText());
        assertEquals("Hello, agent!", createdConversationItem.content().get(0).asInputText().text());

        // retrieve - conversation item
        ConversationItem retrievedItem = client.getConversationService()
            .items()
            .retrieve(ItemRetrieveParams.builder()
                .itemId(createdConversationItem.id())
                .conversationId(conversationId)
                .build());
        assertNotNull(retrievedItem);
        assertTrue(retrievedItem.isMessage());
        assertEquals("Hello, agent!", retrievedItem.asMessage().content().get(0).asInputText().text());

        // retrieve - conversation item with ID
        ConversationItem retrievedItemWithId = client.getConversationService()
            .items()
            .retrieve(ItemRetrieveParams.builder()
                .conversationId(conversationId)
                .itemId(retrievedItem.asMessage().id())
                .build());
        assertNotNull(retrievedItemWithId);
        assertTrue(retrievedItemWithId.isMessage());
        assertEquals("Hello, agent!", retrievedItemWithId.asMessage().content().get(0).asInputText().text());

        // deletion - conversation
        Conversation conversationWithDeletedItem = client.getConversationService()
            .items()
            .delete(ItemDeleteParams.builder()
                .conversationId(conversationId)
                .itemId(retrievedItemWithId.asMessage().id())
                .build());
        assertNotNull(conversationWithDeletedItem);
        assertEquals(conversationId, conversationWithDeletedItem.id());
    }

    @Disabled("Flaky test")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void timeoutResponse(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        ConversationsClient client = getConversationsSyncClient(httpClient, serviceVersion);

        RequestOptions requestOptions
            = RequestOptions.builder().timeout(Timeout.builder().read(Duration.ofMillis(1)).build()).build();
        RuntimeException thrown
            = assertThrows(RuntimeException.class, () -> client.getConversationService().create(requestOptions));
        assertInstanceOf(TimeoutException.class, thrown.getCause());
    }
}
