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
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Date;

public class SasFilter extends ClientFilter {
    private String key;
    private String value;
    private static final String HMAC_SHA256_ALG = "HmacSHA256";
    private final long fiveMinutes = 5 * 60 * 1000; //milliseconds

    public SasFilter(ServiceBusConnectionSettings connectionSettings) {
        this.key = connectionSettings.getSharedAccessKeyName();
        this.value = connectionSettings.getSharedAccessKey();
    }

    @Override
    public ClientResponse handle(ClientRequest cr)
            throws ClientHandlerException {

        String targetUri;
        try {
            targetUri = URLEncoder.encode(cr.getURI().toString().toLowerCase(), "UTF-8").toLowerCase();
        } catch (UnsupportedEncodingException e) {
            throw new ClientHandlerException(e);
        }

        int expiration = Math.round((new Date(System.currentTimeMillis() + fiveMinutes)).getTime() / 1000);
        String signature = valueToSign(targetUri, expiration);

        String hmac;
        try {
            hmac = URLEncoder.encode(calculateHmac(signature), "UTF-8");
        } catch (SignatureException e) {
            throw new ClientHandlerException(e);
        } catch (UnsupportedEncodingException e) {
            throw new ClientHandlerException(e);
        }

        cr.getHeaders().remove("Authorization");
        cr.getHeaders().add("Authorization",
                String.format("SharedAccessSignature sig=%s&se=%d&skn=%s&sr=%s", hmac, expiration, this.key, targetUri));

        return this.getNext().handle(cr);

          /*var targetUri = encodeURIComponent(webResource.uri.toLowerCase()).toLowerCase();

          var expirationDate = Math.round(date.minutesFromNow(5) / 1000);
          var signature = this._generateSignature(targetUri, expirationDate);

          webResource.withHeader(HeaderConstants.AUTHORIZATION,
            util.format('SharedAccessSignature sig=%s&se=%s&skn=%s&sr=%s', signature, expirationDate, this.keyName, targetUri));

          callback(null);*/

        /*Date expiresUtc = new Date(now.getTime() + wrapResponse.getExpiresIn()
                * Timer.ONE_SECOND / 2);*/
    }

    private String valueToSign(String targetUri, int expiration) {
        StringBuilder sb = new StringBuilder();
        if (targetUri != null) {
            sb.append(targetUri);
        }
        sb.append("\n" + expiration);
        return sb.toString();
    }

    private String calculateHmac(String data)
            throws java.security.SignatureException {

        try {

            SecretKeySpec signingKey = new SecretKeySpec(value.getBytes(), HMAC_SHA256_ALG);
            Mac mac = Mac.getInstance(HMAC_SHA256_ALG);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(rawHmac);

            } catch (Exception e) {
                throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
            }
    }
}
