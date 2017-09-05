package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpClient;

public class SendRequestPolicyFactory implements RequestPolicy.Factory {
    private final HttpClient client;

    public SendRequestPolicyFactory(HttpClient client) {
        this.client = client;
    }

    @Override
    public RequestPolicy create(RequestPolicy next) {
        return client;
    }
}
