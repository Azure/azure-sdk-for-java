// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapi;

import java.util.List;

/**
 * Authorization Properties for an Web api.
 */
public class AADOboAuthorizationProperties {

    private List<String> scopes;

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }
}
