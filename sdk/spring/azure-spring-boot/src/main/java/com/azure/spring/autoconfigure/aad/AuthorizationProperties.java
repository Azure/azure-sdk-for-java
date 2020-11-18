// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import java.util.ArrayList;
import java.util.List;

/**
 * Properties for a authorized client.
 */
public class AuthorizationProperties {

    private List<String> scope = new ArrayList<>();

    public List<String> getScope() {
        return scope;
    }

    public void setScope(List<String> scope) {
        this.scope = scope;
    }
}
