// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.metadata;

import java.util.List;

public final class MetadataAuthentication {
    private String loginEndpoint;
    private List<String> audiences;

    public String getLoginEndpoint() {
        return loginEndpoint;
    }

    public List<String> getAudiences() {
        return audiences;
    }
}
