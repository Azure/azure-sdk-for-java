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

package com.microsoft.azure.management.resources;

import com.microsoft.azure.ResourceManagementIntegrationTestBase;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.resources.models.ResourceGroupExtended;
import org.junit.*;

import java.util.ArrayList;

public class ResourceGroupOperationsTest extends ResourceManagementIntegrationTestBase {
    private static String rgName;
    private static String location;

    @BeforeClass
    public static void setup() throws Exception {
        rgName = "testjava" + randomString(10);
        location = "westus";
        addRegexRule("testjava[a-z]{10}");
        createResourceManagementClient();
        setupTest(ResourceGroupOperationsTest.class.getSimpleName());
        resourceManagementClient.getResourceGroupsOperations().createOrUpdate(rgName, new ResourceGroup(location));
        resetTest(ResourceGroupOperationsTest.class.getSimpleName());
    }

    @AfterClass
    public static void cleanup() throws Exception {
        setupTest(ResourceGroupOperationsTest.class.getSimpleName() + CLEANUP_SUFFIX);
        resourceManagementClient.getResourceGroupsOperations().delete(rgName);
        resetTest(ResourceGroupOperationsTest.class.getSimpleName() + CLEANUP_SUFFIX);
    }

    @Test
    public void createAndListResourceGroupsSuccess() throws Exception {
        ArrayList<ResourceGroupExtended> resourceGroups = resourceManagementClient.getResourceGroupsOperations().list(null).getResourceGroups();
        Assert.assertNotNull(resourceGroups);

        Boolean found = false;
        for (ResourceGroupExtended rg : resourceGroups) {
            if (rg.getName().equals(rgName)) {
                found = true;
                Assert.assertEquals(rg.getLocation(), location);
            }
        }
        Assert.assertTrue(found);
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
    
