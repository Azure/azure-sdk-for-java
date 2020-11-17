package com.azure.test.aad.auth;

import com.azure.spring.autoconfigure.aad.AzureClientRegistrationRepository;
import com.azure.spring.autoconfigure.aad.AzureOAuth2AuthorizationCodeGrantRequestEntityConverter;
import com.azure.test.utils.AppRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.util.MultiValueMap;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AuthzCodeGrantRequestEntityConverterTest {

    private AppRunner appRunner;
    private AzureClientRegistrationRepository azureClientRegistrationRepository;
    private ClientRegistration azureClientRegistration;
    private ClientRegistration graphClientRegistration;

    @BeforeEach
    public void setupApp() {
        appRunner = createApp();
        appRunner.start();

        azureClientRegistrationRepository = appRunner.getBean(AzureClientRegistrationRepository.class);
        azureClientRegistration = azureClientRegistrationRepository.findByRegistrationId("azure");
        graphClientRegistration = azureClientRegistrationRepository.findByRegistrationId("graph");
    }

    private AppRunner createApp() {
        AppRunner result = new AppRunner(DumbApp.class);
        result.property("azure.activedirectory.tenant-id", "fake-tenant-id");
        result.property("azure.activedirectory.client-id", "fake-client-id");
        result.property("azure.activedirectory.client-secret", "fake-client-secret");
        result.property("azure.activedirectory.user-group.allowed-groups", "group1");
        result.property("azure.activedirectory.authorization.graph.scope", "Calendars.Read");
        return result;
    }

    @AfterEach
    public void tearDownApp() {
        appRunner.stop();
    }

    @Test
    public void addScopeForDefaultClient() {
        MultiValueMap<String, String> multiValueMap = toMultiValueMap(createCodeGrantRequest(azureClientRegistration));
        assertEquals("openid profile offline_access", multiValueMap.getFirst("scope"));
    }

    @Test
    public void noScopeParamForOtherClient() {
        MultiValueMap<String, String> multiValueMap = toMultiValueMap(createCodeGrantRequest(graphClientRegistration));
        assertNull(multiValueMap.get("scope"));
    }

    @SuppressWarnings("unchecked")
    private MultiValueMap<String, String> toMultiValueMap(OAuth2AuthorizationCodeGrantRequest request) {
        return (MultiValueMap<String, String>)
            Optional.ofNullable(azureClientRegistrationRepository)
                    .map(AzureClientRegistrationRepository::defaultClient)
                    .map(AzureOAuth2AuthorizationCodeGrantRequestEntityConverter::new)
                    .map(converter -> converter.convert(request))
                    .map(HttpEntity::getBody)
                    .orElse(null);
    }

    private OAuth2AuthorizationCodeGrantRequest createCodeGrantRequest(ClientRegistration clientRegistration) {
        return new OAuth2AuthorizationCodeGrantRequest(
            clientRegistration,
            toOAuth2AuthorizationExchange(clientRegistration)
        );
    }

    private OAuth2AuthorizationExchange toOAuth2AuthorizationExchange(ClientRegistration clientRegistration) {
        return new OAuth2AuthorizationExchange(
            toOAuth2AuthorizationRequest(clientRegistration),
            toOAuth2AuthorizationResponse()
        );
    }

    private OAuth2AuthorizationRequest toOAuth2AuthorizationRequest(ClientRegistration clientRegistration) {
        return OAuth2AuthorizationRequest.authorizationCode()
                                         .authorizationUri(
                                             clientRegistration.getProviderDetails().getAuthorizationUri()
                                         )
                                         .clientId(clientRegistration.getClientId())
                                         .scopes(clientRegistration.getScopes())
                                         .state("fake-state")
                                         .redirectUri("http://localhost")
                                         .build();
    }

    private OAuth2AuthorizationResponse toOAuth2AuthorizationResponse() {
        return OAuth2AuthorizationResponse.success("fake-code")
                                          .redirectUri("http://localhost")
                                          .state("fake-state")
                                          .build();
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableWebSecurity
    public static class DumbApp {
    }
}
