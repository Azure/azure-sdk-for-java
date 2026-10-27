// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.chat;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.webpubsub.chat.models.BuiltInChatRoles;
import com.azure.messaging.webpubsub.chat.models.ChatPermission;
import com.azure.messaging.webpubsub.chat.models.ChatMessage;
import com.azure.messaging.webpubsub.chat.models.ChatRole;
import com.azure.messaging.webpubsub.chat.models.ChatRoom;
import com.azure.messaging.webpubsub.chat.models.ChatRoomMember;
import com.azure.messaging.webpubsub.chat.models.ChatTopic;
import com.azure.messaging.webpubsub.chat.models.ChatTopicState;
import com.azure.messaging.webpubsub.chat.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.chat.models.HumanChatUser;
import com.azure.messaging.webpubsub.chat.models.MessageContent;
import com.azure.messaging.webpubsub.chat.models.WebPubSubClientAccessToken;

import java.time.Duration;
import java.util.Arrays;

/** Code snippets used by the package README. */
public final class WebPubSubChatSamples {
    /** Updates an existing message with text or binary content. */
    public void updateMessage(WebPubSubChatServiceClient client, String conversationId, String messageId) {
        // BEGIN: readme-sample-updateChatMessageContent
        client.updateMessage(conversationId, messageId,
            new ChatMessage().setContent(MessageContent.fromText("Updated message")));
        client.updateMessage(conversationId, messageId,
            new ChatMessage().setContent(MessageContent.fromBinary(new byte[] { 1, 2, 3 })));
        // END: readme-sample-updateChatMessageContent
    }

    /** Creates a client from a connection string. */
    public void createClientWithConnectionString() {
        // BEGIN: readme-sample-createChatClientWithConnectionString
        WebPubSubChatServiceClient client = new WebPubSubChatServiceClientBuilder()
            .connectionString("<web-pubsub-connection-string>")
            .hub("chat")
            .buildClient();
        // END: readme-sample-createChatClientWithConnectionString
    }

    /** Creates a client from an endpoint and access key. */
    public void createClientWithKey() {
        // BEGIN: readme-sample-createChatClientWithKey
        WebPubSubChatServiceClient client = new WebPubSubChatServiceClientBuilder()
            .endpoint("https://<resource-name>.webpubsub.azure.com")
            .hub("chat")
            .credential(new AzureKeyCredential("<web-pubsub-access-key>"))
            .buildClient();
        // END: readme-sample-createChatClientWithKey
    }

    /** Creates a client using Microsoft Entra ID. */
    public void createClientWithEntraId() {
        // BEGIN: readme-sample-createChatClientWithEntraId
        WebPubSubChatServiceClient client = new WebPubSubChatServiceClientBuilder()
            .endpoint("https://<resource-name>.webpubsub.azure.com")
            .hub("chat")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: readme-sample-createChatClientWithEntraId
    }

    /** Generates a token for a Chat client connection. */
    public void getClientAccessToken() {
        WebPubSubChatServiceClient client = createClient();

        // BEGIN: readme-sample-getChatClientAccessToken
        WebPubSubClientAccessToken accessToken = client.getClientAccessToken(
            new GetClientAccessTokenOptions().setUserId("alice").setExpiresAfter(Duration.ofHours(1)));
        String clientConnectionUrl = accessToken.getUrl();
        // END: readme-sample-getChatClientAccessToken
    }

    /** Creates a custom Chat role. */
    public void createRole(WebPubSubChatServiceClient client) {

        // BEGIN: readme-sample-createChatRole
        ChatRole moderator = new ChatRole(Arrays.asList(ChatPermission.ROOM_HISTORY,
            ChatPermission.ROOM_REMOVE_USER, ChatPermission.ROOM_PUBLISH_MESSAGE));
        client.createOrReplaceRole("room.moderator", moderator);
        // END: readme-sample-createChatRole
    }

    /** Lists Chat roles. */
    public void listRoles(WebPubSubChatServiceClient client) {
        // BEGIN: readme-sample-listChatRoles
        client.listRoles().forEach(role -> System.out.println(role.getName()));
        // END: readme-sample-listChatRoles
    }

    /** Deletes a custom Chat role. */
    public void deleteRole(WebPubSubChatServiceClient client) {
        // BEGIN: readme-sample-deleteChatRole
        client.deleteRole("room.moderator");
        // END: readme-sample-deleteChatRole
    }

    /** Creates a Chat user with a built-in role. */
    public void createUser(WebPubSubChatServiceClient client) {
        // BEGIN: readme-sample-createChatUser
        client.createOrReplaceUser("alice", new HumanChatUser("Alice", BuiltInChatRoles.USER_NORMAL));
        // END: readme-sample-createChatUser
    }

    /** Creates a Chat room. */
    public void createRoom(WebPubSubChatServiceClient client) {
        // BEGIN: readme-sample-createChatRoom
        ChatRoom room = client.createOrReplaceRoom("general", new ChatRoom("General"));
        String defaultConversationId = room.getDefaultConversation();
        // END: readme-sample-createChatRoom
    }

    /** Adds an existing user to an existing room. */
    public void addRoomMember(WebPubSubChatServiceClient client) {
        // BEGIN: readme-sample-addChatRoomMember
        ChatRoomMember member = client.createOrReplaceRoomMember(
            "general", "alice", new ChatRoomMember(BuiltInChatRoles.ROOM_MEMBER));
        System.out.printf("%s: %s%n", member.getUserId(), member.getRoleName());
        // END: readme-sample-addChatRoomMember
    }

    /** Lists rooms in a hub. */
    public void listRooms() {
        WebPubSubChatServiceClient client = createClient();
        // BEGIN: readme-sample-listChatRooms
        client.listRooms().forEach(room -> System.out.printf("%s: %s%n", room.getId(), room.getTitle()));
        // END: readme-sample-listChatRooms
    }

    /** Lists user profiles in a hub. */
    public void listUsers() {
        WebPubSubChatServiceClient client = createClient();
        // BEGIN: readme-sample-listChatUsers
        client.listUsers().forEach(user -> System.out.printf("%s: %s%n", user.getId(), user.getNickname()));
        // END: readme-sample-listChatUsers
    }

    /** Lists rooms associated with an existing user. */
    public void listRoomsForUser() {
        WebPubSubChatServiceClient client = createClient();
        // BEGIN: readme-sample-listChatRoomsForUser
        client.listRoomsForUser("alice").forEach(room ->
            System.out.printf("%s: %s%n", room.getId(), room.getTitle()));
        // END: readme-sample-listChatRoomsForUser
    }

    /** Creates a topic in an existing room. */
    public void createTopic() {
        WebPubSubChatServiceClient client = createClient();
        // BEGIN: readme-sample-createChatTopic
        ChatTopic topic = client.createOrReplaceTopic("general", "announcements",
            new ChatTopic().setTitle("Announcements"));
        String conversationId = topic.getConversationId();
        // END: readme-sample-createChatTopic
    }

    /** Gets an existing topic. */
    public void getTopic() {
        WebPubSubChatServiceClient client = createClient();
        // BEGIN: readme-sample-getChatTopic
        ChatTopic topic = client.getTopic("general", "announcements");
        System.out.printf("%s: %s%n", topic.getTitle(), topic.getState());
        // END: readme-sample-getChatTopic
    }

    /** Updates an existing topic's title and lifecycle state. */
    public void updateTopic() {
        WebPubSubChatServiceClient client = createClient();
        // BEGIN: readme-sample-updateChatTopic
        ChatTopic updated = client.updateTopic("general", "announcements",
            new ChatTopic().setTitle("Archived announcements").setState(ChatTopicState.ARCHIVED));
        // END: readme-sample-updateChatTopic
    }

    /** Soft deletes an existing topic. */
    public void deleteTopic() {
        WebPubSubChatServiceClient client = createClient();
        // BEGIN: readme-sample-deleteChatTopic
        client.deleteTopic("general", "announcements");
        // END: readme-sample-deleteChatTopic
    }

    /** Lists topics in an existing room. */
    public void listTopics() {
        WebPubSubChatServiceClient client = createClient();
        // BEGIN: readme-sample-listChatTopics
        client.listTopics("general").forEach(topic ->
            System.out.printf("%s: %s%n", topic.getId(), topic.getTitle()));
        // END: readme-sample-listChatTopics
    }

    /** Lists persisted messages in a room's default conversation. */
    public void listMessages(WebPubSubChatServiceClient client) {
        // BEGIN: readme-sample-listChatMessages
        String conversationId = "<conversation-id>";
        client.listMessages(conversationId).forEach(message ->
            System.out.printf("%s: %s%n", message.getCreatedBy(), message.getContent().getText()));
        // END: readme-sample-listChatMessages
    }

    /** Creates an asynchronous client. */
    public void createAsyncClient() {
        // BEGIN: readme-sample-createAsyncChatClient
        WebPubSubChatServiceAsyncClient asyncClient = new WebPubSubChatServiceClientBuilder()
            .connectionString("<web-pubsub-connection-string>")
            .hub("chat")
            .buildAsyncClient();
        // END: readme-sample-createAsyncChatClient
    }

    /** Lists topics asynchronously. */
    public void listTopicsAsync(WebPubSubChatServiceAsyncClient asyncClient) {
        // BEGIN: readme-sample-listChatTopicsAsync
        asyncClient.listTopics("general").subscribe(
            topic -> System.out.println(topic.getTitle()),
            error -> System.err.println("Could not list topics: " + error.getMessage()));
        // END: readme-sample-listChatTopicsAsync
    }

    /** Lists typed topics asynchronously with an explicit page size. */
    public void listTopicsAsyncByPage(WebPubSubChatServiceAsyncClient asyncClient) {
        // BEGIN: readme-sample-listChatTopicsAsyncByPage
        asyncClient.listTopics("general").byPage(10).subscribe(
            page -> page.getValue().forEach(topic -> System.out.println(topic.getTitle())),
            error -> System.err.println("Could not list topic pages: " + error.getMessage()));
        // END: readme-sample-listChatTopicsAsyncByPage
    }

    /** Reads the built-in role and permission values. */
    public void builtInValues() {
        // BEGIN: readme-sample-chatBuiltInValues
        String memberRole = BuiltInChatRoles.ROOM_MEMBER;
        ChatPermission publishPermission = ChatPermission.ROOM_PUBLISH_MESSAGE;
        // END: readme-sample-chatBuiltInValues
    }

    private static WebPubSubChatServiceClient createClient() {
        return new WebPubSubChatServiceClientBuilder().connectionString("<web-pubsub-connection-string>")
            .hub("chat")
            .buildClient();
    }
}
