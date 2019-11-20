// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
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
 *
 * <p>
 * The shared access key can be obtained by creating a <i>shared access policy</i> for the Event Hubs namespace or for
 * a specific Event Hub instance. See
 * <a href="https://docs.microsoft.com/en-us/azure/event-hubs/
 * authorize-access-shared-access-signature#shared-access-authorization-policies">Shared access authorization policies
 * </a> for more information.
 * </p>
 *
 * @see <a href="https://docs.microsoft.com/en-us/azure/event-hubs/authorize-access-shared-access-signature">Authorize
 *     access with shared access signature.</a>
 */
@Immutable
public class EventHubSharedKeyCredential implements TokenCredential {
    private static final String SHARED_ACCESS_SIGNATURE_FORMAT = "SharedAccessSignature sr=%s&sig=%s&se=%s&skn=%s";
    private static final String HASH_ALGORITHM = "HMACSHA256";

    private final ClientLogger logger = new ClientLogger(EventHubSharedKeyCredential.class);

    private final String policyName;
    private final Mac hmac;
    private final Duration tokenValidity;

    /**
     * Creates an instance that authorizes using the {@code policyName} and {@code sharedAccessKey}.
     *
     * @param policyName Name of the shared access key policy.
     * @param sharedAccessKey Value of the shared access key.
     * @throws IllegalArgumentException if {@code policyName}, {@code sharedAccessKey} is an empty string. If the
     *     {@code sharedAccessKey} is an invalid value for the hashing algorithm.
     * @throws NullPointerException if {@code policyName} or {@code sharedAccessKey} is null.
     * @throws UnsupportedOperationException If the hashing algorithm cannot be instantiated, which is used to generate
     *     the shared access signatures.
     */
    public EventHubSharedKeyCredential(String policyName, String sharedAccessKey) {
        this(policyName, sharedAccessKey, ClientConstants.TOKEN_VALIDITY);
    }

    /**
     * Creates an instance that authorizes using the {@code policyName} and {@code sharedAccessKey}. The authorization
     * lasts for a period of {@code tokenValidity} before another token must be requested.
     *
     * @param policyName Name of the shared access key policy.
     * @param sharedAccessKey Value of the shared access key.
     * @param tokenValidity The duration for which the shared access signature is valid.
     * @throws IllegalArgumentException if {@code policyName}, {@code sharedAccessKey} is an empty string. Or the
     *     duration of {@code tokenValidity} is zero or a negative value. If the {@code sharedAccessKey} is an invalid
     *     value for the hashing algorithm.
     * @throws NullPointerException if {@code policyName}, {@code sharedAccessKey}, or {@code tokenValidity} is
     *     null.
     * @throws UnsupportedOperationException If the hashing algorithm cannot be instantiated, which is used to generate
     *     the shared access signatures.
     */
    public EventHubSharedKeyCredential(String policyName, String sharedAccessKey, Duration tokenValidity) {

        Objects.requireNonNull(sharedAccessKey, "'sharedAccessKey' cannot be null.");
        this.policyName = Objects.requireNonNull(policyName, "'sharedAccessKey' cannot be null.");
        this.tokenValidity = Objects.requireNonNull(tokenValidity, "'tokenValidity' cannot be null.");

        if (policyName.isEmpty()) {
            throw new IllegalArgumentException("'policyName' cannot be an empty string.");
        } else if (sharedAccessKey.isEmpty()) {
            throw new IllegalArgumentException("'sharedAccessKey' cannot be an empty string.");
        } else if (tokenValidity.isZero() || tokenValidity.isNegative()) {
            throw new IllegalArgumentException("'tokenTimeToLive' has to positive and in the order-of seconds");
        }

        try {
            hmac = Mac.getInstance(HASH_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw logger.logExceptionAsError(new UnsupportedOperationException(
                String.format("Unable to create hashing algorithm '%s'", HASH_ALGORITHM), e));
        }

        final byte[] sasKeyBytes = sharedAccessKey.getBytes(UTF_8);
        final SecretKeySpec finalKey = new SecretKeySpec(sasKeyBytes, HASH_ALGORITHM);
        try {
            hmac.init(finalKey);
        } catch (InvalidKeyException e) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "'sharedAccessKey' is an invalid value for the hashing algorithm.", e));
        }
    }

    /**
     * Retrieves the token, given the audience/resources requested, for use in authorization against an Event Hubs
     * namespace or a specific Event Hub instance.
     *
     * @param request The details of a token request
     * @return A Mono that completes and returns the shared access signature.
     * @throws IllegalArgumentException if {@code scopes} does not contain a single value, which is the token
     *     audience.
     */
    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        if (request.getScopes().size() != 1) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "'scopes' should only contain a single argument that is the token audience or resource name."));
        }

        return Mono.fromCallable(() -> generateSharedAccessSignature(request.getScopes().get(0)));
    }

    private AccessToken generateSharedAccessSignature(final String resource) throws UnsupportedEncodingException {
        if (CoreUtils.isNullOrEmpty(resource)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("resource cannot be empty"));
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
            URLEncoder.encode(policyName, utf8Encoding));

        return new AccessToken(token, expiresOn);
    }
}
