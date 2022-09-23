// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.webapp;

import com.azure.spring.cloud.autoconfigure.aad.implementation.constants.AadJwtClaimNames;
import com.azure.spring.cloud.autoconfigure.aad.implementation.constants.AuthorityPrefix;
import com.azure.spring.cloud.autoconfigure.aad.implementation.graph.GraphClient;
import com.azure.spring.cloud.autoconfigure.aad.implementation.graph.GroupInformation;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
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

import static com.azure.spring.cloud.autoconfigure.aad.implementation.AadRestTemplateCreator.createOAuth2ErrorResponseHandledRestTemplate;
import static com.azure.spring.cloud.autoconfigure.aad.implementation.constants.Constants.DEFAULT_AUTHORITY_SET;

/**
 * This implementation will retrieve group info of user from Microsoft Graph. Then map group to {@link
 * GrantedAuthority}.
 *
 * @see OidcUserService
 * @see OAuth2UserService
 */
public class AadOAuth2UserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AadOAuth2UserService.class);

    private final OidcUserService oidcUserService;
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
        this(properties, new GraphClient(properties, restTemplateBuilder), restTemplateBuilder);
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
        allowedGroupNames = Optional.ofNullable(properties)
                                    .map(AadAuthenticationProperties::getUserGroup)
                                    .map(AadAuthenticationProperties.UserGroupProperties::getAllowedGroupNames)
                                    .orElseGet(Collections::emptyList);
        allowedGroupIds = Optional.ofNullable(properties)
                                  .map(AadAuthenticationProperties::getUserGroup)
                                  .map(AadAuthenticationProperties.UserGroupProperties::getAllowedGroupIds)
                                  .orElseGet(Collections::emptySet);
        DefaultOAuth2UserService oAuth2UserService = new DefaultOAuth2UserService();
        oAuth2UserService.setRestOperations(createOAuth2ErrorResponseHandledRestTemplate(restTemplateBuilder));
        this.oidcUserService = new OidcUserService();
        this.oidcUserService.setOauth2UserService(oAuth2UserService);
        this.graphClient = graphClient;
    }

    /**
     * Returns an {@link OAuth2User} after obtaining the user attributes of the End-User
     * from the UserInfo Endpoint.
     *
     * @param userRequest the user request
     * @return an {@link OAuth2User}
     * @throws OAuth2AuthenticationException if an error occurs while attempting to obtain
     * the user attributes from the UserInfo Endpoint
     */
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
            LOGGER.debug("User {}'s authorities saved from session: {}.", authentication.getName(), authentication.getAuthorities());
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
                    .orElse(AadJwtClaimNames.NAME);
        LOGGER.debug("User {}'s authorities extracted by id token and access token: {}.", oidcUser.getClaim(nameAttributeKey), authorities);
        // Create a copy of oidcUser but use the mappedAuthorities instead
        DefaultOidcUser defaultOidcUser = new DefaultOidcUser(authorities, idToken, nameAttributeKey);

        session.setAttribute(DEFAULT_OIDC_USER, defaultOidcUser);
        return defaultOidcUser;
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
