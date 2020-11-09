package com.azure.spring.aad.implementation;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.oauth2.client.registration.ClientRegistration;

public class DefaultClient {

    private ClientRegistration client;
    private String [] scope;

    public DefaultClient(ClientRegistration client, String [] scope) {
        this.client = client;
        this.scope = scope;
    }

    public ClientRegistration client() {
        return client;
    }

    public String [] scope() {
        return scope;
    }

    public List<String> scopes() {
        return Arrays.asList(scope);
    }
}
