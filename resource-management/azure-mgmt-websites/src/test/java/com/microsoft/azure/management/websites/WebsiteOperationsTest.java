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

package com.microsoft.azure.management.websites;

import com.microsoft.azure.WebsiteManagementIntegrationTestBase;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.websites.models.SkuOptions;
import com.microsoft.azure.management.websites.models.WebHostingPlan;
import com.microsoft.azure.management.websites.models.WebHostingPlanCreateOrUpdateParameters;
import com.microsoft.azure.management.websites.models.WebHostingPlanProperties;
import com.microsoft.azure.management.websites.models.WebSiteBase;
import com.microsoft.azure.management.websites.models.WebSiteBaseProperties;
import com.microsoft.azure.management.websites.models.WebSiteCreateOrUpdateParameters;
import com.microsoft.azure.management.websites.models.WebSiteCreateResponse;
import com.microsoft.azure.management.websites.models.WorkerSizeOptions;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class WebsiteOperationsTest extends WebsiteManagementIntegrationTestBase {
    private static String rgName;
    private static String siteName;
    private static String planName;
    private static String location;

    @BeforeClass
    public static void setup() throws Exception {
        rgName = "testjava" + randomString(10);
        siteName = "testjavasite" + randomString(5);
        planName = "testjavaplan" + randomString(5);
        location = "westus";
        addRegexRule("testjava[a-z]{10}");
        addRegexRule("testjavasite[a-z]{5}");
        addRegexRule("testjavaplan[a-z]{5}");
        createResourceManagementClient();
        createWebsiteManagementClient();
        setupTest(WebsiteOperationsTest.class.getSimpleName());
        resourceManagementClient.getResourceGroupsOperations().createOrUpdate(rgName, new ResourceGroup(location));
        resetTest(WebsiteOperationsTest.class.getSimpleName());
    }

    @AfterClass
    public static void cleanup() throws Exception {
        setupTest(WebsiteOperationsTest.class.getSimpleName() + CLEANUP_SUFFIX);
        resourceManagementClient.getResourceGroupsOperations().delete(rgName);
        resetTest(WebsiteOperationsTest.class.getSimpleName() + CLEANUP_SUFFIX);
    }

    @Test
    public void createAndListWebsites() throws Exception {
        WebSiteCreateOrUpdateParameters parameters = new WebSiteCreateOrUpdateParameters();
        WebSiteBase web = new WebSiteBase();
        web.setLocation(location);

        WebHostingPlanCreateOrUpdateParameters hostingPlanParameters = new WebHostingPlanCreateOrUpdateParameters();
        WebHostingPlan plan = new WebHostingPlan();
        plan.setLocation(location);
        plan.setName(planName);
        WebHostingPlanProperties planProperties = new WebHostingPlanProperties();
        planProperties.setSku(SkuOptions.Basic);
        planProperties.setWorkerSize(WorkerSizeOptions.Small);
        plan.setProperties(planProperties);
        hostingPlanParameters.setWebHostingPlan(plan);
        webSiteManagementClient.getWebHostingPlansOperations().createOrUpdate(rgName, hostingPlanParameters);

        WebSiteBaseProperties properties = new WebSiteBaseProperties();
        properties.setServerFarm(planName);

        web.setProperties(properties);
        parameters.setWebSite(web);

        WebSiteCreateResponse response = webSiteManagementClient.getWebSitesOperations().createOrUpdate(rgName, siteName, null, parameters);
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(siteName, response.getWebSite().getName());
    }

    @Before
    public void beforeTest() throws Exception {
        setupTest();
    }

    @After
    public void afterTest() throws Exception {
        resetTest();
    }
}
    
