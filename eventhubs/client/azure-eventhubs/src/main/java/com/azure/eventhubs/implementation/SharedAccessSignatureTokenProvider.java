// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.implementation.util.ImplUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SharedAccessSignatureTokenProvider implements TokenProvider {
    private static final String HASH_ALGORITHM = "HMACSHA256";
    private final String keyName;
    private final Mac hmac;

    public SharedAccessSignatureTokenProvider(final String keyName, final String sharedAccessKey) throws NoSuchAlgorithmException, InvalidKeyException {
        if (ImplUtils.isNullOrEmpty(keyName)) {
            throw new IllegalArgumentException("keyName cannot be empty");
        }

        if (ImplUtils.isNullOrEmpty(sharedAccessKey)) {
            throw new IllegalArgumentException("sharedAccessKey cannot be empty");
        }

        this.keyName = keyName;

        hmac = Mac.getInstance(HASH_ALGORITHM);

        final byte[] sasKeyBytes = sharedAccessKey.getBytes(UTF_8);
        final SecretKeySpec finalKey = new SecretKeySpec(sasKeyBytes, HASH_ALGORITHM);
        hmac.init(finalKey);
    }

    public String getToken(final String resource, final Duration tokenTimeToLive) throws UnsupportedEncodingException {
        return generateSharedAccessSignature(resource, tokenTimeToLive);
    }

    private String generateSharedAccessSignature(final String resource, final Duration tokenTimeToLive) throws UnsupportedEncodingException {
        if (ImplUtils.isNullOrEmpty(resource)) {
            throw new IllegalArgumentException("resource cannot be empty");
        }

        if (tokenTimeToLive.isZero() || tokenTimeToLive.isNegative()) {
            throw new IllegalArgumentException("tokenTimeToLive has to positive and in the order-of seconds");
        }

        final String utf8Encoding = UTF_8.name();
        String expiresOn = Long.toString(Instant.now().getEpochSecond() + tokenTimeToLive.getSeconds());
        String audienceUri = URLEncoder.encode(resource, utf8Encoding);
        String secretToSign = audienceUri + "\n" + expiresOn;

        final byte[] signatureBytes = hmac.doFinal(secretToSign.getBytes(utf8Encoding));
        final String signature = Base64.getEncoder().encodeToString(signatureBytes);

        return String.format(Locale.US, "SharedAccessSignature sr=%s&sig=%s&se=%s&skn=%s",
            audienceUri,
            URLEncoder.encode(signature, utf8Encoding),
            URLEncoder.encode(expiresOn, utf8Encoding),
            URLEncoder.encode(keyName, utf8Encoding));
    }
}
