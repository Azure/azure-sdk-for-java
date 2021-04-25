// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.jca.implementation.http.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class OAuthToken implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @JsonProperty("access_token")
    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
