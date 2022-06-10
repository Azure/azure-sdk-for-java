// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.models;

/** The private endpoint of the private endpoint connection. */
public final class PrivateEndpoint {

    private final String id;

    /**
     * Initializes a new instance of the {@link PrivateEndpoint} class.
     *
     * @param id the ID of the private endpoint.
     */
    public PrivateEndpoint(String id) {
        this.id = id;
    }

    /** @return the ID of the private endpoint. */
    public String id() {
        return this.id;
    }
}
