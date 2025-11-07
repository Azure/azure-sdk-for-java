package com.azure.data.appconfiguration.implementation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.data.appconfiguration.models.ConfigurationAudience;

import reactor.core.publisher.Mono;

public class AudiencePolicy implements HttpPipelinePolicy {

    private static final String NO_AUDIENCE
        = "No audience was provided, one should be configured to connect to this cloud.";

    private static final String INCORRECT_AUDIENCE
        = "The incorrect audience was provided. Please update to connect to this cloud.";

    private final ConfigurationAudience audience;

    public AudiencePolicy(ConfigurationAudience audience) {
        this.audience = audience;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        try {
            return next.clone().process();
        } catch (HttpResponseException ex) {
            throw handleAudienceException(ex);
        }
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        try {
            return next.clone().processSync();
        } catch (HttpResponseException ex) {
            throw handleAudienceException(ex);
        }
    }

    private HttpResponseException handleAudienceException(HttpResponseException ex) {
        if (ex.getMessage().contains("AADSTS500011")) {
            String message = INCORRECT_AUDIENCE;
            if (audience == null) {
                message = NO_AUDIENCE;
            }

            return new HttpResponseException(message, ex.getResponse(), ex);
        }
        return ex;
    }
}
