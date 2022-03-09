// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DCountInfo {
    @JsonProperty("dCountAlias")
    private String dCountAlias;

    public String getDCountAlias() {
        return dCountAlias;
    }

    public void setDCountAlias(String dCountAlias) {
        this.dCountAlias = dCountAlias;
    }
}
