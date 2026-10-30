// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.common;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.util.CoreUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StorageSharedKeyCredentialTests {
    @Test
    public void canMapFromAzureNamedKeyCredential() {
        String key = "bar";
        String randomString = CoreUtils.randomUuid().toString();
        AzureNamedKeyCredential namedKey = new AzureNamedKeyCredential("foo", key);

        StorageSharedKeyCredential storageKey = StorageSharedKeyCredential.fromAzureNamedKeyCredential(namedKey);
        String expectedSignature = StorageImplUtils.computeHMac256(key, randomString);
        String signature = storageKey.computeHmac256(randomString);

        assertEquals(storageKey.getAccountName(), namedKey.getAzureNamedKey().getName());
        assertEquals(signature, expectedSignature);
    }

    @Test
    public void canRotateKey() {
        String name = "foo";
        String updatedKey = "bar2";
        String randomString = CoreUtils.randomUuid().toString();
        AzureNamedKeyCredential namedKey = new AzureNamedKeyCredential(name, "bar1");

        StorageSharedKeyCredential storageKey = StorageSharedKeyCredential.fromAzureNamedKeyCredential(namedKey);
        String signature1 = storageKey.computeHmac256(randomString);
        namedKey.update(name, updatedKey);
        String signature2 = storageKey.computeHmac256(randomString);
        String expectedSignature = StorageImplUtils.computeHMac256(updatedKey, randomString);

        assertEquals(storageKey.getAccountName(), namedKey.getAzureNamedKey().getName());
        assertNotEquals(signature1, signature2);
        assertEquals(expectedSignature, signature2);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "DefaultEndpointsProtocol=https;AccountName=teststorage;AccountKey=atestaccountkey;EndpointSuffix=core.windows.net",
            "DefaultEndpointsProtocol=https;accountName=teststorage;AccountKey=atestaccountkey;EndpointSuffix=core.windows.net",
            "DefaultEndpointsProtocol=https;AccountName=teststorage;accountKey=atestaccountkey;EndpointSuffix=core.windows.net",
            "DefaultEndpointsProtocol=https;Accountname=teststorage;accountKey=atestaccountkey;EndpointSuffix=core.windows.net",
            "DefaultEndpointsProtocol=https;AccountName=teststorage;accountkey=atestaccountkey;EndpointSuffix=core.windows.net",
            "DefaultEndpointsProtocol=https;accountname=teststorage;accountkey=atestaccountkey;EndpointSuffix=core.windows.net" })
    public void canParseValidConnectionString(String connectionString) {
        assertEquals("teststorage", StorageSharedKeyCredential.fromConnectionString(connectionString).getAccountName());

    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "DefaultEndpointsProtocol=https;EndpointSuffix=core.windows.net",
            "DefaultEndpointsProtocol=https;AccountKey=atestaccountkey;EndpointSuffix=core.windows.net",
            "DefaultEndpointsProtocol=https;AccountName=teststorage;EndpointSuffix=core.windows.net",
            "DefaultEndpointsProtocol=https;AccountName =teststorage;AccountKey=atestaccountkey;EndpointSuffix=core.windows.net",
            "DefaultEndpointsProtocol=https;AccountName=teststorage;AccountKey =atestaccountkey;EndpointSuffix=core.windows.net",
            "DefaultEndpointsProtocol=https;Account Name=teststorage;AccountKey=atestaccountkey;EndpointSuffix=core.windows.net",
            "DefaultEndpointsProtocol=https;AccountName=teststorage;Account Key=atestaccountkey;EndpointSuffix=core.windows.net" })
    public void cannotParseInvalidConnectionString(String connectionString) {
        assertThrows(IllegalArgumentException.class,
            () -> StorageSharedKeyCredential.fromConnectionString(connectionString));
    }

    @Test
    public void ipStyleUrlCanonicalizedResourceIncludesAccountNameTwice() throws MalformedURLException {
        // For IP-style URLs (e.g., Azurite), the account name appears in the URL path.
        // The canonicalized resource prepends /<accountName> to the absolute path,
        // so the account name correctly appears twice: /<accountName>/<accountName>/container/blob
        String accountName = "myaccount";
        String accountKey = "dGVzdFNlc3Npb25LZXkxMjM0NTY3ODkwMTIzNDU2Nzg5MA==";

        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);

        URL url = new URL("http://127.0.0.1:10000/myaccount/mycontainer/myblob");
        HttpHeaders headers
            = new HttpHeaders().set(HttpHeaderName.fromString("x-ms-date"), "Mon, 31 Mar 2025 00:00:00 GMT")
                .set(HttpHeaderName.fromString("x-ms-version"), "2025-01-05")
                .set(HttpHeaderName.CONTENT_LENGTH, "0");

        String authHeader = credential.generateAuthorizationHeader(url, "GET", headers, false);

        // Verify the signature matches a string-to-sign with account name appearing twice
        String stringToSign = "GET\n\n\n\n\n\n\n\n\n\n\n\n" + "x-ms-date:Mon, 31 Mar 2025 00:00:00 GMT\n"
            + "x-ms-version:2025-01-05\n" + "/myaccount/myaccount/mycontainer/myblob";
        String expectedSignature = credential.computeHmac256(stringToSign);

        assertTrue(authHeader.startsWith("SharedKey myaccount:"),
            "Authorization header should start with 'SharedKey myaccount:' but was: " + authHeader);
        assertEquals("SharedKey myaccount:" + expectedSignature, authHeader);
    }
}
