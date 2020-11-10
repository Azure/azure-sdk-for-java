package com.azure.spring.aad.implementation;

import java.util.Arrays;
import java.util.List;

public class AuthorizationProperties {

    private String[] scope = new String[0];

    public void setScope(String[] scope) {
        this.scope = scope;
    }

    public String[] getScope() {
        return scope;
    }

    public List<String> scopes() {
        return Arrays.asList(scope);
    }
}
