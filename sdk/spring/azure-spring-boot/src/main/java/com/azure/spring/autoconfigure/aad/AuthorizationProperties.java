// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import java.util.Arrays;
import java.util.List;

/**
 * Properties for a authorized client.
 */
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
