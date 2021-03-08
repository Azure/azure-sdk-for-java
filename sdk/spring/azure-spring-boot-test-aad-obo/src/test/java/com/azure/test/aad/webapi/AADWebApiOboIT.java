// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.webapi;

import com.azure.spring.test.AppRunner;
import com.azure.spring.test.aad.AADWebApiITHelper;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
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
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.azure.spring.test.Constant.MULTI_TENANT_SCOPE_GRAPH_READ;
import static com.azure.spring.test.EnvironmentVariable.AAD_MULTI_TENANT_CLIENT_ID;
import static com.azure.spring.test.EnvironmentVariable.AAD_MULTI_TENANT_CLIENT_SECRET;
import static org.junit.Assert.assertEquals;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

public class AADWebApiOboIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADWebApiOboIT.class);
    private static final String GRAPH_ME_ENDPOINT = "https://graph.microsoft.com/v1.0/me";

    private AADWebApiITHelper aadWebApiITHelper;

    @Before
    public void init() {
        Map<String, String> properties = new HashMap<>();
        properties.put("azure.activedirectory.client-id", AAD_MULTI_TENANT_CLIENT_ID);
        properties.put("azure.activedirectory.client-secret", AAD_MULTI_TENANT_CLIENT_SECRET);
        properties.put("azure.activedirectory.app-id-uri", "api://" + AAD_MULTI_TENANT_CLIENT_ID);
        properties.put("azure.activedirectory.authorization-clients.graph.scopes",
            "https://graph.microsoft.com/User.Read");
        aadWebApiITHelper = new AADWebApiITHelper(
            DumbApp.class,
            properties,
            AAD_MULTI_TENANT_CLIENT_ID,
            AAD_MULTI_TENANT_CLIENT_SECRET,
            Collections.singletonList(MULTI_TENANT_SCOPE_GRAPH_READ));
    }

    @Test
    public void testCallGraph() {
        assertEquals("Graph response success.", aadWebApiITHelper.httpGetStringByAccessToken("call-graph"));
    }

    private void runApp(Consumer<AppRunner> command) {
        try (AppRunner app = new AppRunner(AADWebApiOboIT.DumbApp.class)) {
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
