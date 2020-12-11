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
     * Call the graph resource only, return user information
     * @return Response with graph data
     */
    @GetMapping("call-graph-only")
    @PreAuthorize("hasAuthority('SCOPE_ResourceAccessGraph.read')")
    public String callGraphOnly() {
        Authentication principal = SecurityContextHolder.getContext().getAuthentication();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) requestAttributes;
        OAuth2AuthorizedClient graph = oAuth2AuthorizedClientRepository
            .loadAuthorizedClient("graph", principal, sra.getRequest());
        return callMicrosoftGraphMeEndpoint(graph);
    }

    /**
     * Call the graph resource only with annotation, return user information
     * @param graph authorized client for Graph
     * @return Response with graph data
     */
    @GetMapping("call-graph-only-with-annotation")
    @PreAuthorize("hasAuthority('SCOPE_ResourceAccessGraph.read')")
    public String callGraphOnlyWithAnnotation(@RegisteredOAuth2AuthorizedClient("graph") OAuth2AuthorizedClient graph) {
        return callMicrosoftGraphMeEndpoint(graph);
    }

    /**
     * Call the graph and custom(local) resources, combine all the response and return.
     * @param graph authorized client for Graph
     * @param custom authorized client for Custom
     * @return Response Graph and Custom data.
     */
    @PreAuthorize("hasAuthority('SCOPE_ResourceAccessGraphCustomResources.read')")
    @GetMapping("call-graph-and-custom-resources")
    public String callGraphAndCustomResources(
        @RegisteredOAuth2AuthorizedClient("graph") OAuth2AuthorizedClient graph,
        @RegisteredOAuth2AuthorizedClient("custom") OAuth2AuthorizedClient custom) {
        return callMicrosoftGraphMeEndpoint(graph) + " " + callCustomLocalFileEndpoint(custom);
    }

    /**
     * Call microsoft graph me endpoint
     * @param graph Authorized Client
     * @return Response string data.
     */
    private String callMicrosoftGraphMeEndpoint(OAuth2AuthorizedClient graph) {
        String body = webClient
            .get()
            .uri(GRAPH_ME_ENDPOINT)
            .attributes(oauth2AuthorizedClient(graph))
            .retrieve()
            .bodyToMono(String.class)
            .block();
        LOGGER.info("Response from Graph: {}", body);
        return "Graph response " + (null != body ? "success." : "failed.");
    }

    /**
     * Call custom local file endpoint
     * @param custom Authorized Client
     * @return Response string data.
     */
    private String callCustomLocalFileEndpoint(OAuth2AuthorizedClient custom) {
        String body = webClient
            .get()
            .uri(CUSTOM_LOCAL_FILE_ENDPOINT)
            .attributes(oauth2AuthorizedClient(custom))
            .retrieve()
            .bodyToMono(String.class)
            .block();
        LOGGER.info("Response from Custom(local): {}", body);
        return "Custom(local) response " + (null != body ? "success." : "failed.");
    }
}
