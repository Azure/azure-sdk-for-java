// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.jwt;

import com.nimbusds.jose.util.Resource;
import com.nimbusds.jose.util.ResourceRetriever;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import static com.azure.spring.cloud.autoconfigure.aad.implementation.AadRestTemplateCreator.createRestTemplate;

/**
 * A ResourceRetriever that accepts an RestOperations as a constructor parameter.
 * Copied from org.springframework.security.oauth2.jwt.NimbusJwtDecoder.JwkSetUriJwtDecoderBuilder.RestOperationsResourceRetriever
 */
public class RestOperationsResourceRetriever implements ResourceRetriever {

    private static final MediaType APPLICATION_JWK_SET_JSON = new MediaType("application", "jwk-set+json");

    private final RestOperations restOperations;

    public RestOperationsResourceRetriever(RestTemplateBuilder restTemplateBuilder) {
        this.restOperations = createRestTemplate(restTemplateBuilder);
    }

    @Override
    public Resource retrieveResource(URL url) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, APPLICATION_JWK_SET_JSON));
        ResponseEntity<String> response = getResponse(url, headers);
        if (response.getStatusCodeValue() != HttpStatus.OK.value()) {
            throw new IOException(response.toString());
        }
        return new Resource(response.getBody(), "UTF-8");
    }

    private ResponseEntity<String> getResponse(URL url, HttpHeaders headers) throws IOException {
        try {
            RequestEntity<Void> request = new RequestEntity<>(headers, HttpMethod.GET, url.toURI());
            return this.restOperations.exchange(request, String.class);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

}
