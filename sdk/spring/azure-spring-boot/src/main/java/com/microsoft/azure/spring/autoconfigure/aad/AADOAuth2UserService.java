// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.aad;

import com.microsoft.aad.msal4j.MsalServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import javax.naming.ServiceUnavailableException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.Set;

/**
 * This implementation will retrieve group info of user from Microsoft Graph and map groups to {@link GrantedAuthority}.
 */
public class AADOAuth2UserService implements OAuth2UserService<OidcUserRequest, OidcUser> {
    private static final String CONDITIONAL_ACCESS_POLICY = "conditional_access_policy";
    private static final String INVALID_REQUEST = "invalid_request";
    private static final String SERVER_ERROR = "server_error";
    private static final String DEFAULT_USERNAME_ATTR_NAME = "name";

    private AADAuthenticationProperties aadAuthProps;
    private ServiceEndpointsProperties serviceEndpointsProps;
    private OidcUserService oidcUserService;

    public AADOAuth2UserService(AADAuthenticationProperties aadAuthProps,
                                ServiceEndpointsProperties serviceEndpointsProps) {
        this.aadAuthProps = aadAuthProps;
        this.serviceEndpointsProps = serviceEndpointsProps;
        this.oidcUserService = new OidcUserService();
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Delegate to the default implementation for loading a user
        OidcUser oidcUser = oidcUserService.loadUser(userRequest);
        final Set<GrantedAuthority> mappedAuthorities;
        try {
            // https://github.com/MicrosoftDocs/azure-docs/issues/8121#issuecomment-387090099
            // In AAD App Registration configure oauth2AllowImplicitFlow to true
            final ClientRegistration registration = userRequest.getClientRegistration();
            final AzureADGraphClient graphClient = new AzureADGraphClient(
                registration.getClientId(),
                registration.getClientSecret(),
                aadAuthProps,
                serviceEndpointsProps
            );
            String graphApiToken = graphClient
                .acquireTokenForGraphApi(userRequest.getIdToken().getTokenValue(), aadAuthProps.getTenantId())
                .accessToken();
            mappedAuthorities = graphClient.getGrantedAuthorities(graphApiToken);
        } catch (MalformedURLException e) {
            throw wrapException(INVALID_REQUEST, "Failed to acquire token for Graph API.", null, e);
        } catch (ServiceUnavailableException e) {
            throw wrapException(SERVER_ERROR, "Failed to acquire token for Graph API.", null, e);
        } catch (IOException e) {
            throw wrapException(SERVER_ERROR, "Failed to map group to authorities.", null, e);
        } catch (MsalServiceException e) {
            if (e.claims() != null && !e.claims().isEmpty()) {
                throw wrapException(CONDITIONAL_ACCESS_POLICY, "Handle conditional access policy", null, e);
            } else {
                throw e;
            }
        }
        String nameAttributeKey = Optional.of(userRequest)
                                          .map(OAuth2UserRequest::getClientRegistration)
                                          .map(ClientRegistration::getProviderDetails)
                                          .map(ClientRegistration.ProviderDetails::getUserInfoEndpoint)
                                          .map(ClientRegistration.ProviderDetails.UserInfoEndpoint::getUserNameAttributeName)
                                          .filter(s -> !s.isEmpty())
                                          .orElse(DEFAULT_USERNAME_ATTR_NAME);
        // Create a copy of oidcUser but use the mappedAuthorities instead
        return new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), nameAttributeKey);
    }

    private OAuth2AuthenticationException wrapException(String errorCode, String errDesc, String uri, Exception e) {
        final OAuth2Error oAuth2Error = new OAuth2Error(errorCode, errDesc, uri);
        throw new OAuth2AuthenticationException(oAuth2Error, e);
    }
}
