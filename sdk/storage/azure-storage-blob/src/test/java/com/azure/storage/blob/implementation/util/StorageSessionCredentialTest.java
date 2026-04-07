// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.StorageImplUtils;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

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
    public void generateAuthorizationHeaderUsesIpStyleRequestUrl() throws MalformedURLException {
        StorageSessionCredential credential = new StorageSessionCredential(SessionTestHelper.TEST_SESSION_TOKEN,
            SessionTestHelper.TEST_SESSION_KEY, OffsetDateTime.now().plusHours(1), SessionTestHelper.TEST_ACCOUNT_NAME);

        URL url = new URL("http://127.0.0.1:10000/myaccount/mycontainer/myblob");
        HttpHeaders headers
            = new HttpHeaders().set(HttpHeaderName.fromString("x-ms-date"), "Mon, 31 Mar 2025 00:00:00 GMT")
                .set(HttpHeaderName.fromString("x-ms-version"), "2025-01-05")
                .set(HttpHeaderName.CONTENT_LENGTH, "0");

        String authHeader = credential.generateAuthorizationHeader(url, "GET", headers);

        // This matches the expected string-to-sign format for an IP-style URL, wherein the first
        // accoun
        String stringToSign = "GET\n\n\n\n\n\n\n\n\n\n\n\n" + "x-ms-date:Mon, 31 Mar 2025 00:00:00 GMT\n"
            + "x-ms-version:2025-01-05\n" + "/myaccount/myaccount/mycontainer/myblob";
        String expectedSignature = credential.computeHmac256(stringToSign);

        assertEquals("Session " + SessionTestHelper.TEST_SESSION_TOKEN + ":" + expectedSignature, authHeader);
    }

    @Test
    public void generateAuthorizationHeaderUsesExplicitAccountNameForCustomDomainUrl() throws MalformedURLException {
        StorageSessionCredential credential = SessionTestHelper.createCredential(OffsetDateTime.now().plusHours(1),
            SessionTestHelper.TEST_ACCOUNT_NAME);

        URL url = new URL("https://cdn.contoso.com/mycontainer/myblob");
        HttpHeaders headers
            = new HttpHeaders().set(HttpHeaderName.fromString("x-ms-date"), "Mon, 31 Mar 2025 00:00:00 GMT")
                .set(HttpHeaderName.fromString("x-ms-version"), "2025-01-05")
                .set(HttpHeaderName.CONTENT_LENGTH, "0");

        String authHeader = credential.generateAuthorizationHeader(url, "GET", headers);

        String stringToSign = "GET\n\n\n\n\n\n\n\n\n\n\n\n" + "x-ms-date:Mon, 31 Mar 2025 00:00:00 GMT\n"
            + "x-ms-version:2025-01-05\n" + "/myaccount/mycontainer/myblob";
        String expectedSignature = credential.computeHmac256(stringToSign);

        assertEquals("Session " + SessionTestHelper.TEST_SESSION_TOKEN + ":" + expectedSignature, authHeader);
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

    @Test
    public void sessionAndSharedKeyProduceSameSignatureForIpStyleUrl() throws MalformedURLException {
        String accountName = SessionTestHelper.TEST_ACCOUNT_NAME;
        String accountKey = SessionTestHelper.TEST_SESSION_KEY;

        StorageSessionCredential sessionCred
            = new StorageSessionCredential("ignored-token", accountKey, OffsetDateTime.now().plusHours(1), accountName);
        StorageSharedKeyCredential sharedKeyCred = new StorageSharedKeyCredential(accountName, accountKey);

        URL url = new URL("http://127.0.0.1:10000/myaccount/mycontainer/myblob");
        HttpHeaders headers
            = new HttpHeaders().set(HttpHeaderName.fromString("x-ms-date"), "Mon, 31 Mar 2025 00:00:00 GMT")
                .set(HttpHeaderName.fromString("x-ms-version"), "2025-01-05")
                .set(HttpHeaderName.CONTENT_LENGTH, "0");

        // Extract just the signature portion from each — the prefix differs (Session vs SharedKey)
        String sessionAuth = sessionCred.generateAuthorizationHeader(url, "GET", headers);
        String sessionSignature = sessionAuth.substring(sessionAuth.indexOf(':') + 1);

        Map<String, String> headerMap = headers.stream().collect(Collectors.toMap(h -> h.getName(), h -> h.getValue()));
        String sharedKeyAuth = sharedKeyCred.generateAuthorizationHeader(url, "GET", headerMap);
        String sharedKeySignature = sharedKeyAuth.substring(sharedKeyAuth.indexOf(':') + 1);

        assertEquals(sharedKeySignature, sessionSignature,
            "Session and SharedKey should produce identical HMAC signatures for IP-style URLs");
    }
}
