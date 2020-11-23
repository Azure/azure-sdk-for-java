package com.azure.spring.aad.implementation;

import com.azure.test.utils.AppRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.RequestEntity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AuthzCodeGrantRequestEntityConverterTest {

    private AppRunner runner;
    private AzureClientRegistrationRepository repo;
    private ClientRegistration azure;
    private ClientRegistration graph;

    //@BeforeEach
    public void setupApp() {
        runner = createApp();
        runner.start();

        repo = runner.getBean(AzureClientRegistrationRepository.class);
        azure = repo.findByRegistrationId("azure");
        graph = repo.findByRegistrationId("graph");
    }

    private AppRunner createApp() {
        AppRunner result = new AppRunner(DumbApp.class);
        result.property("azure.activedirectory.uri", "http://localhost");
        result.property("azure.activedirectory.tenant-id", "fake-tenant-id");
        result.property("azure.activedirectory.client-id", "fake-client-id");
        result.property("azure.activedirectory.client-secret", "fake-client-secret");
        result.property("azure.activedirectory.authorization.graph.scopes", "Calendars.Read");
        return result;
    }

    //@AfterEach
    public void tearDownApp() {
        runner.stop();
    }

    //@Test
    public void addScopeForDefaultClient() {
        MultiValueMap<String, String> body = convertedBodyOf(createCodeGrantRequest(azure));
        assertEquals("openid profile offline_access", body.getFirst("scope"));
    }

    //@Test
    public void noScopeParamForOtherClient() {
        MultiValueMap<String, String> body = convertedBodyOf(createCodeGrantRequest(graph));
        assertNull(body.get("scope"));
    }

    private MultiValueMap<String, String> convertedBodyOf(OAuth2AuthorizationCodeGrantRequest request) {
        AuthzCodeGrantRequestEntityConverter converter = new AuthzCodeGrantRequestEntityConverter(repo.defaultClient());
        RequestEntity<?> entity = converter.convert(request);
        return (MultiValueMap<String, String>) entity.getBody();
    }

    private OAuth2AuthorizationCodeGrantRequest createCodeGrantRequest(ClientRegistration client) {
        return new OAuth2AuthorizationCodeGrantRequest(client, createExchange(client));
    }

    private OAuth2AuthorizationExchange createExchange(ClientRegistration client) {
        return new OAuth2AuthorizationExchange(
            createAuthorizationRequest(client),
            createAuthorizationResponse());
    }

    private OAuth2AuthorizationRequest createAuthorizationRequest(ClientRegistration client) {
        OAuth2AuthorizationRequest.Builder builder = OAuth2AuthorizationRequest.authorizationCode();
        builder.authorizationUri(client.getProviderDetails().getAuthorizationUri());
        builder.clientId(client.getClientId());
        builder.scopes(client.getScopes());
        builder.state("fake-state");
        builder.redirectUri("http://localhost");
        return builder.build();
    }

    private OAuth2AuthorizationResponse createAuthorizationResponse() {
        OAuth2AuthorizationResponse.Builder builder = OAuth2AuthorizationResponse.success("fake-code");
        builder.redirectUri("http://localhost");
        builder.state("fake-state");
        return builder.build();
    }

    //@Configuration
    //@SpringBootApplication
    //@EnableWebSecurity
    public static class DumbApp {}
}
