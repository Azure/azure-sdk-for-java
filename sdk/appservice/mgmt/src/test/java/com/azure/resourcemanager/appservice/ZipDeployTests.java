// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.appservice;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.appservice.models.FunctionEnvelope;
import com.azure.resourcemanager.resources.core.TestBase;
import com.azure.resourcemanager.resources.core.TestUtilities;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ZipDeployTests extends AppServiceTest {
    private String webappName4 = "";

    public ZipDeployTests() {
        super(TestBase.RunCondition.LIVE_ONLY);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        webappName4 = generateRandomResourceName("java-func-", 20);

        super.initializeClients(httpPipeline, profile);
    }

    @Test
    public void canZipDeployFunction() {
        // Create function app
        FunctionApp functionApp =
            appServiceManager
                .functionApps()
                .define(webappName4)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
                .create();
        Assertions.assertNotNull(functionApp);
        SdkContext.sleep(5000);
        functionApp.zipDeploy(new File(FunctionAppsTests.class.getResource("/square-function-app.zip").getPath()));
        SdkContext.sleep(5000);
        String response = post("http://" + webappName4 + ".azurewebsites.net" + "/api/square", "25");
        Assertions.assertNotNull(response);
        Assertions.assertEquals("625", response);

        PagedIterable<FunctionEnvelope> envelopes =
            appServiceManager.functionApps().listFunctions(rgName, functionApp.name());
        Assertions.assertNotNull(envelopes);
        Assertions.assertEquals(1, TestUtilities.getSize(envelopes));
        Assertions
            .assertEquals(
                envelopes.iterator().next().href(),
                "https://" + webappName4 + ".scm.azurewebsites.net/api/functions/square");
    }
}
