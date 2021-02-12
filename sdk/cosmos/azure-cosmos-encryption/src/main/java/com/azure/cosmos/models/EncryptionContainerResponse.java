// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.EncryptionCosmosAsyncContainer;

public class EncryptionContainerResponse {
    private final CosmosContainerResponse containerResponse;

    private final EncryptionCosmosAsyncContainer encryptionContainer;

    public EncryptionContainerResponse(
        CosmosContainerResponse containerResponse,
        EncryptionCosmosAsyncContainer encryptionContainer) {
        this.containerResponse = containerResponse;
        this.encryptionContainer = encryptionContainer;
    }

    public CosmosContainerResponse getContainerResponse() {
        return containerResponse;
    }

    public EncryptionCosmosAsyncContainer getEncryptionContainer() {
        return encryptionContainer;
    }
}
