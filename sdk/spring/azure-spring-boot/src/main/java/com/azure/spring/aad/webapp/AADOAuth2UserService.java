// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import com.azure.spring.autoconfigure.aad.AADTokenClaim;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.spring.autoconfigure.aad.Constants.APPROLE_PREFIX;
import static com.azure.spring.autoconfigure.aad.Constants.DEFAULT_AUTHORITY_SET;
import static com.azure.spring.autoconfigure.aad.Constants.ROLE_PREFIX;

/**
 * This implementation will retrieve group info of user from Microsoft Graph. Then map group to {@link
 * GrantedAuthority}.
 */
public class AADOAuth2UserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService oidcUserService;
    private final AADAuthenticationProperties properties;
    private final GraphClient graphClient;
    private static final String DEFAULT_OIDC_USER = "defaultOidcUser";
    private static final String ROLES = "roles";

    public AADOAuth2UserService(
        AADAuthenticationProperties properties
    ) {
        this.properties = properties;
        this.oidcUserService = new OidcUserService();
        this.graphClient = new GraphClient(properties);
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Delegate to the default implementation for loading a user
        OidcUser oidcUser = oidcUserService.loadUser(userRequest);
        OidcIdToken idToken = oidcUser.getIdToken();
        Set<String> authorityStrings = new HashSet<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession(true);

        if (authentication != null) {
            return (DefaultOidcUser) session.getAttribute(DEFAULT_OIDC_USER);
        }

        authorityStrings.addAll(extractRolesFromIdToken(idToken));
        authorityStrings.addAll(extractGroupRolesFromAccessToken(userRequest.getAccessToken()));
        Set<SimpleGrantedAuthority> authorities = authorityStrings.stream()
                                                                  .map(SimpleGrantedAuthority::new)
                                                                  .collect(Collectors.toSet());

        if (authorities.isEmpty()) {
            authorities = DEFAULT_AUTHORITY_SET;
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
        DefaultOidcUser defaultOidcUser = new DefaultOidcUser(authorities, idToken, nameAttributeKey);

        session.setAttribute(DEFAULT_OIDC_USER, defaultOidcUser);
        return defaultOidcUser;
    }

    Set<String> extractRolesFromIdToken(OidcIdToken idToken) {
        Set<String> roles = Optional.ofNullable(idToken)
                                    .map(token -> (Collection<?>) token.getClaim(ROLES))
                                    .filter(obj -> obj instanceof List<?>)
                                    .map(Collection::stream)
                                    .orElseGet(Stream::empty)
                                    .filter(s -> StringUtils.hasText(s.toString()))
                                    .map(role -> APPROLE_PREFIX + role)
                                    .collect(Collectors.toSet());
        return roles;
    }

    Set<String> extractGroupRolesFromAccessToken(OAuth2AccessToken accessToken) {
        Set<String> roles = Optional.of(accessToken)
                                    .filter(notUsed -> properties.allowedGroupsConfigured())
                                    .map(AbstractOAuth2Token::getTokenValue)
                                    .map(graphClient::getGroupsFromGraph)
                                    .orElseGet(Collections::emptySet)
                                    .stream()
                                    .filter(properties::isAllowedGroup)
                                    .map(group -> ROLE_PREFIX + group)
                                    .collect(Collectors.toSet());
        return roles;
    }
}
