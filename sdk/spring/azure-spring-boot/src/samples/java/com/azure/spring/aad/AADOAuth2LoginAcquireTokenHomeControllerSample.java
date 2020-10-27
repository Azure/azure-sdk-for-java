// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import com.azure.spring.autoconfigure.aad.AzureADGraphClient;
import com.azure.spring.autoconfigure.aad.ServiceEndpointsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.naming.ServiceUnavailableException;
import java.util.Arrays;
import java.util.HashSet;

@Controller
public class AADOAuth2LoginAcquireTokenHomeControllerSample {
    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;
    @Autowired
    private AADAuthenticationProperties aadAuthenticationProperties;
    @Autowired
    private ServiceEndpointsProperties serviceEndpointsProperties;

    @GetMapping("/")
    public String index(Model model, OAuth2AuthenticationToken authentication) throws ServiceUnavailableException {

        AzureADGraphClient graphClient = new AzureADGraphClient(aadAuthenticationProperties,
            serviceEndpointsProperties);

        String accessToken = graphClient.getOboToken("https://graph.microsoft.com/",
            new HashSet<>(Arrays.asList("User.Read")));

        final OAuth2AuthorizedClient authorizedClient =
            this.authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName());
        model.addAttribute("userName", authentication.getName());
        model.addAttribute("clientName", authorizedClient.getClientRegistration().getClientName());
        return "index";
    }
}
