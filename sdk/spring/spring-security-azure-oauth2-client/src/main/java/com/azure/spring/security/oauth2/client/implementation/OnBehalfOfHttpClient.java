package com.azure.spring.security.oauth2.client.implementation;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.OnBehalfOfParameters;
import com.microsoft.aad.msal4j.UserAssertion;

import java.net.MalformedURLException;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class OnBehalfOfHttpClient {

    public static String getOnBehalfOfAccessToken(String authority,
                                                  String clientId,
                                                  String clientSecret,
                                                  String accessToken,
                                                  Set<String> scopes)
        throws ExecutionException, InterruptedException, MalformedURLException {
        OnBehalfOfParameters parameters = OnBehalfOfParameters.builder(scopes, new UserAssertion(accessToken))
                                                              .build();
        ConfidentialClientApplication clientApplication =
            ConfidentialClientApplication.builder(clientId, ClientCredentialFactory.createFromSecret(clientSecret))
                                         .authority(authority)
                                         .build();
        return clientApplication.acquireToken(parameters).get().accessToken();
    }
}
