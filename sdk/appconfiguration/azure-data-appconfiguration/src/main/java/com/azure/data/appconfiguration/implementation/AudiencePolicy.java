// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration.implementation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.data.appconfiguration.models.ConfigurationAudience;

import reactor.core.publisher.Mono;

/**
 * HTTP pipeline policy that handles Azure Active Directory audience-related authentication errors.
 * This policy intercepts HTTP responses and provides more meaningful error messages when
 * audience configuration issues occur during authentication.
 */
public class AudiencePolicy implements HttpPipelinePolicy {

    private static final String NO_AUDIENCE_ERROR_MESSAGE
        = "Unable to authenticate to Azure App Configuration. No authentication token audience was provided. "
            + "Please set an Audience in your ConfigurationClientBuilder for the target cloud. "
            + "For details on how to configure the authentication token audience visit "
            + "https://aka.ms/appconfig/client-token-audience.";

    private static final String INCORRECT_AUDIENCE_ERROR_MESSAGE
        = "Unable to authenticate to Azure App Configuration. An incorrect token audience was provided. "
            + "Please set the Audience in your ConfigurationClientBuilder to the appropriate audience for this cloud. "
            + "For details on how to configure the authentication token audience visit "
            + "https://aka.ms/appconfig/client-token-audience.";

    private static final String AAD_AUDIENCE_ERROR_CODE = "AADSTS500011";

    private final ConfigurationAudience audience;

    /**
     * Creates a new instance of AudiencePolicy.
     *
     * @param audience The configuration audience to use for validation. May be null.
     */
    public AudiencePolicy(ConfigurationAudience audience) {
        this.audience = audience;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.process().onErrorMap(HttpResponseException.class, this::handleAudienceException);
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        try {
            return next.processSync();
        } catch (HttpResponseException ex) {
            throw handleAudienceException(ex);
        }
    }

    /**
     * Handles audience-related authentication exceptions by providing more meaningful error messages.
     *
     * @param ex The original HttpResponseException
     * @return A new HttpResponseException with improved error message if audience-related, otherwise the original exception
     */
    private HttpResponseException handleAudienceException(HttpResponseException ex) {
        if (ex.getMessage() != null && ex.getMessage().contains(AAD_AUDIENCE_ERROR_CODE)) {
            String message = audience == null ? NO_AUDIENCE_ERROR_MESSAGE : INCORRECT_AUDIENCE_ERROR_MESSAGE;
            return new HttpResponseException(message, ex.getResponse(), ex);
        }
        return ex;
    }
}
