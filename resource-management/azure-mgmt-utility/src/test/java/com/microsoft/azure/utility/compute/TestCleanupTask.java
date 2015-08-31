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

import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.resources.models.ResourceGroupExtended;
import com.microsoft.azure.management.resources.models.ResourceGroupListResult;
import com.microsoft.windowsazure.exception.ServiceException;
import org.apache.commons.logging.LogFactory;
import org.junit.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * This is a special test case used by runner to cleanup resource generated from
 * timeout test cases.
 */
public class TestCleanupTask extends ComputeTestBase {
    static {
        log = LogFactory.getLog(TestCleanupTask.class);
    }

    @BeforeClass
    public static void setup() throws Exception {
        ensureClientsInitialized();
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
    public void cleanResourceGroupsOperations() throws Exception {
        log.info("start cleaning up rgs, in mock: " + IS_MOCKED);
        ResourceGroupListResult listResult = null;
        while (listResult == null || listResult.getNextLink() != null) {
            if (listResult == null) {
                listResult = resourceManagementClient.getResourceGroupsOperations().list(null);
            } else if (listResult.getNextLink() != null && !listResult.getNextLink().isEmpty()) {
                listResult = resourceManagementClient.getResourceGroupsOperations().listNext(listResult.getNextLink());
            }

            removeRGs(listResult.getResourceGroups());
        }
    }

    private void removeRGs(ArrayList<ResourceGroupExtended> groups) {
        groups.stream().filter(new Predicate<ResourceGroupExtended>() {
            @Override
            public boolean test(ResourceGroupExtended rg) {
                return rg.getName().startsWith("javatest");
            }
        }).forEach(new Consumer<ResourceGroupExtended>() {
            @Override
            public void accept(ResourceGroupExtended rg) {
                try {
                    resourceManagementClient.getResourceGroupsOperations().beginDeleting(rg.getName());
                    log.info("removed rg: " + rg.getName());
                } catch (Exception e) {
                    log.info(e.toString());
                }
            }
        });
    }
}
