package com.azure.communication.jobrouter;

import com.azure.core.annotation.ServiceClient;

@ServiceClient(builder = RouterAdministrationClientBuilder.class, isAsync = false)
public class RouterAdministrationClient {

    private RouterAdministrationAsyncClient client;
    RouterAdministrationClient(RouterAdministrationAsyncClient client) {
        this.client = client;
    }
}
