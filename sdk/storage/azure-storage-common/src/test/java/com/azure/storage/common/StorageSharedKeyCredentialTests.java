// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.common;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.util.CoreUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
