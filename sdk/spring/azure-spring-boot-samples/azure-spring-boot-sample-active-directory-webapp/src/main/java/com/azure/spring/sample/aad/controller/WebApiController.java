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
public class WebApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebApiController.class);

    private static final String CUSTOM_LOCAL_FILE_ENDPOINT = "http://localhost:8081/webapiA";

    @Autowired
    private WebClient webClient;

    /**
     * Call webapiA endpoint, combine all the response and return.
     * @param webapiAClient authorized client for Custom
     * @return Response webapiA data.
     */
    @GetMapping("/webapp/webapiA/webapiB")
    @ResponseBody
    public String callWebApi(@RegisteredOAuth2AuthorizedClient("webapiA") OAuth2AuthorizedClient webapiAClient) {
        return callWebApiAEndpoint(webapiAClient);
    }

    /**
     * Call webapiA endpoint
     * @param webapiAClient Authorized Client
     * @return Response string data.
     */
    private String callWebApiAEndpoint(OAuth2AuthorizedClient webapiAClient) {
        if (null != webapiAClient) {
            String body = webClient
                .get()
                .uri(CUSTOM_LOCAL_FILE_ENDPOINT)
                .attributes(oauth2AuthorizedClient(webapiAClient))
                .retrieve()
                .bodyToMono(String.class)
                .block();
            LOGGER.info("Response from webapiA : {}", body);
            return "webapiA response " + (null != body ? "success." : "failed.");
        } else {
            return "webapiA response failed.";
        }
    }
}
