// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StorageSessionCredentialTest {

    @Test
    public void signRequestUsesSessionScheme() throws MalformedURLException {
        StorageSessionCredential credential = SessionTestHelper.createValidCredential();
        HttpRequest request
            = new HttpRequest(HttpMethod.GET, new URL("https://myaccount.blob.core.windows.net/mycontainer/myblob"));

        credential.signRequest(request);

        String authHeader = request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        assertNotNull(authHeader);
        assertTrue(authHeader.startsWith("Session " + SessionTestHelper.TEST_SESSION_TOKEN + ":"),
            "Authorization header should start with 'Session <token>:' but was: " + authHeader);
        String signaturePart = authHeader.substring(authHeader.indexOf(':') + 1);
        assertFalse(signaturePart.isEmpty(), "Signature should not be empty");
    }

    @Test
    public void signRequestSetsXmsDateHeader() throws MalformedURLException {
        StorageSessionCredential credential = SessionTestHelper.createValidCredential();
        HttpRequest request
            = new HttpRequest(HttpMethod.GET, new URL("https://myaccount.blob.core.windows.net/mycontainer/myblob"));

        assertNull(request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-date")));

        credential.signRequest(request);

        assertNotNull(request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-date")),
            "signRequest must set x-ms-date so the signed value matches what is sent on the wire");
    }

    // Regression guard for the URL-decode fix in StorageSessionCredential.canonicalizedResource:
    // verifies Session and SharedKey produce the same HMAC for a well-formed GET with an
    // encoded query string (e.g. snapshot=...%3A...).
    //
    // Scope is intentionally narrow. Session and SharedKey legitimately diverge on:
    //   - missing Content-Length (SharedKey emits literal "null" via String.join; Session emits "").
    // Content-Length is pinned to a realistic non-zero value to bypass that quirk. Equivalence for
    // Content-Length: 0 (which the server normalizes to "") is covered separately.
    @Test
    public void canonicalizationMatchesSharedKeyForEncodedQuery() throws MalformedURLException {
        StorageSessionCredential sessionCred = SessionTestHelper.createValidCredential();
        StorageSharedKeyCredential sharedKeyCred
            = new StorageSharedKeyCredential(SessionTestHelper.TEST_ACCOUNT_NAME, SessionTestHelper.TEST_SESSION_KEY);

        HttpRequest request = new HttpRequest(HttpMethod.GET,
            new URL("https://myaccount.blob.core.windows.net/mycontainer/myblob?snapshot="
                + "2025-03-31T00%3A00%3A00.0000000Z"));
        request.getHeaders()
            .set(HttpHeaderName.fromString("x-ms-version"), BlobServiceVersion.getLatest().getVersion())
            .set(HttpHeaderName.fromString("x-ms-client-request-id"), "11111111-2222-3333-4444-555555555555")
            .set(HttpHeaderName.RANGE, "bytes=0-1023")
            .set(HttpHeaderName.CONTENT_LENGTH, "1024");

        sessionCred.signRequest(request);

        String sessionAuth = request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        String sessionSignature = sessionAuth.substring(sessionAuth.indexOf(':') + 1);

        HttpHeaders headersForSharedKey = request.getHeaders();
        headersForSharedKey.remove(HttpHeaderName.AUTHORIZATION);
        String sharedKeyAuth
            = sharedKeyCred.generateAuthorizationHeader(request.getUrl(), "GET", headersForSharedKey, false);
        String sharedKeySignature = sharedKeyAuth.substring(sharedKeyAuth.indexOf(':') + 1);

        assertEquals(sharedKeySignature, sessionSignature,
            "Session HMAC must match Shared Key HMAC for the same URL/method/headers");
    }

    @Test
    public void isExpiredReturnsTrueWhenPastExpiration() {
        assertTrue(SessionTestHelper.createExpiredCredential().isExpired(),
            "Credential should be expired when expiration is in the past");
    }

    @Test
    public void isExpiredReturnsFalseWhenBeforeExpiration() {
        assertFalse(SessionTestHelper.createValidCredential().isExpired(),
            "Credential should not be expired when expiration is in the future");
    }

    @Test
    public void getExpirationDefaultsWhenConstructedWithNull() {
        OffsetDateTime before = OffsetDateTime.now();
        StorageSessionCredential credential = new StorageSessionCredential(SessionTestHelper.TEST_SESSION_TOKEN,
            SessionTestHelper.TEST_SESSION_KEY, null, SessionTestHelper.TEST_ACCOUNT_NAME);
        OffsetDateTime after = OffsetDateTime.now();

        OffsetDateTime expiration = credential.getExpiration();
        assertNotNull(expiration);
        assertTrue(
            !expiration.isBefore(before.plusMinutes(5L).minusSeconds(1))
                && !expiration.isAfter(after.plusMinutes(5L).plusSeconds(1)),
            "Default expiration should be ~5 minutes from construction time, but was " + expiration);
    }
}
