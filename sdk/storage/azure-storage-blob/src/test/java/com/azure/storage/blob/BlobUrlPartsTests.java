// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BlobUrlPartsTests {

    @ParameterizedTest
    @MethodSource("urlSupplier")
    public void testParseUrl(String uriString) throws MalformedURLException {
        // Arrange
        URL originalUri = new URL(uriString);

        // Act
        BlobUrlParts blobUriBuilder = BlobUrlParts.parse(originalUri);
        String newUri = blobUriBuilder.toUrl().toString();

        // Assert
        assertEquals("https", blobUriBuilder.getScheme());
        assertEquals("myaccount", blobUriBuilder.getAccountName());
        assertEquals("", blobUriBuilder.getBlobContainerName());
        assertNull(blobUriBuilder.getBlobName());
        assertNull(blobUriBuilder.getSnapshot());
        assertEquals("", blobUriBuilder.getCommonSasQueryParameters().encode());
        assertEquals(originalUri.toString(), newUri);
    }

    public static Stream<Arguments> urlSupplier() {
        return Stream.of(Arguments.of("https://myaccount.blob.core.windows.net/"),
            Arguments.of("https://myaccount-secondary.blob.core.windows.net/"),
            Arguments.of("https://myaccount-dualstack.blob.core.windows.net/"),
            Arguments.of("https://myaccount-ipv6.blob.core.windows.net/"),
            Arguments.of("https://myaccount-secondary-dualstack.blob.core.windows.net/"),
            Arguments.of("https://myaccount-secondary-ipv6.blob.core.windows.net/"));
    }
}
