// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.webpubsub.models.GetAuthenticationTokenOptions;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Date;

import static java.time.LocalDateTime.now;

/**
 * An {@link HttpPipelinePolicy} for authenticating against the Azure Web Pub Sub service. Used in the default HTTP
 * pipeline when built using the {@link WebPubSubClientBuilder}, but available here in case an HTTP pipeline is built
 * outside of the {@link WebPubSubClientBuilder}.
 *
 * @see WebPubSubClientBuilder
 */
public final class WebPubSubAuthenticationPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(WebPubSubAuthenticationPolicy.class);

    private final AzureKeyCredential credential;

    /**
     * Creates a new instance of the WebPubSubAuthenticationPolicy, where it will make use of the provided
     * {@link AzureKeyCredential} whenever a HTTP request is made to apply the appropriate modifications to the HTTP
     * request to gain access to the Azure Web Pub Sub service.
     *
     * <p>Note that whilst the credential passed into the constructor is immutable within this policy, the key contained
     * within the credential is not, and as such can be updated by calling {@link AzureKeyCredential#update(String)} as
     * appropriate.</p>
     *
     * @param credential The {@link AzureKeyCredential} that will be used for all outgoing HTTP requests to the
     *      Azure Web Pub Sub service.
     */
    public WebPubSubAuthenticationPolicy(final AzureKeyCredential credential) {
        this.credential = credential;
    }

    AzureKeyCredential getCredential() {
        return credential;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<HttpResponse> process(final HttpPipelineCallContext context, final HttpPipelineNextPolicy next) {
        return Mono.fromRunnable(() -> {
            final String audienceUrl = context.getHttpRequest().getUrl().toString();
            final String token = getAuthenticationToken(audienceUrl, null, credential);

            if (token != null) {
                context.getHttpRequest().setHeader("Authorization", "Bearer " + token);
            }
        }).then(next.process());
    }

    static String getAuthenticationToken(final String audienceUrl,
                                         GetAuthenticationTokenOptions options,
                                         final AzureKeyCredential credential) {
        try {
            Duration expiresAfter = Duration.ofHours(1);
            final JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .audience(audienceUrl);

            if (options != null) {
                expiresAfter = options.getExpiresAfter() == null ? expiresAfter : options.getExpiresAfter();
                String userId = options.getUserId();
                if (!CoreUtils.isNullOrEmpty(options.getRoles())) {
                    claimsBuilder.claim("role", options.getRoles());
                }
                if (!CoreUtils.isNullOrEmpty(userId)) {
                    claimsBuilder.subject(userId);
                }
            }

            claimsBuilder
                .expirationTime(Date.from(now().plus(expiresAfter).atZone(ZoneId.systemDefault()).toInstant()));

            final JWTClaimsSet claims = claimsBuilder.build();

            final JWSSigner signer = new MACSigner(credential.getKey().getBytes(StandardCharsets.UTF_8));
            final SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (final JOSEException e) {
            LOGGER.logThrowableAsError(e);
            return null;
        }
    }
}
