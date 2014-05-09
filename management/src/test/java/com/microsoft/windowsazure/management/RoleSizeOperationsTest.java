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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.management.models.RoleSizeListResponse;

public class RoleSizeOperationsTest  extends ManagementIntegrationTestBase { 
    @BeforeClass
    public static void setup() throws Exception {
        createService();
    }

    @Test
    public void listRoleSizeSuccess() throws Exception {
        RoleSizeListResponse roleSizeListResponse = managementClient.getRoleSizesOperations().list();

        Assert.assertEquals(200, roleSizeListResponse.getStatusCode());
        Assert.assertNotNull(roleSizeListResponse.getRequestId());
        Assert.assertTrue(roleSizeListResponse.getRoleSizes().size() > 0);
    }
}