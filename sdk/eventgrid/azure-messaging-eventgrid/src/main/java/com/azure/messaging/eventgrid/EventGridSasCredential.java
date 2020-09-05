// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.logging.ClientLogger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * A way to use a generated shared access signature as a credential to publish events to a topic through a client.
 */
public final class EventGridSasCredential {

    private String sas;

    private static final ClientLogger logger = new ClientLogger(EventGridSasCredential.class);

    /**
     * Generate a shared access signature to provide time-limited authentication for requests to the Event Grid
     * service.
     * @param endpoint       the endpoint of the Event Grid topic or domain.
     * @param expirationTime the time in which the signature should expire, no longer providing authentication.
     * @param keyCredential  the access key obtained from the Event Grid topic or domain.
     *
     * @return the shared access signature string which can be used to construct an instance of
     * {@link EventGridSasCredential}.
     */
    public static String createSas(String endpoint, OffsetDateTime expirationTime,
                                   AzureKeyCredential keyCredential) {
        try {
            String resKey = "r";
            String expKey = "e";
            String signKey = "s";

            Charset charset = StandardCharsets.UTF_8;
            String encodedResource = URLEncoder.encode(endpoint, charset.name());
            String encodedExpiration = URLEncoder.encode(expirationTime.atZoneSameInstant(ZoneOffset.UTC).format(
                DateTimeFormatter.ofPattern("M/d/yyyy h:m:s a")),
                charset.name());

            String unsignedSas = String.format("%s=%s&%s=%s", resKey, encodedResource, expKey, encodedExpiration);

            Mac hmac = Mac.getInstance("hmacSHA256");
            hmac.init(new SecretKeySpec(Base64.getDecoder().decode(keyCredential.getKey()), "hmacSHA256"));
            String signature = new String(Base64.getEncoder().encode(
                hmac.doFinal(unsignedSas.getBytes(charset))),
                charset);

            String encodedSignature = URLEncoder.encode(signature, charset.name());

            return String.format("%s&%s=%s", unsignedSas, signKey, encodedSignature);

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
    }

    /**
     * Create an instance of this object to authenticate calls to the EventGrid service.
     * @param sas the shared access signature to use.
     */
    public EventGridSasCredential(String sas) {
        if (sas == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("the access signature cannot be null"));
        }
        if (sas.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("the access signature cannot be empty"));
        }
        this.sas = sas;
    }

    /**
     * Get the token string to authenticate service calls
     * @return the Shared Access Signature as a string
     */
    public String getSas() {
        return sas;
    }


    /**
     * Change the shared access signature token to a new one.
     * @param sas the shared access signature token to use.
     */
    public void update(String sas) {
        this.sas = sas;
    }
}
