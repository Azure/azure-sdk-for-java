// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.chat;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static java.time.LocalDateTime.now;

final class WebPubSubTokenGenerator {
    private static final ClientLogger LOGGER = new ClientLogger(WebPubSubTokenGenerator.class);

    static String generateToken(String audience, String userId, List<String> roles, Duration expiresAfter,
        AzureKeyCredential credential) {
        try {
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder().audience(audience)
                .expirationTime(Date.from(now().plus(expiresAfter).atZone(ZoneId.systemDefault()).toInstant()));
            if (!CoreUtils.isNullOrEmpty(userId)) {
                claimsBuilder.subject(userId);
            }
            if (!CoreUtils.isNullOrEmpty(roles)) {
                claimsBuilder.claim("role", roles);
            }

            JWSSigner signer = new MACSigner(credential.getKey().getBytes(StandardCharsets.UTF_8));
            SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsBuilder.build());
            signedJwt.sign(signer);
            return signedJwt.serialize();
        } catch (JOSEException exception) {
            LOGGER.logThrowableAsError(exception);
            return null;
        }
    }

    private WebPubSubTokenGenerator() {
    }
}
