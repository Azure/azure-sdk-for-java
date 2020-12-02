// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.azure.communication.administration.CommunicationIdentityClient;
import com.azure.communication.administration.CommunicationIdentityClientBuilder;
import com.azure.communication.administration.CommunicationUserToken;
import com.azure.communication.common.CommunicationUser;
import com.azure.communication.common.CommunicationUserCredential;
import com.azure.communication.chat.ChatClient;
import com.azure.communication.chat.ChatClientBuilder;
import com.azure.communication.chat.ChatThreadClient;
import com.azure.communication.chat.models.ChatThreadMember;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.rest.PagedIterable;

public class SampleAutentication {

    /**
     * Sample code for creating a sync Communication Identity Client.
     *
     * @return the Communication Identity Client.
     */
    public CommunicationIdentityClient createCommunicationIdentityClient() {
        // You can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
        String accessKey = "SECRET";

        // Create an HttpClient builder of your choice and customize it
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

        CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
            .endpoint(endpoint)
            .accessKey(accessKey)
            .httpClient(httpClient)
            .buildClient();

        return communicationIdentityClient;
    }

    public CommunicationUserToken createComunicationUserToken() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
        CommunicationUser user = communicationIdentityClient.createUser();
        List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
        CommunicationUserToken userToken = communicationIdentityClient.issueToken(user, scopes);
        System.out.println("Token: " + userToken.getToken());
        System.out.println("Expires On: " + userToken.getExpiresOn());
        return userToken;

    }

    /**
     * Sample code for creating a sync chat client.
     *
     * @return the chat client.
     */
    public ChatClient createChatClient() {
        String endpoint = "https://<RESOURCE_NAME>.communcationservices.azure.com";

        // Create an HttpClient builder of your choice and customize it
        // Use com.azure.core.http.netty.NettyAsyncHttpClientBuilder if that suits your needs
        NettyAsyncHttpClientBuilder httpClientBuilder = new NettyAsyncHttpClientBuilder();
        HttpClient httpClient = httpClientBuilder.build();

        // Your user access token retrieved from your trusted service
        String token = "SECRET";
        CommunicationUserCredential credential = new CommunicationUserCredential(token);

        // Initialize the chat client
        final ChatClientBuilder builder = new ChatClientBuilder();
        builder.endpoint(endpoint)
            .credential(credential)
            .httpClient(httpClient);
        ChatClient chatClient = builder.buildClient();

        return chatClient;
    }
    /**
     * Sample code for getting a sync chat thread client using the sync chat client.
     *
     * @return the chat thread client.
     */
    public ChatThreadClient getChatThreadClient() {
        ChatClient chatClient = createChatClient();

        String chatThreadId = "Id";
        ChatThreadClient chatThreadClient = chatClient.getChatThreadClient(chatThreadId);

        return chatThreadClient;
    }
    /**
     * Sample code listing chat thread members using the sync chat thread client.
     */
    public void listChatThreadMember() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        PagedIterable<ChatThreadMember> chatThreadMembersResponse = chatThreadClient.listMembers();
        chatThreadMembersResponse.iterableByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getItems().forEach(chatMember -> {
                System.out.printf("Member id is %s.", chatMember.getUser().getId());
            });
        });
    }
}
