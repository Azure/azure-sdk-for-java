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
        URI expectedRegionServiceEndpoint = URI.create("https://account-name-east-us.documents.azure.com:443");
        assertThat(LocationHelper.getLocationEndpoint(globalServiceEndpoint, "east-us"))
                .isEqualTo(expectedRegionServiceEndpoint);
    }
}
