// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.credentials.AccessToken;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.implementation.annotation.Immutable;
import com.azure.core.implementation.util.ImplUtils;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Authorizes with Azure Event Hubs service using a shared access key from either an Event Hubs namespace or a specific
 * Event Hub.
 */
@Immutable
public class EventHubSharedAccessKeyCredential implements TokenCredential {
    private static final String SHARED_ACCESS_SIGNATURE_FORMAT = "SharedAccessSignature sr=%s&sig=%s&se=%s&skn=%s";
    private static final String HASH_ALGORITHM = "HMACSHA256";

    private final String keyName;
    private final Mac hmac;
    private final Duration tokenValidity;

    /**
     * Creates an instance that authorizes using the {@code keyName} and {@code sharedAccessKey}. The authorization
     * lasts for a period of {@code tokenValidity} before another token must be requested.
     *
     * @param keyName Name of the shared access key policy.
     * @param sharedAccessKey Value of the shared access key.
     * @param tokenValidity The duration for which the shared access signature is valid.
     * @throws IllegalArgumentException if {@code keyName}, {@code sharedAccessKey} is null or empty. Or the duration of
     *         {@code tokenValidity} is zero or a negative value.
     * @throws NoSuchAlgorithmException If the hashing algorithm cannot be instantiated, which is used to generate the
     *         shared access signatures.
     * @throws InvalidKeyException If the {@code sharedAccessKey} is an invalid value for the hashing algorithm.
     * @throws NullPointerException if {@code tokenValidity} is null.
     */
    public EventHubSharedAccessKeyCredential(String keyName, String sharedAccessKey, Duration tokenValidity)
        throws NoSuchAlgorithmException, InvalidKeyException {

        if (ImplUtils.isNullOrEmpty(keyName)) {
            throw new IllegalArgumentException("keyName cannot be null or empty");
        }
        if (ImplUtils.isNullOrEmpty(sharedAccessKey)) {
            throw new IllegalArgumentException("sharedAccessKey cannot be null or empty.");
        }

        Objects.requireNonNull(tokenValidity);
        if (tokenValidity.isZero() || tokenValidity.isNegative()) {
            throw new IllegalArgumentException("tokenTimeToLive has to positive and in the order-of seconds");
        }

        this.keyName = keyName;
        this.tokenValidity = tokenValidity;

        hmac = Mac.getInstance(HASH_ALGORITHM);

        final byte[] sasKeyBytes = sharedAccessKey.getBytes(UTF_8);
        final SecretKeySpec finalKey = new SecretKeySpec(sasKeyBytes, HASH_ALGORITHM);
        hmac.init(finalKey);
    }

    /**
     * Retrieves the token, given the audience/resources requested, for use in authorization against an Event Hubs
     * namespace or a specific Event Hub instance.
     *
     * @param scopes The name of the resource or token audience to obtain a token for.
     * @return A Mono that completes and returns the shared access signature.
     * @throws IllegalArgumentException if {@code scopes} does not contain a single value, which is the token audience.
     */
    @Override
    public Mono<AccessToken> getToken(String... scopes) {
        if (scopes.length != 1) {
            throw new IllegalArgumentException("'scopes' should only contain a single argument that is the token audience or resource name.");
        }

        return Mono.fromCallable(() -> generateSharedAccessSignature(scopes[0]));
    }

    private AccessToken generateSharedAccessSignature(final String resource) throws UnsupportedEncodingException {
        if (ImplUtils.isNullOrEmpty(resource)) {
            throw new IllegalArgumentException("resource cannot be empty");
        }

        final String utf8Encoding = UTF_8.name();
        final OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plus(tokenValidity);
        final String expiresOnEpochSeconds = Long.toString(expiresOn.toEpochSecond());
        final String audienceUri = URLEncoder.encode(resource, utf8Encoding);
        final String secretToSign = audienceUri + "\n" + expiresOnEpochSeconds;

        final byte[] signatureBytes = hmac.doFinal(secretToSign.getBytes(utf8Encoding));
        final String signature = Base64.getEncoder().encodeToString(signatureBytes);

        final String token = String.format(Locale.US, SHARED_ACCESS_SIGNATURE_FORMAT,
            audienceUri,
            URLEncoder.encode(signature, utf8Encoding),
            URLEncoder.encode(expiresOnEpochSeconds, utf8Encoding),
            URLEncoder.encode(keyName, utf8Encoding));

        return new AccessToken(token, expiresOn);
    }
}
