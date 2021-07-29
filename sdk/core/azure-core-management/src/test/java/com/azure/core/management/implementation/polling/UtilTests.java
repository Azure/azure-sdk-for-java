// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import com.azure.core.http.HttpHeaders;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class UtilTests {

    private final ClientLogger logger = new ClientLogger(UtilTests.class);

    @Test
    public void testGetURL() throws MalformedURLException {
        String asyncOpUrl = "https://management.azure.com/subscriptions/000/providers/Microsoft.Network/locations/eastus/operations/123";
        String locationUrl = "https://management.azure.com/subscriptions/000/resourceGroups/rg86829b7a87d74/providers/Microsoft.Search/searchServices/ss3edfb54d";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Azure-AsyncOperation", asyncOpUrl);
        headers.set("Location", locationUrl);

        Assertions.assertEquals(new URL(asyncOpUrl), Util.getAzureAsyncOperationUrl(headers, logger));
        Assertions.assertEquals(new URL(locationUrl), Util.getLocationUrl(headers, logger, true));
    }

    @Test
    public void testGetMalformedURL() {
        HttpHeaders asyncOpHeaders = new HttpHeaders();
        asyncOpHeaders.set("Azure-AsyncOperation", "invalidUrl");
        Assertions.assertThrows(Util.MalformedUrlException.class, () -> Util.getAzureAsyncOperationUrl(asyncOpHeaders, logger));

        asyncOpHeaders.set("Azure-AsyncOperation", "https://management.azure.com/subscriptions/000/providers/Microsoft.Network/locations/east us/operations/123");
        Assertions.assertThrows(Util.MalformedUrlException.class, () -> Util.getAzureAsyncOperationUrl(asyncOpHeaders, logger));

        // malformed URL in location will be ignored
        HttpHeaders locationHeaders = new HttpHeaders();
        locationHeaders.set("Location", "invalidUrl");
        Assertions.assertNull(Util.getLocationUrl(locationHeaders, logger, true));
        Assertions.assertThrows(Util.MalformedUrlException.class, () -> Util.getLocationUrl(locationHeaders, logger));
    }
}
