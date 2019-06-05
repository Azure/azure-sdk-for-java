package com.microsoft.azure.cosmosdb.internal;

import com.microsoft.azure.cosmosdb.rx.internal.routing.LocationHelper;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class LocationHelperTest {
    @Test(groups = "unit")
    public void getLocationEndpoint() throws Exception {
        URL globalServiceEndpoint = URI.create("https://account-name.documents.azure.com:443").toURL();
        URL expectedRegionServiceEndpoint = URI.create("https://account-name-east-us.documents.azure.com:443").toURL();
        assertThat(LocationHelper.getLocationEndpoint(globalServiceEndpoint, "east-us"))
                .isEqualTo(expectedRegionServiceEndpoint);
    }
}
