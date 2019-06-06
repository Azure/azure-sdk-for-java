package com.azure.storage.queue.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.storage.queue.models.SharedKeyCredential;
import reactor.core.publisher.Mono;

import java.net.URL;

public final class SharedKeyCredentialPolicy implements HttpPipelinePolicy {
    private final SharedKeyCredential credential;

    public SharedKeyCredentialPolicy(SharedKeyCredential credential) {
        this.credential = credential;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        URL requestURL = context.httpRequest().url();
        return next.process();
    }
}
