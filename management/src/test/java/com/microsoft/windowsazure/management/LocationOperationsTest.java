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
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseFilter;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import com.microsoft.windowsazure.management.models.LocationsListResponse;

public class LocationOperationsTest extends ManagementIntegrationTestBase {

    private String locationListRequestId = "4297edd3ce9dca4fb1ccab3760162d2e";
    private String subscriptionId = System.getenv(ManagementConfiguration.SUBSCRIPTION_ID);
    private Boolean mocked = new Boolean(System.getenv(ManagementConfiguration.MOCKED));
    private Boolean recording = new Boolean(System.getenv(ManagementConfiguration.RECORDING));

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8043));

    @BeforeClass
    public static void setup() throws Exception {
        createService();
    }

    @Test
    public void listLocationSuccess() throws Exception {
        if (mocked) {
            setupListLocationSuccessMocked();
        }
        
        LocationsListResponse locationsListResponse;

        if (recording) {
            ServiceResponseFilter filter = new ServiceResponseFilter() {
                @Override
                public void filter(ServiceRequestContext request, ServiceResponseContext response) {
                    InputStream is = new BufferedInputStream(response.getEntityInputStream());
                    try {
                        is.mark(Integer.MAX_VALUE);
                        URL url = this.getClass().getClassLoader().getResource(recordFolder);
                        File tape = new File(url.getPath() + getClass().getName() + ".xml");
                        tape.createNewFile();
                        ByteStreams.copy(is, new FileOutputStream(tape));
                        is.reset();
                        response.setEntityInputStream(is);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            managementClient.withResponseFilterLast(filter);
        }
        
        locationsListResponse = managementClient.getLocationsOperations().list();
        Assert.assertEquals(200, locationsListResponse.getStatusCode());
        Assert.assertNotNull(locationsListResponse.getRequestId());
        Assert.assertTrue(locationsListResponse.getLocations().size() > 0);

        if (mocked) {
            verifyListLocationSuccessMocked();
        }
    }

    private void setupListLocationSuccessMocked() throws Exception {
        URL url = this.getClass().getClassLoader().getResource(recordFolder + this.getClass().getName() + ".xml");
        stubFor(get(urlEqualTo("/" + subscriptionId + "/locations"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withHeader("x-ms-servedbyregion", "ussouth2")
                        .withHeader("x-ms-request-id", locationListRequestId)
                        .withBody(Files.toString(new File(url.getPath()), Charsets.UTF_8))));
    }

    private void verifyListLocationSuccessMocked() throws Exception {
        verify(getRequestedFor(urlEqualTo("/" + subscriptionId + "/locations")));
    }
}