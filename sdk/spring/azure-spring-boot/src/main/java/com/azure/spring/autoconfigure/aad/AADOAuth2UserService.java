// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.microsoft.aad.msal4j.MsalServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.StringUtils;

import javax.naming.ServiceUnavailableException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.azure.spring.autoconfigure.aad.AADOAuth2ErrorCode.CONDITIONAL_ACCESS_POLICY;
import static com.azure.spring.autoconfigure.aad.AADOAuth2ErrorCode.INVALID_REQUEST;
import static com.azure.spring.autoconfigure.aad.AADOAuth2ErrorCode.SERVER_SERVER;
import static com.azure.spring.autoconfigure.aad.Constants.DEFAULT_AUTHORITY_SET;
import static com.azure.spring.autoconfigure.aad.Constants.ROLE_PREFIX;

/**
 * This implementation will retrieve group info of user from Microsoft Graph and map groups to {@link
 * GrantedAuthority}.
 */
public class AADOAuth2UserService implements OAuth2UserService<OidcUserRequest, OidcUser> {
    private final AADAuthenticationProperties aadAuthenticationProperties;
    private final ServiceEndpointsProperties serviceEndpointsProperties;
    private final OidcUserService oidcUserService;

    public AADOAuth2UserService(AADAuthenticationProperties aadAuthenticationProperties,
                                ServiceEndpointsProperties serviceEndpointsProperties) {
        this.aadAuthenticationProperties = aadAuthenticationProperties;
        this.serviceEndpointsProperties = serviceEndpointsProperties;
        this.oidcUserService = new OidcUserService();
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Delegate to the default implementation for loading a user
        OidcUser oidcUser = oidcUserService.loadUser(userRequest);
        Set<SimpleGrantedAuthority> authorities;
        try {
            // https://github.com/MicrosoftDocs/azure-docs/issues/8121#issuecomment-387090099
            // In AAD App Registration configure oauth2AllowImplicitFlow to true
            final ClientRegistration registration = userRequest.getClientRegistration();
            final AzureADGraphClient azureADGraphClient = new AzureADGraphClient(
                registration.getClientId(),
                registration.getClientSecret(),
                aadAuthenticationProperties,
                serviceEndpointsProperties
            );
            String graphApiToken = azureADGraphClient
                .acquireTokenForGraphApi(
                    userRequest.getIdToken().getTokenValue(),
                    aadAuthenticationProperties.getTenantId()
                )
                .accessToken();
            Set<String> groups = azureADGraphClient.getGroups(graphApiToken);
            Set<String> groupRoles = groups.stream()
                                           .filter(aadAuthenticationProperties::isAllowedGroup)
                                           .map(group -> ROLE_PREFIX + group)
                                           .collect(Collectors.toSet());
            authorities = groupRoles.stream()
                                    .map(SimpleGrantedAuthority::new)
                                    .collect(Collectors.toSet());
            if (authorities.isEmpty()) {
                authorities = DEFAULT_AUTHORITY_SET;
            }
        } catch (MalformedURLException e) {
            throw toOAuth2AuthenticationException(INVALID_REQUEST, "Failed to acquire token for Graph API.", e);
        } catch (ServiceUnavailableException e) {
            throw toOAuth2AuthenticationException(SERVER_SERVER, "Failed to acquire token for Graph API.", e);
        } catch (IOException e) {
            throw toOAuth2AuthenticationException(SERVER_SERVER, "Failed to map group to authorities.", e);
        } catch (MsalServiceException e) {
            // Handle conditional access policy, step 2.
            // OAuth2AuthenticationException will be caught by AADAuthenticationFailureHandler.
            if (e.claims() != null && !e.claims().isEmpty()) {
                throw toOAuth2AuthenticationException(CONDITIONAL_ACCESS_POLICY, "Handle conditional access policy", e);
            } else {
                throw e;
            }
        }
        String nameAttributeKey =
            Optional.of(userRequest)
                    .map(OAuth2UserRequest::getClientRegistration)
                    .map(ClientRegistration::getProviderDetails)
                    .map(ClientRegistration.ProviderDetails::getUserInfoEndpoint)
                    .map(ClientRegistration.ProviderDetails.UserInfoEndpoint::getUserNameAttributeName)
                    .filter(StringUtils::hasText)
                    .orElse(AADTokenClaim.NAME);
        // Create a copy of oidcUser but use the mappedAuthorities instead
        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), nameAttributeKey);
    }

    private OAuth2AuthenticationException toOAuth2AuthenticationException(String errorCode,
                                                                          String description,
                                                                          Exception cause) {
        OAuth2Error oAuth2Error = new OAuth2Error(errorCode, description, null);
        return new OAuth2AuthenticationException(oAuth2Error, cause);
    }
}
