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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public final class SignalRAuthenticationPolicy implements HttpPipelinePolicy {
    private final ClientLogger logger = new ClientLogger(SignalRAuthenticationPolicy.class);

    private final AzureKeyCredential credential;

    public SignalRAuthenticationPolicy(final AzureKeyCredential credential) {
        this.credential = credential;
    }

    @Override
    public Mono<HttpResponse> process(final HttpPipelineCallContext context, final HttpPipelineNextPolicy next) {
        try {
            final JWTClaimsSet claims = new JWTClaimsSet.Builder()
                  .audience(context.getHttpRequest().getUrl().toString())
                  .expirationTime(
                      Date.from(LocalDateTime.now().plusMinutes(10).atZone(ZoneId.systemDefault()).toInstant()))
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
