// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;

public class BridgeInternalBenchmark {

    static public AsyncDocumentClient getInternalDocumentClient(CosmosAsyncClient client) {
        return client.getDocClientWrapper();
    }
}
