// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

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
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Authorizes with Azure Service Bus service using a shared access key from either an Service Bus namespace or a specific
 * Service Bus.
 *
 * <p>
 * The shared access key can be obtained by creating a <i>shared access policy</i> for the Service Bus namespace or for
 * a specific Service Bus instance. See
 * <a href="https://docs.microsoft.com/en-us/azure/event-hubs/authorize-access-shared-access-signature#shared-access-authorization-policies">Shared access authorization policies</a> for more information.
 * </p>
 *
 * @see <a href="https://docs.microsoft.com/en-us/azure/event-hubs/authorize-access-shared-access-signature">Authorize
 * access with shared access signature.</a>
 */
@Immutable
public class ServiceBusSharedKeyCredential implements TokenCredential {
    private static final String SHARED_ACCESS_SIGNATURE_FORMAT = "SharedAccessSignature sr=%s&sig=%s&se=%s&skn=%s";
    private static final String HASH_ALGORITHM = "HMACSHA256";
    public static final Duration TOKEN_VALIDITY = Duration.ofMinutes(20);

    private final String sharedAccessPolicy;
    private final SecretKeySpec secretKeySpec;
    private final String sharedAccessSignature;
    private final ClientLogger logger = new ClientLogger(ServiceBusSharedKeyCredential.class);

    /**
     * Creates an instance that authorizes using the {@code sharedAccessPolicy} and {@code secretKeySpec}
     * and {@code sharedAccessSignature}.
     *
     * @param sharedAccessPolicy Name of the shared access policy.
     * @param secretKeySpec Value of the shared access key.
     * @param sharedAccessSignature Value of the shared access signature.
     */
    public ServiceBusSharedKeyCredential(String sharedAccessPolicy, SecretKeySpec secretKeySpec,
                                       String sharedAccessSignature) {
        this.sharedAccessPolicy = sharedAccessPolicy;
        this.secretKeySpec = secretKeySpec;
        this.sharedAccessSignature = sharedAccessSignature;
    }

    /**
     * Retrieves the token, given the audience/resources requested, for use in authorization against an Service Bus
     * namespace or a specific Service Bus instance.
     *
     * @param request The details of a token request
     * @return A Mono that completes and returns the shared access signature.
     * @throws IllegalArgumentException if {@code scopes} does not contain a single value, which is the token
     *                                  audience.
     */
    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.fromCallable(() -> generateSharedAccessSignature(request.getScopes().get(0)));
    }

    private AccessToken generateSharedAccessSignature(final String resource) throws UnsupportedEncodingException {
        if (CoreUtils.isNullOrEmpty(resource)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("resource cannot be empty"));
        }

        if (sharedAccessSignature != null) {
            return new AccessToken(sharedAccessSignature, getExpirationTime(sharedAccessSignature));
        }

        final Mac hmac;
        try {
            hmac = Mac.getInstance(HASH_ALGORITHM);
            hmac.init(secretKeySpec);
        } catch (NoSuchAlgorithmException e) {
            throw logger.logExceptionAsError(new UnsupportedOperationException(
                String.format("Unable to create hashing algorithm '%s'", HASH_ALGORITHM), e));
        } catch (InvalidKeyException e) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "'sharedAccessKey' is an invalid value for the hashing algorithm.", e));
        }

        final String utf8Encoding = UTF_8.name();
        final OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plus(TOKEN_VALIDITY);
        final String expiresOnEpochSeconds = Long.toString(expiresOn.toEpochSecond());
        final String audienceUri = URLEncoder.encode(resource, utf8Encoding);
        final String secretToSign = audienceUri + "\n" + expiresOnEpochSeconds;

        final byte[] signatureBytes = hmac.doFinal(secretToSign.getBytes(utf8Encoding));
        final String signature = Base64.getEncoder().encodeToString(signatureBytes);

        final String token = String.format(Locale.US, SHARED_ACCESS_SIGNATURE_FORMAT,
            audienceUri,
            URLEncoder.encode(signature, utf8Encoding),
            URLEncoder.encode(expiresOnEpochSeconds, utf8Encoding),
            URLEncoder.encode(sharedAccessPolicy, utf8Encoding));
        return new AccessToken(token, expiresOn);
    }

    private OffsetDateTime getExpirationTime(String sharedAccessSignature) {
        String[] parts = sharedAccessSignature.split("&");
        return Arrays.stream(parts)
            .map(part -> part.split("="))
            .filter(pair -> pair.length == 2 && pair[0].equalsIgnoreCase("se"))
            .findFirst()
            .map(pair -> pair[1])
            .map(expirationTimeStr -> {
                try {
                    long epochSeconds = Long.parseLong(expirationTimeStr);
                    return Instant.ofEpochSecond(epochSeconds).atOffset(ZoneOffset.UTC);
                } catch (NumberFormatException exception) {
                    logger.verbose("Invalid expiration time format in the SAS token: {}. Falling back to max "
                        + "expiration time.", expirationTimeStr);
                    return OffsetDateTime.MAX;
                }
            })
            .orElse(OffsetDateTime.MAX);
    }
}
