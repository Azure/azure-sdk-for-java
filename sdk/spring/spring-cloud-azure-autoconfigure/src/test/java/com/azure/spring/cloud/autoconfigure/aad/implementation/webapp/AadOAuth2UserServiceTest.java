// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.webapp;

import com.azure.spring.cloud.autoconfigure.aad.implementation.constants.AuthorityPrefix;
import com.azure.spring.cloud.autoconfigure.aad.implementation.graph.GraphClient;
import com.azure.spring.cloud.autoconfigure.aad.implementation.graph.GroupInformation;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link AadOAuth2UserService}.
 */
class AadOAuth2UserServiceTest {
    private ClientRegistration.Builder clientRegistrationBuilder;
    private OidcIdToken idToken;
    private AadOAuth2UserService aadOAuth2UserService;
    private OAuth2AccessToken accessToken;
    private Map<String, Object> idTokenClaims = new HashMap<>();
    private GraphClient graphClient;
    private AadAuthenticationProperties properties;
    private static final String DEFAULT_OIDC_USER = "defaultOidcUser";


    @BeforeEach
    void setup() {

        clientRegistrationBuilder = ClientRegistration
            .withRegistrationId("registrationId")
            .clientName("registrationId")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("redirectUri")
            .userInfoUri(null)
            .clientId("cliendId")
            .clientSecret("clientSecret")
            .userInfoAuthenticationMethod(AuthenticationMethod.HEADER)
            .authorizationUri("authorizationUri")
            .tokenUri("tokenUri");

        this.accessToken = TestOAuth2AccessTokens.scopes(OidcScopes.OPENID, OidcScopes.PROFILE);

        idTokenClaims.put(IdTokenClaimNames.ISS, "https://provider.com");
        idTokenClaims.put(IdTokenClaimNames.SUB, "subject1");
        idTokenClaims.put(StandardClaimNames.NAME, "user1");
        idTokenClaims.put(StandardClaimNames.EMAIL, "user1@example.com");

        this.idToken = new OidcIdToken("access-token", Instant.MIN, Instant.MAX, idTokenClaims);
        aadOAuth2UserService = new AadOAuth2UserService(properties, graphClient, null);

    }

    @Test
    void loadUserWhenUserRequestIsNullThenThrowIllegalArgumentException() {
        assertThatIllegalArgumentException().isThrownBy(() -> this.aadOAuth2UserService.loadUser(null));
    }

    @Test
    void loadUserFromSession() {
        //given
        ServletRequestAttributes mockAttributes = mock(ServletRequestAttributes.class, RETURNS_DEEP_STUBS);
        DefaultOidcUser mockDefaultOidcUser = mock(DefaultOidcUser.class);
        HttpSession mockHttpSession = mock(HttpSession.class);
        when(mockHttpSession.getAttribute(DEFAULT_OIDC_USER)).thenReturn(mockDefaultOidcUser);
        Authentication mockAuthentication = mock(Authentication.class);

        when(mockAttributes.getRequest().getSession(true)).thenReturn(mockHttpSession);

        RequestContextHolder.setRequestAttributes(mockAttributes);
        SecurityContextHolder.getContext().setAuthentication(mockAuthentication);

        ClientRegistration clientRegistration = this.clientRegistrationBuilder
            .build();

        // when
        OidcUser user = aadOAuth2UserService
            .loadUser(new OidcUserRequest(clientRegistration, this.accessToken, this.idToken));

        // then
        assertThat(user).isEqualTo(mockDefaultOidcUser);
    }

    @Test
    void loadUserWithDefaultAuthority() {
        // given
        ClientRegistration clientRegistration = this.clientRegistrationBuilder
            .build();

        // when
        OidcUser user = aadOAuth2UserService
            .getUser(new OidcUserRequest(clientRegistration, this.accessToken, this.idToken));

        // then
        assertThat(user.getUserInfo()).isNull();
        assertThat(user.getClaims()).isEqualTo(idTokenClaims);
        assertThat(user.getAuthorities().size()).isEqualTo(1);
        SimpleGrantedAuthority defaultGrantedAuthority = new SimpleGrantedAuthority(AuthorityPrefix.ROLE + "USER");
        assertThat(user.getAuthorities().stream().findFirst().get()).isEqualTo(defaultGrantedAuthority);
    }

    @Test
    void loadUserWhenCustomUserNameAttributeNameThenGetNameReturnsCustomUserName() {
        // given
        ClientRegistration clientRegistration = this.clientRegistrationBuilder
            .userNameAttributeName(StandardClaimNames.EMAIL)
            .build();

        // when
        OidcUser user = this.aadOAuth2UserService
            .getUser(new OidcUserRequest(clientRegistration, this.accessToken, this.idToken));

        // then
        assertThat(user.getName()).isEqualTo("user1@example.com");
    }

    @Test
    void loadUserWithDefaultUserNameAttributeName() {

        // given
        ClientRegistration clientRegistration = this.clientRegistrationBuilder
            .build();

        // when
        OidcUser user = this.aadOAuth2UserService
            .getUser(new OidcUserRequest(clientRegistration, this.accessToken, this.idToken));

        // then
        assertThat(user.getName()).isEqualTo("user1");
    }

    @Test
    void loadUserWithCustomAuthorities() {

        // given
        idTokenClaims.put("roles", Stream.of("role1", "role2")
            .collect(Collectors.toList()));

        GroupInformation groupInformation = new GroupInformation();
        groupInformation.setGroupsIds(Stream.of("groupId1", "groupId2")
            .collect(Collectors.toSet()));
        groupInformation.setGroupsNames(Stream.of("groupName1", "groupName2")
            .collect(Collectors.toSet()));
        graphClient = mock(GraphClient.class);
        when(graphClient.getGroupInformation(anyString())).thenReturn(groupInformation);

        properties = new AadAuthenticationProperties();
        properties.getUserGroup().setAllowedGroupNames(Stream.of("groupName1", "groupName2")
            .collect(Collectors.toList()));
        properties.getUserGroup().setAllowedGroupIds(Stream.of("groupId1", "groupId2")
            .collect(Collectors.toSet()));

        AadOAuth2UserService aadOAuth2UserService = new AadOAuth2UserService(properties, graphClient, null);

        ClientRegistration clientRegistration = this.clientRegistrationBuilder
            .build();

        // when
        OidcUser user = aadOAuth2UserService
            .getUser(new OidcUserRequest(clientRegistration, this.accessToken,
                new OidcIdToken("access-token", Instant.MIN, Instant.MAX, idTokenClaims)));

        // then
        assertThat(user.getUserInfo()).isNull();
        assertThat(user.getClaims()).isEqualTo(idTokenClaims);
        assertThat(user.getAuthorities().size()).isEqualTo(6);
        Set<SimpleGrantedAuthority> simpleGrantedAuthorities
            = Stream.of(new SimpleGrantedAuthority("APPROLE_role1"),
                new SimpleGrantedAuthority("APPROLE_role2"),
                new SimpleGrantedAuthority("ROLE_groupId1"),
                new SimpleGrantedAuthority("ROLE_groupId2"),
                new SimpleGrantedAuthority("ROLE_groupName1"),
                new SimpleGrantedAuthority("ROLE_groupName2"))
            .collect(Collectors.toSet());
        assertThat(user.getAuthorities()).isEqualTo(simpleGrantedAuthorities);
    }

    static class TestOAuth2AccessTokens {

        private TestOAuth2AccessTokens() {
        }

        static OAuth2AccessToken noScopes() {
            return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "no-scopes", Instant.now(),
                Instant.now().plus(Duration.ofDays(1)));
        }

        static OAuth2AccessToken scopes(String... scopes) {
            return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "scopes", Instant.now(),
                Instant.now().plus(Duration.ofDays(1)), new HashSet<>(Arrays.asList(scopes)));
        }

    }

}
