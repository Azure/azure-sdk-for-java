// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.security.keyvault.certificates.sample.client.side;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class SampleController {

    private static final String SERVER_SIDE_ENDPOINT = "https://localhost:8443/";

    final RestTemplate restTemplate;

    public SampleController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/")
    public String helloWorld() {
        return String.format("Response from \"%s\": %s",
            SERVER_SIDE_ENDPOINT,
            restTemplate.getForObject(SERVER_SIDE_ENDPOINT, String.class));
    }
}
