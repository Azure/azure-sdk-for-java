// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.webapp;

import com.azure.spring.cloud.autoconfigure.aad.implementation.constants.AuthorityPrefix;
import com.azure.spring.cloud.autoconfigure.aad.implementation.graph.GraphClient;
import com.azure.spring.cloud.autoconfigure.aad.implementation.graph.GroupInformation;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
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
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link AadOAuth2UserService}.
 */
public class AadOAuth2UserServiceTest {
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

        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void loadUserWhenUserRequestIsNullThenThrowIllegalArgumentException() {
        aadOAuth2UserService = new AadOAuth2UserService(properties, graphClient, null);
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

        aadOAuth2UserService = new AadOAuth2UserService(properties, graphClient, null);
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
        aadOAuth2UserService = new AadOAuth2UserService(properties, graphClient, null);

        ClientRegistration clientRegistration = this.clientRegistrationBuilder
            .build();
        OidcUser user = aadOAuth2UserService
            .loadUser(new OidcUserRequest(clientRegistration, this.accessToken, this.idToken));

        assertThat(user.getUserInfo()).isNull();
        assertThat(user.getClaims()).isEqualTo(idTokenClaims);
        assertThat(user.getAuthorities().size()).isEqualTo(1);
        SimpleGrantedAuthority defaultGrantedAuthority = new SimpleGrantedAuthority(AuthorityPrefix.ROLE + "USER");
        assertThat(user.getAuthorities().stream().findFirst().get()).isEqualTo(defaultGrantedAuthority);
    }

    @Test
    void loadUserWithCustomAuthorities() {

        idTokenClaims.put("roles", List.of("role1", "role2"));

        GroupInformation groupInformation = new GroupInformation();
        groupInformation.setGroupsIds(Set.of("groupId1", "groupId2"));
        groupInformation.setGroupsNames(Set.of("groupName1", "groupName2"));
        graphClient = mock(GraphClient.class);
        when(graphClient.getGroupInformation(anyString())).thenReturn(groupInformation);

        properties = new AadAuthenticationProperties();
        properties.getUserGroup().setAllowedGroupNames(List.of("groupName1", "groupName2"));
        properties.getUserGroup().setAllowedGroupIds(Set.of("groupId1", "groupId2"));

        aadOAuth2UserService = new AadOAuth2UserService(properties, graphClient, null);

        ClientRegistration clientRegistration = this.clientRegistrationBuilder
            .build();

        OidcUser user = this.aadOAuth2UserService
            .loadUser(new OidcUserRequest(clientRegistration, this.accessToken,
                new OidcIdToken("access-token", Instant.MIN, Instant.MAX, idTokenClaims)));

        assertThat(user.getUserInfo()).isNull();
        assertThat(user.getClaims()).isEqualTo(idTokenClaims);
        assertThat(user.getAuthorities().size()).isEqualTo(6);
        Set<SimpleGrantedAuthority> simpleGrantedAuthorities
            = Set.of(new SimpleGrantedAuthority("APPROLE_role1"),
            new SimpleGrantedAuthority("APPROLE_role2"),
            new SimpleGrantedAuthority("ROLE_groupId1"),
            new SimpleGrantedAuthority("ROLE_groupId2"),
            new SimpleGrantedAuthority("ROLE_groupName1"),
            new SimpleGrantedAuthority("ROLE_groupName2")
        );
        assertThat(user.getAuthorities()).isEqualTo(simpleGrantedAuthorities);
    }

    @Test
    void loadUserWhenCustomUserNameAttributeNameThenGetNameReturnsCustomUserName() {
        aadOAuth2UserService = new AadOAuth2UserService(properties, graphClient, null);

        ClientRegistration clientRegistration = this.clientRegistrationBuilder
            .userNameAttributeName(StandardClaimNames.EMAIL)
            .build();

        OidcUser user = this.aadOAuth2UserService
            .loadUser(new OidcUserRequest(clientRegistration, this.accessToken, this.idToken));
        assertThat(user.getName()).isEqualTo("user1@example.com");
    }

    @Test
    void loadUserWithDefaultUserNameAttributeName() {
        aadOAuth2UserService = new AadOAuth2UserService(properties, graphClient, null);

        ClientRegistration clientRegistration = this.clientRegistrationBuilder
            .build();

        OidcUser user = this.aadOAuth2UserService
            .loadUser(new OidcUserRequest(clientRegistration, this.accessToken, this.idToken));
        assertThat(user.getName()).isEqualTo("user1");
    }
}
