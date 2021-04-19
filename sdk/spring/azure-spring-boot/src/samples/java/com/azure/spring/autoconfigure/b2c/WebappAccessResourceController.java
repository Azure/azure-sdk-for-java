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
    private static final String LOCAL_WEB_API_A_SAMPLE_ENDPOINT = "http://localhost:8081/webApiA/webApiB/sample";

    @Autowired
    private WebClient webClient;

    /**
     * Access to protected data through client credential flow. The access token is obtained by webclient, or
     * <p>@RegisteredOAuth2AuthorizedClient("webapiA")</p>. In the end, these two approaches will be executed to
     * DefaultOAuth2AuthorizedClientManager#authorize method, get the access token.
     *
     * @return Respond to protected data.
     */
    @GetMapping("/webapp/webApiA")
    public String callWebApiA() {
        String body = webClient
            .get()
            .uri(LOCAL_WEB_API_A_SAMPLE_ENDPOINT)
            .attributes(clientRegistrationId("webapiA"))
            .retrieve()
            .bodyToMono(String.class)
            .block();
        LOGGER.info("Webapp callWebApiA() returned: {}", body);
        return "Response from WebApi A: " + (null != body ? "success." : "failed.");
    }

    /**
     * Access to protected data through client credential flow. The access token is obtained by webclient, or
     * <p>@RegisteredOAuth2AuthorizedClient("webapiA")</p>. In the end, these two approaches will be executed to
     * DefaultOAuth2AuthorizedClientManager#authorize method, get the access token.
     *
     * @return Respond to protected data.
     */
    @GetMapping("/webapp/webApiA/webApiB")
    public String callWebApiAThenCallWebApiB() {
        String body = webClient
            .get()
            .uri(LOCAL_WEB_API_A_SAMPLE_ENDPOINT)
            .attributes(clientRegistrationId("webapiA"))
            .retrieve()
            .bodyToMono(String.class)
            .block();
        LOGGER.info("Webapp callWebApiAThenCallWebApiB() returned: {}", body);
        return "Response from WebApi A(WebApi A response from WebApi B): " + (null != body ? "success." : "failed.");
    }
}
