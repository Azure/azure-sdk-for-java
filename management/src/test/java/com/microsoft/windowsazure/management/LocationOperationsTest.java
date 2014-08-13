/**
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.management;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import com.microsoft.windowsazure.management.models.LocationsListResponse;

public class LocationOperationsTest extends ManagementIntegrationTestBase {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    private String locationListRequestId = "4297edd3ce9dca4fb1ccab3760162d2e";
    private String subscriptionId = System
            .getenv(ManagementConfiguration.SUBSCRIPTION_ID);
    private Boolean mocked = new Boolean(System.getenv(managementMockedConfiguration));

    @BeforeClass
    public static void setup() throws Exception {
        createService();
    }

    @Test
    public void listLocationSuccess() throws Exception {
        if (mocked) {
            setupListLocationSuccessMocked();
        }

        LocationsListResponse locationsListResponse = managementClient
                .getLocationsOperations().list();
        Assert.assertEquals(200, locationsListResponse.getStatusCode());
        Assert.assertNotNull(locationsListResponse.getRequestId());
        Assert.assertTrue(locationsListResponse.getLocations().size() > 0);

        if (mocked) {
            verifyListLocationSuccessMocked();
        }
    }

    private void setupListLocationSuccessMocked() throws Exception {
        stubFor(get(urlEqualTo("/" + subscriptionId + "/locations"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withHeader("x-ms-servedbyregion", "ussouth2")
                        .withHeader("x-ms-request-id",
                                locationListRequestId)
                                .withBodyFile("ListOperationsResponse.xml")));
    }

    private void verifyListLocationSuccessMocked() throws Exception {
        verify(getRequestedFor(urlEqualTo("/" + subscriptionId + "/locations")));
    }
}