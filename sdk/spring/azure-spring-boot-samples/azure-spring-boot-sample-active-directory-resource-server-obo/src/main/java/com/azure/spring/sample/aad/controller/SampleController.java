// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.aad.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@RestController
public class SampleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleController.class);

    private static final String GRAPH_ME_ENDPOINT = "https://graph.microsoft.com/v1.0/me";

    private static final String CUSTOM_LOCAL_FILE_ENDPOINT = "http://localhost:8080/file";

    @Autowired
    private WebClient webClient;

    @Autowired
    private OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository;

    /**
     * Call the graph resource, return user information
     * @return Response with graph data
     */
    @PreAuthorize("hasAuthority('SCOPE_Obo.Graph.Read')")
    @GetMapping("call-graph-with-repository")
    public String callGraphWithRepository() {
        Authentication principal = SecurityContextHolder.getContext().getAuthentication();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) requestAttributes;
        OAuth2AuthorizedClient graph = oAuth2AuthorizedClientRepository
            .loadAuthorizedClient("graph", principal, sra.getRequest());
        return callMicrosoftGraphMeEndpoint(graph);
    }

    /**
     * Call the graph resource with annotation, return user information
     * @param graph authorized client for Graph
     * @return Response with graph data
     */
    @PreAuthorize("hasAuthority('SCOPE_Obo.Graph.Read')")
    @GetMapping("call-graph")
    public String callGraph(@RegisteredOAuth2AuthorizedClient("graph") OAuth2AuthorizedClient graph) {
        return callMicrosoftGraphMeEndpoint(graph);
    }

    /**
     * Call custom resources, combine all the response and return.
     * @param custom authorized client for Custom
     * @return Response Graph and Custom data.
     */
    @PreAuthorize("hasAuthority('SCOPE_Obo.File.Read')")
    @GetMapping("call-custom")
    public String callCustom(
        @RegisteredOAuth2AuthorizedClient("custom") OAuth2AuthorizedClient custom) {
        return callCustomLocalFileEndpoint(custom);
    }

    /**
     * Call microsoft graph me endpoint
     * @param graph Authorized Client
     * @return Response string data.
     */
    private String callMicrosoftGraphMeEndpoint(OAuth2AuthorizedClient graph) {
        if (null != graph) {
            String body = webClient
                .get()
                .uri(GRAPH_ME_ENDPOINT)
                .attributes(oauth2AuthorizedClient(graph))
                .retrieve()
                .bodyToMono(String.class)
                .block();
            LOGGER.info("Response from Graph: {}", body);
            return "Graph response " + (null != body ? "success." : "failed.");
        } else {
            return "Graph response failed.";
        }
    }

    /**
     * Call custom local file endpoint
     * @param custom Authorized Client
     * @return Response string data.
     */
    private String callCustomLocalFileEndpoint(OAuth2AuthorizedClient custom) {
        if (null != custom) {
            String body = webClient
                .get()
                .uri(CUSTOM_LOCAL_FILE_ENDPOINT)
                .attributes(oauth2AuthorizedClient(custom))
                .retrieve()
                .bodyToMono(String.class)
                .block();
            LOGGER.info("Response from Custom: {}", body);
            return "Custom response " + (null != body ? "success." : "failed.");
        } else {
            return "Custom response failed.";
        }
    }
}
