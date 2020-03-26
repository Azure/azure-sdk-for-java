/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.appservice;

import com.azure.core.http.rest.PagedIterable;
import com.azure.management.RestClient;
import com.azure.management.resources.core.TestBase;
import com.azure.management.resources.core.TestUtilities;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

public class ZipDeployTests extends AppServiceTest {
    private String WEBAPP_NAME_4 = "";

    public ZipDeployTests() {
        super(TestBase.RunCondition.LIVE_ONLY);
    }
    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        WEBAPP_NAME_4 = generateRandomResourceName("java-func-", 20);

        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Test
    public void canZipDeployFunction() {
        // Create function app
        FunctionApp functionApp = appServiceManager.functionApps().define(WEBAPP_NAME_4)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME)
                .create();
        Assertions.assertNotNull(functionApp);
        SdkContext.sleep(5000);
        functionApp.zipDeploy(new File(FunctionAppsTests.class.getResource("/square-function-app.zip").getPath()));
        SdkContext.sleep(5000);
        String response = post("http://" + WEBAPP_NAME_4 + ".azurewebsites.net" + "/api/square", "25");
        Assertions.assertNotNull(response);
        Assertions.assertEquals("625", response);

        PagedIterable<FunctionEnvelope> envelopes = appServiceManager.functionApps().listFunctions(RG_NAME, functionApp.name());
        Assertions.assertNotNull(envelopes);
        Assertions.assertEquals(1, TestUtilities.getSize(envelopes));
        Assertions.assertEquals(envelopes.iterator().next().href(), "https://" + WEBAPP_NAME_4 +".scm.azurewebsites.net/api/functions/square");
    }
}
