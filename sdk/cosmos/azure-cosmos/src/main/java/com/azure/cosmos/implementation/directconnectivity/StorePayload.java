// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

public interface StorePayload<T> {
    int getResponsePayloadSize();

    T getPayload();
}
