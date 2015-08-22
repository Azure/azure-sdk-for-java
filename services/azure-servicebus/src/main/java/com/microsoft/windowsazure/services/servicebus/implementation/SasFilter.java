/**
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.servicebus.implementation;

import com.sun.jersey.api.client.ClientHandlerException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SignatureException;

import java.util.Date;

public class SasFilter extends AuthorizationFilter {
    private String key;
    private String value;
    private static final String HMAC_SHA256_ALG = "HmacSHA256";
    private final long fiveMinutes = 5 * 60 * 1000; //milliseconds

    public SasFilter(ServiceBusConnectionSettings connectionSettings) {
        this.key = connectionSettings.getSharedAccessKeyName();
        this.value = connectionSettings.getSharedAccessKey();
    }

    @Override
    protected String createAuthorization(String targetUri) {
        try {
            targetUri = URLEncoder.encode(targetUri.toLowerCase(), "UTF-8").toLowerCase();
        } catch (UnsupportedEncodingException e) {
            throw new ClientHandlerException(e);
        }

        int expiration = Math.round((new Date(System.currentTimeMillis() + fiveMinutes)).getTime() / 1000);
        String signature = valueToSign(targetUri, expiration);

        String hmac;
        try {
            hmac = URLEncoder.encode(calculateHmac(signature), "UTF-8");
            return String.format("SharedAccessSignature sig=%s&se=%d&skn=%s&sr=%s",
                    hmac, expiration, this.key, targetUri);
        } catch (SignatureException e) {
            throw new ClientHandlerException(e);
        } catch (UnsupportedEncodingException e) {
            throw new ClientHandlerException(e);
        }
    }

    private String valueToSign(String targetUri, int expiration) {
        StringBuilder sb = new StringBuilder();
        if (targetUri != null) {
            sb.append(targetUri);
        }
        sb.append("\n").append(expiration);
        return sb.toString();
    }

    private String calculateHmac(String data)
            throws java.security.SignatureException {

        try {

            SecretKeySpec signingKey = new SecretKeySpec(value.getBytes(), HMAC_SHA256_ALG);
            Mac mac = Mac.getInstance(HMAC_SHA256_ALG);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes());
            return DatatypeConverter.printBase64Binary(rawHmac);

            } catch (Exception e) {
                throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
            }
    }
}
