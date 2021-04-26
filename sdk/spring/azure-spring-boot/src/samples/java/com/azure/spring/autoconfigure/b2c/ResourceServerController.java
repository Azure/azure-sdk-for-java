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
    public String callWebApiASample() {
        LOGGER.info("WebApiA callSample() returned.");
        return "Response from Client Credential from WebApiA success.";
    }

    /**
     * webApiB resource api for other web application
     * @return test content
     */
    @PreAuthorize("hasAuthority('APPROLE_WebApiB.SampleScope')")
    @GetMapping("/webApiB/sample")
    public String callSample() {
        LOGGER.info("WebApiB callSample() returned.");
        return "Response from Client Credential from WebApiB success.";
    }

    /**
     * webApiA and webApiB resources api for web app
     * @return test content from web api b
     */
    @GetMapping("webApiA/webApiB/sample")
    @PreAuthorize("hasAuthority('APPROLE_WebApiA.SampleScope')")
    public String callWebApiB() {
        String body = webClient
            .get()
            .uri(LOCAL_WEB_API_B_SAMPLE_ENDPOINT)
            .attributes(clientRegistrationId("webApiB"))
            .retrieve()
            .bodyToMono(String.class)
            .block();
        LOGGER.info("WebApiA callWebApiB() returned: {}", body);
        return "Response from WebApi B to WebApiA(Client Credential flow): " + (null != body ? "success." : "failed.");
    }
}
