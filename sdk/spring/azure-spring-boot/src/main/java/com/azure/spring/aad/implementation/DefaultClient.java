package com.azure.spring.aad.implementation;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.oauth2.client.registration.ClientRegistration;

public class DefaultClient {

    private final ClientRegistration clientRegistration;
    private final String [] scope;

    public DefaultClient(ClientRegistration clientRegistration, String [] scope) {
        this.clientRegistration = clientRegistration;
        this.scope = scope;
    }

    public ClientRegistration getClientRegistration() {
        return clientRegistration;
    }

    public String [] getScope() {
        return scope;
    }

    public List<String> getScopeList() {
        return Arrays.asList(scope);
    }
}
