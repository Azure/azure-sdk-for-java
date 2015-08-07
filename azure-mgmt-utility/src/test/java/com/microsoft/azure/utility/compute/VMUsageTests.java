/**
 * Copyright Microsoft Corporation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.utility.compute;

import com.microsoft.azure.management.compute.models.*;
import com.microsoft.azure.utility.ResourceContext;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.junit.*;

public class VMUsageTests extends ComputeTestBase {
    static {
        log = LogFactory.getLog(VMUsageTests.class);
    }

    @BeforeClass
    public static void setup() throws Exception {
        ensureClientsInitialized();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        log.debug("after class, clean resource group: " + m_rgName);
        cleanupResourceGroup();
    }

    @Before
    public void beforeTest() throws Exception {
        setupTest();
    }

    @After
    public void afterTest() throws Exception {
        resetTest();
    }

    @Test
    public void testVMUsage() throws Exception {
        log.info("creating VM, in mock: " + IS_MOCKED);
        ResourceContext context = createTestResourceContext(false);

        VirtualMachine vm = createVM(context, generateName("VM"));

        log.info("get usage for VM: " + vm.getName());
        ListUsagesResponse luResponse = computeManagementClient.getUsageOperations().list(context.getLocation());
        validateListUsageResponse(luResponse);
    }

    private void validateListUsageResponse(ListUsagesResponse luResponse) {
        Assert.assertEquals("status code should be ok", HttpStatus.SC_OK, luResponse.getStatusCode());
        Assert.assertNotNull("usage not null", luResponse.getUsages());
        Assert.assertTrue("usage size > 0", luResponse.getUsages().size() > 0);

        for (Usage usage : luResponse.getUsages()) {
            Assert.assertNotNull("usage name localizedValue not null", usage.getName().getLocalizedValue());
            Assert.assertNotNull("usage name Value not null", usage.getName().getValue());
        }
    }
}
