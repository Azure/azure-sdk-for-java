package com.azure.spring.aad.implementation;

import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.util.MultiValueMap;

public class AuthzCodeGrantRequestEntityConverter extends OAuth2AuthorizationCodeGrantRequestEntityConverter {

    private DefaultClient defaultClient;

    public AuthzCodeGrantRequestEntityConverter(DefaultClient client) {
        defaultClient = client;
    }

    @Override
    public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest request) {
        RequestEntity<?> result = super.convert(request);

        if (isRequestForDefaultClient(request)) {
            MultiValueMap<String, String> body = (MultiValueMap<String, String>) result.getBody();
            body.add("scope", scopeValue());
        }
        return result;
    }

    private boolean isRequestForDefaultClient(OAuth2AuthorizationCodeGrantRequest request) {
        return request.getClientRegistration().equals(defaultClient.client());
    }

    private String scopeValue() {
        return String.join(" ", defaultClient.scope());
    }
}
