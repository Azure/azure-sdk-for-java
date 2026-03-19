// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import org.testng.annotations.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RegionalRoutingContextTest {

    @Test(groups = "unit")
    public void equalsShouldIgnoreThinclientEndpoint() throws URISyntaxException {
        URI gw = new URI("https://region1.documents.azure.com:443/");
        URI tc = new URI("https://region1.documents.azure.com:10250/");

        RegionalRoutingContext keyOnly = new RegionalRoutingContext(gw);
        RegionalRoutingContext full = new RegionalRoutingContext(gw);
        full.setThinclientRegionalEndpoint(tc);

        assertThat(keyOnly).isEqualTo(full);
        assertThat(keyOnly.hashCode()).isEqualTo(full.hashCode());
    }

    @Test(groups = "unit")
    public void equalsShouldDifferForDifferentGateways() throws URISyntaxException {
        URI gw1 = new URI("https://region1.documents.azure.com:443/");
        URI gw2 = new URI("https://region2.documents.azure.com:443/");

        RegionalRoutingContext ctx1 = new RegionalRoutingContext(gw1);
        RegionalRoutingContext ctx2 = new RegionalRoutingContext(gw2);

        assertThat(ctx1).isNotEqualTo(ctx2);
        assertThat(ctx1.hashCode()).isNotEqualTo(ctx2.hashCode());
    }

    @Test(groups = "unit")
    public void hashMapLookupWithGatewayOnlyKey() throws URISyntaxException {
        URI gw = new URI("https://region1.documents.azure.com:443/");
        URI tc = new URI("https://region1.documents.azure.com:10250/");

        RegionalRoutingContext keyOnly = new RegionalRoutingContext(gw);
        RegionalRoutingContext full = new RegionalRoutingContext(gw);
        full.setThinclientRegionalEndpoint(tc);

        Map<RegionalRoutingContext, String> map = new HashMap<>();
        map.put(full, "region1");

        assertThat(map.get(keyOnly)).isEqualTo("region1");
    }

    @Test(groups = "unit")
    public void toStringShouldContainGatewayEndpoint() throws URISyntaxException {
        URI gw = new URI("https://region1.documents.azure.com:443/");
        RegionalRoutingContext ctx = new RegionalRoutingContext(gw);

        assertThat(ctx.toString()).contains("https://region1.documents.azure.com:443/");
    }
}
