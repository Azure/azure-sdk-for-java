// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.microsoft.aad.msal4j.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.naming.ServiceUnavailableException;
import java.net.MalformedURLException;
import java.util.Set;

/**
 * Provide an AccessTokenProvider by obo flow
 */
public class AccessTokenProvider {

    @Autowired
    private AADAuthenticationProperties aadAuthenticationProperties;

    private String clientId = aadAuthenticationProperties.getClientId();
    private String clientSecret = aadAuthenticationProperties.getClientSecret();

    private static String AUTHORITY = "https://login.microsoftonline.com/common/";

    private ConfidentialClientApplication createClientApplication() throws MalformedURLException {
        return ConfidentialClientApplication.builder(clientId, ClientCredentialFactory.createFromSecret(clientSecret)).
            authority(AUTHORITY).
            build();
    }

    public String acquireTokenByOboflow(Set<String> scope, String accessToken) throws Throwable {

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
