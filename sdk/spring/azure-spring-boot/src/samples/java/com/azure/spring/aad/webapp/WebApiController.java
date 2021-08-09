// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

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
    private static final String WEB_API_A_URI = "http://localhost:8081/webapiA/webapiB";
    private static final String WEB_API_B_URI = "http://localhost:8082/webapiB/clientCredential";

    @Autowired
    private WebClient webClient;

    /**
     * Check whether webapiA is accessible.
     *
     * @param client authorized client for webapiA
     * @return Response webapiA data.
     */
    @GetMapping("/webapp/webapiA/webapiB")
    @ResponseBody
    public String webapiA(@RegisteredOAuth2AuthorizedClient("webapiA") OAuth2AuthorizedClient client) {
        return canVisitUri(client, WEB_API_A_URI);
    }

    /**
     * Check whether webapiB/clientCredential is accessible.
     *
     * @param client authorized client for webapiB
     * @return Response webapiA data.
     */
    @GetMapping("/webapp/webapiB/clientCredential")
    @ResponseBody
    public String webapiB(@RegisteredOAuth2AuthorizedClient("webapiB") OAuth2AuthorizedClient client) {
        return canVisitUri(client, WEB_API_B_URI);
    }

    /**
     * Check whether uri is accessible by provided client.
     *
     * @param client Authorized client.
     * @param uri The request uri.
     * @return "Get http response successfully." or "Get http response failed."
     */
    private String canVisitUri(OAuth2AuthorizedClient client, String uri) {
        if (null == client) {
            return "Get response failed.";
        }
        String body = webClient
            .get()
            .uri(uri)
            .attributes(oauth2AuthorizedClient(client))
            .retrieve()
            .bodyToMono(String.class)
            .block();
        LOGGER.info("Response from {} : {}", uri, body);
        return "Get response " + (null != body ? "successfully" : "failed");
    }
}
