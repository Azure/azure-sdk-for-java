// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.webapi;

import com.azure.test.oauth.OAuthResponse;
import com.azure.test.oauth.OAuthUtils;
import com.azure.test.utils.AppRunner;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.function.Consumer;

import static com.azure.test.oauth.OAuthUtils.AAD_MULTI_TENANT_CLIENT_ID;
import static com.azure.test.oauth.OAuthUtils.AAD_MULTI_TENANT_CLIENT_SECRET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

public class AADWebApiOboIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADWebApiOboIT.class);

    private static final String GRAPH_ME_ENDPOINT = "https://graph.microsoft.com/v1.0/me";

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    public void testCallGraph() {
        this.runApp(app -> {
            final OAuthResponse authResponse = OAuthUtils.executeOAuth2ROPCFlow(
                System.getenv(AAD_MULTI_TENANT_CLIENT_ID), System.getenv(AAD_MULTI_TENANT_CLIENT_SECRET));
            assertNotNull(authResponse);

            final HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", String.format("Bearer %s", authResponse.getAccessToken()));
            HttpEntity<Object> entity = new HttpEntity<>(headers);
            final ResponseEntity<String> response = restTemplate.exchange(
                app.root() + "/call-graph",
                HttpMethod.GET,
                entity,
                String.class,
                new HashMap<>()
            );
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Graph response success.", response.getBody());
        });
    }

    private void runApp(Consumer<AppRunner> command) {
        try (AppRunner app = new AppRunner(AADWebApiOboIT.DumbApp.class)) {
            final String clientId = System.getenv(AAD_MULTI_TENANT_CLIENT_ID);
            final String clientSecret = System.getenv(AAD_MULTI_TENANT_CLIENT_SECRET);
            app.property("azure.activedirectory.client-id", clientId);
            app.property("azure.activedirectory.client-secret", clientSecret);
            app.property("azure.activedirectory.app-id-uri", "api://" + clientId);
            app.property("azure.activedirectory.authorization-clients.graph.scopes", "https://graph.microsoft.com/User.Read");
            app.start();
            command.accept(app);
        }
    }

    @EnableWebSecurity
    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
    @SpringBootApplication
    @RestController
    public static class DumbApp extends WebSecurityConfigurerAdapter {

        @Autowired
        private WebClient webClient;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
        }

        @Bean
        public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository) {
            OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                                                     .refreshToken()
                                                     .build();
            DefaultOAuth2AuthorizedClientManager authorizedClientManager =
                new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository);
            authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
            return authorizedClientManager;
        }

        @Bean
        public WebClient webClient(OAuth2AuthorizedClientManager authorizedClientManager) {
            ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
            return WebClient.builder()
                            .apply(oauth2Client.oauth2Configuration())
                            .build();
        }

        /**
         * Call the graph resource only with annotation, return user information
         * @param graph authorized client for Graph
         * @return Response with graph data
         */
        @GetMapping("call-graph")
        @PreAuthorize("hasAuthority('SCOPE_ResourceAccessGraph.Read')")
        public String callGraph(@RegisteredOAuth2AuthorizedClient("graph") OAuth2AuthorizedClient graph) {
            return callMicrosoftGraphMeEndpoint(graph);
        }

        /**
         * Call microsoft graph me endpoint
         * @param graph Authorized Client
         * @return Response string data.
         */
        private String callMicrosoftGraphMeEndpoint(OAuth2AuthorizedClient graph) {
            String body = webClient
                .get()
                .uri(GRAPH_ME_ENDPOINT)
                .attributes(oauth2AuthorizedClient(graph))
                .retrieve()
                .bodyToMono(String.class)
                .block();
            LOGGER.info("Response from Graph: {}", body);
            return "Graph response " + (null != body ? "success." : "failed.");
        }
    }
}
