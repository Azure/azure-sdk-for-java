// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.signalr;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Date;

import static java.time.LocalDateTime.now;

/**
 * An {@link HttpPipelinePolicy} for authenticating against the SignalR service. Used in the default HTTP pipeline
 * when built using the {@link SignalRClientBuilder}, but available here in case an HTTP pipeline is built outside of
 * the {@link SignalRClientBuilder}.
 *
 * @see SignalRClientBuilder
 */
public final class SignalRAuthenticationPolicy implements HttpPipelinePolicy {
    private final ClientLogger logger = new ClientLogger(SignalRAuthenticationPolicy.class);

    private final AzureKeyCredential credential;

    /**
     * Creates a new instance of the SignalRAuthenticationPolicy, where it will make use of the provided
     * {@link AzureKeyCredential} whenever a HTTP request is made to apply the appropriate modifications to the HTTP
     * request to gain access to the SignalR service.
     *
     * <p>Note that whilst the credential passed into the constructor is immutable within this policy, the key contained
     * within the credential is not, and as such can be updated by calling {@link AzureKeyCredential#update(String)} as
     * appropriate.</p>
     *
     * @param credential The {@link AzureKeyCredential} that will be used for all outgoing HTTP requests to the
     * SignalR service.
     */
    public SignalRAuthenticationPolicy(final AzureKeyCredential credential) {
        this.credential = credential;
    }

    @Override
    public Mono<HttpResponse> process(final HttpPipelineCallContext context, final HttpPipelineNextPolicy next) {
        try {
            final JWTClaimsSet claims = new JWTClaimsSet.Builder()
                  .audience(context.getHttpRequest().getUrl().toString())
                  .expirationTime(Date.from(now().plusMinutes(10).atZone(ZoneId.systemDefault()).toInstant()))
                  .build();

            final JWSSigner signer = new MACSigner(credential.getKey().getBytes(StandardCharsets.UTF_8));
            final SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            signedJWT.sign(signer);
            final String token = signedJWT.serialize();

            context.getHttpRequest().setHeader("Authorization", "Bearer " + token);
        } catch (final JOSEException e) {
            return Mono.error(logger.logThrowableAsError(e));
        }

        return next.process();
    }
}
