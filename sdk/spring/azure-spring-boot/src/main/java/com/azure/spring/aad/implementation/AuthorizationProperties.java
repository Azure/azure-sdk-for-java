// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.implementation;

import java.util.Arrays;
import java.util.List;

public class AuthorizationProperties {

    private String[] scope = new String[0];

    public void setScope(String[] scope) {
        this.scope = scope.clone();
    }

    public String[] getScope() {
        return scope.clone();
    }

    public List<String> scopes() {
        return Arrays.asList(scope);
    }
}
