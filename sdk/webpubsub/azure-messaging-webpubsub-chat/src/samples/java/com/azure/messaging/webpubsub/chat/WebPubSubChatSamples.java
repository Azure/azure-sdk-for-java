// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.chat;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.webpubsub.chat.models.BuiltInChatRoles;
import com.azure.messaging.webpubsub.chat.models.ChatPermission;
import com.azure.messaging.webpubsub.chat.models.ChatRole;
import com.azure.messaging.webpubsub.chat.models.ChatRoom;
import com.azure.messaging.webpubsub.chat.models.ChatRoomMember;
import com.azure.messaging.webpubsub.chat.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.chat.models.HumanChatUser;
import com.azure.messaging.webpubsub.chat.models.WebPubSubClientAccessToken;

import java.time.Duration;
import java.util.Arrays;

/** Code snippets used by the package README. */
public final class WebPubSubChatSamples {
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

    /** Creates and lists a custom Chat role. */
    public void manageRoles() {
        WebPubSubChatServiceClient client = createClient();

        // BEGIN: readme-sample-manageChatRoles
        ChatRole moderator = new ChatRole(Arrays.asList(ChatPermission.ROOM_HISTORY,
            ChatPermission.ROOM_REMOVE_USER, ChatPermission.ROOM_PUBLISH_MESSAGE));
        client.createOrReplaceRole("room.moderator", moderator);

        client.listRoles().forEach(role -> System.out.println(role.getName()));
        client.deleteRole("room.moderator");
        // END: readme-sample-manageChatRoles
    }

    /** Creates a room and assigns a member. */
    public void manageRooms() {
        WebPubSubChatServiceClient client = createClient();

        // BEGIN: readme-sample-manageChatRooms
        client.createOrReplaceRole("user.room_creator",
            new ChatRole(Arrays.asList(ChatPermission.USER_CREATE_ROOM)));
        client.createOrReplaceRole("room.contributor",
            new ChatRole(Arrays.asList(ChatPermission.ROOM_PUBLISH_MESSAGE)));
        client.createOrReplaceUser("alice", new HumanChatUser("Alice", "user.room_creator"));

        ChatRoom room = client.createOrReplaceRoom("general", new ChatRoom("General"));
        ChatRoomMember member = client.createOrReplaceRoomMember(
            room.getId(), "alice", new ChatRoomMember("room.contributor"));
        System.out.printf("%s: %s%n", member.getUserId(), member.getRoleName());

        client.deleteRoom(room.getId());
        client.deleteUser("alice");
        client.deleteRole("room.contributor");
        client.deleteRole("user.room_creator");
        // END: readme-sample-manageChatRooms
    }

    /** Lists persisted messages in a room's default conversation. */
    public void listMessages() {
        WebPubSubChatServiceClient client = createClient();

        // BEGIN: readme-sample-listChatMessages
        ChatRoom room = client.getRoom("general");
        client.listMessages(room.getDefaultConversation()).forEach(message ->
            System.out.printf("%s: %s%n", message.getCreatedBy(), message.getContent().getText()));
        // END: readme-sample-listChatMessages
    }

    /** Creates an asynchronous client and lists roles. */
    public void createAsyncClient() {
        // BEGIN: readme-sample-createAsyncChatClient
        WebPubSubChatServiceAsyncClient asyncClient = new WebPubSubChatServiceClientBuilder()
            .connectionString("<web-pubsub-connection-string>")
            .hub("chat")
            .buildAsyncClient();

        asyncClient.listRoles().subscribe(role -> System.out.println(role.getName()));
        // END: readme-sample-createAsyncChatClient
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
