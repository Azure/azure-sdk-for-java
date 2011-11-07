package com.microsoft.azure.services.blob.implementation;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.sun.jersey.core.util.Base64;

/*
 * TODO: Move to some other common package?
 */
public class HmacSHA256Sign {
    private final String accessKey;

    public HmacSHA256Sign(String accessKey) {
        this.accessKey = accessKey;
    }

    public String sign(String stringToSign) {
        try {
            // Encoding the Signature
            // Signature=Base64(HMAC-SHA256(UTF8(StringToSign)))

            Mac hmac = Mac.getInstance("hmacSHA256");
            hmac.init(new SecretKeySpec(Base64.decode(accessKey), "hmacSHA256"));
            byte[] digest = hmac.doFinal(stringToSign.getBytes("UTF8"));
            return new String(Base64.encode(digest));
        }
        catch (Exception e) {
            throw new IllegalArgumentException("accessKey", e);
        }
    }
}
