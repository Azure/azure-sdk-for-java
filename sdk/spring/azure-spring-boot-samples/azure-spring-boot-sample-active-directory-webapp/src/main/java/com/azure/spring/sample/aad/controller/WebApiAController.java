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
public class WebApiAController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebApiAController.class);

    private static final String CUSTOM_LOCAL_FILE_ENDPOINT = "http://localhost:8081/webapiA";

    @Autowired
    private WebClient webClient;

    /**
     * Call webapiA endpoint, combine all the response and return.
     * @param webapiA authorized client for Custom
     * @return Response webapiA data.
     */
    @GetMapping("/webapiA/webApiB")
    @ResponseBody
    public String callWebApi(@RegisteredOAuth2AuthorizedClient("webapiA") OAuth2AuthorizedClient webapiA) {
        return callWebApiEndpoint(webapiA);
    }

    /**
     * Call webapiA endpoint
     * @param webapiA Authorized Client
     * @return Response string data.
     */
    private String callWebApiEndpoint(OAuth2AuthorizedClient webapiA) {
        if (null != webapiA) {
            String body = webClient
                .get()
                .uri(CUSTOM_LOCAL_FILE_ENDPOINT)
                .attributes(oauth2AuthorizedClient(webapiA))
                .retrieve()
                .bodyToMono(String.class)
                .block();
            LOGGER.info("Response from webapi : {}", body);
            return "webapi response " + (null != body ? "success." : "failed.");
        } else {
            return "webapi response failed.";
        }
    }
}
