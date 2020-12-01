package com.azure.spring.autoconfigure.aad;

import com.azure.spring.aad.implementation.AzureClientRegistrationRepository;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IClientSecret;
import com.microsoft.aad.msal4j.OnBehalfOfParameters;
import com.microsoft.aad.msal4j.UserAssertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AADOAuth2OboAuthorizedClientRepository implements OAuth2AuthorizedClientRepository {

    private static final Logger LOG = LoggerFactory.getLogger(AADOAuth2OboAuthorizedClientRepository.class);

    private final AzureClientRegistrationRepository azureClientRegistrationRepository;

    private Map<String, ConfidentialClientApplication> confidentialClientApplicationMap = new HashMap<>();

    private JwtDecoder jwtDecoder;

    private Map<ClientRegistration,OAuth2AuthorizedClient> oAuth2AuthorizedClientMap =new ConcurrentHashMap<>();

    public AADOAuth2OboAuthorizedClientRepository(AzureClientRegistrationRepository azureClientRegistrationRepository,JwtDecoder jwtDecoder) {
        this.azureClientRegistrationRepository = azureClientRegistrationRepository;
        this.jwtDecoder = jwtDecoder;
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

            if(oAuth2AuthorizedClientMap.get(clientRegistration) == null){
                AbstractOAuth2TokenAuthenticationToken<AbstractOAuth2Token> authenticationToken = (AbstractOAuth2TokenAuthenticationToken)
                    authentication;

                String accessToken = authenticationToken.getToken().getTokenValue();

                OnBehalfOfParameters parameters = OnBehalfOfParameters
                    .builder(clientRegistration.getScopes(), new UserAssertion(accessToken))
                    .build();

                ConfidentialClientApplication clientApplication = confidentialClientApplicationMap.get(clientRegistration.getClientId());

                String oboAccessToken = clientApplication.acquireToken(parameters).get().accessToken();
                //TODO can't decode graph Jwt
                Jwt jwt = this.jwtDecoder.decode(oboAccessToken);

                Map<String, Object> claims = jwt.getClaims();

                Instant iat =  (Instant) this.jwtDecoder.decode(oboAccessToken).getClaims().get("iat");
                Instant exp =  (Instant) this.jwtDecoder.decode(oboAccessToken).getClaims().get("exp");

                OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,oboAccessToken,iat,exp);

                OAuth2AuthorizedClient oAuth2AuthorizedClient = new OAuth2AuthorizedClient(clientRegistration,
                    authenticationToken.getName(), oAuth2AccessToken);

                oAuth2AuthorizedClientMap.put(clientRegistration,oAuth2AuthorizedClient);

                return (T) oAuth2AuthorizedClient;
            }
            return (T) oAuth2AuthorizedClientMap.get(clientRegistration);

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


}
