// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.models;

public final class PrivateEndpoint {

    private final String id;

    public PrivateEndpoint(String id) {
        this.id = id;
    }

    private String id() {
        return this.id;
    }
}
