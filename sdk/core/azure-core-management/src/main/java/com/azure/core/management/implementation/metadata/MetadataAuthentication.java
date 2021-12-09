// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public final class MetadataAuthentication {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String loginEndpoint;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> audiences;

    public String getLoginEndpoint() {
        return loginEndpoint;
    }

    public List<String> getAudiences() {
        return audiences;
    }
}
