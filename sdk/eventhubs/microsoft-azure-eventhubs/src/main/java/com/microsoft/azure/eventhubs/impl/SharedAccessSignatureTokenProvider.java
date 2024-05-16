// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.microsoft.azure.eventhubs.ITokenProvider;
import com.microsoft.azure.eventhubs.SecurityToken;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SharedAccessSignatureTokenProvider implements ITokenProvider {
    final String keyName;
    final String sharedAccessKey;
    final String sharedAccessSignature;

    SharedAccessSignatureTokenProvider(
            final String keyName,
            final String sharedAccessKey) {
        this.keyName = keyName;
        this.sharedAccessKey = sharedAccessKey;
        this.sharedAccessSignature = null;
    }

    SharedAccessSignatureTokenProvider(final String sharedAccessSignature) {
        this.keyName = null;
        this.sharedAccessKey = null;
        this.sharedAccessSignature = sharedAccessSignature;
    }

    public static String generateSharedAccessSignature(
            final String keyName,
            final String sharedAccessKey,
            final String resource,
            final Duration tokenTimeToLive)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        if (StringUtil.isNullOrWhiteSpace(keyName)) {
            throw new IllegalArgumentException("keyName cannot be empty");
        }

        if (StringUtil.isNullOrWhiteSpace(sharedAccessKey)) {
            throw new IllegalArgumentException("sharedAccessKey cannot be empty");
        }

        if (StringUtil.isNullOrWhiteSpace(resource)) {
            throw new IllegalArgumentException("resource cannot be empty");
        }

        if (tokenTimeToLive.isZero() || tokenTimeToLive.isNegative()) {
            throw new IllegalArgumentException("tokenTimeToLive has to positive and in the order-of seconds");
        }

        final String utf8Encoding = UTF_8.name();
        String expiresOn = Long.toString(Instant.now().getEpochSecond() + tokenTimeToLive.getSeconds());
        String audienceUri = URLEncoder.encode(resource, utf8Encoding);
        String secretToSign = audienceUri + "\n" + expiresOn;

        final String hashAlgorithm = "HMACSHA256";
        Mac hmac = Mac.getInstance(hashAlgorithm);
        byte[] sasKeyBytes = sharedAccessKey.getBytes(utf8Encoding);
        SecretKeySpec finalKey = new SecretKeySpec(sasKeyBytes, hashAlgorithm);
        hmac.init(finalKey);
        byte[] signatureBytes = hmac.doFinal(secretToSign.getBytes(utf8Encoding));
        String signature = Base64.getEncoder().encodeToString(signatureBytes);

        return String.format(Locale.US, "SharedAccessSignature sr=%s&sig=%s&se=%s&skn=%s",
                audienceUri,
                URLEncoder.encode(signature, utf8Encoding),
                URLEncoder.encode(expiresOn, utf8Encoding),
                URLEncoder.encode(keyName, utf8Encoding));
    }

    public CompletableFuture<SecurityToken> getToken(final String resource, final Duration tokenTimeToLive) {
        final CompletableFuture<SecurityToken> result = new CompletableFuture<>();

        if (this.sharedAccessSignature == null) {
            try {
                // Generation is nonblocking so there is no need to run it async.
                final String token = generateSharedAccessSignature(this.keyName, this.sharedAccessKey, resource, ClientConstants.TOKEN_VALIDITY);
                result.complete(new SecurityToken(token, Date.from(Instant.now().plus(ClientConstants.TOKEN_VALIDITY)), resource, ClientConstants.SAS_TOKEN_TYPE));
            } catch (NoSuchAlgorithmException | IOException | InvalidKeyException e) {
                result.completeExceptionally(e);
            }
        } else {
            result.complete(new SecurityToken(this.sharedAccessSignature, Date.from(Instant.now().plus(ClientConstants.TOKEN_VALIDITY)), resource, ClientConstants.SAS_TOKEN_TYPE));
        }

        return result;
    }
}
