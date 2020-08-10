// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.messaging.eventgrid;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.CoreUtils;

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
public final class EventGridSharedAccessSignatureCredential {

    private String accessToken;

    public static String createSharedAccessSignature(String endpoint, OffsetDateTime expiration,
                                                     AzureKeyCredential key) {
        try {
            String resKey = "r";
            String expKey = "e";
            String signKey = "s";

            Charset charset = StandardCharsets.UTF_8;
            String encodedResource = URLEncoder.encode(endpoint, charset.name());
            String encodedExpiration = URLEncoder.encode(expiration.atZoneSameInstant(ZoneOffset.UTC).format(
                DateTimeFormatter.ofPattern("M/d/yyyy h:m:s a")),
                charset.name());

            String unsignedSas = String.format("%s=%s&%s=%s", resKey, encodedResource, expKey, encodedExpiration);

            Mac hmac = Mac.getInstance("hmacSHA256");
            hmac.init(new SecretKeySpec(Base64.getDecoder().decode(key.getKey()), "hmacSHA256"));
            String signature = new String(Base64.getEncoder().encode(
                hmac.doFinal(unsignedSas.getBytes(charset))),
                charset);

            String encodedSignature = URLEncoder.encode(signature, charset.name());

            return String.format("%s&%s=%s", unsignedSas, signKey, encodedSignature);

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create an instance of this object to authenticate calls to the EventGrid service.
     * @param accessToken the shared access signature to use.
     */
    public EventGridSharedAccessSignatureCredential(String accessToken) {
        if (CoreUtils.isNullOrEmpty(accessToken)) {
            throw new IllegalArgumentException("the access token cannot be null or empty");
        }
        this.accessToken = accessToken;
    }

    /**
     * Get the token string to authenticate service calls
     * @return the SharedAccessSignature token as a string
     */
    public String getSignature() {
        return accessToken;
    }


    /**
     * Change the shared access signature token to a new one.
     * @param accessSignature the shared access signature token to use.
     */
    public void update(String accessSignature) {
        this.accessToken = accessSignature;
    }
}
