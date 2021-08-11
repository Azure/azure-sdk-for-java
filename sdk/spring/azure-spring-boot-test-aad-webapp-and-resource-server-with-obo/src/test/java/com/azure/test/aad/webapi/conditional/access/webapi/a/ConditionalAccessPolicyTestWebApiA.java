// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.test.aad.webapi.conditional.access.webapi.a;

import com.azure.spring.test.AppRunner;
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
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.test.EnvironmentVariable.AAD_TENANT_ID_1;
import static com.azure.spring.test.EnvironmentVariable.CONDITIONAL_ACCESS_POLICY_TEST_WEB_API_A_CLIENT_ID;
import static com.azure.spring.test.EnvironmentVariable.CONDITIONAL_ACCESS_POLICY_TEST_WEB_API_A_CLIENT_SECRET;
import static com.azure.spring.test.EnvironmentVariable.CONDITIONAL_ACCESS_POLICY_TEST_WEB_API_B_CLIENT_ID;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

public class ConditionalAccessPolicyTestWebApiA {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConditionalAccessPolicyTestWebApiA.class);
    private static final String WEB_API_B_ENDPOINT = "http://localhost:8883/webapiB";

    private static void start() {
        Map<String, String> properties = new HashMap<>();
        properties.put("server.port", "8882");
        properties.put("azure.activedirectory.client-id", CONDITIONAL_ACCESS_POLICY_TEST_WEB_API_A_CLIENT_ID);
        properties.put("azure.activedirectory.client-secret", CONDITIONAL_ACCESS_POLICY_TEST_WEB_API_A_CLIENT_SECRET);
        properties.put("azure.activedirectory.tenant-id", AAD_TENANT_ID_1);
        properties.put("azure.activedirectory.authorization-clients.webapiB.scopes",
            "api://" + CONDITIONAL_ACCESS_POLICY_TEST_WEB_API_B_CLIENT_ID + "/File.Read");
        properties.put("azure.activedirectory.app-id-uri",
            "api://" + CONDITIONAL_ACCESS_POLICY_TEST_WEB_API_A_CLIENT_ID);
        AppRunner app = new AppRunner(DumbApp.class);
        properties.forEach(app::property);
        app.start();
    }

    public static void main(String[] args) {
        start();
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
        public static WebClient webClient(ClientRegistrationRepository clientRegistrationRepository,
                                          OAuth2AuthorizedClientRepository authorizedClientRepository) {
            ServletOAuth2AuthorizedClientExchangeFilterFunction function =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrationRepository,
                    authorizedClientRepository);
            return WebClient.builder()
                            .apply(function.oauth2Configuration())
                            .build();
        }


        /**
         * webapiA, return response body.
         *
         * @param webapiB authorized client for webapiB
         * @return Response response body data.
         */
        @GetMapping("webapiA")
        @PreAuthorize("hasAuthority('SCOPE_File.Read')")
        public String callCustom(@RegisteredOAuth2AuthorizedClient("webapiB") OAuth2AuthorizedClient webapiB) {
            return callCustomLocalFileEndpoint(webapiB);
        }

        /**
         * Call webapiB endpoint
         *
         * @param webapiB Authorized Client
         * @return Response string data.
         */
        private String callCustomLocalFileEndpoint(OAuth2AuthorizedClient webapiB) {
            String body = webClient
                .get()
                .uri(WEB_API_B_ENDPOINT)
                .attributes(oauth2AuthorizedClient(webapiB))
                .retrieve()
                .bodyToMono(String.class)
                .block();
            LOGGER.info("Response from webapiB: {}", body);
            return body;
        }
    }
}
