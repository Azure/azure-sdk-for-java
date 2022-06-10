// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.implementation.OpenConnectionResponse;

import java.util.concurrent.CompletableFuture;

public class OpenConnectionRntbdRequestRecord extends CompletableFuture<OpenConnectionResponse> {
    private final Uri addressUri;

    public OpenConnectionRntbdRequestRecord(Uri addressUri) {
        this.addressUri = addressUri;
    }

    public Uri getAddressUri() {
        return addressUri;
    }
}
