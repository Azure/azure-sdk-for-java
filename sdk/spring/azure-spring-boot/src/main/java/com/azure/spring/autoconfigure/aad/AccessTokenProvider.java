// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IConfidentialClientApplication;
import com.microsoft.aad.msal4j.OnBehalfOfParameters;
import com.microsoft.aad.msal4j.UserAssertion;
import org.springframework.beans.factory.annotation.Autowired;

import javax.naming.ServiceUnavailableException;
import java.net.MalformedURLException;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Provide an AccessTokenProvider by obo flow
 */
public class AccessTokenProvider {

    @Autowired
    private AADAuthenticationProperties aadAuthenticationProperties;

    private static final String AUTHORITY = "https://login.microsoftonline.com/common/";

    private ConfidentialClientApplication createClientApplication() throws MalformedURLException {

        if(aadAuthenticationProperties == null || aadAuthenticationProperties.getClientSecret() == null
            || aadAuthenticationProperties.getClientId() == null){
            throw new NullPointerException("your first client was unavailable");
        }

        return ConfidentialClientApplication.builder(aadAuthenticationProperties.getClientId(),
            ClientCredentialFactory.createFromSecret(aadAuthenticationProperties.getClientSecret())).
            authority(AUTHORITY).build();
    }

    public String acquireTokenByOboflow(Set<String> scope, String accessToken) throws
        ExecutionException, InterruptedException, ServiceUnavailableException, MalformedURLException {

        IConfidentialClientApplication app = createClientApplication();

        UserAssertion userAssertion = new UserAssertion(accessToken);

        IAuthenticationResult updatedResult =
            app.acquireToken(
                OnBehalfOfParameters.builder(
                    scope,
                    userAssertion).
                    build()).
                    get();

        if (updatedResult == null) {
            throw new ServiceUnavailableException("authentication result was null");
        }

        return updatedResult.accessToken();
    }
}
