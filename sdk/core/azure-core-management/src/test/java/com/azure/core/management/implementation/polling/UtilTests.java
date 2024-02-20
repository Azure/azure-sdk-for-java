// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.azure.core.management.implementation.polling.Util.AZURE_ASYNC_OPERATION;

public class UtilTests {

    private final ClientLogger logger = new ClientLogger(UtilTests.class);

    @Test
    public void testGetURL() throws MalformedURLException, URISyntaxException {
        String asyncOpUrl
            = "https://management.azure.com/subscriptions/000/providers/Microsoft.Network/locations/eastus/operations/123";
        String locationUrl
            = "https://management.azure.com/subscriptions/000/resourceGroups/rg86829b7a87d74/providers/Microsoft.Search/searchServices/ss3edfb54d";

        HttpHeaders headers = new HttpHeaders();
        headers.set(AZURE_ASYNC_OPERATION, asyncOpUrl);
        headers.set(HttpHeaderName.LOCATION, locationUrl);

        Assertions.assertEquals(new URI(asyncOpUrl).toURL(), Util.getAzureAsyncOperationUrl(headers, logger));
        Assertions.assertEquals(new URI(locationUrl).toURL(), Util.getLocationUrl(headers, logger, true));
    }

    @Test
    public void testGetMalformedURL() {
        HttpHeaders asyncOpHeaders = new HttpHeaders();
        asyncOpHeaders.set(AZURE_ASYNC_OPERATION, "invalidUrl");
        Assertions.assertThrows(Util.MalformedUrlException.class,
            () -> Util.getAzureAsyncOperationUrl(asyncOpHeaders, logger));

        asyncOpHeaders.set(AZURE_ASYNC_OPERATION,
            "https://management.azure.com/subscriptions/000/providers/Microsoft.Network/locations/east us/operations/123");
        Assertions.assertThrows(Util.MalformedUrlException.class,
            () -> Util.getAzureAsyncOperationUrl(asyncOpHeaders, logger));

        // malformed URL in location will be ignored
        HttpHeaders locationHeaders = new HttpHeaders();
        locationHeaders.set(HttpHeaderName.LOCATION, "invalidUrl");
        Assertions.assertNull(Util.getLocationUrl(locationHeaders, logger, true));
        Assertions.assertThrows(Util.MalformedUrlException.class, () -> Util.getLocationUrl(locationHeaders, logger));
    }
}
