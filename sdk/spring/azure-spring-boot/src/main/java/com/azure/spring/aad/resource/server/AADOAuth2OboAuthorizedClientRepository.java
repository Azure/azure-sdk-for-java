// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.resource.server;

import com.azure.spring.aad.implementation.AzureClientRegistrationRepository;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IClientSecret;
import com.microsoft.aad.msal4j.OnBehalfOfParameters;
import com.microsoft.aad.msal4j.UserAssertion;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AADOAuth2OboAuthorizedClientRepository implements OAuth2AuthorizedClientRepository {

    private static final Logger LOG = LoggerFactory.getLogger(AADOAuth2OboAuthorizedClientRepository.class);

    private final AzureClientRegistrationRepository azureClientRegistrationRepository;

    private Map<String, ConfidentialClientApplication> confidentialClientApplicationMap = new HashMap<>();

    public AADOAuth2OboAuthorizedClientRepository(AzureClientRegistrationRepository azureClientRegistrationRepository) {
        this.azureClientRegistrationRepository = azureClientRegistrationRepository;
        Iterator<ClientRegistration> iterator = azureClientRegistrationRepository.iterator();
        while (iterator.hasNext()) {
            ClientRegistration next = iterator.next();
            this.confidentialClientApplicationMap.put(next.getClientId(), createApp(next));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String registrationId,
                                                                     Authentication authentication,
                                                                     HttpServletRequest request) {
        try {
            ClientRegistration clientRegistration = azureClientRegistrationRepository
                .findByRegistrationId(registrationId);

                AbstractOAuth2TokenAuthenticationToken<AbstractOAuth2Token> authenticationToken = (AbstractOAuth2TokenAuthenticationToken)
                    authentication;

                String accessToken = authenticationToken.getToken().getTokenValue();

                OnBehalfOfParameters parameters = OnBehalfOfParameters
                    .builder(clientRegistration.getScopes(), new UserAssertion(accessToken))
                    .build();

                ConfidentialClientApplication clientApplication = confidentialClientApplicationMap.get(clientRegistration.getClientId());

                String oboAccessToken = clientApplication.acquireToken(parameters).get().accessToken();

                OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,oboAccessToken,
                    dateToInstant(oboAccessToken,"iat"),dateToInstant(oboAccessToken,"exp"));

                OAuth2AuthorizedClient oAuth2AuthorizedClient = new OAuth2AuthorizedClient(clientRegistration,
                    authenticationToken.getName(), oAuth2AccessToken);

                return (T) oAuth2AuthorizedClient;

        } catch (Throwable throwable) {
            LOG.debug("Failed to loadAuthorizedClient");
            throwable.printStackTrace();
        }
        return null;
}

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient oAuth2AuthorizedClient, Authentication authentication,
                                     HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    }

    @Override
    public void removeAuthorizedClient(String s, Authentication authentication,
                                       HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    }

    private ConfidentialClientApplication createApp(ClientRegistration clientRegistration) {

        String authorizationUri = clientRegistration.getProviderDetails().getAuthorizationUri();

        String authority = interceptAuthorizationUri(authorizationUri);

        IClientSecret clientCredential = ClientCredentialFactory.createFromSecret(clientRegistration.getClientSecret());
        try {
            return ConfidentialClientApplication.builder(clientRegistration.getClientId(), clientCredential)
                                                .authority(authority)
                                                .build();
        } catch (MalformedURLException e) {
            LOG.debug("Failed to create ConfidentialClientApplication");
            e.printStackTrace();
        }
        return null;
    }

    private String interceptAuthorizationUri(String authorizationUri) {
        int count = 0;
        int slashNumber = 4;
        for (int i = 0; i < authorizationUri.length(); i++) {
            if (authorizationUri.charAt(i) == '/') {
                count++;
            }
            if (count == slashNumber) {
                return authorizationUri.substring(0, i + 1);
            }
        }
        return null;
    }

    private Instant dateToInstant(String tokenValue,String claimName){
        try {
            JWT parse = JWTParser.parse(tokenValue);
            Date date =(Date) parse.getJWTClaimsSet().getClaim(claimName);
            return Instant.ofEpochMilli(date.getTime());

        } catch (ParseException e) {
            LOG.debug("Failed to convert to Instant");
            e.printStackTrace();
        }

        return null;

    }


}
