// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.aad.controller;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import com.azure.spring.autoconfigure.aad.AzureADGraphClient;
import com.azure.spring.autoconfigure.aad.ServiceEndpointsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.naming.ServiceUnavailableException;
import java.util.Arrays;
import java.util.stream.Collectors;

@Controller
public class HomeController {
    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;
    @Autowired
    private AADAuthenticationProperties aadAuthenticationProperties;

    @Autowired
    private ServiceEndpointsProperties serviceEndpointsProperties;

    @GetMapping("group1")
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_group1')")
    public String group1() {
        return "group1 message";
    }

    @GetMapping("group2")
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_group2')")
    public String group2() {
        return "group2 message";
    }

    @GetMapping("/")
    public String index(Model model, OAuth2AuthenticationToken authentication) throws ServiceUnavailableException {

        AzureADGraphClient graphClient = new AzureADGraphClient(aadAuthenticationProperties,
            serviceEndpointsProperties);

        String accesstoken = graphClient.getOboToken(Arrays.asList("https://graph.microsoft.com/user.read")
                                                              .stream().collect(Collectors.toSet()));

        final OAuth2AuthorizedClient authorizedClient =
                this.authorizedClientService.loadAuthorizedClient(
                        authentication.getAuthorizedClientRegistrationId(),
                        authentication.getName());
        model.addAttribute("userName", authentication.getName());
        model.addAttribute("clientName", authorizedClient.getClientRegistration().getClientName());
        return "index";
    }
}
