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

import com.microsoft.windowsazure.exception.ServiceException;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.management.timer.Timer;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

public class SasFilter extends ClientFilter {
	private String key;
	private String value;
	private static final String HMAC_SHA256_ALG = "HmacSHA256";
	
    public SasFilter(String key, String value) {
        this.key = key;
        this.value = value;
    }

	@Override
	public ClientResponse handle(ClientRequest arg0)
			throws ClientHandlerException {
		
		// TODO Auto-generated method stub
		  /*var targetUri = encodeURIComponent(webResource.uri.toLowerCase()).toLowerCase();

		  var expirationDate = Math.round(date.minutesFromNow(5) / 1000);
		  var signature = this._generateSignature(targetUri, expirationDate);

		  webResource.withHeader(HeaderConstants.AUTHORIZATION,
		    util.format('SharedAccessSignature sig=%s&se=%s&skn=%s&sr=%s', signature, expirationDate, this.keyName, targetUri));

		  callback(null);*/
		
        /*Date expiresUtc = new Date(now.getTime() + wrapResponse.getExpiresIn()
                * Timer.ONE_SECOND / 2);*/
		
		return null;
	}

	private String valueToSign(String targetUri, String expiration) {
		StringBuilder sb = new StringBuilder();
		if (targetUri != null) {
			sb.append(targetUri);
		}
		sb.append("\n");
		if (expiration != null) {
			sb.append(expiration);
		}
		
		return sb.toString();
	}
	
	private String calculateHmac(String data)
			throws java.security.SignatureException
	{
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
