// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.b2c;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@RestController
public class ResourceServerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceServerController.class);
    private static final String LOCAL_WEB_API_B_SAMPLE_ENDPOINT = "http://localhost:8082/webApiB/sample";

    @Autowired
    private WebClient webClient;

    /**
     * webApiA resource api for web app
     * @return test content
     */
    @PreAuthorize("hasAuthority('APPROLE_WebApiA.SampleScope')")
    @GetMapping("/webApiA/sample")
    public String webApiASample() {
        LOGGER.info("Call webApiASample()");
        return "Request '/webApiA/sample'(WebApi A) returned successfully.";
    }

    /**
     * webApiB resource api for other web application
     * @return test content
     */
    @PreAuthorize("hasAuthority('APPROLE_WebApiB.SampleScope')")
    @GetMapping("/webApiB/sample")
    public String webApiBSample() {
        LOGGER.info("Call webApiBSample()");
        return "Request '/webApiB/sample'(WebApi B) returned successfully.";
    }

    /**
     * Access to protected data from WebApiA to WebApiB through client credential flow. The access token is obtained by webclient, or
     * <p>@RegisteredOAuth2AuthorizedClient("webApiA")</p>. In the end, these two approaches will be executed to
     * DefaultOAuth2AuthorizedClientManager#authorize method, get the access token.
     *
     * @return Respond to protected data from WebApi B.
     */
    @GetMapping("/webApiA/webApiB/sample")
    @PreAuthorize("hasAuthority('APPROLE_WebApiA.SampleScope')")
    public String callWebApiB() {
        String body = webClient
            .get()
            .uri(LOCAL_WEB_API_B_SAMPLE_ENDPOINT)
            .attributes(clientRegistrationId("webApiB"))
            .retrieve()
            .bodyToMono(String.class)
            .block();
        LOGGER.info("Call callWebApiB(), request '/webApiB/sample' returned: {}", body);
        return "Request 'webApiA/webApiB/sample'(WebApi A) returned a " + (body != null ? "success." : "failure.");
    }
}
