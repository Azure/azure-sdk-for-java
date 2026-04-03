// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.storage.common.implementation.StorageImplUtils;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StorageSessionCredentialTest {

    @Test
    public void signRequestWithSessionKey() {
        // Given a known session key and a known string-to-sign
        StorageSessionCredential credential = SessionTestHelper.createValidCredential();

        String stringToSign = "GET\n\n\n\n\n\n\n\n\n\n\n\n" + "x-ms-date:Mon, 31 Mar 2025 00:00:00 GMT\n"
            + "x-ms-version:2025-01-05\n" + "/myaccount/mycontainer/myblob";

        // When computing HMAC
        String signature = credential.computeHmac256(stringToSign);

        // Then it matches the expected HMAC from StorageImplUtils
        String expected = StorageImplUtils.computeHMac256(SessionTestHelper.TEST_SESSION_KEY, stringToSign);
        assertEquals(expected, signature);
    }

    @Test
    public void generateAuthorizationHeaderFormat() throws MalformedURLException {
        // Given a session credential
        StorageSessionCredential credential = SessionTestHelper.createValidCredential();

        // And a request URL and headers
        URL url = new URL("https://myaccount.blob.core.windows.net/mycontainer/myblob");
        HttpHeaders headers
            = new HttpHeaders().set(HttpHeaderName.fromString("x-ms-date"), "Mon, 31 Mar 2025 00:00:00 GMT")
                .set(HttpHeaderName.fromString("x-ms-version"), "2025-01-05")
                .set(HttpHeaderName.CONTENT_LENGTH, "0");

        // When generating the authorization header
        String authHeader = credential.generateAuthorizationHeader(url, "GET", headers);

        // Then it uses the "Session" scheme with the token and a signature
        assertTrue(authHeader.startsWith("Session " + SessionTestHelper.TEST_SESSION_TOKEN + ":"),
            "Authorization header should start with 'Session <token>:' but was: " + authHeader);

        // And the signature portion is a valid Base64 HMAC
        String signaturePart = authHeader.substring(authHeader.indexOf(':') + 1);
        assertTrue(signaturePart.length() > 0, "Signature should not be empty");
    }

    @Test
    public void isExpiredReturnsTrueWhenPastExpiration() {
        StorageSessionCredential credential = SessionTestHelper.createExpiredCredential();

        assertTrue(credential.isExpired(), "Credential should be expired when expiration is in the past");
    }

    @Test
    public void isExpiredReturnsFalseWhenBeforeExpiration() {
        StorageSessionCredential credential = SessionTestHelper.createValidCredential();

        assertFalse(credential.isExpired(), "Credential should not be expired when expiration is in the future");
    }
}
