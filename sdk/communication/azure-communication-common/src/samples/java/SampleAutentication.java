// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.azure.communication.administration.CommunicationIdentityClient;
import com.azure.communication.administration.CommunicationIdentityClientBuilder;
import com.azure.communication.administration.CommunicationUserToken;
import com.azure.communication.common.CommunicationUser;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;


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


}
