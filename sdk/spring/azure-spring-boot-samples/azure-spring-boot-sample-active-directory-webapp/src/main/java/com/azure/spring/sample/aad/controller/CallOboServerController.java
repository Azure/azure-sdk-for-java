// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.aad.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;


import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@Controller
public class CallOboServerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CallOboServerController.class);

    private static final String CUSTOM_LOCAL_FILE_ENDPOINT = "http://localhost:8081/call-custom";

    @Autowired
    private WebClient webClient;

    /**
     * Call obo server, combine all the response and return.
     * @param obo authorized client for Custom
     * @return Response Graph and Custom data.
     */
    @GetMapping("/obo")
    @ResponseBody
    public String callOboServer(@RegisteredOAuth2AuthorizedClient("obo") OAuth2AuthorizedClient obo) {
        return callOboEndpoint(obo);
    }

    /**
     * Call obo local file endpoint
     * @param obo Authorized Client
     * @return Response string data.
     */
    private String callOboEndpoint(OAuth2AuthorizedClient obo) {
        if (null != obo) {
            String body = webClient
                .get()
                .uri(CUSTOM_LOCAL_FILE_ENDPOINT)
                .attributes(oauth2AuthorizedClient(obo))
                .retrieve()
                .bodyToMono(String.class)
                .block();
            LOGGER.info("Response from obo server: {}", body);
            return "Obo server response " + (null != body ? "success." : "failed.");
        } else {
            return "Obo server response failed.";
        }
    }
}
