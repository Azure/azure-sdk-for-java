// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security;

import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.JwtBearerGrantRequest;
import org.springframework.security.oauth2.client.endpoint.JwtBearerGrantRequestEntityConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

/**
 * This is a special JWT Bearer flow implementation for Microsoft identify platform.
 *
 * @since 4.3.0
 * @see <a href="https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-on-behalf-of-flow">OAuth 2.0 On-Behalf-Of</a>
 */
@SuppressWarnings({"deprecation", "removal"})
public class AadJwtBearerGrantRequestEntityConverter extends JwtBearerGrantRequestEntityConverter {

    @Override
    protected MultiValueMap<String, String> createParameters(JwtBearerGrantRequest jwtBearerGrantRequest) {
        MultiValueMap<String, String> parameters = super.createParameters(jwtBearerGrantRequest);
        parameters.add("requested_token_use", "on_behalf_of");
        return parameters;
    }

    @Override
    public RequestEntity<?> convert(JwtBearerGrantRequest jwtBearerGrantRequest) {
        // Call the parent convert() method which will run all registered parameter converters
        RequestEntity<?> requestEntity = super.convert(jwtBearerGrantRequest);
        
        // Get the body (parameters) from the request entity
        Object body = requestEntity.getBody();
        if (!(body instanceof MultiValueMap)) {
            return requestEntity;
        }
        
        @SuppressWarnings("unchecked")
        MultiValueMap<String, String> parameters = (MultiValueMap<String, String>) body;
        
        // Flatten multi-valued parameters to single values
        // This fixes the issue where multiple converters add the same parameter key,
        // causing duplicate values (e.g., grant_type=[value1, value2] instead of grant_type=[value1])
        MultiValueMap<String, String> flattenedParameters = new LinkedMultiValueMap<>();
        for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
            List<String> values = entry.getValue();
            if (values != null && !values.isEmpty()) {
                // Use set() to ensure only one value per key
                // Take the first value to preserve the base implementation's default
                flattenedParameters.set(entry.getKey(), values.get(0));
            }
            // Note: Parameters with null or empty value lists are intentionally excluded
            // from the request, as per OAuth2 specification
        }
        
        // Return a new RequestEntity with the flattened parameters
        return RequestEntity
                .method(requestEntity.getMethod(), requestEntity.getUrl())
                .headers(requestEntity.getHeaders())
                .body(flattenedParameters);
    }
}

