// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.implementation;

import java.util.List;

public class AuthorizationProperties {

    private List<String> scopes;

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public List<String> getScopes() {
        return scopes;
    }
}
