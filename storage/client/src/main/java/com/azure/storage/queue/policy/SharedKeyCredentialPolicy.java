package com.azure.storage.queue.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.storage.queue.credentials.SharedKeyCredential;
import reactor.core.publisher.Mono;

public final class SharedKeyCredentialPolicy implements HttpPipelinePolicy {
    private final SharedKeyCredential credential;

    public SharedKeyCredentialPolicy(SharedKeyCredential credential) {
        this.credential = credential;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        String authorizationValue = credential.generateAuthorizationHeader(context.httpRequest().url(),
            context.httpRequest().httpMethod().toString(),
            context.httpRequest().headers().toMap());
        context.httpRequest().withHeader("Authorization", authorizationValue);
        return next.process();
    }
}
