// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.routing.LocationHelper;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class LocationHelperTest {
    @Test(groups = "unit")
    public void getLocationEndpoint() throws Exception {
        URI globalServiceEndpoint = URI.create("https://account-name.documents.azure.com:443");

        // Canonical region name with spaces — spaces are stripped for URL suffix
        URI expectedWithSpaces = URI.create("https://account-name-eastus.documents.azure.com:443");
        assertThat(LocationHelper.getLocationEndpoint(globalServiceEndpoint, "East US"))
                .isEqualTo(expectedWithSpaces);

        // Hyphenated input — hyphens are also stripped (Azure regional DNS uses "eastus" not "east-us")
        assertThat(LocationHelper.getLocationEndpoint(globalServiceEndpoint, "east-us"))
                .isEqualTo(expectedWithSpaces);

        // Already-normalized input
        assertThat(LocationHelper.getLocationEndpoint(globalServiceEndpoint, "eastus"))
                .isEqualTo(expectedWithSpaces);
    }
}
