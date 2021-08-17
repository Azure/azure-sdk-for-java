// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.test.aad.selenium;

import com.azure.spring.aad.webapi.AADResourceServerWebSecurityConfigurerAdapter;
import com.azure.spring.aad.webapp.AADWebSecurityConfigurerAdapter;
import com.azure.spring.test.aad.AADWebApiITHelper;
import com.azure.test.aad.common.AADSeleniumITHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.test.Constant.MULTI_TENANT_SCOPE_GRAPH_READ;
import static com.azure.spring.test.EnvironmentVariable.AAD_MULTI_TENANT_CLIENT_ID;
import static com.azure.spring.test.EnvironmentVariable.AAD_MULTI_TENANT_CLIENT_SECRET;
import static com.azure.spring.test.EnvironmentVariable.AAD_TENANT_ID_1;
import static com.azure.spring.test.EnvironmentVariable.AAD_USER_NAME_2;
import static com.azure.spring.test.EnvironmentVariable.AAD_USER_PASSWORD_2;
import static com.azure.spring.test.EnvironmentVariable.AZURE_CLOUD_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AADWebAppAndWebApiInOneAppIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADWebAppAndWebApiInOneAppIT.class);
    private static final String GRAPH_ME_ENDPOINT = "https://graph.microsoft.com/v1.0/me";

    private Map<String, String> properties;

    @BeforeAll
    public void beforeAll() {
        properties = new HashMap<>();
        properties.put("azure.activedirectory.tenant-id", AAD_TENANT_ID_1);
        properties.put("azure.activedirectory.client-id", AAD_MULTI_TENANT_CLIENT_ID);
        properties.put("azure.activedirectory.client-secret", AAD_MULTI_TENANT_CLIENT_SECRET);
        properties.put("azure.activedirectory.user-group.allowed-groups", "group1");
        properties.put("azure.activedirectory.post-logout-redirect-uri", "http://localhost:${server.port}");
        properties.put("azure.activedirectory.base-uri", AADSeleniumITHelper.getBaseUrl(AZURE_CLOUD_TYPE));
        properties.put("azure.activedirectory.graph-base-uri", AADSeleniumITHelper.getGraphBaseUrl(AZURE_CLOUD_TYPE));
        properties.put("azure.activedirectory.application-type", "web_application_and_resource_server");

        properties.put("azure.activedirectory.app-id-uri", "api://" + AAD_MULTI_TENANT_CLIENT_ID);
        properties.put("azure.activedirectory.authorization-clients.graph.scopes",
            "https://graph.microsoft.com/User.Read");
        properties.put("azure.activedirectory.authorization-clients.graph.authorization-grant-type", "on_behalf_of");
    }

    @Test
    public void testHomeApiOfWebApplication() {
        AADSeleniumITHelper aadSeleniumITHelper = new AADSeleniumITHelper(
            DumbApp.class,
            properties,
            AAD_USER_NAME_2,
            AAD_USER_PASSWORD_2);
        aadSeleniumITHelper.logIn();
        String httpResponse = aadSeleniumITHelper.httpGet("home");
        assertTrue(httpResponse.contains("home"));
        aadSeleniumITHelper.destroy();
    }

    @Test
    public void testCallGraphApiOfResourceServer() {
        AADWebApiITHelper aadWebApiITHelper = new AADWebApiITHelper(
            DumbApp.class,
            properties,
            AAD_MULTI_TENANT_CLIENT_ID,
            AAD_MULTI_TENANT_CLIENT_SECRET,
            Collections.singletonList(MULTI_TENANT_SCOPE_GRAPH_READ));
        assertEquals("Graph response success.",
            aadWebApiITHelper.httpGetStringByAccessToken("/api/call-graph"));
    }

    @SpringBootApplication
    @ImportAutoConfiguration(AADWebApplicationAndResourceServerITConfig.class)
    public static class DumbApp {

        @Autowired
        private WebClient webClient;

        @Bean
        public WebClient webClient(OAuth2AuthorizedClientManager authorizedClientManager) {
            ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
            return WebClient.builder()
                            .apply(oauth2Client.oauth2Configuration())
                            .build();
        }

        @RestController
        class WebApplicationController {

            @GetMapping(value = "/home")
            public ResponseEntity<String> home(Principal principal) {
                LOGGER.info(((OAuth2AuthenticationToken) principal).getAuthorities().toString());
                return ResponseEntity.ok("home");
            }
        }

        @RestController
        @RequestMapping("/api")
        class ResourceServerController {

            /**
             * Call the graph resource only with annotation, return user information
             *
             * @param graphClient authorized client for Graph
             * @return Response with graph data
             */
            @GetMapping("/call-graph")
            @PreAuthorize("hasAuthority('SCOPE_ResourceAccessGraph.Read')")
            public String callGraph(@RegisteredOAuth2AuthorizedClient("graph") OAuth2AuthorizedClient graphClient) {
                return callMicrosoftGraphMeEndpoint(graphClient);
            }

            /**
             * Call microsoft graph me endpoint
             *
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

    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public static class AADWebApplicationAndResourceServerITConfig {

        @Order(1)
        @Configuration
        public static class ApiWebSecurityConfigurationAdapter extends AADResourceServerWebSecurityConfigurerAdapter {
            protected void configure(HttpSecurity http) throws Exception {
                super.configure(http);
                http.antMatcher("/api/**")
                    .authorizeRequests().anyRequest().authenticated();
            }
        }

        @Configuration
        public static class HtmlWebSecurityConfigurerAdapter extends AADWebSecurityConfigurerAdapter {

            @Override
            protected void configure(HttpSecurity http) throws Exception {
                super.configure(http);
                // @formatter:off
                http.authorizeRequests()
                    .antMatchers("/login").permitAll()
                    .anyRequest().authenticated();
                // @formatter:on
            }
        }
    }
}
