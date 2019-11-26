package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;

public class BridgeInternalBenchmark {

    static public AsyncDocumentClient getOldClient(CosmosAsyncClient client) {
        return client.getDocClientWrapper();
    }
}
