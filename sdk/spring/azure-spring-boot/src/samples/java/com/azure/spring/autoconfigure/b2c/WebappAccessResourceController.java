// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.b2c;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@RestController
public class WebappAccessResourceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebappAccessResourceController.class);
    private static final String LOCAL_WEB_API_A_SAMPLE_ENDPOINT = "http://localhost:8081/webApiA/sample";
    private static final String LOCAL_WEB_API_A_WEB_API_B_SAMPLE_ENDPOINT = "http://localhost:8081/webApiA/webApiB/sample";

    @Autowired
    private WebClient webClient;

    /**
     * Access to protected data from Webapp to WebApiA through client credential flow. The access token is obtained by webclient, or
     * <p>@RegisteredOAuth2AuthorizedClient("webApiA")</p>. In the end, these two approaches will be executed to
     * DefaultOAuth2AuthorizedClientManager#authorize method, get the access token.
     *
     * @return Respond to protected data from WebApi A.
     */
    @GetMapping("/webapp/webApiA")
    public String callWebApiA() {
        String body = webClient
            .get()
            .uri(LOCAL_WEB_API_A_SAMPLE_ENDPOINT)
            .attributes(clientRegistrationId("webApiA"))
            .retrieve()
            .bodyToMono(String.class)
            .block();
        LOGGER.info("Call callWebApiA(), request '/webApiA/sample' returned: {}", body);
        return "Request '/webApiA/sample'(WebApi A) returned a " + (body != null ? "success." : "failure.");
    }

    /**
     * Access to protected data from Webapp to WebApiA through client credential flow. The access token is obtained by webclient, or
     * <p>@RegisteredOAuth2AuthorizedClient("webApiA")</p>. In the end, these two approaches will be executed to
     * DefaultOAuth2AuthorizedClientManager#authorize method, get the access token.
     *
     * @return Respond to protected data from WebApi A and WebApi B.
     */
    @GetMapping("/webapp/webApiA/webApiB")
    public String callWebApiAThenCallWebApiB() {
        String body = webClient
            .get()
            .uri(LOCAL_WEB_API_A_WEB_API_B_SAMPLE_ENDPOINT)
            .attributes(clientRegistrationId("webApiA"))
            .retrieve()
            .bodyToMono(String.class)
            .block();
        LOGGER.info("Call callWebApiAThenCallWebApiB(), request '/webApiA/webApiB/sample' returned: {}", body);
        return "Request '/webApiA/webApiB/sample'(WebApi A) returned a " + (body != null ? "success." : "failure.");
    }
}
