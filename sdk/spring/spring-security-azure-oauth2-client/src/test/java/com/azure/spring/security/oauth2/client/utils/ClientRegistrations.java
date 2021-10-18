package com.azure.spring.security.oauth2.client.utils;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.spring.security.oauth2.client.OtherClientRefreshTokenOAuth2AuthorizedClientProvider.toTargetAuthorizationGrantType;

public class ClientRegistrations {

    public static final String CLIENT_REGISTRATION_ID_1 = "client1";
    public static final String CLIENT_REGISTRATION_ID_2 = "client2";
    public static final String CLIENT_REGISTRATION_ID_3 = "client3";

    public static final Set<String> CLIENT_1_SCOPES = new HashSet<>(Arrays.asList("scope1_1", "scope1_2"));
    public static final Set<String> CLIENT_2_SCOPES = new HashSet<>(Arrays.asList("scope2_1", "scope2_2"));
    public static final Set<String> CLIENT_3_SCOPES = new HashSet<>(Arrays.asList("scope3_1", "scope3_2"));
    public static final Set<String> ALL_SCOPES = Stream.of(CLIENT_1_SCOPES, CLIENT_2_SCOPES, CLIENT_3_SCOPES)
                                                       .flatMap(Collection::stream)
                                                       .collect(Collectors.toSet());
    public static final String CLIENT_1_SCOPES_STRING =
        CLIENT_1_SCOPES.stream()
                       .reduce((a, b) -> a + " " + b)
                       .orElse("");
    public static final String CLIENT_2_SCOPES_STRING =
        CLIENT_2_SCOPES.stream()
                       .reduce((a, b) -> a + " " + b)
                       .orElse("");

    public static final ClientRegistration CLIENT_REGISTRATION_1 =
        toClientRegistrationBuilder(CLIENT_REGISTRATION_ID_1)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .scope(ALL_SCOPES)
            .build();
    public static final ClientRegistration CLIENT_REGISTRATION_2 =
        toClientRegistrationBuilder(CLIENT_REGISTRATION_ID_2)
            .authorizationGrantType(new AuthorizationGrantType(toTargetAuthorizationGrantType(CLIENT_REGISTRATION_ID_1)))
            .scope(CLIENT_2_SCOPES)
            .build();
    public static final ClientRegistration CLIENT_REGISTRATION_3 =
        toClientRegistrationBuilder(CLIENT_REGISTRATION_ID_3)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .scope(CLIENT_3_SCOPES)
            .build();
    public static final ClientRegistration CLIENT_REGISTRATION_WITH_NAME_ATTRIBUTE_KEY =
        ClientRegistration.withClientRegistration(CLIENT_REGISTRATION_1)
                          .userNameAttributeName(IdTokenClaimNames.SUB)
                          .build();
    public static final ClientRegistration CLIENT_REGISTRATION_OBO =
        toClientRegistrationBuilder("clientObo")
            .authorizationGrantType(new AuthorizationGrantType("on_behalf_of"))
            .scope("oboScope1", "oboScope2", "oboScope3")
            .build();

    public static ClientRegistration.Builder toClientRegistrationBuilder(String registrationId) {
        return ClientRegistration.withRegistrationId(registrationId)
                                 .clientId("clientId")
                                 .clientSecret("clientSecret")
                                 .redirectUri("redirectUri")
                                 .authorizationUri("https://login.microsoftonline.com/common/oauth2/v2.0/authorize")
                                 .tokenUri("tokenUri");
    }
}
