package com.azure.data.tables;


import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

public final class TablesSharedKeyCredentialPolicy  implements HttpPipelinePolicy{

    private final TablesSharedKeyCredential credential;

    public TablesSharedKeyCredentialPolicy(TablesSharedKeyCredential credential) {
        this.credential = credential;
    }

    public TablesSharedKeyCredential sharedKeyCredential() {
        return this.credential;
    }

    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        String authorizationValue = this.credential.generateAuthorizationHeader(context.getHttpRequest().getUrl(), context.getHttpRequest().getHttpMethod().toString(), context.getHttpRequest().getHeaders().toMap());
        context.getHttpRequest().setHeader("Authorization", authorizationValue);
        return next.process();
    }
}
