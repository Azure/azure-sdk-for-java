// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security;

import com.azure.spring.cloud.autoconfigure.implementation.aad.security.constants.AadJwtClaimNames;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.constants.AuthorityPrefix;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.graph.GraphClient;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.graph.GroupInformation;
import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.spring.cloud.autoconfigure.implementation.aad.security.constants.Constants.DEFAULT_AUTHORITY_SET;

/**
 * This implementation will retrieve group info of user from Microsoft Graph. Then map group to {@link
 * GrantedAuthority}.
 *
 * @see OAuth2UserService
 */
public class AadOAuth2UserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AadOAuth2UserService.class);
    private final List<String> allowedGroupNames;
    private final Set<String> allowedGroupIds;
    private final GraphClient graphClient;
    private static final String DEFAULT_OIDC_USER = "defaultOidcUser";
    private static final String ROLES = "roles";

    /**
     * Creates a new instance of {@link AadOAuth2UserService}.
     *
     * @param properties the AAD authentication properties
     * @param restTemplateBuilder the restTemplateBuilder
     */
    public AadOAuth2UserService(AadAuthenticationProperties properties, RestTemplateBuilder restTemplateBuilder) {
        this(properties, new GraphClient(properties, restTemplateBuilder));
    }

    /**
     * Creates a new instance of {@link AadOAuth2UserService}.
     *
     * @param properties the AAD authentication properties
     * @param graphClient the graph client
     * @param restTemplateBuilder the restTemplateBuilder
     */
    public AadOAuth2UserService(AadAuthenticationProperties properties,
                                GraphClient graphClient,
                                RestTemplateBuilder restTemplateBuilder) {
        this(properties, graphClient);
    }

    private AadOAuth2UserService(AadAuthenticationProperties properties,
                                 GraphClient graphClient) {
        allowedGroupNames = Optional.ofNullable(properties)
            .map(AadAuthenticationProperties::getUserGroup)
            .map(AadAuthenticationProperties.UserGroupProperties::getAllowedGroupNames)
            .orElseGet(Collections::emptyList);
        allowedGroupIds = Optional.ofNullable(properties)
            .map(AadAuthenticationProperties::getUserGroup)
            .map(AadAuthenticationProperties.UserGroupProperties::getAllowedGroupIds)
            .orElseGet(Collections::emptySet);
        this.graphClient = graphClient;
    }

    /**
     * Returns a {@link DefaultOidcUser} instance.
     * <p/>
     *
     * The {@link DefaultOidcUser} instance is constructed with {@link GrantedAuthority}, {@link OidcIdToken} and nameAttributeKey.
     * <a href="https://learn.microsoft.com/azure/active-directory/develop/userinfo#consider-using-an-id-token-instead">Azure AD</a> suggests get userinfo from idToken instead from the UserInfo Endpoint,
     * this implementation will not get userinfo from the UserInfo Endpoint. Calling {@link org.springframework.security.oauth2.core.oidc.user.OidcUser#getUserInfo()} with the return instance will return null.
     *
     * <p/>
     *
     * @param userRequest the user request
     *
     * @return a {@link DefaultOidcUser} instance.
     *
     * @throws OAuth2AuthenticationException if an error occurs.
     */
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        Assert.notNull(userRequest, "userRequest cannot be null");

        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession(true);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            LOGGER.debug("User {}'s authorities saved from session: {}.", authentication.getName(), authentication.getAuthorities());
            return (DefaultOidcUser) session.getAttribute(DEFAULT_OIDC_USER);
        }

        DefaultOidcUser defaultOidcUser = getUser(userRequest);
        session.setAttribute(DEFAULT_OIDC_USER, defaultOidcUser);
        return defaultOidcUser;
    }

    DefaultOidcUser getUser(OidcUserRequest userRequest) {
        Set<SimpleGrantedAuthority> authorities = buildAuthorities(userRequest);
        String nameAttributeKey = getNameAttributeKey(userRequest);
        OidcIdToken idToken = userRequest.getIdToken();
        DefaultOidcUser defaultOidcUser = new DefaultOidcUser(authorities, idToken, nameAttributeKey);
        return defaultOidcUser;
    }

    private String getNameAttributeKey(OidcUserRequest userRequest) {
        return Optional.of(userRequest)
                       .map(u -> u.getClientRegistration())
                       .map(u -> u.getProviderDetails())
                       .map(u -> u.getUserInfoEndpoint())
                       .map(u -> u.getUserNameAttributeName())
                       .filter(StringUtils::hasText)
                       .orElse(AadJwtClaimNames.NAME);
    }

    private Set<SimpleGrantedAuthority> buildAuthorities(OidcUserRequest userRequest) {
        Set<String> authorityStrings = new HashSet<>();
        authorityStrings.addAll(extractRolesFromIdToken(userRequest.getIdToken()));
        authorityStrings.addAll(extractGroupRolesFromAccessToken(userRequest.getAccessToken()));
        Set<SimpleGrantedAuthority> authorities = authorityStrings.stream()
                                                                  .map(SimpleGrantedAuthority::new)
                                                                  .collect(Collectors.toSet());

        if (authorities.isEmpty()) {
            authorities = DEFAULT_AUTHORITY_SET;
        }
        return authorities;
    }

    /**
     * Extract roles from idToken.
     *
     * @return roles the roles
     */
    Set<String> extractRolesFromIdToken(OidcIdToken idToken) {
        return Optional.ofNullable(idToken)
                       .map(token -> (Collection<?>) token.getClaim(ROLES))
                       .filter(obj -> obj instanceof List<?>)
                       .map(Collection::stream)
                       .orElseGet(Stream::empty)
                       .filter(s -> StringUtils.hasText(s.toString()))
                       .map(role -> AuthorityPrefix.APP_ROLE + role)
                       .collect(Collectors.toSet());
    }

    /**
     * Extract group roles from accessToken.
     *
     * @return roles the group roles
     */
    Set<String> extractGroupRolesFromAccessToken(OAuth2AccessToken accessToken) {
        if (allowedGroupNames.isEmpty() && allowedGroupIds.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> roles = new HashSet<>();
        GroupInformation groupInformation = getGroupInformation(accessToken);
        if (!allowedGroupNames.isEmpty()) {
            Optional.of(groupInformation)
                    .map(GroupInformation::getGroupsNames)
                    .map(Collection::stream)
                    .orElseGet(Stream::empty)
                    .filter(allowedGroupNames::contains)
                    .forEach(roles::add);
        }
        if (!allowedGroupIds.isEmpty()) {
            Optional.of(groupInformation)
                    .map(GroupInformation::getGroupsIds)
                    .map(Collection::stream)
                    .orElseGet(Stream::empty)
                    .filter(this::isAllowedGroupId)
                    .forEach(roles::add);
        }
        return roles.stream()
                    .map(roleStr -> AuthorityPrefix.ROLE + roleStr)
                    .collect(Collectors.toSet());
    }

    private boolean isAllowedGroupId(String groupId) {
        if (allowedGroupIds.size() == 1 && allowedGroupIds.contains("all")) {
            return true;
        }
        return allowedGroupIds.contains(groupId);
    }

    private GroupInformation getGroupInformation(OAuth2AccessToken accessToken) {
        return Optional.of(accessToken)
                       .map(AbstractOAuth2Token::getTokenValue)
                       .map(graphClient::getGroupInformation)
                       .orElseGet(GroupInformation::new);
    }
}
